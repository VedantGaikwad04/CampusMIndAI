package com.example.campusmindai;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SubjectsActivity extends AppCompatActivity {

    private Button btnAddSubject;

    private LinearLayout listSubjects;

    private TextView tvSubjectCount;
    private TextView tvSubjectMessage;
    private TextView tvEmptySubjects;

    private ArrayList<SubjectItem> subjects;

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    // ============================================================
    // SUBJECT MODEL
    // ============================================================

    private static class SubjectItem {

        String name;
        String documentId;

        SubjectItem() {
        }
    }

    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.subjectactivity);

        btnAddSubject = findViewById(R.id.btnAddSubject);
        listSubjects = findViewById(R.id.listSubjects);
        tvSubjectCount = findViewById(R.id.tvSubjectCount);
        tvSubjectMessage = findViewById(R.id.tvSubjectMessage);
        tvEmptySubjects = findViewById(R.id.tvEmptySubjects);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        applyThemeMode();

        subjects = new ArrayList<>();

        btnAddSubject.setOnClickListener(
                v -> showAddSubjectDialog()
        );

        loadSubjects();
    }

    // ============================================================
    // RESUME
    // ============================================================

    @Override
    protected void onResume() {
        super.onResume();

        applyThemeMode();
        loadSubjects();
    }

    // ============================================================
    // PROFESSIONAL ADD SUBJECT DIALOG
    // NO EXTRA XML FILE REQUIRED
    // ============================================================

    private void showAddSubjectDialog() {

        // --------------------------------------------------------
        // MAIN CONTAINER
        // --------------------------------------------------------

        LinearLayout mainLayout =
                new LinearLayout(this);

        mainLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        mainLayout.setPadding(
                dpToPx(22),
                dpToPx(8),
                dpToPx(22),
                dpToPx(8)
        );

        // --------------------------------------------------------
        // ICON + TITLE ROW
        // --------------------------------------------------------

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        // Icon box

        TextView icon =
                new TextView(this);

        icon.setText("📚");
        icon.setTextSize(21);
        icon.setGravity(Gravity.CENTER);

        GradientDrawable iconBackground =
                new GradientDrawable();

        iconBackground.setColor(
                Color.rgb(
                        239,
                        246,
                        255
                )
        );

        iconBackground.setCornerRadius(
                dpToPx(14)
        );

        icon.setBackground(
                iconBackground
        );

        header.addView(
                icon,
                new LinearLayout.LayoutParams(
                        dpToPx(48),
                        dpToPx(48)
                )
        );

        // Title section

        LinearLayout titleLayout =
                new LinearLayout(this);

        titleLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                );

        titleParams.leftMargin =
                dpToPx(14);

        TextView title =
                new TextView(this);

        title.setText(
                "Add New Subject"
        );

        title.setTextColor(
                Color.rgb(
                        15,
                        23,
                        42
                )
        );

        title.setTextSize(20);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "Create a subject for your academic workspace"
        );

        subtitle.setTextColor(
                Color.rgb(
                        100,
                        116,
                        139
                )
        );

        subtitle.setTextSize(12);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.topMargin =
                dpToPx(3);

        titleLayout.addView(title);

        titleLayout.addView(
                subtitle,
                subtitleParams
        );

        header.addView(
                titleLayout,
                titleParams
        );

        mainLayout.addView(header);

        // --------------------------------------------------------
        // SPACE
        // --------------------------------------------------------

        View space1 =
                new View(this);

        mainLayout.addView(
                space1,
                new LinearLayout.LayoutParams(
                        1,
                        dpToPx(22)
                )
        );

        // --------------------------------------------------------
        // LABEL
        // --------------------------------------------------------

        TextView label =
                new TextView(this);

        label.setText(
                "Subject Name"
        );

        label.setTextColor(
                Color.rgb(
                        51,
                        65,
                        85
                )
        );

        label.setTextSize(13);

        label.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        mainLayout.addView(label);

        // --------------------------------------------------------
        // INPUT
        // --------------------------------------------------------

        EditText input =
                new EditText(this);

        input.setHint(
                "e.g. Advanced Web Programming"
        );

        input.setSingleLine(true);

        input.setTextSize(14);

        input.setTextColor(
                Color.rgb(
                        15,
                        23,
                        42
                )
        );

        input.setHintTextColor(
                Color.rgb(
                        148,
                        163,
                        184
                )
        );

        input.setPadding(
                dpToPx(16),
                0,
                dpToPx(16),
                0
        );

        GradientDrawable inputBackground =
                new GradientDrawable();

        inputBackground.setColor(
                Color.rgb(
                        248,
                        250,
                        252
                )
        );

        inputBackground.setStroke(
                dpToPx(1),
                Color.rgb(
                        203,
                        213,
                        225
                )
        );

        inputBackground.setCornerRadius(
                dpToPx(14)
        );

        input.setBackground(
                inputBackground
        );

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(56)
                );

        inputParams.topMargin =
                dpToPx(8);

        mainLayout.addView(
                input,
                inputParams
        );

        // --------------------------------------------------------
        // HELPER TEXT
        // --------------------------------------------------------

        TextView helper =
                new TextView(this);

        helper.setText(
                "You can manage attendance, assignments and focus time for this subject."
        );

        helper.setTextColor(
                Color.rgb(
                        148,
                        163,
                        184
                )
        );

        helper.setTextSize(11);

        LinearLayout.LayoutParams helperParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        helperParams.topMargin =
                dpToPx(8);

        mainLayout.addView(
                helper,
                helperParams
        );

        // --------------------------------------------------------
        // BUTTON ROW
        // --------------------------------------------------------

        LinearLayout buttonRow =
                new LinearLayout(this);

        buttonRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        buttonRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        LinearLayout.LayoutParams buttonRowParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(52)
                );

        buttonRowParams.topMargin =
                dpToPx(22);

        // CANCEL BUTTON

        MaterialButton cancelButton =
                new MaterialButton(this);

        cancelButton.setText(
                "Cancel"
        );

        cancelButton.setAllCaps(false);

        cancelButton.setTextSize(14);

        cancelButton.setTextColor(
                Color.rgb(
                        71,
                        85,
                        105
                )
        );

        cancelButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        cancelButton.setCornerRadius(
                dpToPx(14)
        );

        cancelButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.rgb(
                                241,
                                245,
                                249
                        )
                )
        );

        LinearLayout.LayoutParams cancelParams =
                new LinearLayout.LayoutParams(
                        0,
                        dpToPx(52),
                        1
                );

        buttonRow.addView(
                cancelButton,
                cancelParams
        );

        // ADD BUTTON

        MaterialButton addButton =
                new MaterialButton(this);

        addButton.setText(
                "Add Subject"
        );

        addButton.setAllCaps(false);

        addButton.setTextSize(14);

        addButton.setTextColor(
                Color.WHITE
        );

        addButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        addButton.setCornerRadius(
                dpToPx(14)
        );

        addButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.rgb(
                                37,
                                99,
                                235
                        )
                )
        );

        LinearLayout.LayoutParams addParams =
                new LinearLayout.LayoutParams(
                        0,
                        dpToPx(52),
                        1
                );

        addParams.leftMargin =
                dpToPx(10);

        buttonRow.addView(
                addButton,
                addParams
        );

        mainLayout.addView(
                buttonRow,
                buttonRowParams
        );

        // --------------------------------------------------------
        // DIALOG
        // --------------------------------------------------------

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setView(mainLayout)
                        .create();

        // --------------------------------------------------------
        // CANCEL
        // --------------------------------------------------------

        cancelButton.setOnClickListener(
                v -> dialog.dismiss()
        );

        // --------------------------------------------------------
        // ADD SUBJECT
        // --------------------------------------------------------

        addButton.setOnClickListener(
                v -> {

                    String subjectName =
                            input.getText()
                                    .toString()
                                    .trim();

                    if (subjectName.isEmpty()) {

                        input.setError(
                                "Enter subject name"
                        );

                        input.requestFocus();

                        return;
                    }

                    // Check duplicate

                    boolean alreadyExists =
                            false;

                    for (
                            SubjectItem subject :
                            subjects
                    ) {

                        if (
                                subject.name
                                        .equalsIgnoreCase(
                                                subjectName
                                        )
                        ) {

                            alreadyExists =
                                    true;

                            break;
                        }
                    }

                    if (alreadyExists) {

                        input.setError(
                                "Subject already exists"
                        );

                        input.requestFocus();

                        return;
                    }

                    saveSubject(
                            subjectName
                    );

                    dialog.dismiss();
                }
        );

        // --------------------------------------------------------
        // SHOW DIALOG
        // --------------------------------------------------------

        dialog.show();

        if (isDarkModeEnabled()
                && dialog.getWindow() != null) {

            applyDarkThemeTree(
                    dialog.getWindow()
                            .getDecorView()
            );
        }

        // --------------------------------------------------------
        // PROFESSIONAL ROUNDED WINDOW
        // --------------------------------------------------------

        if (dialog.getWindow() != null) {

            GradientDrawable windowBackground =
                    new GradientDrawable();

            boolean darkMode = isDarkModeEnabled();

            windowBackground.setColor(
                    darkMode
                            ? Color.rgb(17, 24, 39)
                            : Color.WHITE
            );

            windowBackground.setCornerRadius(
                    dpToPx(24)
            );

            windowBackground.setStroke(
                    dpToPx(1),
                    darkMode
                            ? Color.rgb(51, 65, 85)
                            : Color.rgb(226, 232, 240)
            );

            dialog.getWindow()
                    .setBackgroundDrawable(
                            windowBackground
                    );

            dialog.getWindow()
                    .setDimAmount(
                            0.35f
                    );

            dialog.getWindow()
                    .addFlags(
                            android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
                    );

            dialog.getWindow()
                    .setLayout(
                            (int) (
                                    getResources()
                                            .getDisplayMetrics()
                                            .widthPixels
                                            * 0.90
                            ),
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
        }
    }

    // ============================================================
    // SAVE SUBJECT
    // ============================================================

    private void saveSubject(
            String subjectName
    ) {

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

        String userId =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        java.util.Map<String, Object> subject =
                new java.util.HashMap<>();

        subject.put(
                "name",
                subjectName
        );

        subject.put(
                "createdAt",
                System.currentTimeMillis()
        );

        firestore
                .collection("students")
                .document(userId)
                .collection("subjects")
                .add(subject)
                .addOnSuccessListener(
                        documentReference -> {

                            Toast.makeText(
                                    this,
                                    "Subject added successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadSubjects();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Unable to add subject:\n" +
                                            getFirebaseError(e),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // ============================================================
    // LOAD SUBJECTS
    // ============================================================

    private void loadSubjects() {

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

        String userId =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        firestore
                .collection("students")
                .document(userId)
                .collection("subjects")
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            subjects.clear();

                            Set<String> uniqueSubjects =
                                    new HashSet<>();

                            for (
                                    DocumentSnapshot document :
                                    snapshot.getDocuments()
                            ) {

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

                                if (
                                        !uniqueSubjects.contains(
                                                normalized
                                        )
                                ) {

                                    uniqueSubjects.add(
                                            normalized
                                    );

                                    SubjectItem item =
                                            new SubjectItem();

                                    item.name =
                                            name;

                                    item.documentId =
                                            document.getId();

                                    subjects.add(
                                            item
                                    );
                                }
                            }

                            Collections.sort(
                                    subjects,
                                    (a, b) ->
                                            a.name.compareToIgnoreCase(
                                                    b.name
                                            )
                            );

                            displaySubjects();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            String error =
                                    getFirebaseError(e);

                            Toast.makeText(
                                    this,
                                    "Unable to load subjects:\n" +
                                            error,
                                    Toast.LENGTH_LONG
                            ).show();

                            tvSubjectMessage.setText(
                                    "Unable to load subjects"
                            );

                            tvEmptySubjects.setVisibility(
                                    View.VISIBLE
                            );

                            applyThemeMode();
                        }
                );
    }

    // ============================================================
    // DISPLAY SUBJECTS
    // ============================================================

    private void displaySubjects() {

        listSubjects.removeAllViews();

        int count =
                subjects.size();

        tvSubjectCount.setText(
                String.valueOf(count)
        );

        if (count == 0) {

            tvSubjectMessage.setText(
                    "Add your first subject"
            );

            tvEmptySubjects.setVisibility(
                    View.VISIBLE
            );

            applyThemeMode();
            return;
        }

        tvEmptySubjects.setVisibility(
                View.GONE
        );

        if (count == 1) {

            tvSubjectMessage.setText(
                    "1 subject in your workspace"
            );

        } else {

            tvSubjectMessage.setText(
                    count +
                            " subjects in your workspace"
            );
        }

        for (
                int i = 0;
                i < subjects.size();
                i++
        ) {

            addSubjectCard(
                    subjects.get(i),
                    i
            );
        }

        applyThemeMode();
    }

    // ============================================================
    // SUBJECT CARD
    // ============================================================

    private void addSubjectCard(
            SubjectItem subject,
            int position
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
                Color.WHITE
        );

        card.setStrokeColor(
                Color.rgb(
                        226,
                        232,
                        240
                )
        );

        card.setStrokeWidth(
                dpToPx(1)
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
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
                dpToPx(16),
                dpToPx(16),
                dpToPx(16),
                dpToPx(16)
        );

        // --------------------------------------------------------
        // TOP ROW
        // --------------------------------------------------------

        LinearLayout topRow =
                new LinearLayout(this);

        topRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        topRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView number =
                new TextView(this);

        number.setText(
                String.format(
                        Locale.getDefault(),
                        "%02d",
                        position + 1
                )
        );

        number.setTextColor(
                Color.rgb(
                        37,
                        99,
                        235
                )
        );

        number.setTextSize(13);

        number.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        number.setGravity(
                Gravity.CENTER
        );

        GradientDrawable numberBackground =
                new GradientDrawable();

        numberBackground.setColor(
                Color.rgb(
                        239,
                        246,
                        255
                )
        );

        numberBackground.setCornerRadius(
                dpToPx(12)
        );

        number.setBackground(
                numberBackground
        );

        topRow.addView(
                number,
                new LinearLayout.LayoutParams(
                        dpToPx(44),
                        dpToPx(44)
                )
        );

        // --------------------------------------------------------
        // SUBJECT INFORMATION
        // --------------------------------------------------------

        LinearLayout info =
                new LinearLayout(this);

        info.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                );

        infoParams.leftMargin =
                dpToPx(13);

        TextView name =
                new TextView(this);

        name.setText(
                subject.name
        );

        name.setTextColor(
                Color.rgb(
                        15,
                        23,
                        42
                )
        );

        name.setTextSize(17);

        name.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "Academic subject"
        );

        subtitle.setTextColor(
                Color.rgb(
                        100,
                        116,
                        139
                )
        );

        subtitle.setTextSize(12);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.topMargin =
                dpToPx(3);

        info.addView(name);

        info.addView(
                subtitle,
                subtitleParams
        );

        topRow.addView(
                info,
                infoParams
        );

        content.addView(topRow);

        // --------------------------------------------------------
        // STATS
        // --------------------------------------------------------

        LinearLayout statsRow =
                new LinearLayout(this);

        statsRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        LinearLayout.LayoutParams statsParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        statsParams.topMargin =
                dpToPx(15);

        TextView attendance =
                createStatText(
                        "Attendance",
                        "Loading..."
                );

        TextView assignments =
                createStatText(
                        "Assignments",
                        "Loading..."
                );

        TextView focus =
                createStatText(
                        "Focus Time",
                        "Loading..."
                );

        statsRow.addView(
                attendance,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        statsRow.addView(
                assignments,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        statsRow.addView(
                focus,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        content.addView(
                statsRow,
                statsParams
        );

        // --------------------------------------------------------
        // OPEN SUBJECT BUTTON
        // --------------------------------------------------------

        MaterialButton openButton =
                new MaterialButton(this);

        openButton.setText(
                "OPEN SUBJECT  →"
        );

        openButton.setTextSize(13);

        openButton.setTextColor(
                Color.WHITE
        );

        openButton.setAllCaps(false);

        openButton.setGravity(
                Gravity.CENTER
        );

        openButton.setCornerRadius(
                dpToPx(13)
        );

        openButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.rgb(
                                37,
                                99,
                                235
                        )
                )
        );

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(50)
                );

        buttonParams.topMargin =
                dpToPx(15);

        content.addView(
                openButton,
                buttonParams
        );

        card.addView(content);

        // --------------------------------------------------------
        // LOAD STATISTICS
        // --------------------------------------------------------

        loadSubjectStats(
                subject.name,
                attendance,
                assignments,
                focus
        );

        // --------------------------------------------------------
        // OPEN SUBJECT
        // --------------------------------------------------------

        openButton.setOnClickListener(
                v -> openSubject(
                        subject.name
                )
        );

        card.setOnClickListener(
                v -> openSubject(
                        subject.name
                )
        );

        // --------------------------------------------------------
        // LONG PRESS DELETE
        // --------------------------------------------------------

        card.setOnLongClickListener(
                v -> {

                    showDeleteDialog(
                            subject
                    );

                    return true;
                }
        );

        listSubjects.addView(card);
    }

    // ============================================================
    // STAT TEXT
    // ============================================================

    private TextView createStatText(
            String label,
            String value
    ) {

        TextView text =
                new TextView(this);

        text.setText(
                label +
                        "\n" +
                        value
        );

        text.setTextColor(
                Color.rgb(
                        71,
                        85,
                        105
                )
        );

        text.setTextSize(12);

        return text;
    }

    // ============================================================
    // SUBJECT STATISTICS
    // ============================================================

    private void loadSubjectStats(
            String subjectName,
            TextView attendanceView,
            TextView assignmentView,
            TextView focusView
    ) {

        if (
                firebaseAuth.getCurrentUser()
                        == null
        ) {
            return;
        }

        String userId =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        // --------------------------------------------------------
        // ATTENDANCE
        // --------------------------------------------------------

        firestore
                .collection("students")
                .document(userId)
                .collection("attendance")
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            int total = 0;
                            int present = 0;

                            for (
                                    DocumentSnapshot document :
                                    snapshot.getDocuments()
                            ) {

                                String subject =
                                        document.getString(
                                                "subject"
                                        );

                                String status =
                                        document.getString(
                                                "status"
                                        );

                                if (
                                        subject != null &&
                                                subject.equalsIgnoreCase(
                                                        subjectName
                                                )
                                ) {

                                    total++;

                                    if (
                                            "Present"
                                                    .equalsIgnoreCase(
                                                            status
                                                    )
                                    ) {

                                        present++;
                                    }
                                }
                            }

                            int percentage =
                                    total > 0
                                            ? (present * 100)
                                              / total
                                            : 0;

                            attendanceView.setText(
                                    "Attendance\n" +
                                            percentage +
                                            "%"
                            );
                        }
                );

        // --------------------------------------------------------
        // ASSIGNMENTS
        // --------------------------------------------------------

        firestore
                .collection("students")
                .document(userId)
                .collection("assignments")
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            int pending = 0;

                            for (
                                    DocumentSnapshot document :
                                    snapshot.getDocuments()
                            ) {

                                String subject =
                                        document.getString(
                                                "subject"
                                        );

                                String status =
                                        document.getString(
                                                "status"
                                        );

                                if (
                                        subject != null &&
                                                subject.equalsIgnoreCase(
                                                        subjectName
                                                ) &&
                                                !"Completed"
                                                        .equalsIgnoreCase(
                                                                status
                                                        )
                                ) {

                                    pending++;
                                }
                            }

                            assignmentView.setText(
                                    "Assignments\n" +
                                            pending +
                                            " pending"
                            );
                        }
                );

        // --------------------------------------------------------
        // FOCUS
        // --------------------------------------------------------

        firestore
                .collection("students")
                .document(userId)
                .collection("studySessions")
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            int totalMinutes = 0;

                            for (
                                    DocumentSnapshot document :
                                    snapshot.getDocuments()
                            ) {

                                String subject =
                                        document.getString(
                                                "subject"
                                        );

                                Long duration =
                                        document.getLong(
                                                "durationMinutes"
                                        );

                                if (
                                        subject != null &&
                                                subject.equalsIgnoreCase(
                                                        subjectName
                                                )
                                ) {

                                    if (duration != null) {

                                        totalMinutes +=
                                                duration;

                                    } else {

                                        totalMinutes +=
                                                45;
                                    }
                                }
                            }

                            focusView.setText(
                                    "Focus Time\n" +
                                            totalMinutes +
                                            " min"
                            );
                        }
                );
    }

    // ============================================================
    // OPEN SUBJECT
    // ============================================================

    private void openSubject(
            String subjectName
    ) {

        Intent intent =
                new Intent(
                        SubjectsActivity.this,
                        SubjectDetailsActivity.class
                );

        intent.putExtra(
                "subjectName",
                subjectName
        );

        startActivity(intent);
    }

    // ============================================================
    // DELETE SUBJECT
    // ============================================================

    private void showDeleteDialog(
            SubjectItem subject
    ) {

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Delete Subject?"
                        )
                        .setMessage(
                                "Delete \"" +
                                        subject.name +
                                        "\" from your subjects?"
                        )
                        .setPositiveButton(
                                "DELETE",
                                (dialogInterface, which) ->
                                        deleteSubject(
                                                subject
                                        )
                        )
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {
                    if (isDarkModeEnabled()
                            && dialog.getWindow() != null) {
                        applyDarkThemeTree(
                                dialog.getWindow()
                                        .getDecorView()
                        );

                        dialog.getButton(
                                AlertDialog.BUTTON_POSITIVE
                        ).setTextColor(
                                Color.rgb(248, 113, 113)
                        );

                        dialog.getButton(
                                AlertDialog.BUTTON_NEGATIVE
                        ).setTextColor(
                                Color.rgb(203, 213, 225)
                        );
                    }
                }
        );

        dialog.show();
    }

    private void deleteSubject(
            SubjectItem subject
    ) {

        if (
                firebaseAuth.getCurrentUser()
                        == null
        ) {
            return;
        }

        String userId =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        firestore
                .collection("students")
                .document(userId)
                .collection("subjects")
                .document(subject.documentId)
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Subject deleted",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadSubjects();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Unable to delete subject:\n" +
                                            getFirebaseError(e),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // ============================================================
    // FIREBASE ERROR
    // ============================================================

    private String getFirebaseError(
            Exception e
    ) {

        if (
                e == null ||
                        e.getMessage() == null
        ) {

            return "Unknown Firebase error";
        }

        String message =
                e.getMessage();

        if (
                message.contains(
                        "PERMISSION_DENIED"
                )
        ) {

            return "Firestore permission denied. Check Firestore Security Rules.";
        }

        if (
                message.contains(
                        "UNAUTHENTICATED"
                )
        ) {

            return "User is not authenticated. Please login again.";
        }

        if (
                message.contains(
                        "network"
                ) ||
                        message.contains(
                                "Network"
                        )
        ) {

            return "Network error. Check your internet connection.";
        }

        return message;
    }

    // ============================================================
    // LIGHT / DARK MODE
    // ============================================================

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

        boolean darkMode =
                isDarkModeEnabled();

        int background =
                darkMode
                        ? Color.rgb(11, 17, 32)
                        : Color.rgb(248, 250, 252);

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

        View root =
                findViewById(
                        android.R.id.content
                );

        if (root == null) {
            return;
        }

        root.setBackgroundColor(
                background
        );

        if (root instanceof ViewGroup
                && ((ViewGroup) root).getChildCount() > 0) {

            ((ViewGroup) root)
                    .getChildAt(0)
                    .setBackgroundColor(
                            background
                    );
        }

        if (darkMode) {
            applyDarkThemeTree(root);
        }
    }

    private void applyDarkThemeTree(View view) {

        if (view == null) {
            return;
        }

        final int darkBackground =
                Color.rgb(11, 17, 32);
        final int darkCard =
                Color.rgb(17, 24, 39);
        final int darkText =
                Color.rgb(248, 250, 252);
        final int darkSecondary =
                Color.rgb(203, 213, 225);
        final int darkMuted =
                Color.rgb(148, 163, 184);
        final int darkBorder =
                Color.rgb(51, 65, 85);
        final int darkPrimary =
                Color.rgb(96, 165, 250);
        final int darkSoftBlue =
                Color.rgb(23, 37, 84);
        final int darkSoftGray =
                Color.rgb(30, 41, 59);

        if (view instanceof MaterialCardView) {

            MaterialCardView card =
                    (MaterialCardView) view;

            int current =
                    card.getCardBackgroundColor()
                            .getDefaultColor();

            if (current == Color.rgb(239, 246, 255)) {
                card.setCardBackgroundColor(darkSoftBlue);
            } else if (current == Color.rgb(240, 253, 244)) {
                card.setCardBackgroundColor(Color.rgb(20, 83, 45));
            } else if (current == Color.rgb(255, 247, 237)) {
                card.setCardBackgroundColor(Color.rgb(120, 53, 15));
            } else if (current == Color.rgb(254, 242, 242)) {
                card.setCardBackgroundColor(Color.rgb(127, 29, 29));
            } else {
                card.setCardBackgroundColor(darkCard);
            }

            card.setStrokeColor(darkBorder);
            card.setStrokeWidth(dpToPx(1));
        }

        if (view instanceof MaterialButton) {

            MaterialButton button =
                    (MaterialButton) view;

            int current =
                    button.getBackgroundTintList() == null
                            ? Color.TRANSPARENT
                            : button.getBackgroundTintList()
                            .getDefaultColor();

            if (current == Color.rgb(37, 99, 235)
                    || current == Color.rgb(29, 78, 216)
                    || current == Color.rgb(96, 165, 250)) {

                button.setBackgroundTintList(
                        ColorStateList.valueOf(
                                Color.rgb(59, 130, 246)
                        )
                );

                button.setTextColor(
                        Color.WHITE
                );

            } else if (current == Color.rgb(239, 246, 255)) {

                button.setBackgroundTintList(
                        ColorStateList.valueOf(
                                darkSoftBlue
                        )
                );

                button.setTextColor(
                        darkPrimary
                );

            } else {

                button.setBackgroundTintList(
                        ColorStateList.valueOf(
                                darkSoftGray
                        )
                );

                button.setTextColor(
                        darkSecondary
                );

                button.setStrokeColor(
                        ColorStateList.valueOf(
                                darkBorder
                        )
                );
            }
        } else if (view instanceof Button) {

            Button button =
                    (Button) view;

            button.setTextColor(
                    Color.WHITE
            );
        }

        if (view instanceof EditText) {

            EditText editText =
                    (EditText) view;

            editText.setTextColor(
                    darkText
            );

            editText.setHintTextColor(
                    darkMuted
            );

            GradientDrawable inputBackground =
                    new GradientDrawable();

            inputBackground.setColor(
                    darkSoftGray
            );

            inputBackground.setCornerRadius(
                    dpToPx(13)
            );

            inputBackground.setStroke(
                    dpToPx(1),
                    darkBorder
            );

            editText.setBackground(
                    inputBackground
            );
        }

        if (view instanceof TextView
                && !(view instanceof EditText)
                && !(view instanceof MaterialButton)
                && !(view instanceof Button)) {

            TextView text =
                    (TextView) view;

            int current =
                    text.getCurrentTextColor();

            if (current == Color.WHITE
                    || current == Color.rgb(15, 23, 42)
                    || current == Color.rgb(30, 41, 59)
                    || current == Color.rgb(51, 65, 85)
                    || current == Color.BLACK) {

                text.setTextColor(
                        darkText
                );

            } else if (current == Color.rgb(71, 85, 105)
                    || current == Color.rgb(100, 116, 139)) {

                text.setTextColor(
                        darkSecondary
                );

            } else if (current == Color.rgb(148, 163, 184)) {

                text.setTextColor(
                        darkMuted
                );

            } else if (current == Color.rgb(37, 99, 235)) {

                text.setTextColor(
                        darkPrimary
                );
            }
        }

        if (view.getBackground() instanceof ColorDrawable) {

            int background =
                    ((ColorDrawable) view.getBackground())
                            .getColor();

            if (background == Color.WHITE) {

                if (view instanceof TextView) {
                    view.setBackgroundColor(darkCard);
                } else {
                    view.setBackgroundColor(darkCard);
                }

            } else if (background == Color.rgb(248, 250, 252)) {

                view.setBackgroundColor(
                        darkBackground
                );
            }
        }

        if (view instanceof ViewGroup) {

            ViewGroup group =
                    (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                applyDarkThemeTree(
                        group.getChildAt(i)
                );
            }
        }
    }

    // ============================================================
    // DP TO PX
    // ============================================================

    private int dpToPx(
            int dp
    ) {

        return (int) (
                dp *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }
}