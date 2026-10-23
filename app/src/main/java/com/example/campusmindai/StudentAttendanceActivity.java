
package com.example.campusmindai;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class StudentAttendanceActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private static final int BLE_PERMISSION_REQUEST = 602;
    private static final long BLE_DETECTION_MAX_AGE_MILLIS = 5000L;

    private AttendanceBleScanner attendanceBleScanner;
    private DocumentSnapshot pendingSessionForPermission;
    private TextView tvProximityStatus;
    private String detectedBleCode;
    private String expectedCodeForOpenDialog;
    private int detectedBleRssi = -999;
    private long lastBleDetectionAtMillis = 0L;

    private LinearLayout sessionContainer;
    private TextView tvEmpty;
    private TextView tvStatus;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.student_attendance_activity
        );

        firebaseAuth =
                FirebaseAuth.getInstance();

        firestore =
                FirebaseFirestore.getInstance();

        initializeBleScanner();

        sessionContainer =
                findViewById(
                        R.id.studentSessionContainer
                );

        tvEmpty =
                findViewById(
                        R.id.tvStudentAttendanceEmpty
                );

        tvStatus =
                findViewById(
                        R.id.tvStudentAttendanceStatus
                );

        loadThemeColors();
        applyTheme();

        loadActiveSessions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadThemeColors();
        applyTheme();

        loadActiveSessions();
    }

    private void loadActiveSessions() {

        sessionContainer.removeAllViews();

        tvEmpty.setVisibility(
                View.GONE
        );

        tvStatus.setText(
                "Checking for active classroom attendance..."
        );

        firestore
                .collection("attendanceSessions")
                .whereEqualTo(
                        "active",
                        true
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    boolean found =
                            false;

                    for (
                            DocumentSnapshot document :
                            snapshot.getDocuments()
                    ) {

                        Long expiresAt =
                                document.getLong(
                                        "expiresAtMillis"
                                );

                        if (expiresAt == null) {
                            continue;
                        }

                        if (
                                System.currentTimeMillis()
                                        >= expiresAt
                        ) {
                            continue;
                        }

                        String subject =
                                document.getString(
                                        "subject"
                                );

                        if (subject == null) {
                            subject = "Unknown Subject";
                        }

                        addSessionCard(
                                document,
                                subject,
                                expiresAt
                        );

                        found = true;
                    }

                    if (!found) {

                        tvEmpty.setVisibility(
                                View.VISIBLE
                        );

                        tvStatus.setText(
                                "No active attendance session found"
                        );

                    } else {

                        tvStatus.setText(
                                "Active classroom attendance sessions"
                        );
                    }

                    applyTheme();
                })
                .addOnFailureListener(e -> {

                    tvStatus.setText(
                            "Unable to check attendance sessions"
                    );

                    tvEmpty.setVisibility(
                            View.VISIBLE
                    );

                    Toast.makeText(
                            this,
                            "Unable to load attendance",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void addSessionCard(
            DocumentSnapshot document,
            String subject,
            long expiresAt
    ) {

        MaterialCardView card =
                new MaterialCardView(this);

        card.setCardBackgroundColor(
                colorCard
        );

        card.setStrokeColor(
                colorBorder
        );

        card.setStrokeWidth(
                dpToPx(1)
        );

        card.setRadius(
                dpToPx(20)
        );

        card.setCardElevation(
                dpToPx(2)
        );

        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                dpToPx(18),
                dpToPx(18),
                dpToPx(18),
                dpToPx(18)
        );

        TextView subjectView =
                new TextView(this);

        subjectView.setText(
                subject
        );

        subjectView.setTextColor(
                colorText
        );

        subjectView.setTextSize(
                18
        );

        subjectView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        content.addView(
                subjectView
        );

        TextView expiryView =
                new TextView(this);

        expiryView.setText(
                getExpiryText(expiresAt)
        );

        expiryView.setTextColor(
                colorSecondary
        );

        expiryView.setTextSize(
                13
        );

        LinearLayout.LayoutParams
                expiryParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        expiryParams.topMargin =
                dpToPx(5);

        content.addView(
                expiryView,
                expiryParams
        );

        MaterialButton
                verifyButton =
                new MaterialButton(this);

        verifyButton.setText(
                "MARK ATTENDANCE"
        );

        verifyButton.setAllCaps(false);

        verifyButton.setTextColor(
                android.graphics.Color.WHITE
        );

        verifyButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        verifyButton.setBackgroundTintList(
                android.content.res
                        .ColorStateList
                        .valueOf(
                                colorPrimary
                        )
        );

        verifyButton.setCornerRadius(
                dpToPx(13)
        );

        verifyButton.setInsetTop(0);
        verifyButton.setInsetBottom(0);

        LinearLayout.LayoutParams
                buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(50)
                );

        buttonParams.topMargin =
                dpToPx(15);

        content.addView(
                verifyButton,
                buttonParams
        );

        verifyButton.setOnClickListener(
                v -> showCodeDialog(
                        document
                )
        );

        card.addView(
                content
        );

        LinearLayout.LayoutParams
                cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin =
                dpToPx(14);

        sessionContainer.addView(
                card,
                cardParams
        );
    }

    private void showCodeDialog(
            DocumentSnapshot session
    ) {

        String sessionId =
                session.getId();

        String subject =
                session.getString(
                        "subject"
                );

        String expectedCode =
                session.getString(
                        "code"
                );

        Long expiresAt =
                session.getLong(
                        "expiresAtMillis"
                );

        if (
                expectedCode == null ||
                        expiresAt == null
        ) {

            Toast.makeText(
                    this,
                    "Invalid attendance session",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (
                System.currentTimeMillis()
                        >= expiresAt
        ) {

            Toast.makeText(
                    this,
                    "This attendance session has expired",
                    Toast.LENGTH_SHORT
            ).show();

            loadActiveSessions();

            return;
        }

        if (!hasBleScanPermission()) {

            pendingSessionForPermission = session;
            requestBleScanPermission();
            return;
        }

        detectedBleCode = null;
        detectedBleRssi = -999;
        lastBleDetectionAtMillis = 0L;
        expectedCodeForOpenDialog = expectedCode;

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setPadding(
                dpToPx(22),
                dpToPx(5),
                dpToPx(22),
                dpToPx(5)
        );

        TextView info =
                new TextView(this);

        info.setText(
                "Enter the 6-digit code provided by your teacher for "
                        + subject
                        + "."
        );

        info.setTextColor(
                colorSecondary
        );

        info.setTextSize(
                14
        );

        container.addView(
                info
        );

        tvProximityStatus =
                new TextView(this);

        tvProximityStatus.setText(
                "Searching for the teacher's Bluetooth signal..."
        );

        tvProximityStatus.setTextColor(
                colorSecondary
        );

        tvProximityStatus.setTextSize(12);

        LinearLayout.LayoutParams proximityParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        proximityParams.topMargin = dpToPx(8);

        container.addView(
                tvProximityStatus,
                proximityParams
        );

        EditText codeInput =
                new EditText(this);

        codeInput.setHint(
                "Attendance code"
        );

        codeInput.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );

        codeInput.setSingleLine(
                true
        );

        codeInput.setTextColor(
                colorText
        );

        codeInput.setHintTextColor(
                colorMuted
        );

        codeInput.setTextSize(
                18
        );

        codeInput.setGravity(
                android.view.Gravity.CENTER
        );

        codeInput.setBackgroundTintList(
                android.content.res
                        .ColorStateList
                        .valueOf(
                                colorPrimary
                        )
        );

        LinearLayout.LayoutParams
                codeParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(55)
                );

        codeParams.topMargin =
                dpToPx(15);

        container.addView(
                codeInput,
                codeParams
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Verify Attendance"
                        )
                        .setView(
                                container
                        )
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .setPositiveButton(
                                "VERIFY",
                                null
                        )
                        .create();

        dialog.setOnDismissListener(d -> {

            if (attendanceBleScanner != null) {
                attendanceBleScanner.stopScanning();
            }

            tvProximityStatus = null;
            detectedBleCode = null;
            expectedCodeForOpenDialog = null;
            detectedBleRssi = -999;
            lastBleDetectionAtMillis = 0L;
        });

        dialog.setOnShowListener(
                d -> {

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setTextColor(
                            colorPrimary
                    );

                    dialog.getButton(
                            AlertDialog.BUTTON_NEGATIVE
                    ).setTextColor(
                            colorSecondary
                    );

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setOnClickListener(
                            v -> verifyAttendance(
                                    session,
                                    sessionId,
                                    expectedCode,
                                    expiresAt,
                                    codeInput,
                                    dialog
                            )
                    );

                    applyDialogTheme(
                            dialog
                    );
                }
        );

        dialog.show();

        applyDialogTheme(
                dialog
        );

        if (attendanceBleScanner != null) {
            boolean started = attendanceBleScanner.startScanning();
            if (!started && tvProximityStatus != null) {
                tvProximityStatus.setText(
                        "Bluetooth scan could not start. Check Bluetooth and permissions."
                );
                tvProximityStatus.setTextColor(colorError);
            }
        }
    }

    private void verifyAttendance(
            DocumentSnapshot session,
            String sessionId,
            String expectedCode,
            long expiresAt,
            EditText codeInput,
            AlertDialog dialog
    ) {

        String enteredCode =
                codeInput
                        .getText()
                        .toString()
                        .trim();

        if (enteredCode.isEmpty()) {

            codeInput.setError(
                    "Enter the attendance code"
            );

            return;
        }

        if (!enteredCode.equals(expectedCode)) {

            codeInput.setError(
                    "Incorrect attendance code"
            );

            return;
        }

        if (
                System.currentTimeMillis()
                        >= expiresAt
        ) {

            Toast.makeText(
                    this,
                    "Attendance session has expired",
                    Toast.LENGTH_SHORT
            ).show();

            dialog.dismiss();

            loadActiveSessions();

            return;
        }

        boolean recentTeacherSignal =
                detectedBleCode != null
                        && detectedBleCode.equals(expectedCode)
                        && lastBleDetectionAtMillis > 0L
                        && System.currentTimeMillis() - lastBleDetectionAtMillis
                        <= BLE_DETECTION_MAX_AGE_MILLIS
                        && detectedBleRssi >= -75;

        if (!recentTeacherSignal) {

            if (tvProximityStatus != null) {
                tvProximityStatus.setText(
                        "Teacher's Bluetooth signal not confirmed nearby. Move closer and wait for the green confirmation."
                );
                tvProximityStatus.setTextColor(colorError);
            }

            Toast.makeText(
                    this,
                    "Move closer to the teacher's phone and wait for Bluetooth verification.",
                    Toast.LENGTH_LONG
            ).show();

            if (attendanceBleScanner != null
                    && !attendanceBleScanner.isScanning()) {
                attendanceBleScanner.startScanning();
            }

            return;
        }

        if (
                firebaseAuth.getCurrentUser()
                        == null
        ) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String uid =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        String subject =
                session.getString(
                        "subject"
                );

        firestore
                .collection("students")
                .document(uid)
                .collection("attendance")
                .whereEqualTo(
                        "sessionId",
                        sessionId
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            if (!snapshot.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "Attendance already marked",
                                        Toast.LENGTH_SHORT
                                ).show();

                                dialog.dismiss();

                                return;
                            }

                            firestore
                                    .collection("students")
                                    .document(uid)
                                    .collection("attendance")
                                    .whereEqualTo(
                                            "subject",
                                            subject
                                    )
                                    .get()
                                    .addOnSuccessListener(
                                            subjectSnapshot -> {

                                                int lectureNumber =
                                                        subjectSnapshot
                                                                .size()
                                                                + 1;

                                                java.util.Map<
                                                        String,
                                                        Object
                                                        > data =
                                                        new java.util.HashMap<>();

                                                data.put(
                                                        "subject",
                                                        subject
                                                );

                                                data.put(
                                                        "status",
                                                        "Present"
                                                );

                                                data.put(
                                                        "date",
                                                        getTodayDate()
                                                );

                                                data.put(
                                                        "lectureNumber",
                                                        lectureNumber
                                                );

                                                data.put(
                                                        "sessionId",
                                                        sessionId
                                                );

                                                data.put(
                                                        "attendanceMethod",
                                                        "BLE + Verified Code"
                                                );

                                                data.put(
                                                        "createdAt",
                                                        System.currentTimeMillis()
                                                );

                                                firestore
                                                        .collection(
                                                                "students"
                                                        )
                                                        .document(uid)
                                                        .collection(
                                                                "attendance"
                                                        )
                                                        .add(data)
                                                        .addOnSuccessListener(
                                                                unused -> saveLiveParticipant(
                                                                        sessionId,
                                                                        uid,
                                                                        subject,
                                                                        dialog
                                                                )
                                                        )
                                                        .addOnFailureListener(
                                                                e -> {

                                                                    Toast.makeText(
                                                                            this,
                                                                            "Unable to save attendance",
                                                                            Toast.LENGTH_SHORT
                                                                    ).show();
                                                                }
                                                        );
                                            }
                                    );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Unable to verify attendance",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }

    // =============================================================
    // SAVE LIVE PARTICIPANT FOR THE TEACHER'S ATTENDANCE SCREEN
    // =============================================================


    private void saveLiveParticipant(
            String sessionId,
            String uid,
            String subject,
            AlertDialog dialog
    ) {

        firestore.collection("students")
                .document(uid)
                .get()
                .addOnSuccessListener(profile -> {

                    String name = profile.getString("name");

                    if (name == null || name.trim().isEmpty()) {
                        name = "Student";
                    }

                    // Get roll number from the student's profile.
                    String rollNumber =
                            profile.getString("rollNumber");

                    if (rollNumber == null) {
                        rollNumber = "";
                    }

                    String email = "";

                    if (firebaseAuth.getCurrentUser() != null
                            && firebaseAuth.getCurrentUser().getEmail() != null) {

                        email = firebaseAuth.getCurrentUser().getEmail();
                    }

                    java.util.Map<String, Object> participant =
                            new java.util.HashMap<>();

                    participant.put("studentUid", uid);
                    participant.put("name", name);
                    participant.put("email", email);
                    participant.put("rollNumber", rollNumber);
                    participant.put(
                            "rollNumberNormalized",
                            rollNumber.trim().toLowerCase(
                                    java.util.Locale.ROOT
                            )
                    );
                    participant.put("subject", subject);
                    participant.put("status", "Present");
                    participant.put(
                            "markedAtMillis",
                            System.currentTimeMillis()
                    );

                    firestore.collection("attendanceSessions")
                            .document(sessionId)
                            .collection("participants")
                            .document(uid)
                            .set(participant)
                            .addOnSuccessListener(saved -> {

                                dialog.dismiss();

                                Toast.makeText(
                                        StudentAttendanceActivity.this,
                                        "Attendance marked successfully",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();
                            })
                            .addOnFailureListener(e -> {

                                dialog.dismiss();

                                Toast.makeText(
                                        StudentAttendanceActivity.this,
                                        "Attendance saved, but the teacher's live list could not be updated. Check Firestore rules.",
                                        Toast.LENGTH_LONG
                                ).show();

                                finish();
                            });
                })
                .addOnFailureListener(e -> {

                    dialog.dismiss();

                    Toast.makeText(
                            StudentAttendanceActivity.this,
                            "Attendance saved, but student details could not be loaded.",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                });
    }


    // =============================================================
    // BLE SCANNER INITIALIZATION
    // =============================================================

    private void initializeBleScanner() {

        attendanceBleScanner =
                new AttendanceBleScanner(
                        this,
                        new AttendanceBleScanner.Listener() {

                            @Override
                            public void onTeacherDetected(
                                    String attendanceCode,
                                    int rssi
                            ) {

                                runOnUiThread(() -> {

                                    detectedBleCode = attendanceCode;
                                    detectedBleRssi = rssi;
                                    lastBleDetectionAtMillis =
                                            System.currentTimeMillis();

                                    if (tvProximityStatus == null) {
                                        return;
                                    }

                                    if (expectedCodeForOpenDialog != null
                                            && expectedCodeForOpenDialog.equals(
                                            attendanceCode
                                    )) {

                                        tvProximityStatus.setText(
                                                "✓ Teacher detected nearby • Bluetooth verified"
                                        );

                                        tvProximityStatus.setTextColor(
                                                colorSuccess
                                        );

                                    } else {

                                        tvProximityStatus.setText(
                                                "A nearby CampusMind signal was found, but it does not match this session."
                                        );

                                        tvProximityStatus.setTextColor(
                                                colorError
                                        );
                                    }
                                });
                            }

                            @Override
                            public void onScanStarted() {

                                runOnUiThread(() -> {

                                    if (tvProximityStatus != null
                                            && detectedBleCode == null) {

                                        tvProximityStatus.setText(
                                                "Searching for the teacher's Bluetooth signal..."
                                        );

                                        tvProximityStatus.setTextColor(
                                                colorSecondary
                                        );
                                    }
                                });
                            }

                            @Override
                            public void onSignalTooWeak(int rssi) {

                                runOnUiThread(() -> {

                                    long now = System.currentTimeMillis();
                                    boolean recentGoodSignal =
                                            detectedBleCode != null
                                                    && lastBleDetectionAtMillis > 0L
                                                    && now - lastBleDetectionAtMillis
                                                    <= BLE_DETECTION_MAX_AGE_MILLIS;

                                    // Do not let intermittent weak BLE packets erase a
                                    // valid nearby signal received moments ago.
                                    if (!recentGoodSignal) {
                                        detectedBleCode = null;
                                        detectedBleRssi = rssi;
                                        lastBleDetectionAtMillis = 0L;
                                    }

                                    if (tvProximityStatus != null && !recentGoodSignal) {

                                        tvProximityStatus.setText(
                                                "Teacher signal found but too weak. Move closer to the teacher."
                                        );

                                        tvProximityStatus.setTextColor(
                                                colorError
                                        );
                                    }
                                });
                            }

                            @Override
                            public void onScanError(String message) {

                                runOnUiThread(() -> {

                                    if (tvProximityStatus != null) {

                                        tvProximityStatus.setText(
                                                message
                                        );

                                        tvProximityStatus.setTextColor(
                                                colorError
                                        );
                                    }
                                });
                            }
                        }
                );
    }

    // =============================================================
    // BLUETOOTH PERMISSIONS
    // =============================================================

    private boolean hasBleScanPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            return checkSelfPermission(
                    Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(
                    Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED;
        }

        return checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestBleScanPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                    },
                    BLE_PERMISSION_REQUEST
            );

        } else {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    BLE_PERMISSION_REQUEST
            );
        }
    }

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

        if (requestCode != BLE_PERMISSION_REQUEST) {
            return;
        }

        if (hasBleScanPermission()) {

            DocumentSnapshot pendingSession =
                    pendingSessionForPermission;

            pendingSessionForPermission = null;

            if (pendingSession != null) {
                showCodeDialog(pendingSession);
            }

        } else {

            pendingSessionForPermission = null;

            Toast.makeText(
                    this,
                    "Bluetooth permission is required to verify classroom proximity.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onDestroy() {

        if (attendanceBleScanner != null) {
            attendanceBleScanner.release();
            attendanceBleScanner = null;
        }

        super.onDestroy();
    }

    private String getExpiryText(
            long expiresAt
    ) {

        long remaining =
                Math.max(
                        0,
                        expiresAt
                                - System.currentTimeMillis()
                );

        long seconds =
                remaining / 1000;

        return "Active • expires in "
                + String.format(
                Locale.getDefault(),
                "%02d:%02d",
                seconds / 60,
                seconds % 60
        );
    }

    private String getTodayDate() {

        return new java.text.SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
        ).format(
                new java.util.Date()
        );
    }

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
                findViewById(
                        android.R.id.content
                );

        root.setBackgroundColor(
                colorBackground
        );

        getWindow().setStatusBarColor(
                colorBackground
        );

        getWindow().setNavigationBarColor(
                colorBackground
        );

        boolean dark =
                getSharedPreferences(
                        "CampusMindSettings",
                        MODE_PRIVATE
                ).getBoolean(
                        "dark_mode",
                        false
                );

        getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                        dark
                                ? 0
                                : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );

        tvStatus.setTextColor(
                colorSecondary
        );

        tvEmpty.setTextColor(
                colorMuted
        );
    }

    private void applyDialogTheme(
            AlertDialog dialog
    ) {

        if (!isDarkModeEnabled()) {
            return;
        }

        int darkText =
                ContextCompat.getColor(
                        this,
                        R.color.app_text
                );

        int darkSecondary =
                ContextCompat.getColor(
                        this,
                        R.color.app_secondary_text
                );

        int darkCard =
                ContextCompat.getColor(
                        this,
                        R.color.app_card
                );

        dialog.getWindow()
                .setBackgroundDrawableResource(
                        android.R.color.transparent
                );

        int contentId =
                getResources()
                        .getIdentifier(
                                "alertTitle",
                                "id",
                                "android"
                        );

        TextView title =
                dialog.findViewById(
                        contentId
                );

        if (title != null) {
            title.setTextColor(
                    darkText
            );
        }

        View message =
                dialog.findViewById(
                        android.R.id.message
                );

        if (message instanceof TextView) {
            ((TextView) message)
                    .setTextColor(
                            darkSecondary
                    );
        }

        View root =
                dialog.findViewById(
                        android.R.id.content
                );

        if (root != null) {
            root.setBackgroundColor(
                    darkCard
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
