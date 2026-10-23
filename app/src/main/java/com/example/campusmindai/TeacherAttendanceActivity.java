package com.example.campusmindai;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TeacherAttendanceActivity extends AppCompatActivity {

    // ---------------------------------------------------------
    // SESSION SETTINGS
    // ---------------------------------------------------------

    private static final long SESSION_DURATION_MILLIS = 60_000L;

    // ---------------------------------------------------------
    // COLORS
    // ---------------------------------------------------------

    private int colorBackground;
    private int colorCard;
    private int colorPrimary;
    private int colorText;
    private int colorSecondary;
    private int colorMuted;
    private int colorBorder;
    private int colorSuccess;
    private int colorError;
    private int colorInput;

    // ---------------------------------------------------------
    // VIEWS
    // ---------------------------------------------------------

    private EditText etSubject;

    private MaterialButton btnStartAttendance;
    private MaterialButton btnStopAttendance;

    private MaterialCardView activeSessionCard;

    private TextView tvSessionSubject;
    private TextView tvSessionCode;
    private TextView tvSessionTimer;
    private TextView tvSessionStatus;

    // ---------------------------------------------------------
    // FIREBASE
    // ---------------------------------------------------------

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private AttendanceBleManager bleManager;

    // ---------------------------------------------------------
    // SESSION DATA
    // ---------------------------------------------------------

    private String currentSessionId;
    private String currentCode;
    private long currentSessionCreatedAtMillis;
    private CountDownTimer countDownTimer;
    private boolean bleAdvertising;
    private boolean restoringSession;
    private boolean teacherAccessVerified;
    private String bleStatusMessage = "Waiting for Bluetooth signal...";

    // ---------------------------------------------------------
    // ON CREATE
    // ---------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.teacher_attendance_activity);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        bleManager = new AttendanceBleManager(this);

        bindViews();
        bleManager.setStatusListener((message, advertising) -> runOnUiThread(() -> {
            if (isFinishing() || tvSessionStatus == null) {
                return;
            }

            bleAdvertising = advertising;
            bleStatusMessage = message == null ? "Bluetooth status unavailable." : message;

            if (currentCode != null) {
                if (bleAdvertising) {
                    tvSessionStatus.setText("● Attendance Active • Bluetooth signal ON");
                    tvSessionStatus.setTextColor(colorSuccess);
                } else if (bleStatusMessage.startsWith("Waiting for an active attendance session.")) {
                    tvSessionStatus.setText("● Attendance Active • Waiting for Bluetooth signal...");
                    tvSessionStatus.setTextColor(colorSuccess);
                } else {
                    tvSessionStatus.setText("● Attendance Active • BLE: " + bleStatusMessage);
                    tvSessionStatus.setTextColor(colorError);
                }
            } else if (message != null
                    && !message.startsWith("Waiting for an active attendance session.")) {
                Toast.makeText(TeacherAttendanceActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }));

        loadThemeColors();
        applyTheme();

        activeSessionCard.setVisibility(View.GONE);

        btnStartAttendance.setOnClickListener(
                v -> startAttendanceSession()
        );

        btnStopAttendance.setOnClickListener(
                v -> confirmStopAttendance()
        );

        checkTeacherAccess();

    }

    // ---------------------------------------------------------
    // BIND VIEWS
    // ---------------------------------------------------------

    private void bindViews() {

        etSubject =
                findViewById(R.id.etTeacherSubject);

        btnStartAttendance =
                findViewById(R.id.btnStartAttendance);

        btnStopAttendance =
                findViewById(R.id.btnStopAttendance);

        activeSessionCard =
                findViewById(R.id.activeSessionCard);

        tvSessionSubject =
                findViewById(R.id.tvSessionSubject);

        tvSessionCode =
                findViewById(R.id.tvSessionCode);

        tvSessionTimer =
                findViewById(R.id.tvSessionTimer);

        tvSessionStatus =
                findViewById(R.id.tvSessionStatus);
    }

    // ---------------------------------------------------------
    // TEACHER ACCESS
    // ---------------------------------------------------------

    private void checkTeacherAccess() {

        if (firebaseAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String uid =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        firestore
                .collection("students")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    String role =
                            document.getString("role");

                    if ("teacher".equalsIgnoreCase(role)) {

                        enableTeacherScreen();

                    } else {

                        showAccessDenied();
                    }

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to verify teacher access",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });
    }

    private void enableTeacherScreen() {

        teacherAccessVerified = true;
        btnStartAttendance.setEnabled(false);
        etSubject.setEnabled(false);

        tvSessionStatus.setText("Checking for an active attendance session...");

        // Restore an existing session before allowing a new one.
        restoreActiveSession();

        // Ask for Bluetooth permission and watch for the active session.
        startBlePermissionAndWatcher();
    }

    // ---------------------------------------------------------
    // RESTORE ACTIVE SESSION FROM FIRESTORE
    // ---------------------------------------------------------

    private void restoreActiveSession() {

        if (restoringSession || firebaseAuth.getCurrentUser() == null) {
            return;
        }

        restoringSession = true;
        String teacherUid = firebaseAuth.getCurrentUser().getUid();

        firestore.collection("attendanceSessions")
                .whereEqualTo("teacherUid", teacherUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    long now = System.currentTimeMillis();
                    com.google.firebase.firestore.DocumentSnapshot activeSession = null;
                    long latestCreatedAt = Long.MIN_VALUE;

                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Boolean active = document.getBoolean("active");
                        Long expiresAt = document.getLong("expiresAtMillis");
                        Long createdAt = document.getLong("createdAtMillis");

                        if (Boolean.TRUE.equals(active)
                                && expiresAt != null
                                && expiresAt > now) {
                            long created = createdAt == null ? 0L : createdAt;
                            if (activeSession == null || created > latestCreatedAt) {
                                activeSession = document;
                                latestCreatedAt = created;
                            }
                        } else if (Boolean.TRUE.equals(active)
                                && expiresAt != null
                                && expiresAt <= now) {
                            // Mark expired sessions inactive so they cannot be restored later.
                            Map<String, Object> expiredUpdate = new HashMap<>();
                            expiredUpdate.put("active", false);
                            expiredUpdate.put("status", "Expired");
                            expiredUpdate.put("endedAtMillis", now);
                            expiredUpdate.put("absencesFinalized", false);
                            document.getReference().update(expiredUpdate)
                                    .addOnSuccessListener(unused -> {
                                        Long sessionCreated = document.getLong("createdAtMillis");
                                        markEnrolledStudentsAbsent(
                                                document.getId(),
                                                document.getString("subject"),
                                                sessionCreated == null ? now : sessionCreated,
                                                teacherUid,
                                                success -> {
                                                    if (!success && !isFinishing()) {
                                                        Toast.makeText(
                                                                TeacherAttendanceActivity.this,
                                                                "Some automatic absences could not be saved. Reopen Attendance when connected to retry.",
                                                                Toast.LENGTH_LONG
                                                        ).show();
                                                    }
                                                }
                                        );
                                    });
                        } else if (!Boolean.TRUE.equals(active)
                                && Boolean.FALSE.equals(document.getBoolean("absencesFinalized"))
                                && document.getLong("endedAtMillis") != null) {
                            // Retry an interrupted absence-finalization attempt for a session
                            // created by this version of the app.
                            Long sessionCreated = document.getLong("createdAtMillis");
                            markEnrolledStudentsAbsent(
                                    document.getId(),
                                    document.getString("subject"),
                                    sessionCreated == null ? now : sessionCreated,
                                    teacherUid,
                                    success -> {
                                        if (!success && !isFinishing()) {
                                            Toast.makeText(
                                                    TeacherAttendanceActivity.this,
                                                    "Some automatic absences are still pending. Check the connection and reopen Attendance.",
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        }
                                    }
                            );
                        }
                    }

                    if (activeSession != null) {
                        currentSessionId = activeSession.getId();
                        currentCode = activeSession.getString("code");
                        Long sessionCreated = activeSession.getLong("createdAtMillis");
                        currentSessionCreatedAtMillis = sessionCreated == null
                                ? System.currentTimeMillis() : sessionCreated;
                        String subject = activeSession.getString("subject");
                        Long expiresAt = activeSession.getLong("expiresAtMillis");

                        if (currentCode != null && expiresAt != null) {
                            etSubject.setText(subject == null ? "" : subject);
                            etSubject.setEnabled(false);
                            btnStartAttendance.setEnabled(false);
                            btnStopAttendance.setEnabled(true);
                            showActiveSession(subject == null ? "Attendance" : subject, expiresAt);
                        } else {
                            currentSessionId = null;
                            currentCode = null;
                            setReadyForNewSession();
                        }
                    } else {
                        currentSessionId = null;
                        currentCode = null;
                        setReadyForNewSession();
                    }
                })
                .addOnFailureListener(e -> {
                    // Do not silently create a duplicate session if restoration fails.
                    tvSessionStatus.setText("Could not check active attendance. Check your connection and try again.");
                    tvSessionStatus.setTextColor(colorError);
                    btnStartAttendance.setEnabled(true);
                    etSubject.setEnabled(true);
                    Toast.makeText(this, "Unable to restore attendance session", Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> restoringSession = false);
    }

    private void setReadyForNewSession() {
        activeSessionCard.setVisibility(View.GONE);
        btnStartAttendance.setEnabled(true);
        btnStopAttendance.setEnabled(false);
        etSubject.setEnabled(true);
        tvSessionStatus.setText("Ready to start a classroom attendance session");
        tvSessionStatus.setTextColor(colorSuccess);
    }

    private void showAccessDenied() {

        new AlertDialog.Builder(this)
                .setTitle("Teacher Access Required")
                .setMessage(
                        "This screen is available only to an authorized teacher account."
                )
                .setPositiveButton(
                        "OK",
                        (dialog, which) -> finish()
                )
                .setCancelable(false)
                .show();
    }

    // ---------------------------------------------------------
    // BLE PERMISSION + SESSION WATCHER
    // ---------------------------------------------------------

    private void startBlePermissionAndWatcher() {

        if (bleManager == null) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.S) {

            if (checkSelfPermission(
                    android.Manifest.permission.BLUETOOTH_ADVERTISE
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(
                    android.Manifest.permission.BLUETOOTH_CONNECT
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                android.Manifest.permission.BLUETOOTH_ADVERTISE,
                                android.Manifest.permission.BLUETOOTH_CONNECT
                        },
                        501
                );

                return;
            }
        }

        bleManager.startSessionWatcher();
    }

    // ---------------------------------------------------------
    // BLE PERMISSION RESULT
    // ---------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode != 501
                || bleManager == null) {

            return;
        }

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.S) {

            if (checkSelfPermission(
                    android.Manifest.permission.BLUETOOTH_ADVERTISE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(
                    android.Manifest.permission.BLUETOOTH_CONNECT
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED) {

                bleManager.startSessionWatcher();

            } else {

                Toast.makeText(
                        this,
                        "Bluetooth permission is required for teacher attendance",
                        Toast.LENGTH_LONG
                ).show();
            }
        } else {

            bleManager.startSessionWatcher();
        }
    }

    // ---------------------------------------------------------
    // START SESSION
    // ---------------------------------------------------------

    private void startAttendanceSession() {

        if (restoringSession) {
            Toast.makeText(this, "Checking for an active session. Please wait.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        if (currentSessionId != null && currentCode != null) {
            Toast.makeText(this, "An attendance session is already active.", Toast.LENGTH_SHORT).show();
            return;
        }

        String subject =
                etSubject
                        .getText()
                        .toString()
                        .trim();

        if (subject.isEmpty()) {

            etSubject.setError(
                    "Enter subject name"
            );

            etSubject.requestFocus();

            return;
        }

        if (countDownTimer != null) {

            countDownTimer.cancel();
            countDownTimer = null;
        }

        btnStartAttendance.setEnabled(false);
        etSubject.setEnabled(false);

        currentCode =
                generateAttendanceCode();

        long createdAtMillis =
                System.currentTimeMillis();

        long expiresAtMillis =
                createdAtMillis
                        + SESSION_DURATION_MILLIS;

        String teacherUid =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        String sessionId =
                UUID.randomUUID()
                        .toString();

        Map<String, Object> sessionData =
                new HashMap<>();

        sessionData.put(
                "sessionId",
                sessionId
        );

        sessionData.put(
                "subject",
                subject
        );

        sessionData.put(
                "code",
                currentCode
        );

        sessionData.put(
                "teacherUid",
                teacherUid
        );

        sessionData.put(
                "active",
                true
        );

        sessionData.put(
                "status",
                "Active"
        );

        sessionData.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        sessionData.put(
                "createdAtMillis",
                createdAtMillis
        );

        sessionData.put(
                "expiresAtMillis",
                expiresAtMillis
        );

        // False until the session closes and all enrolled, unmarked students
        // have been finalized as Absent.
        sessionData.put("absencesFinalized", false);

        firestore
                .collection("attendanceSessions")
                .document(sessionId)
                .set(sessionData)
                .addOnSuccessListener(unused -> {

                    currentSessionId =
                            sessionId;
                    currentSessionCreatedAtMillis = createdAtMillis;

                    showActiveSession(
                            subject,
                            expiresAtMillis
                    );

                    Toast.makeText(
                            this,
                            "Attendance session started",
                            Toast.LENGTH_SHORT
                    ).show();

                })
                .addOnFailureListener(e -> {

                    btnStartAttendance
                            .setEnabled(true);

                    etSubject
                            .setEnabled(true);

                    Toast.makeText(
                            this,
                            "Unable to start attendance",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // ---------------------------------------------------------
    // SHOW ACTIVE SESSION
    // ---------------------------------------------------------

    private void showActiveSession(
            String subject,
            long expiresAtMillis
    ) {

        tvSessionSubject.setText(
                subject
        );

        tvSessionCode.setText(
                currentCode
        );

        if (bleAdvertising) {
            tvSessionStatus.setText("● Attendance Active • Bluetooth signal ON");
            tvSessionStatus.setTextColor(colorSuccess);
        } else if (bleStatusMessage.startsWith("Waiting for an active attendance session.")
                || bleStatusMessage.startsWith("Waiting for Bluetooth signal...")) {
            tvSessionStatus.setText("● Attendance Active • Waiting for Bluetooth signal...");
            tvSessionStatus.setTextColor(colorSuccess);
        } else {
            tvSessionStatus.setText("● Attendance Active • BLE: " + bleStatusMessage);
            tvSessionStatus.setTextColor(colorError);
        }

        activeSessionCard.setVisibility(
                View.VISIBLE
        );
        btnStopAttendance.setEnabled(true);
        btnStartAttendance.setEnabled(false);
        etSubject.setEnabled(false);

        startCountdown(
                expiresAtMillis
        );
    }

    // ---------------------------------------------------------
    // COUNTDOWN
    // ---------------------------------------------------------

    private void startCountdown(
            long expiresAtMillis
    ) {

        long remaining =
                Math.max(
                        0,
                        expiresAtMillis
                                - System.currentTimeMillis()
                );

        countDownTimer =
                new CountDownTimer(
                        remaining,
                        1000
                ) {

                    @Override
                    public void onTick(
                            long millisUntilFinished
                    ) {

                        long totalSeconds =
                                millisUntilFinished
                                        / 1000;

                        long minutes =
                                totalSeconds / 60;

                        long seconds =
                                totalSeconds % 60;

                        tvSessionTimer.setText(
                                String.format(
                                        java.util.Locale
                                                .getDefault(),
                                        "Expires in %02d:%02d",
                                        minutes,
                                        seconds
                                )
                        );
                    }

                    @Override
                    public void onFinish() {

                        tvSessionTimer.setText(
                                "Attendance session expired"
                        );

                        tvSessionStatus.setText(
                                "● Expired"
                        );

                        tvSessionStatus
                                .setTextColor(
                                        colorError
                                );

                        btnStopAttendance.setEnabled(false);
                        btnStartAttendance.setEnabled(true);
                        etSubject.setEnabled(true);

                        deactivateCurrentSession("Expired");
                    }

                }.start();
    }

    // ---------------------------------------------------------
    // STOP SESSION
    // ---------------------------------------------------------

    private void confirmStopAttendance() {

        new AlertDialog.Builder(this)
                .setTitle("Stop Attendance?")
                .setMessage(
                        "Students will no longer be able to use this attendance session."
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "STOP",
                        (dialog, which) ->
                                stopAttendanceSession()
                )
                .show();
    }

    private void stopAttendanceSession() {

        if (countDownTimer != null) {

            countDownTimer.cancel();

            countDownTimer = null;
        }

        deactivateCurrentSession(
                "Stopped"
        );
    }

    private void deactivateCurrentSession(
            String finalStatus
    ) {

        if (currentSessionId == null) {
            resetSessionUI();
            return;
        }

        final String endingSessionId = currentSessionId;
        final String endingSubject = tvSessionSubject.getText() == null
                ? etSubject.getText().toString().trim()
                : tvSessionSubject.getText().toString().trim();
        final long sessionCreatedAt = currentSessionCreatedAtMillis > 0
                ? currentSessionCreatedAtMillis : System.currentTimeMillis();
        final String teacherUid = firebaseAuth.getCurrentUser() == null
                ? null : firebaseAuth.getCurrentUser().getUid();

        btnStartAttendance.setEnabled(false);
        btnStopAttendance.setEnabled(false);
        etSubject.setEnabled(false);
        tvSessionStatus.setText("Closing session and recording absences...");
        tvSessionStatus.setTextColor(colorSecondary);

        Map<String, Object> update = new HashMap<>();
        update.put("active", false);
        update.put("status", finalStatus);
        update.put("endedAtMillis", System.currentTimeMillis());
        update.put("absencesFinalized", false);

        firestore.collection("attendanceSessions")
                .document(endingSessionId)
                .update(update)
                .addOnSuccessListener(unused -> {
                    AbsenceCompletion completion = success -> runOnUiThread(() -> {
                        // Do not clear another session if the teacher has navigated or
                        // this activity has been reused while Firebase was processing.
                        if (endingSessionId.equals(currentSessionId)) {
                            resetSessionUI();
                        }

                        if ("Stopped".equals(finalStatus)) {
                            String message = success
                                    ? "Attendance stopped. Unmarked students enrolled in "
                                      + endingSubject + " were marked Absent."
                                    : "Attendance stopped, but some absences could not be saved. Check Firestore rules/connection.";
                            Toast.makeText(
                                    TeacherAttendanceActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        } else if (!success) {
                            Toast.makeText(
                                    TeacherAttendanceActivity.this,
                                    "Session expired, but some absences could not be saved. Reopen Attendance when connected to retry.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });

                    if (teacherUid == null) {
                        completion.onComplete(false);
                        return;
                    }

                    markEnrolledStudentsAbsent(
                            endingSessionId,
                            endingSubject,
                            sessionCreatedAt,
                            teacherUid,
                            completion
                    );
                })
                .addOnFailureListener(e -> {
                    tvSessionStatus.setText("Could not close the attendance session. Check your connection and try again.");
                    tvSessionStatus.setTextColor(colorError);
                    btnStopAttendance.setEnabled(true);
                    btnStartAttendance.setEnabled(false);
                    etSubject.setEnabled(false);
                    Toast.makeText(
                            this,
                            "Unable to close attendance session. Please try Stop again.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // ---------------------------------------------------------
    // AUTOMATIC ABSENCE FINALIZATION
    // ---------------------------------------------------------

    /** Callback for asynchronous absence finalization. */
    private interface AbsenceCompletion {
        void onComplete(boolean success);
    }

    /**
     * At session close/expiry, only students who have this exact subject in
     * students/{uid}/subjects are considered. Students with a recorded check-in
     * remain Present; only those without a check-in receive Absent records.
     */
    private void markEnrolledStudentsAbsent(
            String sessionId,
            String subject,
            long sessionCreatedAt,
            String teacherUid,
            AbsenceCompletion completion
    ) {
        if (subject == null || subject.trim().isEmpty() || teacherUid == null) {
            if (completion != null) completion.onComplete(false);
            return;
        }

        final String normalizedSessionSubject = subject.trim();

        // Compute the lecture number from this teacher's own session history.
        // Do not query every student's attendance by subject: Firestore rules
        // intentionally limit teachers to records linked to sessions they own.
        firestore.collection("attendanceSessions")
                .whereEqualTo("teacherUid", teacherUid)
                .get()
                .addOnSuccessListener(teacherSessions -> {
                    int lectureNumber = 0;
                    for (DocumentSnapshot priorSession : teacherSessions.getDocuments()) {
                        String priorSubject = priorSession.getString("subject");
                        Long priorCreatedAt = priorSession.getLong("createdAtMillis");
                        boolean notAfterThisSession = priorSession.getId().equals(sessionId)
                                || priorCreatedAt == null
                                || priorCreatedAt <= sessionCreatedAt;

                        if (notAfterThisSession
                                && priorSubject != null
                                && priorSubject.trim().equalsIgnoreCase(normalizedSessionSubject)) {
                            lectureNumber++;
                        }
                    }
                    final int lectureNumberForSession = Math.max(1, lectureNumber);

                    firestore.collection("students")
                            .get()
                            .addOnSuccessListener(allStudents -> {
                                List<DocumentSnapshot> studentProfiles = allStudents.getDocuments();
                                if (studentProfiles.isEmpty()) {
                                    markAbsencesFinalized(sessionId, true, completion);
                                    return;
                                }

                                AtomicInteger pending = new AtomicInteger(studentProfiles.size());
                                AtomicBoolean hadFailure = new AtomicBoolean(false);

                                for (DocumentSnapshot profile : studentProfiles) {
                                    String role = profile.getString("role");
                                    if ("Teacher".equalsIgnoreCase(role)) {
                                        completeOneStudentAbsenceCheck(
                                                sessionId, pending, hadFailure, true, completion
                                        );
                                        continue;
                                    }

                                    String studentUid = profile.getId();
                                    firestore.collection("students")
                                            .document(studentUid)
                                            .collection("subjects")
                                            .get()
                                            .addOnSuccessListener(subjectsSnapshot -> {
                                                boolean enrolledInThisSubject = false;
                                                for (DocumentSnapshot subjectDocument : subjectsSnapshot.getDocuments()) {
                                                    String savedSubject = subjectDocument.getString("name");
                                                    if (savedSubject != null
                                                            && savedSubject.trim().equalsIgnoreCase(normalizedSessionSubject)) {
                                                        enrolledInThisSubject = true;
                                                        break;
                                                    }
                                                }

                                                if (!enrolledInThisSubject) {
                                                    completeOneStudentAbsenceCheck(
                                                            sessionId, pending, hadFailure, true, completion
                                                    );
                                                    return;
                                                }

                                                markStudentAbsentIfNotCheckedIn(
                                                        sessionId,
                                                        normalizedSessionSubject,
                                                        sessionCreatedAt,
                                                        teacherUid,
                                                        profile,
                                                        lectureNumberForSession,
                                                        success -> completeOneStudentAbsenceCheck(
                                                                sessionId, pending, hadFailure, success, completion
                                                        )
                                                );
                                            })
                                            .addOnFailureListener(e ->
                                                    completeOneStudentAbsenceCheck(
                                                            sessionId, pending, hadFailure, false, completion
                                                    ));
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(
                                        TeacherAttendanceActivity.this,
                                        "Could not read student profiles to finalize absences. Check Firestore rules.",
                                        Toast.LENGTH_LONG
                                ).show();
                                if (completion != null) completion.onComplete(false);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            TeacherAttendanceActivity.this,
                            "Could not read teacher attendance sessions to finalize absences. Check Firestore rules or your connection.",
                            Toast.LENGTH_LONG
                    ).show();
                    if (completion != null) completion.onComplete(false);
                });
    }

    private void completeOneStudentAbsenceCheck(
            String sessionId,
            AtomicInteger pending,
            AtomicBoolean hadFailure,
            boolean success,
            AbsenceCompletion completion
    ) {
        if (!success) hadFailure.set(true);
        if (pending.decrementAndGet() == 0) {
            markAbsencesFinalized(sessionId, !hadFailure.get(), completion);
        }
    }

    private void markAbsencesFinalized(
            String sessionId,
            boolean success,
            AbsenceCompletion completion
    ) {
        Map<String, Object> update = new HashMap<>();
        update.put("absencesFinalized", success);
        update.put("absencesFinalizationAttemptedAtMillis", System.currentTimeMillis());

        firestore.collection("attendanceSessions")
                .document(sessionId)
                .update(update)
                .addOnSuccessListener(unused -> {
                    if (completion != null) completion.onComplete(success);
                })
                .addOnFailureListener(e -> {
                    if (completion != null) completion.onComplete(false);
                });
    }

    private void markStudentAbsentIfNotCheckedIn(
            String sessionId,
            String subject,
            long sessionCreatedAt,
            String teacherUid,
            DocumentSnapshot student,
            int lectureNumber,
            AbsenceCompletion completion
    ) {
        String studentUid = student.getId();
        DocumentReference participantReference = firestore
                .collection("attendanceSessions")
                .document(sessionId)
                .collection("participants")
                .document(studentUid);

        // Use a deterministic document ID so retrying the finalization cannot
        // create duplicate automatic-Absent records for this student/session.
        DocumentReference automaticAttendanceReference = firestore
                .collection("students")
                .document(studentUid)
                .collection("attendance")
                .document("automatic_absence_" + sessionId);

        participantReference.get()
                .addOnSuccessListener(participant -> {
                    String participantStatus = participant.getString("status");
                    if (participant.exists()
                            && ("Present".equalsIgnoreCase(participantStatus)
                            || "Absent".equalsIgnoreCase(participantStatus))) {
                        // Existing Present or Absent is already final; never overwrite it.
                        if (completion != null) completion.onComplete(true);
                        return;
                    }

                    firestore.collection("students")
                            .document(studentUid)
                            .collection("attendance")
                            .whereEqualTo("sessionId", sessionId)
                            .get()
                            .addOnSuccessListener(sessionAttendance -> {
                                boolean hasPresentRecord = false;
                                boolean hasAbsentRecord = false;

                                for (DocumentSnapshot record : sessionAttendance.getDocuments()) {
                                    String savedStatus = record.getString("status");
                                    if ("Present".equalsIgnoreCase(savedStatus)) {
                                        hasPresentRecord = true;
                                        break;
                                    }
                                    if ("Absent".equalsIgnoreCase(savedStatus)) {
                                        hasAbsentRecord = true;
                                    }
                                }

                                if (hasPresentRecord || hasAbsentRecord) {
                                    String existingStatus = hasPresentRecord ? "Present" : "Absent";
                                    Map<String, Object> recoveredParticipant = buildAutomaticParticipant(
                                            student, subject, existingStatus, teacherUid,
                                            System.currentTimeMillis()
                                    );
                                    participantReference.set(recoveredParticipant, SetOptions.merge())
                                            .addOnSuccessListener(unused -> {
                                                if (completion != null) completion.onComplete(true);
                                            })
                                            .addOnFailureListener(e -> {
                                                if (completion != null) completion.onComplete(false);
                                            });
                                    return;
                                }

                                long now = System.currentTimeMillis();
                                long recordDate = sessionCreatedAt > 0
                                        ? sessionCreatedAt : now;

                                Map<String, Object> participantData = buildAutomaticParticipant(
                                        student, subject, "Absent", teacherUid, now
                                );

                                Map<String, Object> attendanceData = new HashMap<>();
                                attendanceData.put("subject", subject);
                                attendanceData.put("status", "Absent");
                                attendanceData.put("date", formatAttendanceDate(recordDate));
                                attendanceData.put("lectureNumber", lectureNumber);
                                attendanceData.put("sessionId", sessionId);
                                attendanceData.put("attendanceMethod", "Automatic Session Finalization");
                                attendanceData.put("automaticAbsence", true);
                                attendanceData.put("createdAt", now);

                                WriteBatch batch = firestore.batch();
                                batch.set(participantReference, participantData, SetOptions.merge());
                                batch.set(automaticAttendanceReference, attendanceData);
                                batch.commit()
                                        .addOnSuccessListener(unused -> {
                                            if (completion != null) completion.onComplete(true);
                                        })
                                        .addOnFailureListener(e -> {
                                            if (completion != null) completion.onComplete(false);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                if (completion != null) completion.onComplete(false);
                            });
                })
                .addOnFailureListener(e -> {
                    if (completion != null) completion.onComplete(false);
                });
    }

    private Map<String, Object> buildAutomaticParticipant(
            DocumentSnapshot student,
            String subject,
            String status,
            String teacherUid,
            long markedAtMillis
    ) {
        String studentUid = student.getId();
        String name = student.getString("name");
        if (name == null || name.trim().isEmpty()) name = "Student";
        String email = student.getString("email");
        if (email == null) email = "";
        String rollNumber = student.getString("rollNumber");
        if (rollNumber == null) rollNumber = "";

        Map<String, Object> participant = new HashMap<>();
        participant.put("studentUid", studentUid);
        participant.put("name", name);
        participant.put("email", email);
        participant.put("rollNumber", rollNumber);
        participant.put("rollNumberNormalized", student.getString("rollNumberNormalized"));
        participant.put("subject", subject);
        participant.put("status", status);
        participant.put("markedAtMillis", markedAtMillis);
        participant.put("attendanceMethod", "Present".equalsIgnoreCase(status)
                ? "Recovered Attendance Record" : "Automatic Session Finalization");
        participant.put("automaticAbsence", "Absent".equalsIgnoreCase(status));
        participant.put("markedByTeacherUid", teacherUid);
        return participant;
    }

    private String formatAttendanceDate(long millis) {
        return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                .format(new Date(millis));
    }

    // ---------------------------------------------------------
    // RESET SESSION UI
    // ---------------------------------------------------------

    private void resetSessionUI() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        activeSessionCard.setVisibility(
                View.GONE
        );

        btnStartAttendance.setEnabled(
                true
        );
        btnStopAttendance.setEnabled(false);

        etSubject.setEnabled(
                true
        );

        currentSessionId = null;
        currentCode = null;
        currentSessionCreatedAtMillis = 0L;
    }

    // ---------------------------------------------------------
    // CODE GENERATION
    // ---------------------------------------------------------

    private String generateAttendanceCode() {

        SecureRandom random =
                new SecureRandom();

        int code =
                100000
                        + random.nextInt(
                        900000
                );

        return String.valueOf(
                code
        );
    }

    // ---------------------------------------------------------
    // THEME
    // ---------------------------------------------------------

    private void loadThemeColors() {

        colorBackground =
                ContextCompat.getColor(
                        this,
                        R.color.app_background
                );

        colorCard =
                ContextCompat.getColor(
                        this,
                        R.color.app_card
                );

        colorPrimary =
                ContextCompat.getColor(
                        this,
                        R.color.primary
                );

        colorText =
                ContextCompat.getColor(
                        this,
                        R.color.app_text
                );

        colorSecondary =
                ContextCompat.getColor(
                        this,
                        R.color.app_secondary_text
                );

        colorMuted =
                ContextCompat.getColor(
                        this,
                        R.color.app_muted_text
                );

        colorBorder =
                ContextCompat.getColor(
                        this,
                        R.color.app_border
                );

        colorSuccess =
                ContextCompat.getColor(
                        this,
                        R.color.success
                );

        colorError =
                ContextCompat.getColor(
                        this,
                        R.color.error
                );

        colorInput =
                ContextCompat.getColor(
                        this,
                        R.color.app_input
                );
    }

    private void applyTheme() {

        View root =
                findViewById(android.R.id.content);

        root.setBackgroundColor(
                colorBackground
        );

        getWindow().setStatusBarColor(
                colorBackground
        );

        getWindow().setNavigationBarColor(
                colorBackground
        );

        boolean darkMode =
                isDarkModeEnabled();

        getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                        darkMode
                                ? 0
                                : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );

        if (etSubject != null) {

            etSubject.setTextColor(
                    colorText
            );

            etSubject.setHintTextColor(
                    colorMuted
            );

            etSubject.setBackgroundTintList(
                    android.content.res
                            .ColorStateList
                            .valueOf(
                                    colorPrimary
                            )
            );
        }

        if (tvSessionSubject != null) {

            tvSessionSubject.setTextColor(
                    colorText
            );
        }

        if (tvSessionCode != null) {

            tvSessionCode.setTextColor(
                    colorPrimary
            );
        }

        if (tvSessionTimer != null) {

            tvSessionTimer.setTextColor(
                    colorSecondary
            );
        }

        if (tvSessionStatus != null) {

            tvSessionStatus.setTextColor(
                    colorSuccess
            );
        }

        if (btnStartAttendance != null) {

            btnStartAttendance.setBackgroundTintList(
                    android.content.res
                            .ColorStateList
                            .valueOf(
                                    colorPrimary
                            )
            );

            btnStartAttendance.setTextColor(
                    android.graphics.Color.WHITE
            );

            btnStartAttendance.setCornerRadius(
                    dpToPx(14)
            );
        }

        if (btnStopAttendance != null) {

            btnStopAttendance.setBackgroundTintList(
                    android.content.res
                            .ColorStateList
                            .valueOf(
                                    colorInput
                            )
            );

            btnStopAttendance.setTextColor(
                    colorError
            );

            btnStopAttendance.setStrokeColor(
                    android.content.res
                            .ColorStateList
                            .valueOf(
                                    colorBorder
                            )
            );

            btnStopAttendance.setCornerRadius(
                    dpToPx(14)
            );
        }

        if (activeSessionCard != null) {

            activeSessionCard
                    .setCardBackgroundColor(
                            colorCard
                    );

            activeSessionCard.setStrokeColor(
                    colorBorder
            );
        }
    }

    private boolean isDarkModeEnabled() {

        return getSharedPreferences(
                "CampusMindSettings",
                MODE_PRIVATE
        ).getBoolean(
                "dark_mode",
                false
        );
    }

    // ---------------------------------------------------------
    // RESUME
    // ---------------------------------------------------------

    @Override
    protected void onResume() {

        super.onResume();

        loadThemeColors();
        applyTheme();

        if (teacherAccessVerified && currentSessionId == null && !restoringSession) {
            restoreActiveSession();
        }
    }

    // ---------------------------------------------------------
    // DESTROY
    // ---------------------------------------------------------

    @Override
    protected void onDestroy() {

        if (countDownTimer != null) {

            countDownTimer.cancel();

            countDownTimer = null;
        }

        if (bleManager != null) {

            bleManager.stopSessionWatcher();
        }

        super.onDestroy();
    }

    // ---------------------------------------------------------
    // DP
    // ---------------------------------------------------------


    private int dpToPx(
            int dp
    ) {

        return Math.round(
                dp
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
