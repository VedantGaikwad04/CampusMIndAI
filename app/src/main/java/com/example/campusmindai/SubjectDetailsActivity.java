package com.example.campusmindai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Locale;

public class SubjectDetailsActivity extends AppCompatActivity {

    private TextView tvSubjectName;
    private TextView tvSubjectAttendance;
    private TextView tvSubjectAssignments;
    private TextView tvSubjectFocusTime;
    private TextView tvSubjectStudyMessage;

    private Button btnAskSubjectAI;
    private Button btnOpenSubjectAttendance;
    private Button btnOpenSubjectAssignments;
    private Button btnOpenSubjectFocus;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String subjectName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_subject_details);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();

        subjectName =
                getIntent().getStringExtra("subjectName");

        if (subjectName == null ||
                subjectName.trim().isEmpty()) {

            subjectName = "Subject";
        }

        tvSubjectName.setText(subjectName);

        setupButtons();

        loadAttendance();
        loadAssignments();
        loadFocusTime();
    }

    private void initializeViews() {

        tvSubjectName =
                findViewById(R.id.tvSubjectName);

        tvSubjectAttendance =
                findViewById(R.id.tvSubjectAttendance);

        tvSubjectAssignments =
                findViewById(R.id.tvSubjectAssignments);

        tvSubjectFocusTime =
                findViewById(R.id.tvSubjectFocusTime);

        tvSubjectStudyMessage =
                findViewById(R.id.tvSubjectStudyMessage);

        btnAskSubjectAI =
                findViewById(R.id.btnAskSubjectAI);

        btnOpenSubjectAttendance =
                findViewById(
                        R.id.btnOpenSubjectAttendance
                );

        btnOpenSubjectAssignments =
                findViewById(
                        R.id.btnOpenSubjectAssignments
                );

        btnOpenSubjectFocus =
                findViewById(
                        R.id.btnOpenSubjectFocus
                );
    }

    private void setupButtons() {

        // AI
        btnAskSubjectAI.setOnClickListener(v -> {

            Intent intent = new Intent(
                    SubjectDetailsActivity.this,
                    AIActivity.class
            );

            intent.putExtra(
                    "subjectName",
                    subjectName
            );

            intent.putExtra(
                    "subjectOnly",
                    true
            );

            startActivity(intent);
        });

        // Attendance
        btnOpenSubjectAttendance.setOnClickListener(v -> {

            Intent intent = new Intent(
                    SubjectDetailsActivity.this,
                    AttendanceActivity.class
            );

            intent.putExtra(
                    "subjectName",
                    subjectName
            );

            intent.putExtra(
                    "subjectOnly",
                    true
            );

            startActivity(intent);
        });

        // Assignments
        btnOpenSubjectAssignments.setOnClickListener(v -> {

            Intent intent = new Intent(
                    SubjectDetailsActivity.this,
                    AssignmentsActivity.class
            );

            intent.putExtra(
                    "subjectName",
                    subjectName
            );

            intent.putExtra(
                    "subjectOnly",
                    true
            );

            startActivity(intent);
        });

        // Focus
        btnOpenSubjectFocus.setOnClickListener(v -> {

            Intent intent = new Intent(
                    SubjectDetailsActivity.this,
                    FocusActivity.class
            );

            intent.putExtra(
                    "subjectName",
                    subjectName
            );

            intent.putExtra(
                    "subjectOnly",
                    true
            );

            startActivity(intent);
        });
    }

    private void loadAttendance() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId =
                auth.getCurrentUser().getUid();

        db.collection("students")
                .document(userId)
                .collection("attendance")
                .get()
                .addOnSuccessListener(snapshot -> {

                    int total = 0;
                    int present = 0;

                    for (QueryDocumentSnapshot document :
                            snapshot) {

                        String subject =
                                document.getString("subject");

                        if (subject == null ||
                                !subject.equalsIgnoreCase(
                                        subjectName)) {

                            continue;
                        }

                        total++;

                        String status =
                                document.getString("status");

                        if ("Present".equalsIgnoreCase(
                                status)) {

                            present++;
                        }
                    }

                    if (total == 0) {

                        tvSubjectAttendance.setText(
                                "0%"
                        );

                        tvSubjectStudyMessage.setText(
                                "No attendance records yet."
                        );

                        return;
                    }

                    double percentage =
                            (present * 100.0) / total;

                    tvSubjectAttendance.setText(
                            String.format(
                                    Locale.getDefault(),
                                    "%.0f%%",
                                    percentage
                            )
                    );

                    if (percentage >= 75) {

                        tvSubjectStudyMessage.setText(
                                "Your attendance for " +
                                        subjectName +
                                        " is on track."
                        );

                    } else {

                        tvSubjectStudyMessage.setText(
                                "Your attendance is below 75%. " +
                                        "Try to attend more " +
                                        subjectName +
                                        " lectures."
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to load attendance",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void loadAssignments() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId =
                auth.getCurrentUser().getUid();

        db.collection("students")
                .document(userId)
                .collection("assignments")
                .get()
                .addOnSuccessListener(snapshot -> {

                    int pending = 0;

                    for (QueryDocumentSnapshot document :
                            snapshot) {

                        String subject =
                                document.getString("subject");

                        if (subject == null ||
                                !subject.equalsIgnoreCase(
                                        subjectName)) {

                            continue;
                        }

                        String status =
                                document.getString("status");

                        if (status == null ||
                                !status.equalsIgnoreCase(
                                        "Completed")) {

                            pending++;
                        }
                    }

                    tvSubjectAssignments.setText(
                            pending + " pending"
                    );
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to load assignments",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void loadFocusTime() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId =
                auth.getCurrentUser().getUid();

        db.collection("students")
                .document(userId)
                .collection("studySessions")
                .get()
                .addOnSuccessListener(snapshot -> {

                    double totalSeconds = 0;

                    for (QueryDocumentSnapshot document :
                            snapshot) {

                        String subject =
                                document.getString("subject");

                        if (subject == null ||
                                !subject.equalsIgnoreCase(
                                        subjectName)) {

                            continue;
                        }

                        /*
                         * New exact-time sessions
                         */
                        Number durationSeconds =
                                document.getDouble(
                                        "durationSeconds"
                                );

                        if (durationSeconds != null) {

                            totalSeconds +=
                                    durationSeconds.doubleValue();

                            continue;
                        }

                        /*
                         * Fallback for durationMillis
                         */
                        Number durationMillis =
                                document.getDouble(
                                        "durationMillis"
                                );

                        if (durationMillis != null) {

                            totalSeconds +=
                                    durationMillis.doubleValue()
                                            / 1000.0;

                            continue;
                        }

                        /*
                         * Fallback for old sessions
                         */
                        Number durationMinutes =
                                document.getDouble(
                                        "durationMinutes"
                                );

                        if (durationMinutes != null) {

                            totalSeconds +=
                                    durationMinutes.doubleValue()
                                            * 60.0;
                        }
                    }

                    tvSubjectFocusTime.setText(
                            formatFocusTime(
                                    totalSeconds
                            )
                    );
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to load focus time",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private String formatFocusTime(
            double seconds
    ) {

        long totalSeconds =
                Math.round(seconds);

        if (totalSeconds <= 0) {
            return "0 min";
        }

        long hours =
                totalSeconds / 3600;

        long minutes =
                (totalSeconds % 3600) / 60;

        long remainingSeconds =
                totalSeconds % 60;

        if (hours > 0) {

            if (minutes > 0) {

                return hours + " hr " +
                        minutes + " min";

            } else {

                return hours + " hr";
            }
        }

        if (minutes > 0) {

            if (remainingSeconds > 0) {

                return minutes + " min " +
                        remainingSeconds + " sec";

            } else {

                return minutes + " min";
            }
        }

        return remainingSeconds + " sec";
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (auth != null &&
                auth.getCurrentUser() != null) {

            loadAttendance();
            loadAssignments();
            loadFocusTime();
        }
    }
}