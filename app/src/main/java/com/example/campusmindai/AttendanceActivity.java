package com.example.campusmindai;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AttendanceActivity extends AppCompatActivity {

    // ---------------------------------------------------------
    // DESIGN SYSTEM
    // ---------------------------------------------------------

    private static final int COLOR_BACKGROUND =
            Color.rgb(248, 250, 252);

    private static final int COLOR_CARD =
            Color.WHITE;

    private static final int COLOR_PRIMARY =
            Color.rgb(37, 99, 235);

    private static final int COLOR_PRIMARY_DARK =
            Color.rgb(29, 78, 216);

    private static final int COLOR_TEXT =
            Color.rgb(15, 23, 42);

    private static final int COLOR_SECONDARY =
            Color.rgb(71, 85, 105);

    private static final int COLOR_MUTED =
            Color.rgb(100, 116, 139);

    private static final int COLOR_BORDER =
            Color.rgb(226, 232, 240);

    private static final int COLOR_INPUT =
            Color.rgb(248, 250, 252);

    private static final int COLOR_SUCCESS =
            Color.rgb(22, 163, 74);

    private static final int COLOR_WARNING =
            Color.rgb(217, 119, 6);

    private static final int COLOR_ERROR =
            Color.rgb(220, 38, 38);

    private static final int COLOR_SOFT_BLUE =
            Color.rgb(239, 246, 255);

    private static final int COLOR_SOFT_GREEN =
            Color.rgb(240, 253, 244);

    private static final int COLOR_SOFT_RED =
            Color.rgb(254, 242, 242);

    private static final int COLOR_SOFT_ORANGE =
            Color.rgb(255, 247, 237);

    // ---------------------------------------------------------
    // VIEWS
    // ---------------------------------------------------------

    private LinearLayout subjectAttendanceContainer;

    private TextView tvOverallPercentage;
    private TextView tvOverallCount;
    private TextView tvSubjectTotal;
    private TextView tvAttendanceEmpty;
    private TextView tvTargetDescription;

    private MaterialButton btnEditTarget;

    private MaterialCardView teacherAttendanceCard;
    private MaterialButton btnTeacherAttendance;

    private MaterialButton btnMarkAttendance;

    private ProgressBar progressOverall;

    // ---------------------------------------------------------
    // FIREBASE
    // ---------------------------------------------------------

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    // ---------------------------------------------------------
    // DATA
    // ---------------------------------------------------------

    private final ArrayList<String> subjects =
            new ArrayList<>();

    private int attendanceTarget = 75;

    private boolean isLoading = false;

    // ---------------------------------------------------------
    // ON CREATE
    // ---------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.attendenceactivity);

        subjectAttendanceContainer =
                findViewById(
                        R.id.subjectAttendanceContainer
                );

        tvOverallPercentage =
                findViewById(
                        R.id.tvOverallPercentage
                );

        tvOverallCount =
                findViewById(
                        R.id.tvOverallCount
                );

        tvSubjectTotal =
                findViewById(
                        R.id.tvSubjectTotal
                );

        tvAttendanceEmpty =
                findViewById(
                        R.id.tvAttendanceEmpty
                );

        tvTargetDescription =
                findViewById(
                        R.id.tvTargetDescription
                );

        btnEditTarget =
                findViewById(
                        R.id.btnEditTarget
                );

        progressOverall =
                findViewById(
                        R.id.progressOverall
                );

        teacherAttendanceCard =
                findViewById(
                        R.id.teacherAttendanceCard
                );

        btnTeacherAttendance =
                findViewById(
                        R.id.btnTeacherAttendance
                );

        btnMarkAttendance =
                findViewById(
                        R.id.btnMarkAttendance
                );

        btnMarkAttendance.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    AttendanceActivity.this,
                                    StudentAttendanceActivity.class
                            );

                    startActivity(intent);
                }
        );

        firestore =
                FirebaseFirestore.getInstance();

        firebaseAuth =
                FirebaseAuth.getInstance();

        applyMainScreenDesign();
        applyThemeMode();

        btnEditTarget.setOnClickListener(
                v -> showTargetDialog()
        );

        btnTeacherAttendance.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    AttendanceActivity.this,
                                    TeacherAttendanceActivity.class
                            );

                    startActivity(intent);
                }
        );

        // Check if current user is a teacher.
        checkTeacherRole();

        // Keep 75% as the default target so the attendance
        // screen can load subjects immediately.
        attendanceTarget = 75;
        updateTargetUI();

        // Load subjects immediately without waiting for
        // the saved attendance target.
        loadSubjects();

        // Load the user's saved target independently.
        loadAttendanceTarget();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyMainScreenDesign();
        applyThemeMode();
    }


    private void checkTeacherRole() {

        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        String userId =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        firestore
                .collection("students")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {

                    String role =
                            document.getString("role");

                    if ("teacher".equalsIgnoreCase(role)) {

                        if (teacherAttendanceCard != null) {
                            teacherAttendanceCard.setVisibility(
                                    View.VISIBLE
                            );
                        }

                    } else {

                        if (teacherAttendanceCard != null) {
                            teacherAttendanceCard.setVisibility(
                                    View.GONE
                            );
                        }
                    }

                    applyThemeMode();
                })
                .addOnFailureListener(e -> {

                    if (teacherAttendanceCard != null) {
                        teacherAttendanceCard.setVisibility(
                                View.GONE
                        );
                    }
                });
    }

    // ---------------------------------------------------------
    // MAIN SCREEN DESIGN
    // ---------------------------------------------------------

    private void applyMainScreenDesign() {

        View root =
                findViewById(android.R.id.content);

        root.setBackgroundColor(
                COLOR_BACKGROUND
        );

        if (tvOverallPercentage != null) {

            tvOverallPercentage.setTextColor(
                    COLOR_PRIMARY
            );

            tvOverallPercentage.setTextSize(
                    34
            );

            tvOverallPercentage.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        if (tvOverallCount != null) {

            tvOverallCount.setTextColor(
                    COLOR_SECONDARY
            );

            tvOverallCount.setTextSize(
                    14
            );
        }

        if (tvSubjectTotal != null) {

            tvSubjectTotal.setTextColor(
                    COLOR_SECONDARY
            );

            tvSubjectTotal.setTextSize(
                    14
            );
        }

        if (tvAttendanceEmpty != null) {

            tvAttendanceEmpty.setTextColor(
                    COLOR_MUTED
            );

            tvAttendanceEmpty.setTextSize(
                    14
            );

            tvAttendanceEmpty.setGravity(
                    Gravity.CENTER
            );
        }

        if (tvTargetDescription != null) {

            tvTargetDescription.setTextColor(
                    COLOR_SECONDARY
            );

            tvTargetDescription.setTextSize(
                    14
            );
        }

        if (btnEditTarget != null) {

            stylePrimaryButton(
                    btnEditTarget
            );

            btnEditTarget.setTextSize(
                    13
            );
        }

        if (progressOverall != null) {

            progressOverall.setMax(100);

            progressOverall.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(
                            COLOR_PRIMARY
                    )
            );

            progressOverall.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            COLOR_BORDER
                    )
            );
        }
    }

    // ---------------------------------------------------------
    // TARGET
    // ---------------------------------------------------------

    private void loadAttendanceTarget() {

        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        String userId =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        firestore
                .collection("students")
                .document(userId)
                .collection("settings")
                .document("attendance")
                .get()
                .addOnSuccessListener(document -> {

                    Long savedTarget =
                            document.getLong("target");

                    if (savedTarget != null) {

                        attendanceTarget =
                                savedTarget.intValue();

                    } else {

                        attendanceTarget = 75;
                    }

                    updateTargetUI();

                    // Refresh the attendance cards so their
                    // target/status messages use the saved target.
                    if (!subjects.isEmpty()) {
                        loadAllAttendance();
                    }

                })
                .addOnFailureListener(e -> {

                    attendanceTarget = 75;

                    updateTargetUI();

                    // Keep the already loaded attendance using
                    // the default 75% target.
                    if (!subjects.isEmpty()) {
                        loadAllAttendance();
                    }
                });
    }

    private void updateTargetUI() {

        if (btnEditTarget != null) {

            btnEditTarget.setText(
                    attendanceTarget + "%  ✎"
            );
        }

        if (tvTargetDescription != null) {

            tvTargetDescription.setText(
                    "Target attendance: "
                            + attendanceTarget
                            + "%"
            );
        }
    }

    // ---------------------------------------------------------
    // TARGET DIALOG
    // ---------------------------------------------------------

    private void showTargetDialog() {

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setPadding(
                dpToPx(22),
                dpToPx(6),
                dpToPx(22),
                dpToPx(6)
        );

        TextView description =
                new TextView(this);

        description.setText(
                "Set the minimum attendance percentage you want to maintain."
        );

        description.setTextColor(
                COLOR_SECONDARY
        );

        description.setTextSize(
                14
        );

        description.setLineSpacing(
                dpToPx(2),
                1.0f
        );

        container.addView(
                description
        );

        EditText input =
                new EditText(this);

        input.setHint(
                "Attendance percentage"
        );

        input.setText(
                String.valueOf(attendanceTarget)
        );

        input.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        input.setSingleLine(true);

        input.setTextColor(
                COLOR_TEXT
        );

        input.setHintTextColor(
                COLOR_MUTED
        );

        input.setTextSize(
                16
        );

        input.setPadding(
                dpToPx(16),
                0,
                dpToPx(16),
                0
        );

        input.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        COLOR_PRIMARY
                )
        );

        LinearLayout.LayoutParams
                inputParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(54)
                );

        inputParams.topMargin =
                dpToPx(16);

        container.addView(
                input,
                inputParams
        );

        TextView helper =
                new TextView(this);

        helper.setText(
                "Choose a value between 1% and 100%."
        );

        helper.setTextColor(
                COLOR_MUTED
        );

        helper.setTextSize(
                12
        );

        LinearLayout.LayoutParams
                helperParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        helperParams.topMargin =
                dpToPx(8);

        container.addView(
                helper,
                helperParams
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Attendance Target"
                        )
                        .setView(container)
                        .setPositiveButton(
                                "SAVE",
                                null
                        )
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                dialogInterface -> {

                    Button positive =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    Button negative =
                            dialog.getButton(
                                    AlertDialog.BUTTON_NEGATIVE
                            );

                    styleAttendanceDialog(
                            dialog
                    );

                    positive.setTextColor(
                            isDarkModeEnabled()
                                    ? COLOR_DARK_PRIMARY
                                    : COLOR_PRIMARY
                    );

                    positive.setTypeface(
                            Typeface.DEFAULT,
                            Typeface.BOLD
                    );

                    negative.setTextColor(
                            isDarkModeEnabled()
                                    ? COLOR_DARK_SECONDARY
                                    : COLOR_SECONDARY
                    );

                    positive.setOnClickListener(
                            v -> {

                                String value =
                                        input.getText()
                                                .toString()
                                                .trim();

                                if (value.isEmpty()) {

                                    input.setError(
                                            "Enter a percentage"
                                    );

                                    return;
                                }

                                int target;

                                try {

                                    target =
                                            Integer.parseInt(
                                                    value
                                            );

                                } catch (
                                        NumberFormatException e
                                ) {

                                    input.setError(
                                            "Enter a valid number"
                                    );

                                    return;
                                }

                                if (target < 1 ||
                                        target > 100) {

                                    input.setError(
                                            "Enter a value from 1 to 100"
                                    );

                                    return;
                                }

                                saveAttendanceTarget(
                                        target
                                );

                                dialog.dismiss();
                            }
                    );
                }
        );

        dialog.show();

        styleAttendanceDialog(
                dialog
        );
    }

    private void saveAttendanceTarget(
            int target
    ) {

        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        String userId =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "target",
                target
        );

        firestore
                .collection("students")
                .document(userId)
                .collection("settings")
                .document("attendance")
                .set(data)
                .addOnSuccessListener(unused -> {

                    attendanceTarget =
                            target;

                    updateTargetUI();

                    Toast.makeText(
                            this,
                            "Attendance target updated",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadSubjects();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to save target",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // ---------------------------------------------------------
    // SUBJECTS
    // ---------------------------------------------------------

    private void loadSubjects() {

        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        if (isLoading) {
            return;
        }

        isLoading = true;

        String userId =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        firestore
                .collection("students")
                .document(userId)
                .collection("subjects")
                .get()
                .addOnSuccessListener(snapshot -> {

                    subjects.clear();

                    Set<String> uniqueSubjects =
                            new HashSet<>();

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        String name =
                                document.getString(
                                        "name"
                                );

                        if (name == null) {
                            continue;
                        }

                        name =
                                name.trim();

                        if (name.isEmpty()) {
                            continue;
                        }

                        String normalized =
                                name.toLowerCase(
                                        Locale.getDefault()
                                );

                        if (!uniqueSubjects.contains(
                                normalized
                        )) {

                            uniqueSubjects.add(
                                    normalized
                            );

                            subjects.add(
                                    name
                            );
                        }
                    }

                    Collections.sort(
                            subjects
                    );

                    isLoading = false;

                    displaySubjects();

                })
                .addOnFailureListener(e -> {

                    isLoading = false;

                    Toast.makeText(
                            this,
                            "Unable to load subjects",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void displaySubjects() {

        subjectAttendanceContainer.removeAllViews();

        tvSubjectTotal.setText(
                subjects.size()
                        + " subjects"
        );

        if (subjects.isEmpty()) {

            tvAttendanceEmpty.setVisibility(
                    View.VISIBLE
            );

            tvOverallPercentage.setText(
                    "0%"
            );

            tvOverallCount.setText(
                    "0 / 0 lectures"
            );

            progressOverall.setProgress(
                    0
            );

            return;
        }

        tvAttendanceEmpty.setVisibility(
                View.GONE
        );

        loadAllAttendance();
    }

    // ---------------------------------------------------------
    // ATTENDANCE
    // ---------------------------------------------------------

    private void loadAllAttendance() {

        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        String userId =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        firestore
                .collection("students")
                .document(userId)
                .collection("attendance")
                .get()
                .addOnSuccessListener(snapshot -> {

                    subjectAttendanceContainer
                            .removeAllViews();

                    Map<String,
                            ArrayList<DocumentSnapshot>>
                            subjectRecords =
                            new HashMap<>();

                    for (String subject :
                            subjects) {

                        subjectRecords.put(
                                subject,
                                new ArrayList<>()
                        );
                    }

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        String attendanceSubject =
                                document.getString(
                                        "subject"
                                );

                        if (attendanceSubject == null) {
                            continue;
                        }

                        String matchedSubject =
                                findMatchingSubject(
                                        attendanceSubject
                                );

                        if (matchedSubject != null) {

                            subjectRecords
                                    .get(matchedSubject)
                                    .add(document);
                        }
                    }

                    int overallTotal = 0;
                    int overallPresent = 0;

                    for (String subject :
                            subjects) {

                        ArrayList<DocumentSnapshot>
                                records =
                                subjectRecords.get(
                                        subject
                                );

                        int total =
                                records.size();

                        int present =
                                countPresent(
                                        records
                                );

                        overallTotal +=
                                total;

                        overallPresent +=
                                present;

                        addSubjectCard(
                                subject,
                                records,
                                present,
                                total
                        );
                    }

                    updateOverallAttendance(
                            overallPresent,
                            overallTotal
                    );

                    applyThemeMode();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to load attendance",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private String findMatchingSubject(
            String attendanceSubject
    ) {

        for (String subject :
                subjects) {

            if (subject.equalsIgnoreCase(
                    attendanceSubject.trim()
            )) {

                return subject;
            }
        }

        return null;
    }

    // ---------------------------------------------------------
    // SUBJECT CARD
    // ---------------------------------------------------------

    private void addSubjectCard(
            String subject,
            ArrayList<DocumentSnapshot> records,
            int present,
            int total
    ) {

        MaterialCardView card =
                new MaterialCardView(this);

        card.setRadius(
                dpToPx(20)
        );

        card.setCardElevation(
                dpToPx(2)
        );

        card.setCardBackgroundColor(
                COLOR_CARD
        );

        card.setStrokeWidth(
                dpToPx(1)
        );

        card.setStrokeColor(
                COLOR_BORDER
        );

        LinearLayout.LayoutParams
                cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin =
                dpToPx(14);

        card.setLayoutParams(
                cardParams
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

        // -----------------------------------------------------
        // TITLE ROW
        // -----------------------------------------------------

        LinearLayout titleRow =
                new LinearLayout(this);

        titleRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        titleRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView subjectName =
                new TextView(this);

        subjectName.setText(
                subject
        );

        subjectName.setTextColor(
                COLOR_TEXT
        );

        subjectName.setTextSize(
                18
        );

        subjectName.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        LinearLayout.LayoutParams
                titleParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        titleRow.addView(
                subjectName,
                titleParams
        );

        int percentage =
                calculatePercentage(
                        present,
                        total
                );

        TextView percentageView =
                new TextView(this);

        percentageView.setText(
                percentage + "%"
        );

        percentageView.setTextColor(
                getAttendanceColor(
                        percentage
                )
        );

        percentageView.setTextSize(
                22
        );

        percentageView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        titleRow.addView(
                percentageView
        );

        content.addView(
                titleRow
        );

        // -----------------------------------------------------
        // COUNT
        // -----------------------------------------------------

        TextView count =
                new TextView(this);

        count.setText(
                present
                        + " present  •  "
                        + total
                        + " lectures"
        );

        count.setTextColor(
                COLOR_SECONDARY
        );

        count.setTextSize(
                13
        );

        addTopMargin(
                count,
                5
        );

        content.addView(
                count
        );

        // -----------------------------------------------------
        // PROGRESS
        // -----------------------------------------------------

        ProgressBar progressBar =
                new ProgressBar(
                        this,
                        null,
                        android.R.attr.progressBarStyleHorizontal
                );

        progressBar.setMax(
                100
        );

        progressBar.setProgress(
                percentage
        );

        progressBar.setProgressTintList(
                android.content.res.ColorStateList
                        .valueOf(
                                getAttendanceColor(
                                        percentage
                                )
                        )
        );

        progressBar.setBackgroundTintList(
                android.content.res.ColorStateList
                        .valueOf(
                                COLOR_BORDER
                        )
        );

        LinearLayout.LayoutParams
                progressParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(9)
                );

        progressParams.topMargin =
                dpToPx(14);

        content.addView(
                progressBar,
                progressParams
        );

        // -----------------------------------------------------
        // STATUS MESSAGE
        // -----------------------------------------------------

        if (total == 0) {

            TextView noData =
                    createStatusText(
                            "No lectures recorded yet",
                            COLOR_MUTED
                    );

            addTopMargin(
                    noData,
                    10
            );

            content.addView(
                    noData
            );

        } else if (
                percentage >= attendanceTarget
        ) {

            TextView achieved =
                    createStatusText(
                            "✓  Target achieved",
                            COLOR_SUCCESS
                    );

            addTopMargin(
                    achieved,
                    10
            );

            content.addView(
                    achieved
            );

        } else {

            int required =
                    calculateRequiredLectures(
                            present,
                            total,
                            attendanceTarget
                    );

            TextView warning =
                    createStatusText(
                            "⚠  Below target • Attend the next "
                                    + required
                                    + " lectures",
                            COLOR_WARNING
                    );

            addTopMargin(
                    warning,
                    10
            );

            content.addView(
                    warning
            );
        }

        // -----------------------------------------------------
        // HISTORY BUTTON
        // -----------------------------------------------------

        MaterialButton historyButton =
                new MaterialButton(this);

        historyButton.setText(
                "VIEW LECTURE HISTORY"
        );

        historyButton.setTextSize(
                12
        );

        historyButton.setTextColor(
                COLOR_PRIMARY
        );

        historyButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        historyButton.setBackgroundTintList(
                android.content.res.ColorStateList
                        .valueOf(
                                COLOR_SOFT_BLUE
                        )
        );

        historyButton.setStrokeColor(
                android.content.res.ColorStateList
                        .valueOf(
                                Color.rgb(
                                        191,
                                        219,
                                        254
                                )
                        )
        );

        historyButton.setStrokeWidth(
                dpToPx(1)
        );

        historyButton.setCornerRadius(
                dpToPx(13)
        );

        historyButton.setMinHeight(
                dpToPx(50)
        );

        historyButton.setInsetTop(0);
        historyButton.setInsetBottom(0);

        LinearLayout.LayoutParams
                historyParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(50)
                );

        historyParams.topMargin =
                dpToPx(9);

        content.addView(
                historyButton,
                historyParams
        );

        // -----------------------------------------------------
        // LISTENERS
        // -----------------------------------------------------
        historyButton.setOnClickListener(
                v -> showLectureHistory(
                        subject
                )
        );


        card.addView(
                content
        );

        subjectAttendanceContainer.addView(
                card
        );
    }

    private TextView createStatusText(
            String text,
            int color
    ) {

        TextView view =
                new TextView(this);

        view.setText(
                text
        );

        view.setTextColor(
                color
        );

        view.setTextSize(
                12
        );

        view.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        return view;
    }

    // ---------------------------------------------------------
    // HISTORY
    // ---------------------------------------------------------

    private void showLectureHistory(
            String subject
    ) {

        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        String userId =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        firestore
                .collection("students")
                .document(userId)
                .collection("attendance")
                .whereEqualTo(
                        "subject",
                        subject
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    ArrayList<DocumentSnapshot>
                            records =
                            new ArrayList<>(
                                    snapshot.getDocuments()
                            );

                    Collections.sort(
                            records,
                            new Comparator<DocumentSnapshot>() {

                                @Override
                                public int compare(
                                        DocumentSnapshot first,
                                        DocumentSnapshot second
                                ) {

                                    Long firstTime =
                                            first.getLong(
                                                    "createdAt"
                                            );

                                    Long secondTime =
                                            second.getLong(
                                                    "createdAt"
                                            );

                                    if (firstTime == null) {
                                        firstTime = 0L;
                                    }

                                    if (secondTime == null) {
                                        secondTime = 0L;
                                    }

                                    return Long.compare(
                                            secondTime,
                                            firstTime
                                    );
                                }
                            }
                    );

                    if (records.isEmpty()) {

                        showSimpleInfoDialog(
                                subject
                                        + " • Attendance History",
                                "No lecture records available yet."
                        );

                        return;
                    }

                    int present =
                            countPresent(
                                    records
                            );

                    int total =
                            records.size();

                    int percentage =
                            calculatePercentage(
                                    present,
                                    total
                            );

                    LinearLayout historyLayout =
                            new LinearLayout(this);

                    historyLayout.setOrientation(
                            LinearLayout.VERTICAL
                    );

                    historyLayout.setPadding(
                            dpToPx(4),
                            dpToPx(4),
                            dpToPx(4),
                            dpToPx(4)
                    );

                    TextView summary =
                            new TextView(this);

                    summary.setText(
                            percentage
                                    + "%  •  "
                                    + present
                                    + " present / "
                                    + total
                                    + " lectures"
                    );

                    summary.setTextColor(
                            getAttendanceColor(
                                    percentage
                            )
                    );

                    summary.setTextSize(
                            18
                    );

                    summary.setTypeface(
                            Typeface.DEFAULT,
                            Typeface.BOLD
                    );

                    addBottomMargin(
                            summary,
                            8
                    );

                    historyLayout.addView(
                            summary
                    );


                    for (int i = 0;
                         i < records.size();
                         i++) {

                        addHistoryRow(
                                historyLayout,
                                records.get(i),
                                records.size() - i
                        );
                    }

                    ScrollView scrollView =
                            new ScrollView(this);

                    scrollView.setFillViewport(
                            true
                    );

                    int historyHeight =
                            (int) (
                                    getResources()
                                            .getDisplayMetrics()
                                            .heightPixels
                                            * 0.62
                            );

                    scrollView.setLayoutParams(
                            new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    historyHeight
                            )
                    );

                    scrollView.addView(
                            historyLayout
                    );

                    AlertDialog dialog =
                            new AlertDialog.Builder(this)
                                    .setTitle(
                                            subject
                                                    + " • Attendance History"
                                    )
                                    .setView(
                                            scrollView
                                    )
                                    .setNegativeButton(
                                            "CLOSE",
                                            null
                                    )
                                    .create();

                    dialog.setOnShowListener(
                            d -> {

                                Button close =
                                        dialog.getButton(
                                                AlertDialog.BUTTON_NEGATIVE
                                        );

                                close.setTextColor(
                                        COLOR_PRIMARY
                                );

                                close.setTypeface(
                                        Typeface.DEFAULT,
                                        Typeface.BOLD
                                );
                            }
                    );

                    dialog.show();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to load lecture history",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void addHistoryRow(
            LinearLayout parent,
            DocumentSnapshot document,
            int fallbackNumber
    ) {

        MaterialCardView card =
                new MaterialCardView(this);

        card.setRadius(
                dpToPx(15)
        );

        card.setCardElevation(
                dpToPx(1)
        );

        card.setCardBackgroundColor(
                COLOR_CARD
        );

        card.setStrokeWidth(
                dpToPx(1)
        );

        card.setStrokeColor(
                COLOR_BORDER
        );

        LinearLayout.LayoutParams
                cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin =
                dpToPx(8);

        card.setLayoutParams(
                cardParams
        );

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                dpToPx(12),
                dpToPx(12),
                dpToPx(12),
                dpToPx(12)
        );

        Long storedNumber =
                document.getLong(
                        "lectureNumber"
                );

        long lectureNumber;

        if (storedNumber != null) {

            lectureNumber =
                    storedNumber;

        } else {

            lectureNumber =
                    fallbackNumber;
        }

        TextView number =
                new TextView(this);

        number.setText(
                "L"
                        + lectureNumber
        );

        number.setTextColor(
                COLOR_PRIMARY
        );

        number.setTextSize(
                13
        );

        number.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        number.setGravity(
                Gravity.CENTER
        );

        number.setBackgroundColor(
                COLOR_SOFT_BLUE
        );

        row.addView(
                number,
                new LinearLayout.LayoutParams(
                        dpToPx(44),
                        dpToPx(42)
                )
        );

        LinearLayout info =
                new LinearLayout(this);

        info.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams
                infoParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        infoParams.leftMargin =
                dpToPx(12);

        String date =
                document.getString(
                        "date"
                );

        if (date == null ||
                date.isEmpty()) {

            date = "No date";
        }

        TextView dateText =
                new TextView(this);

        dateText.setText(
                date
        );

        dateText.setTextColor(
                COLOR_TEXT
        );

        dateText.setTextSize(
                14
        );

        dateText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        TextView lectureText =
                new TextView(this);

        lectureText.setText(
                "Lecture "
                        + lectureNumber
        );

        lectureText.setTextColor(
                COLOR_MUTED
        );

        lectureText.setTextSize(
                11
        );

        info.addView(
                dateText
        );

        addTopMargin(
                lectureText,
                2
        );

        info.addView(
                lectureText
        );

        row.addView(
                info,
                infoParams
        );

        String currentStatus =
                document.getString(
                        "status"
                );

        if (currentStatus == null) {
            currentStatus = "Unknown";
        }

        TextView status =
                new TextView(this);

        status.setText(
                currentStatus
        );

        status.setTextSize(
                12
        );

        status.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        status.setGravity(
                Gravity.CENTER
        );

        if ("Present".equalsIgnoreCase(
                currentStatus
        )) {

            status.setTextColor(
                    COLOR_SUCCESS
            );

        } else {

            status.setTextColor(
                    COLOR_ERROR
            );
        }

        row.addView(
                status,
                new LinearLayout.LayoutParams(
                        dpToPx(72),
                        dpToPx(40)
                )
        );

        card.addView(
                row
        );


        parent.addView(
                card
        );
    }

    // ---------------------------------------------------------
    // CALCULATIONS
    // ---------------------------------------------------------

    private void updateOverallAttendance(
            int present,
            int total
    ) {

        int percentage =
                calculatePercentage(
                        present,
                        total
                );

        tvOverallPercentage.setText(
                percentage + "%"
        );

        tvOverallPercentage.setTextColor(
                getAttendanceColor(
                        percentage
                )
        );

        tvOverallCount.setText(
                present
                        + " / "
                        + total
                        + " lectures"
        );

        progressOverall.setProgress(
                percentage
        );

        progressOverall.setProgressTintList(
                android.content.res.ColorStateList
                        .valueOf(
                                getAttendanceColor(
                                        percentage
                                )
                        )
        );
    }

    private int calculateRequiredLectures(
            int present,
            int total,
            int target
    ) {

        if (total == 0 ||
                calculatePercentage(
                        present,
                        total
                ) >= target) {

            return 0;
        }

        int required = 0;

        while (
                (
                        (present + required)
                                * 100
                )
                        /
                        (total + required)
                        < target
        ) {

            required++;

            if (required > 10000) {
                break;
            }
        }

        return required;
    }

    private int countPresent(
            ArrayList<DocumentSnapshot> records
    ) {

        int present = 0;

        for (DocumentSnapshot document :
                records) {

            String status =
                    document.getString(
                            "status"
                    );

            if ("Present".equalsIgnoreCase(
                    status
            )) {

                present++;
            }
        }

        return present;
    }

    private int calculatePercentage(
            int present,
            int total
    ) {

        if (total == 0) {
            return 0;
        }

        return (
                present * 100
        ) / total;
    }

    private int getAttendanceColor(
            int percentage
    ) {

        if (percentage <
                attendanceTarget) {

            return COLOR_ERROR;

        } else if (
                percentage <
                        attendanceTarget + 10
        ) {

            return COLOR_WARNING;

        } else {

            return COLOR_SUCCESS;
        }
    }

    // ---------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------

    private void stylePrimaryButton(
            MaterialButton button
    ) {

        button.setTextColor(
                Color.WHITE
        );

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setBackgroundTintList(
                android.content.res.ColorStateList
                        .valueOf(
                                COLOR_PRIMARY
                        )
        );

        button.setCornerRadius(
                dpToPx(13)
        );

        button.setMinHeight(
                dpToPx(50)
        );

        button.setInsetTop(0);
        button.setInsetBottom(0);

        button.setStrokeWidth(0);
    }

    private void showSimpleInfoDialog(
            String title,
            String message
    ) {

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(
                                "CLOSE",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    Button close =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    close.setTextColor(
                            COLOR_PRIMARY
                    );

                    close.setTypeface(
                            Typeface.DEFAULT,
                            Typeface.BOLD
                    );
                }
        );

        dialog.show();
    }

    private String getTodayDate() {

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "dd-MM-yyyy",
                        Locale.getDefault()
                );

        return dateFormat.format(
                new java.util.Date()
        );
    }

    private void addTopMargin(
            View view,
            int dp
    ) {

        ViewGroup.LayoutParams existing =
                view.getLayoutParams();

        LinearLayout.LayoutParams params;

        if (existing instanceof
                LinearLayout.LayoutParams) {

            params =
                    (LinearLayout.LayoutParams)
                            existing;

        } else {

            params =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
        }

        params.topMargin =
                dpToPx(dp);

        view.setLayoutParams(
                params
        );
    }

    private void addBottomMargin(
            View view,
            int dp
    ) {

        ViewGroup.LayoutParams existing =
                view.getLayoutParams();

        LinearLayout.LayoutParams params;

        if (existing instanceof
                LinearLayout.LayoutParams) {

            params =
                    (LinearLayout.LayoutParams)
                            existing;

        } else {

            params =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
        }

        params.bottomMargin =
                dpToPx(dp);

        view.setLayoutParams(
                params
        );
    }

    // ---------------------------------------------------------
    // DARK MODE
    // ---------------------------------------------------------

    private static final int COLOR_DARK_BACKGROUND =
            Color.rgb(11, 17, 32);

    private static final int COLOR_DARK_CARD =
            Color.rgb(17, 24, 39);

    private static final int COLOR_DARK_TEXT =
            Color.rgb(248, 250, 252);

    private static final int COLOR_DARK_SECONDARY =
            Color.rgb(203, 213, 225);

    private static final int COLOR_DARK_MUTED =
            Color.rgb(148, 163, 184);

    private static final int COLOR_DARK_BORDER =
            Color.rgb(51, 65, 85);

    private static final int COLOR_DARK_INPUT =
            Color.rgb(30, 41, 59);

    private static final int COLOR_DARK_PRIMARY =
            Color.rgb(96, 165, 250);

    private static final int COLOR_DARK_SOFT_BLUE =
            Color.rgb(23, 37, 84);

    private static final int COLOR_DARK_SOFT_GREEN =
            Color.rgb(20, 83, 45);

    private static final int COLOR_DARK_SOFT_RED =
            Color.rgb(127, 29, 29);

    private static final int COLOR_DARK_SOFT_ORANGE =
            Color.rgb(120, 53, 15);

    private boolean isDarkModeEnabled() {

        return getSharedPreferences(
                "CampusMindSettings",
                MODE_PRIVATE
        ).getBoolean(
                "dark_mode",
                false
        );
    }

    private void applyThemeMode() {

        View root =
                findViewById(android.R.id.content);

        if (root == null) {
            return;
        }

        boolean darkMode =
                isDarkModeEnabled();

        int background =
                darkMode
                        ? COLOR_DARK_BACKGROUND
                        : COLOR_BACKGROUND;

        root.setBackgroundColor(
                background
        );

        getWindow().setStatusBarColor(
                background
        );

        getWindow().setNavigationBarColor(
                background
        );

        getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                        darkMode
                                ? 0
                                : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );

        if (darkMode) {

            applyDarkThemeTree(
                    root
            );
        }
    }

    private void applyDarkThemeTree(
            View view
    ) {

        if (view == null) {
            return;
        }

        // -----------------------------------------------------
        // CARDS
        // -----------------------------------------------------

        if (view instanceof MaterialCardView) {

            MaterialCardView card =
                    (MaterialCardView) view;

            int current =
                    card.getCardBackgroundColor()
                            .getDefaultColor();

            if (current == COLOR_SOFT_BLUE) {

                card.setCardBackgroundColor(
                        COLOR_DARK_SOFT_BLUE
                );

            } else if (current == COLOR_SOFT_GREEN) {

                card.setCardBackgroundColor(
                        COLOR_DARK_SOFT_GREEN
                );

            } else if (current == COLOR_SOFT_RED) {

                card.setCardBackgroundColor(
                        COLOR_DARK_SOFT_RED
                );

            } else if (current == COLOR_SOFT_ORANGE) {

                card.setCardBackgroundColor(
                        COLOR_DARK_SOFT_ORANGE
                );

            } else {

                card.setCardBackgroundColor(
                        COLOR_DARK_CARD
                );
            }

            card.setStrokeColor(
                    COLOR_DARK_BORDER
            );

            card.setStrokeWidth(
                    dpToPx(1)
            );
        }

        // -----------------------------------------------------
        // PROGRESS BAR
        // -----------------------------------------------------

        if (view instanceof ProgressBar) {

            ProgressBar progress =
                    (ProgressBar) view;

            progress.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            COLOR_DARK_BORDER
                    )
            );
        }

        // -----------------------------------------------------
        // MATERIAL BUTTONS
        // -----------------------------------------------------

        if (view instanceof MaterialButton) {

            MaterialButton button =
                    (MaterialButton) view;

            int backgroundColor =
                    Color.TRANSPARENT;

            if (button.getBackgroundTintList() != null) {

                backgroundColor =
                        button.getBackgroundTintList()
                                .getDefaultColor();
            }

            // Present button: keep green
            if (backgroundColor == COLOR_SUCCESS) {

                button.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                Color.rgb(22, 163, 74)
                        )
                );

                button.setTextColor(
                        Color.WHITE
                );

                // Absent button
            } else if (
                    backgroundColor == COLOR_SOFT_RED
            ) {

                button.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                COLOR_DARK_SOFT_RED
                        )
                );

                button.setTextColor(
                        Color.rgb(252, 165, 165)
                );

                button.setStrokeColor(
                        android.content.res.ColorStateList.valueOf(
                                COLOR_DARK_BORDER
                        )
                );

                // History / blue button
            } else if (
                    backgroundColor == COLOR_SOFT_BLUE
            ) {

                button.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                COLOR_DARK_SOFT_BLUE
                        )
                );

                button.setTextColor(
                        COLOR_DARK_PRIMARY
                );

                button.setStrokeColor(
                        android.content.res.ColorStateList.valueOf(
                                COLOR_DARK_BORDER
                        )
                );

                // Primary button
            } else if (
                    backgroundColor == COLOR_PRIMARY
                            ||
                            backgroundColor == COLOR_PRIMARY_DARK
            ) {

                button.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                Color.rgb(59, 130, 246)
                        )
                );

                button.setTextColor(
                        Color.WHITE
                );

            } else {

                button.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                COLOR_DARK_INPUT
                        )
                );

                button.setTextColor(
                        COLOR_DARK_TEXT
                );

                button.setStrokeColor(
                        android.content.res.ColorStateList.valueOf(
                                COLOR_DARK_BORDER
                        )
                );
            }
        }

        // -----------------------------------------------------
        // NORMAL BUTTONS
        // -----------------------------------------------------

        else if (view instanceof Button) {

            ((Button) view).setTextColor(
                    COLOR_DARK_TEXT
            );
        }

        // -----------------------------------------------------
        // EDIT TEXT
        // -----------------------------------------------------

        if (view instanceof EditText) {

            EditText editText =
                    (EditText) view;

            editText.setTextColor(
                    COLOR_DARK_TEXT
            );

            editText.setHintTextColor(
                    COLOR_DARK_MUTED
            );

            editText.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            COLOR_DARK_PRIMARY
                    )
            );
        }

        // -----------------------------------------------------
        // TEXT VIEWS
        // -----------------------------------------------------

        if (view instanceof TextView
                && !(view instanceof EditText)
                && !(view instanceof Button)) {

            TextView text =
                    (TextView) view;

            int currentColor =
                    text.getCurrentTextColor();

            // Preserve status / attendance colors.
            boolean statusColor =
                    currentColor == COLOR_SUCCESS
                            ||
                            currentColor == COLOR_WARNING
                            ||
                            currentColor == COLOR_ERROR
                            ||
                            currentColor == COLOR_PRIMARY;

            if (!statusColor) {

                if (
                        currentColor == COLOR_TEXT
                                ||
                                currentColor == Color.BLACK
                                ||
                                currentColor == Color.WHITE
                ) {

                    text.setTextColor(
                            COLOR_DARK_TEXT
                    );

                } else if (
                        currentColor == COLOR_SECONDARY
                                ||
                                currentColor == COLOR_MUTED
                ) {

                    text.setTextColor(
                            currentColor == COLOR_MUTED
                                    ? COLOR_DARK_MUTED
                                    : COLOR_DARK_SECONDARY
                    );

                } else if (
                        currentColor == Color.rgb(
                                148,
                                163,
                                184
                        )
                ) {

                    text.setTextColor(
                            COLOR_DARK_MUTED
                    );
                }
            }
        }

        // -----------------------------------------------------
        // GENERIC WHITE / LIGHT BACKGROUNDS
        // -----------------------------------------------------

        if (!(view instanceof MaterialCardView)
                && !(view instanceof MaterialButton)
                && !(view instanceof EditText)) {

            android.graphics.drawable.Drawable drawable =
                    view.getBackground();

            if (drawable instanceof android.graphics.drawable.ColorDrawable) {

                int currentBackground =
                        ((android.graphics.drawable.ColorDrawable) drawable)
                                .getColor();

                if (
                        currentBackground == Color.WHITE
                                ||
                                currentBackground == COLOR_CARD
                ) {

                    view.setBackgroundColor(
                            COLOR_DARK_CARD
                    );

                } else if (
                        currentBackground == COLOR_BACKGROUND
                ) {

                    view.setBackgroundColor(
                            COLOR_DARK_BACKGROUND
                    );
                }
            }
        }

        // -----------------------------------------------------
        // CHILDREN
        // -----------------------------------------------------

        if (view instanceof ViewGroup) {

            ViewGroup group =
                    (ViewGroup) view;

            for (
                    int i = 0;
                    i < group.getChildCount();
                    i++
            ) {

                applyDarkThemeTree(
                        group.getChildAt(i)
                );
            }
        }
    }

    private void styleAttendanceDialog(
            AlertDialog dialog
    ) {

        if (dialog.getWindow() == null) {
            return;
        }

        boolean darkMode =
                isDarkModeEnabled();

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                darkMode
                        ? COLOR_DARK_CARD
                        : COLOR_CARD
        );

        background.setCornerRadius(
                dpToPx(20)
        );

        background.setStroke(
                dpToPx(1),
                darkMode
                        ? COLOR_DARK_BORDER
                        : COLOR_BORDER
        );

        dialog.getWindow()
                .setBackgroundDrawable(
                        background
                );

        dialog.getWindow()
                .setDimAmount(
                        darkMode
                                ? 0.55f
                                : 0.32f
                );

        if (darkMode) {

            applyDarkThemeTree(
                    dialog.getWindow()
                            .getDecorView()
            );
        }
    }

    private int dpToPx(int dp) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return (int) (
                dp * density + 0.5f
        );
    }
}
