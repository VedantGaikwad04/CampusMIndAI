
package com.example.campusmindai;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TeacherAttendanceHistoryActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private LinearLayout list;
    private TextView empty;

    private int bg, card, text, secondary, muted, border;
    private int primary, success, error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadColors();
        createUi();

        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        loadHistory();
    }

    // ---------------------------------------------------------
    // UI
    // ---------------------------------------------------------

    private void createUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(bg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(28));
        scroll.addView(root);

        root.addView(makeText("Attendance History", 25, text, true));

        TextView subtitle = makeText(
                "Review sessions and correct student attendance",
                13, secondary, false
        );

        LinearLayout.LayoutParams subtitleParams = wrap();
        subtitleParams.topMargin = dp(5);
        root.addView(subtitle, subtitleParams);

        empty = makeText("Loading attendance history...", 14, secondary, false);
        empty.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams emptyParams = wrap();
        emptyParams.topMargin = dp(24);
        root.addView(empty, emptyParams);

        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams listParams = wrap();
        listParams.topMargin = dp(14);
        root.addView(list, listParams);

        setContentView(scroll);
    }

    // ---------------------------------------------------------
    // LOAD TEACHER'S HISTORY
    // ---------------------------------------------------------

    private void loadHistory() {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String teacherUid = auth.getCurrentUser().getUid();

        db.collection("attendanceSessions")
                .whereEqualTo("teacherUid", teacherUid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    list.removeAllViews();

                    List<DocumentSnapshot> sessions =
                            new ArrayList<>(snapshot.getDocuments());

                    Collections.sort(sessions,
                            (a, b) -> Long.compare(timeOf(b), timeOf(a)));

                    if (sessions.isEmpty()) {
                        empty.setText("No attendance sessions found.");
                        return;
                    }

                    empty.setText("Sessions found: " + sessions.size());

                    for (DocumentSnapshot session : sessions) {
                        addSessionCard(session);
                    }
                })
                .addOnFailureListener(e -> {
                    empty.setText("Could not load history. Check your internet connection and Firestore rules.");
                    empty.setTextColor(error);
                });
    }

    // ---------------------------------------------------------
    // SESSION CARD
    // ---------------------------------------------------------

    private void addSessionCard(DocumentSnapshot session) {

        MaterialCardView cardView = new MaterialCardView(this);
        cardView.setCardBackgroundColor(card);
        cardView.setStrokeColor(border);
        cardView.setStrokeWidth(dp(1));
        cardView.setRadius(dp(16));
        cardView.setCardElevation(dp(2));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(15), dp(16), dp(15));
        cardView.addView(content);

        String subjectValue = session.getString("subject");
        final String subject =
                subjectValue == null || subjectValue.trim().isEmpty()
                        ? "Class session" : subjectValue;

        content.addView(makeText(subject, 18, text, true));

        long created = timeOf(session);
        String date = created > 0
                ? new SimpleDateFormat(
                "dd MMM yyyy • hh:mm a",
                Locale.getDefault()
        ).format(new Date(created))
                : "Date unavailable";

        TextView dateView = makeText(date, 12, secondary, false);
        LinearLayout.LayoutParams dateParams = wrap();
        dateParams.topMargin = dp(5);
        content.addView(dateView, dateParams);

        boolean active = Boolean.TRUE.equals(session.getBoolean("active"));

        TextView state = makeText(
                active ? "● Active" : "● Completed / Stopped",
                12, active ? success : muted, true
        );

        LinearLayout.LayoutParams stateParams = wrap();
        stateParams.topMargin = dp(8);
        content.addView(state, stateParams);

        TextView count = makeText("Loading attendance...", 13, secondary, false);
        LinearLayout.LayoutParams countParams = wrap();
        countParams.topMargin = dp(8);
        content.addView(count, countParams);

        TextView viewStudents = makeText(
                "View attendance records  ›", 14, primary, true
        );

        LinearLayout.LayoutParams viewParams = wrap();
        viewParams.topMargin = dp(12);
        content.addView(viewStudents, viewParams);

        TextView editAttendance = makeText(
                "✎  EDIT ATTENDANCE", 14, primary, true
        );

        LinearLayout.LayoutParams editParams = wrap();
        editParams.topMargin = dp(12);
        content.addView(editAttendance, editParams);

        String sessionId = session.getId();

        db.collection("attendanceSessions")
                .document(sessionId)
                .collection("participants")
                .get()
                .addOnSuccessListener(participants -> {

                    int present = 0;
                    int absent = 0;

                    for (DocumentSnapshot participant :
                            participants.getDocuments()) {

                        String status = participant.getString("status");

                        if ("Absent".equalsIgnoreCase(status)) {
                            absent++;
                        } else {
                            present++;
                        }
                    }

                    count.setText(
                            "Present: " + present + " • Absent: " + absent
                    );
                })
                .addOnFailureListener(e ->
                        count.setText("Could not load attendance count"));

        viewStudents.setOnClickListener(
                v -> showParticipants(sessionId, subject));

        editAttendance.setOnClickListener(
                v -> showRollNumberDialog(sessionId, subject, created));

        LinearLayout.LayoutParams cardParams = wrap();
        cardParams.topMargin = dp(12);
        list.addView(cardView, cardParams);
    }

    // ---------------------------------------------------------
    // VIEW ATTENDANCE RECORDS
    // ---------------------------------------------------------

    private void showParticipants(String sessionId, String subject) {

        db.collection("attendanceSessions")
                .document(sessionId)
                .collection("participants")
                .get()
                .addOnSuccessListener(snapshot -> {

                    StringBuilder message = new StringBuilder();

                    if (snapshot.isEmpty()) {
                        message.append("No attendance records for this session.");
                    } else {
                        for (DocumentSnapshot student :
                                snapshot.getDocuments()) {

                            String name = student.getString("name");
                            String roll = student.getString("rollNumber");
                            String status = student.getString("status");

                            if (name == null || name.trim().isEmpty()) {
                                name = "Student";
                            }

                            if (roll == null || roll.trim().isEmpty()) {
                                roll = "Not assigned";
                            }

                            if (status == null) {
                                status = "Present";
                            }

                            message.append(name)
                                    .append(" • Roll: ")
                                    .append(roll)
                                    .append(" • ")
                                    .append(status)
                                    .append('\n');
                        }
                    }

                    new AlertDialog.Builder(this)
                            .setTitle(subject + " — Attendance")
                            .setMessage(message.toString())
                            .setPositiveButton("Close", null)
                            .show();
                })
                .addOnFailureListener(e ->
                        showError("Could not load attendance. Check Firestore rules."));
    }

    // ---------------------------------------------------------
    // SEARCH BY ROLL NUMBER
    // ---------------------------------------------------------

    private void showRollNumberDialog(
            String sessionId,
            String subject,
            long sessionCreatedAt) {

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Enter student roll number");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(8), dp(24), dp(4));
        container.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Attendance")
                .setMessage(
                        "Search for a registered student by roll number."
                )
                .setView(container)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Search", null)
                .create();

        dialog.setOnShowListener(unused ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {

                            String roll = input.getText()
                                    .toString().trim();

                            if (roll.isEmpty()) {
                                input.setError("Enter a roll number");
                                return;
                            }

                            dialog.dismiss();

                            findStudentByRoll(
                                    sessionId,
                                    subject,
                                    sessionCreatedAt,
                                    roll
                            );
                        }));

        dialog.show();
    }

    private void findStudentByRoll(
            String sessionId,
            String subject,
            long sessionCreatedAt,
            String rollNumber) {

        String normalized = rollNumber.toLowerCase(Locale.ROOT);

        db.collection("students")
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<DocumentSnapshot> matches = new ArrayList<>();

                    for (DocumentSnapshot student :
                            snapshot.getDocuments()) {

                        String role = student.getString("role");

                        if ("Teacher".equalsIgnoreCase(role)) {
                            continue;
                        }

                        String savedNormalized =
                                student.getString("rollNumberNormalized");

                        String savedRoll =
                                student.getString("rollNumber");

                        if (savedNormalized == null && savedRoll != null) {
                            savedNormalized =
                                    savedRoll.trim().toLowerCase(Locale.ROOT);
                        }

                        if (savedNormalized != null
                                && savedNormalized.equals(normalized)) {
                            matches.add(student);
                        }
                    }

                    if (matches.isEmpty()) {
                        showError(
                                "No student found with roll number "
                                        + rollNumber
                                        + ". Check the student's saved profile."
                        );
                        return;
                    }

                    if (matches.size() > 1) {
                        showError(
                                "More than one student has this roll number. "
                                        + "Resolve duplicate roll numbers first."
                        );
                        return;
                    }

                    showStatusDialog(
                            sessionId,
                            subject,
                            sessionCreatedAt,
                            matches.get(0)
                    );
                })
                .addOnFailureListener(e ->
                        showError(
                                "Cannot search students. Check teacher access in Firestore rules."
                        ));
    }

    // ---------------------------------------------------------
    // CHOOSE PRESENT OR ABSENT
    // ---------------------------------------------------------

    private void showStatusDialog(
            String sessionId,
            String subject,
            long sessionCreatedAt,
            DocumentSnapshot student) {

        String name = student.getString("name");
        if (name == null || name.trim().isEmpty()) {
            name = "Student";
        }

        String rollNumber = student.getString("rollNumber");
        if (rollNumber == null) {
            rollNumber = "Not assigned";
        }

        final String studentName = name;
        final String displayedRoll = rollNumber;

        String[] statuses = {"Present", "Absent"};

        new AlertDialog.Builder(this)
                .setTitle(studentName + " • Roll " + displayedRoll)
                // Do not call setMessage() here: Android AlertDialog does not
                // reliably show its message and list together. The options
                // below will now be visible as Present and Absent.
                .setItems(statuses, (dialog, which) -> {

                    String status = statuses[which];

                    new AlertDialog.Builder(this)
                            .setTitle("Confirm Correction")
                            .setMessage(
                                    "Mark " + studentName + " as "
                                            + status + " for " + subject + "?"
                            )
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Save", (d, w) ->
                                    saveCorrection(
                                            sessionId,
                                            subject,
                                            sessionCreatedAt,
                                            student,
                                            status
                                    ))
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------------------------------------------------------
    // SAVE BOTH ATTENDANCE RECORDS IN ONE BATCH
    // ---------------------------------------------------------

    private void saveCorrection(
            String sessionId,
            String subject,
            long sessionCreatedAt,
            DocumentSnapshot student,
            String status) {

        if (auth.getCurrentUser() == null) {
            showError("Please log in again.");
            return;
        }

        String teacherUid = auth.getCurrentUser().getUid();
        String studentUid = student.getId();

        // Verify session ownership before preparing a correction.
        db.collection("attendanceSessions")
                .document(sessionId)
                .get()
                .addOnSuccessListener(session -> {

                    if (!session.exists()
                            || !teacherUid.equals(
                            session.getString("teacherUid"))) {
                        showError("You cannot edit a session owned by another teacher.");
                        return;
                    }

                    // Session may be closed. Closed sessions remain editable.
                    writeCorrection(
                            sessionId,
                            subject,
                            sessionCreatedAt,
                            student,
                            status,
                            teacherUid
                    );
                })
                .addOnFailureListener(e ->
                        showError("Could not verify session ownership."));
    }

    private void writeCorrection(
            String sessionId,
            String subject,
            long sessionCreatedAt,
            DocumentSnapshot student,
            String status,
            String teacherUid) {

        String studentUid = student.getId();

        String name = student.getString("name");
        if (name == null || name.trim().isEmpty()) {
            name = "Student";
        }

        String email = student.getString("email");
        if (email == null) {
            email = "";
        }

        String rollNumber = student.getString("rollNumber");
        if (rollNumber == null) {
            rollNumber = "";
        }

        long now = System.currentTimeMillis();

        Map<String, Object> participant = new HashMap<>();
        participant.put("studentUid", studentUid);
        participant.put("name", name);
        participant.put("email", email);
        participant.put("rollNumber", rollNumber);
        participant.put("subject", subject);
        participant.put("status", status);
        participant.put("markedAtMillis", now);
        participant.put("manualCorrection", true);
        participant.put("correctedBy", teacherUid);
        participant.put("correctedAtMillis", now);

        db.collection("students")
                .document(studentUid)
                .collection("attendance")
                .whereEqualTo("sessionId", sessionId)
                .get()
                .addOnSuccessListener(attendanceSnapshot -> {

                    WriteBatch batch = db.batch();

                    // Create or update the session participant record.
                    batch.set(
                            db.collection("attendanceSessions")
                                    .document(sessionId)
                                    .collection("participants")
                                    .document(studentUid),
                            participant,
                            SetOptions.merge()
                    );

                    Map<String, Object> attendance = new HashMap<>();
                    attendance.put("subject", subject);
                    attendance.put("status", status);
                    attendance.put("sessionId", sessionId);
                    attendance.put("date", formatDay(
                            sessionCreatedAt > 0
                                    ? sessionCreatedAt : now
                    ));
                    attendance.put("attendanceMethod", "Teacher Manual Correction");
                    attendance.put("manualCorrection", true);
                    attendance.put("correctedBy", teacherUid);
                    attendance.put("correctedAtMillis", now);

                    // Update the existing student attendance record if found.
                    // Otherwise create one for the manually marked student.
                    if (attendanceSnapshot.isEmpty()) {

                        attendance.put("createdAt", now);

                        batch.set(
                                db.collection("students")
                                        .document(studentUid)
                                        .collection("attendance")
                                        .document(),
                                attendance
                        );

                    } else {

                        for (DocumentSnapshot record :
                                attendanceSnapshot.getDocuments()) {

                            batch.set(record.getReference(),
                                    attendance,
                                    SetOptions.merge());
                        }
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(
                                        this,
                                        "Attendance corrected: " + status,
                                        Toast.LENGTH_LONG
                                ).show();

                                loadHistory();
                            })
                            .addOnFailureListener(e ->
                                    showError(
                                            "Correction failed. Check Firestore rules. "
                                                    + e.getMessage()
                                    ));
                })
                .addOnFailureListener(e ->
                        showError(
                                "Cannot access this student's attendance. "
                                        + "Update the teacher attendance permissions in Firestore rules."
                        ));
    }

    // ---------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------

    private long timeOf(DocumentSnapshot document) {
        Long millis = document.getLong("createdAtMillis");
        return millis == null ? 0L : millis;
    }

    private String formatDay(long millis) {
        return new SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
        ).format(new Date(millis));
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void loadColors() {
        bg = getColor(R.color.app_background);
        card = getColor(R.color.app_card);
        text = getColor(R.color.app_text);
        secondary = getColor(R.color.app_secondary_text);
        muted = getColor(R.color.app_muted_text);
        border = getColor(R.color.app_border);
        primary = getColor(R.color.primary);
        success = getColor(R.color.success);
        error = getColor(R.color.error);
    }

    private TextView makeText(
            String value,
            float size,
            int color,
            boolean bold) {

        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);

        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        return view;
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(int value) {
        return (int) (
                value * getResources().getDisplayMetrics().density + 0.5f
        );
    }
}

