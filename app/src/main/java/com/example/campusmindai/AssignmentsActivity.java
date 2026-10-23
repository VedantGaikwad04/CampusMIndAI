package com.example.campusmindai;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AssignmentsActivity extends AppCompatActivity {

    private MaterialButton btnAddAssignment;
    private MaterialButton btnAddNote;

    private LinearLayout listAssignments;
    private LinearLayout listNotes;

    private TextView tvTotalAssignments;
    private TextView tvCompletedAssignments;
    private TextView tvPendingAssignments;

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    private ActivityResultLauncher<String[]> filePickerLauncher;

    private Uri selectedFileUri;
    private String selectedFileName = "";
    private String selectedFileType = "";
    private String selectedMimeType = "";

    private TextView tvSelectedFile;

    private AlertDialog currentAssignmentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.assignmentsactivity
        );

        btnAddAssignment =
                findViewById(
                        R.id.btnAddAssignment
                );

        btnAddNote =
                findViewById(
                        R.id.btnAddNote
                );

        listAssignments =
                findViewById(
                        R.id.listAssignments
                );

        listNotes =
                findViewById(
                        R.id.listNotes
                );

        tvTotalAssignments =
                findViewById(
                        R.id.tvTotalAssignments
                );

        tvCompletedAssignments =
                findViewById(
                        R.id.tvCompletedAssignments
                );

        tvPendingAssignments =
                findViewById(
                        R.id.tvPendingAssignments
                );

        firestore =
                FirebaseFirestore.getInstance();

        firebaseAuth =
                FirebaseAuth.getInstance();

        applyThemeMode();

        setupFilePicker();

        btnAddAssignment.setOnClickListener(
                v -> showAddAssignmentDialog()
        );

        btnAddNote.setOnClickListener(
                v -> showAddNoteDialog()
        );

        loadAssignments();
        loadNotes();
    }

    @Override
    protected void onResume() {
        super.onResume();

        applyThemeMode();

        if (firestore != null &&
                firebaseAuth != null &&
                firebaseAuth.getCurrentUser() != null) {

            loadAssignments();
            loadNotes();
        }
    }

    // =========================================================
    // FILE PICKER
    // =========================================================

    private void setupFilePicker() {

        filePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.OpenDocument(),
                        uri -> {

                            if (uri == null) {
                                return;
                            }

                            selectedFileUri = uri;

                            selectedFileName =
                                    getFileName(uri);

                            selectedFileType =
                                    getFileExtension(
                                            selectedFileName
                                    );

                            selectedMimeType =
                                    getMimeTypeFromUri(uri);

                            try {

                                getContentResolver()
                                        .takePersistableUriPermission(
                                                uri,
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        );

                            } catch (Exception ignored) {
                            }

                            if (tvSelectedFile != null) {

                                tvSelectedFile.setText(
                                        getFileIcon(
                                                selectedFileType
                                        )
                                                + " "
                                                + selectedFileName
                                                + "\nStored on this device"
                                );

                                tvSelectedFile.setTextColor(
                                        Color.rgb(
                                                22,
                                                101,
                                                52
                                        )
                                );
                            }
                        }
                );
    }

    // =========================================================
    // ADD ASSIGNMENT - PROFESSIONAL LIGHT DIALOG
    // =========================================================

    private void showAddAssignmentDialog() {

        selectedFileUri = null;
        selectedFileName = "";
        selectedFileType = "";
        selectedMimeType = "";

        LinearLayout root =
                createDialogRoot();

        TextView icon =
                createDialogIcon("📝");

        root.addView(
                icon,
                wrapParams(0, 10)
        );

        TextView title =
                createDialogTitle(
                        "Add Assignment"
                );

        root.addView(
                title,
                wrapParams(14, 0)
        );

        TextView subtitle =
                createDialogSubtitle(
                        "Add a deadline and study material to stay organized."
                );

        root.addView(
                subtitle,
                wrapParams(5, 0)
        );

        EditText titleInput =
                createModernInput(
                        "Assignment title",
                        true
                );

        root.addView(
                titleInput,
                wrapParams(20, 0)
        );

        EditText subjectInput =
                createModernInput(
                        "Subject (optional)",
                        true
                );

        root.addView(
                subjectInput,
                wrapParams(12, 0)
        );

        EditText dueDateInput =
                createModernInput(
                        "Due date  •  dd-MM-yyyy",
                        true
                );

        dueDateInput.setInputType(
                InputType.TYPE_CLASS_DATETIME
        );

        root.addView(
                dueDateInput,
                wrapParams(12, 0)
        );

        TextView materialLabel =
                createSectionLabel(
                        "Study Material"
                );

        root.addView(
                materialLabel,
                wrapParams(18, 0)
        );

        TextView supportedText =
                createDialogSubtitle(
                        "PDF • Word • Images  •  Stored locally on your device"
                );

        root.addView(
                supportedText,
                wrapParams(3, 0)
        );

        MaterialButton attachButton =
                createPrimaryButton(
                        "📎  CHOOSE FILE",
                        52
                );

        LinearLayout.LayoutParams attachParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(52)
                );

        attachParams.topMargin =
                dpToPx(10);

        root.addView(
                attachButton,
                attachParams
        );

        tvSelectedFile =
                new TextView(this);

        tvSelectedFile.setText(
                "No file selected"
        );

        tvSelectedFile.setTextSize(
                12
        );

        tvSelectedFile.setTextColor(
                Color.rgb(
                        100,
                        116,
                        139
                )
        );

        tvSelectedFile.setPadding(
                dpToPx(4),
                dpToPx(6),
                dpToPx(4),
                dpToPx(2)
        );

        root.addView(
                tvSelectedFile,
                wrapParams(3, 0)
        );

        attachButton.setOnClickListener(
                v -> filePickerLauncher.launch(
                        new String[]{
                                "application/pdf",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "image/*"
                        }
                )
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setView(root)
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .setPositiveButton(
                                "ADD ASSIGNMENT",
                                null
                        )
                        .create();

        currentAssignmentDialog =
                dialog;

        dialog.setOnShowListener(
                d -> {

                    styleDialogWindow(
                            dialog
                    );

                    dialog.getButton(
                            AlertDialog.BUTTON_NEGATIVE
                    ).setTextColor(
                            isDarkModeEnabled()
                                    ? Color.rgb(203, 213, 225)
                                    : Color.rgb(71, 85, 105)
                    );

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setTextColor(
                            isDarkModeEnabled()
                                    ? Color.rgb(96, 165, 250)
                                    : Color.rgb(37, 99, 235)
                    );

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setOnClickListener(
                            v -> {

                                String assignmentTitle =
                                        titleInput
                                                .getText()
                                                .toString()
                                                .trim();

                                String subject =
                                        subjectInput
                                                .getText()
                                                .toString()
                                                .trim();

                                String dueDate =
                                        dueDateInput
                                                .getText()
                                                .toString()
                                                .trim();

                                if (
                                        assignmentTitle.isEmpty()
                                ) {

                                    titleInput.setError(
                                            "Enter assignment title"
                                    );

                                    titleInput.requestFocus();

                                    return;
                                }

                                if (
                                        !dueDate.isEmpty()
                                                &&
                                                parseDate(
                                                        dueDate
                                                ) == -1
                                ) {

                                    dueDateInput.setError(
                                            "Use dd-MM-yyyy"
                                    );

                                    dueDateInput.requestFocus();

                                    return;
                                }

                                addAssignment(
                                        assignmentTitle,
                                        subject,
                                        dueDate
                                );
                            }
                    );
                }
        );

        dialog.show();
    }

    // =========================================================
    // MODERN DIALOG UI HELPERS
    // =========================================================

    private LinearLayout createDialogRoot() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dpToPx(24),
                dpToPx(12),
                dpToPx(24),
                dpToPx(6)
        );

        root.setBackgroundColor(
                Color.WHITE
        );

        return root;
    }

    private TextView createDialogIcon(
            String text
    ) {

        TextView icon =
                new TextView(this);

        icon.setText(
                text
        );

        icon.setTextSize(
                22
        );

        icon.setGravity(
                Gravity.CENTER
        );

        icon.setTextColor(
                Color.rgb(
                        37,
                        99,
                        235
                )
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                Color.rgb(
                        239,
                        246,
                        255
                )
        );

        background.setCornerRadius(
                dpToPx(14)
        );

        icon.setBackground(
                background
        );

        icon.setPadding(
                dpToPx(12),
                dpToPx(8),
                dpToPx(12),
                dpToPx(8)
        );

        icon.setLayoutParams(
                new LinearLayout.LayoutParams(
                        dpToPx(48),
                        dpToPx(48)
                )
        );

        return icon;
    }

    private TextView createDialogTitle(
            String text
    ) {

        TextView title =
                new TextView(this);

        title.setText(
                text
        );

        title.setTextColor(
                Color.rgb(
                        15,
                        23,
                        42
                )
        );

        title.setTextSize(
                22
        );

        title.setTypeface(
                null,
                Typeface.BOLD
        );

        return title;
    }

    private TextView createDialogSubtitle(
            String text
    ) {

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                text
        );

        subtitle.setTextColor(
                Color.rgb(
                        100,
                        116,
                        139
                )
        );

        subtitle.setTextSize(
                12
        );

        subtitle.setLineSpacing(
                0,
                1.1f
        );

        return subtitle;
    }

    private TextView createSectionLabel(
            String text
    ) {

        TextView label =
                new TextView(this);

        label.setText(
                text
        );

        label.setTextColor(
                Color.rgb(
                        15,
                        23,
                        42
                )
        );

        label.setTextSize(
                13
        );

        label.setTypeface(
                null,
                Typeface.BOLD
        );

        return label;
    }

    private EditText createModernInput(
            String hint,
            boolean singleLine
    ) {

        EditText input =
                new EditText(this);

        boolean darkMode =
                isDarkModeEnabled();

        input.setHint(
                hint
        );

        input.setHintTextColor(
                Color.rgb(
                        148,
                        163,
                        184
                )
        );

        input.setTextColor(
                darkMode
                        ? Color.rgb(248, 250, 252)
                        : Color.rgb(15, 23, 42)
        );

        input.setTextSize(
                14
        );

        input.setPadding(
                dpToPx(15),
                dpToPx(12),
                dpToPx(15),
                dpToPx(12)
        );

        input.setBackground(
                createInputBackground()
        );

        if (singleLine) {

            input.setSingleLine(
                    true
            );
        }

        return input;
    }

    private GradientDrawable createInputBackground() {

        boolean darkMode =
                isDarkModeEnabled();

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                darkMode
                        ? Color.rgb(30, 41, 59)
                        : Color.rgb(248, 250, 252)
        );

        background.setCornerRadius(
                dpToPx(13)
        );

        background.setStroke(
                dpToPx(1),
                darkMode
                        ? Color.rgb(51, 65, 85)
                        : Color.rgb(226, 232, 240)
        );

        return background;
    }

    private LinearLayout.LayoutParams wrapParams(
            int topMargin,
            int bottomMargin
    ) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin =
                dpToPx(topMargin);

        params.bottomMargin =
                dpToPx(bottomMargin);

        return params;
    }

    private void styleDialogWindow(
            AlertDialog dialog
    ) {

        if (dialog.getWindow() == null) {
            return;
        }

        boolean darkMode =
                isDarkModeEnabled();

        GradientDrawable background =
                new GradientDrawable();

        if (darkMode) {

            background.setColor(
                    Color.rgb(
                            17,
                            24,
                            39
                    )
            );

            background.setStroke(
                    dpToPx(1),
                    Color.rgb(
                            51,
                            65,
                            85
                    )
            );

        } else {

            background.setColor(
                    Color.WHITE
            );

            background.setStroke(
                    dpToPx(1),
                    Color.rgb(
                            226,
                            232,
                            240
                    )
            );
        }

        background.setCornerRadius(
                dpToPx(22)
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

        dialog.getWindow()
                .setLayout(
                        (int) (
                                getResources()
                                        .getDisplayMetrics()
                                        .widthPixels
                                        * 0.90f
                        ),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        if (darkMode) {
            applyDarkThemeTree(
                    dialog.getWindow()
                            .getDecorView()
            );
        }
    }

    // =========================================================
    // PRIMARY BUTTON
    // =========================================================

    private MaterialButton createPrimaryButton(
            String text,
            int height
    ) {

        MaterialButton button =
                new MaterialButton(this);

        button.setText(
                text
        );

        button.setTextSize(
                12
        );

        button.setTextColor(
                Color.WHITE
        );

        button.setTypeface(
                null,
                Typeface.BOLD
        );

        button.setGravity(
                Gravity.CENTER
        );

        button.setInsetTop(
                0
        );

        button.setInsetBottom(
                0
        );

        button.setPadding(
                12,
                0,
                12,
                0
        );

        button.setMinHeight(
                0
        );

        button.setMinimumHeight(
                0
        );

        button.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.rgb(
                                37,
                                99,
                                235
                        )
                )
        );

        button.setCornerRadius(
                dpToPx(15)
        );

        return button;
    }

    // =========================================================
    // ACTION BUTTON
    // =========================================================

    private MaterialButton createActionButton(
            String text,
            int textColor,
            int strokeColor
    ) {

        MaterialButton button =
                new MaterialButton(this);

        button.setText(
                text
        );

        button.setTextSize(
                12
        );

        button.setTypeface(
                null,
                Typeface.BOLD
        );

        button.setGravity(
                Gravity.CENTER
        );

        button.setInsetTop(
                0
        );

        button.setInsetBottom(
                0
        );

        button.setPadding(
                12,
                0,
                12,
                0
        );

        button.setMinHeight(
                0
        );

        button.setMinimumHeight(
                0
        );

        button.setTextColor(
                textColor
        );

        button.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.WHITE
                )
        );

        button.setStrokeColor(
                ColorStateList.valueOf(
                        strokeColor
                )
        );

        button.setStrokeWidth(
                2
        );

        button.setCornerRadius(
                dpToPx(15)
        );

        return button;
    }

    // =========================================================
    // SAVE ASSIGNMENT
    // =========================================================

    private void addAssignment(
            String title,
            String subject,
            String dueDate
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

        saveAssignmentToFirestore(
                title,
                subject,
                dueDate
        );
    }

    private void saveAssignmentToFirestore(
            String title,
            String subject,
            String dueDate
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

        long dueDateMillis =
                parseDate(
                        dueDate
                );

        Map<String, Object> assignment =
                new HashMap<>();

        assignment.put(
                "title",
                title
        );

        assignment.put(
                "subject",
                subject
        );

        assignment.put(
                "dueDate",
                dueDate
        );

        assignment.put(
                "dueDateMillis",
                dueDateMillis
        );

        assignment.put(
                "status",
                "Pending"
        );

        assignment.put(
                "createdAt",
                System.currentTimeMillis()
        );

        if (
                selectedFileUri != null
        ) {

            assignment.put(
                    "fileUri",
                    selectedFileUri.toString()
            );

            assignment.put(
                    "fileName",
                    selectedFileName
            );

            assignment.put(
                    "fileType",
                    selectedFileType
            );

            assignment.put(
                    "mimeType",
                    selectedMimeType
            );

            assignment.put(
                    "storageType",
                    "LOCAL"
            );

        } else {

            assignment.put(
                    "fileUri",
                    ""
            );

            assignment.put(
                    "fileName",
                    ""
            );

            assignment.put(
                    "fileType",
                    ""
            );

            assignment.put(
                    "mimeType",
                    ""
            );

            assignment.put(
                    "storageType",
                    "NONE"
            );
        }

        firestore
                .collection(
                        "students"
                )
                .document(
                        userId
                )
                .collection(
                        "assignments"
                )
                .add(
                        assignment
                )
                .addOnSuccessListener(
                        documentReference -> {

                            Toast.makeText(
                                    this,
                                    selectedFileUri != null
                                            ? "Assignment and file saved"
                                            : "Assignment added",
                                    Toast.LENGTH_SHORT
                            ).show();

                            if (
                                    currentAssignmentDialog != null
                                            &&
                                            currentAssignmentDialog.isShowing()
                            ) {

                                currentAssignmentDialog.dismiss();
                            }

                            selectedFileUri =
                                    null;

                            selectedFileName =
                                    "";

                            selectedFileType =
                                    "";

                            selectedMimeType =
                                    "";

                            loadAssignments();
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Could not save assignment",
                                        Toast.LENGTH_SHORT
                                ).show()
                );
    }

    // =========================================================
    // LOAD ASSIGNMENTS
    // =========================================================

    private void loadAssignments() {

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
                .collection(
                        "students"
                )
                .document(
                        userId
                )
                .collection(
                        "assignments"
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            listAssignments
                                    .removeAllViews();

                            int total = 0;
                            int completed = 0;
                            int pending = 0;

                            if (
                                    snapshot.isEmpty()
                            ) {

                                showEmptyAssignmentState();

                                updateAssignmentSummary(
                                        0,
                                        0,
                                        0
                                );

                                applyThemeMode();

                                return;
                            }

                            for (
                                    var document :
                                    snapshot.getDocuments()
                            ) {

                                String title =
                                        document.getString(
                                                "title"
                                        );

                                if (
                                        title == null
                                                ||
                                                title.isEmpty()
                                ) {

                                    title =
                                            "Untitled Assignment";
                                }

                                String subject =
                                        document.getString(
                                                "subject"
                                        );

                                if (
                                        subject == null
                                ) {

                                    subject = "";
                                }

                                String dueDate =
                                        document.getString(
                                                "dueDate"
                                        );

                                if (
                                        dueDate == null
                                ) {

                                    dueDate = "";
                                }

                                String status =
                                        document.getString(
                                                "status"
                                        );

                                if (
                                        status == null
                                                ||
                                                status.isEmpty()
                                ) {

                                    status =
                                            "Pending";
                                }

                                Long storedMillis =
                                        document.getLong(
                                                "dueDateMillis"
                                        );

                                long dueMillis =
                                        storedMillis != null
                                                ? storedMillis
                                                : parseDate(
                                                dueDate
                                        );

                                String fileUri =
                                        document.getString(
                                                "fileUri"
                                        );

                                String fileName =
                                        document.getString(
                                                "fileName"
                                        );

                                String fileType =
                                        document.getString(
                                                "fileType"
                                        );

                                String mimeType =
                                        document.getString(
                                                "mimeType"
                                        );

                                String storageType =
                                        document.getString(
                                                "storageType"
                                        );

                                if (
                                        fileUri == null
                                ) {
                                    fileUri = "";
                                }

                                if (
                                        fileName == null
                                ) {
                                    fileName = "";
                                }

                                if (
                                        fileType == null
                                ) {
                                    fileType = "";
                                }

                                if (
                                        mimeType == null
                                ) {
                                    mimeType = "";
                                }

                                if (
                                        storageType == null
                                ) {
                                    storageType = "";
                                }

                                // Compatibility with older assignments

                                if (
                                        fileUri.isEmpty()
                                ) {

                                    String oldFileUrl =
                                            document.getString(
                                                    "fileUrl"
                                            );

                                    if (
                                            oldFileUrl != null
                                    ) {

                                        fileUri =
                                                oldFileUrl;
                                    }
                                }

                                total++;

                                if (
                                        status.equalsIgnoreCase(
                                                "Completed"
                                        )
                                ) {

                                    completed++;

                                } else {

                                    pending++;
                                }

                                addAssignmentCard(
                                        document.getId(),
                                        title,
                                        subject,
                                        dueDate,
                                        dueMillis,
                                        status,
                                        fileUri,
                                        fileName,
                                        fileType,
                                        mimeType,
                                        storageType
                                );
                            }

                            updateAssignmentSummary(
                                    total,
                                    completed,
                                    pending
                            );

                            applyThemeMode();
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Unable to load assignments",
                                        Toast.LENGTH_SHORT
                                ).show()
                );
    }

    private void updateAssignmentSummary(
            int total,
            int completed,
            int pending
    ) {

        tvTotalAssignments.setText(
                String.valueOf(
                        total
                )
        );

        tvCompletedAssignments.setText(
                String.valueOf(
                        completed
                )
        );

        tvPendingAssignments.setText(
                String.valueOf(
                        pending
                )
        );
    }

    // =========================================================
    // EMPTY ASSIGNMENT STATE
    // =========================================================

    private void showEmptyAssignmentState() {

        TextView empty =
                new TextView(this);

        empty.setText(
                "No assignments yet.\n\n" +
                        "Tap + ADD to create your first assignment."
        );

        empty.setTextSize(
                14
        );

        empty.setTextColor(
                Color.rgb(
                        100,
                        116,
                        139
                )
        );

        empty.setGravity(
                Gravity.CENTER
        );

        empty.setPadding(
                20,
                35,
                20,
                35
        );

        listAssignments.addView(
                empty
        );
    }

    // =========================================================
    // ASSIGNMENT CARD
    // =========================================================

    private void addAssignmentCard(
            String documentId,
            String title,
            String subject,
            String dueDate,
            long dueMillis,
            String status,
            String fileUri,
            String fileName,
            String fileType,
            String mimeType,
            String storageType
    ) {

        MaterialCardView card =
                new MaterialCardView(this);

        card.setCardBackgroundColor(
                Color.WHITE
        );

        card.setRadius(
                dpToPx(20)
        );

        card.setCardElevation(
                dpToPx(2)
        );

        card.setStrokeWidth(
                dpToPx(1)
        );

        card.setStrokeColor(
                Color.rgb(
                        226,
                        232,
                        240
                )
        );

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                dpToPx(18),
                dpToPx(18),
                dpToPx(18),
                dpToPx(18)
        );

        // TITLE + STATUS

        LinearLayout topRow =
                new LinearLayout(this);

        topRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        topRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView titleView =
                new TextView(this);

        titleView.setText(
                title
        );

        titleView.setTextSize(
                17
        );

        titleView.setTextColor(
                Color.rgb(
                        30,
                        41,
                        59
                )
        );

        titleView.setTypeface(
                null,
                Typeface.BOLD
        );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                );

        topRow.addView(
                titleView,
                titleParams
        );

        TextView statusView =
                new TextView(this);

        boolean isCompleted =
                status.equalsIgnoreCase(
                        "Completed"
                );

        statusView.setText(
                isCompleted
                        ? "✓ COMPLETED"
                        : "● PENDING"
        );

        statusView.setTextSize(
                10
        );

        statusView.setTypeface(
                null,
                Typeface.BOLD
        );

        statusView.setPadding(
                11,
                7,
                11,
                7
        );

        statusView.setTextColor(
                isCompleted
                        ? Color.rgb(
                        22,
                        163,
                        74
                )
                        : Color.rgb(
                        217,
                        119,
                        6
                )
        );

        topRow.addView(
                statusView
        );

        layout.addView(
                topRow
        );

        // SUBJECT

        if (
                !subject.isEmpty()
        ) {

            TextView subjectView =
                    new TextView(this);

            subjectView.setText(
                    "📚 " + subject
            );

            subjectView.setTextSize(
                    13
            );

            subjectView.setTextColor(
                    Color.rgb(
                            100,
                            116,
                            139
                    )
            );

            LinearLayout.LayoutParams subjectParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            subjectParams.topMargin =
                    dpToPx(8);

            layout.addView(
                    subjectView,
                    subjectParams
            );
        }

        // DUE DATE

        TextView dueView =
                new TextView(this);

        String dueStatus =
                getDueStatus(
                        dueDate,
                        dueMillis,
                        status
                );

        dueView.setText(
                dueStatus
        );

        dueView.setTextSize(
                13
        );

        dueView.setTypeface(
                null,
                Typeface.BOLD
        );

        dueView.setTextColor(
                getDueStatusColor(
                        dueStatus
                )
        );

        LinearLayout.LayoutParams dueParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        dueParams.topMargin =
                dpToPx(9);

        layout.addView(
                dueView,
                dueParams
        );

        // ATTACHED FILE

        if (
                !fileName.isEmpty()
                        &&
                        !fileUri.isEmpty()
        ) {

            MaterialCardView fileCard =
                    new MaterialCardView(this);

            fileCard.setCardBackgroundColor(
                    Color.rgb(
                            248,
                            250,
                            252
                    )
            );

            fileCard.setRadius(
                    dpToPx(15)
            );

            fileCard.setCardElevation(
                    0
            );

            fileCard.setStrokeWidth(
                    dpToPx(1)
            );

            fileCard.setStrokeColor(
                    Color.rgb(
                            226,
                            232,
                            240
                    )
            );

            LinearLayout fileLayout =
                    new LinearLayout(this);

            fileLayout.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            fileLayout.setGravity(
                    Gravity.CENTER_VERTICAL
            );

            fileLayout.setPadding(
                    dpToPx(12),
                    dpToPx(12),
                    dpToPx(12),
                    dpToPx(12)
            );

            TextView icon =
                    new TextView(this);

            icon.setText(
                    getFileIcon(
                            fileType
                    )
            );

            icon.setTextSize(
                    22
            );

            fileLayout.addView(
                    icon
            );

            TextView fileText =
                    new TextView(this);

            String displayText =
                    fileName +
                            "\nStored on this device";

            fileText.setText(
                    displayText
            );

            fileText.setTextSize(
                    12
            );

            fileText.setTextColor(
                    Color.rgb(
                            71,
                            85,
                            105
                    )
            );

            fileText.setTypeface(
                    null,
                    Typeface.BOLD
            );

            LinearLayout.LayoutParams fileTextParams =
                    new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1
                    );

            fileTextParams.leftMargin =
                    dpToPx(10);

            fileLayout.addView(
                    fileText,
                    fileTextParams
            );

            MaterialButton openButton =
                    createActionButton(
                            "OPEN FILE",
                            Color.rgb(
                                    37,
                                    99,
                                    235
                            ),
                            Color.rgb(
                                    37,
                                    99,
                                    235
                            )
                    );

            openButton.setTextSize(
                    10
            );

            openButton.setOnClickListener(
                    v -> openFile(
                            fileUri,
                            mimeType,
                            fileName
                    )
            );

            LinearLayout.LayoutParams openParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(115),
                            dpToPx(52)
                    );

            fileLayout.addView(
                    openButton,
                    openParams
            );

            fileCard.addView(
                    fileLayout
            );

            LinearLayout.LayoutParams fileCardParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            fileCardParams.topMargin =
                    dpToPx(12);

            layout.addView(
                    fileCard,
                    fileCardParams
            );
        }

        // COMPLETE / PENDING

        MaterialButton completeButton;

        if (
                isCompleted
        ) {

            completeButton =
                    createActionButton(
                            "↩  MARK AS PENDING",
                            Color.rgb(
                                    180,
                                    83,
                                    9
                            ),
                            Color.rgb(
                                    217,
                                    119,
                                    6
                            )
                    );

        } else {

            completeButton =
                    createActionButton(
                            "✓  MARK AS COMPLETED",
                            Color.rgb(
                                    22,
                                    163,
                                    74
                            ),
                            Color.rgb(
                                    22,
                                    163,
                                    74
                            )
                    );
        }

        completeButton.setOnClickListener(
                v -> updateAssignmentStatus(
                        documentId,
                        status
                )
        );

        LinearLayout.LayoutParams completeParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(60)
                );

        completeParams.topMargin =
                dpToPx(16);

        layout.addView(
                completeButton,
                completeParams
        );

        // DELETE

        MaterialButton deleteButton =
                createActionButton(
                        "🗑  DELETE ASSIGNMENT",
                        Color.rgb(
                                220,
                                38,
                                38
                        ),
                        Color.rgb(
                                220,
                                38,
                                38
                        )
                );

        deleteButton.setOnClickListener(
                v -> confirmDeleteAssignment(
                        documentId,
                        title
                )
        );

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(60)
                );

        deleteParams.topMargin =
                dpToPx(10);

        layout.addView(
                deleteButton,
                deleteParams
        );

        card.addView(
                layout
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin =
                dpToPx(14);

        listAssignments.addView(
                card,
                cardParams
        );
    }

    // =========================================================
    // DUE STATUS
    // =========================================================

    private String getDueStatus(
            String dueDate,
            long dueMillis,
            String status
    ) {

        if (
                status.equalsIgnoreCase(
                        "Completed"
                )
        ) {

            return "✓ Completed";
        }

        if (
                dueDate == null
                        ||
                        dueDate.isEmpty()
                        ||
                        dueMillis == -1
        ) {

            return "📅 No due date";
        }

        Calendar today =
                Calendar.getInstance();

        clearTime(
                today
        );

        Calendar due =
                Calendar.getInstance();

        due.setTimeInMillis(
                dueMillis
        );

        clearTime(
                due
        );

        long difference =
                due.getTimeInMillis()
                        -
                        today.getTimeInMillis();

        long days =
                difference /
                        (
                                24 *
                                        60 *
                                        60 *
                                        1000
                        );

        if (
                days < 0
        ) {

            return "⚠ OVERDUE • " +
                    dueDate;

        } else if (
                days == 0
        ) {

            return "🔥 DUE TODAY • " +
                    dueDate;

        } else if (
                days == 1
        ) {

            return "⏰ 1 DAY LEFT • " +
                    dueDate;

        } else {

            return "📅 " +
                    days +
                    " DAYS LEFT • " +
                    dueDate;
        }
    }

    private int getDueStatusColor(
            String text
    ) {

        if (
                text.startsWith(
                        "⚠"
                )
        ) {

            return Color.rgb(
                    220,
                    38,
                    38
            );

        } else if (
                text.startsWith(
                        "🔥"
                )
        ) {

            return Color.rgb(
                    234,
                    88,
                    12
            );

        } else if (
                text.startsWith(
                        "⏰"
                )
        ) {

            return Color.rgb(
                    180,
                    83,
                    9
            );

        } else if (
                text.startsWith(
                        "✓"
                )
        ) {

            return Color.rgb(
                    22,
                    163,
                    74
            );

        } else {

            return Color.rgb(
                    100,
                    116,
                    139
            );
        }
    }

    // =========================================================
    // UPDATE ASSIGNMENT STATUS
    // =========================================================

    private void updateAssignmentStatus(
            String documentId,
            String currentStatus
    ) {

        if (
                firebaseAuth.getCurrentUser()
                        == null
        ) {
            return;
        }

        String newStatus =
                currentStatus.equalsIgnoreCase(
                        "Completed"
                )
                        ? "Pending"
                        : "Completed";

        String userId =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        firestore
                .collection(
                        "students"
                )
                .document(
                        userId
                )
                .collection(
                        "assignments"
                )
                .document(
                        documentId
                )
                .update(
                        "status",
                        newStatus
                )
                .addOnSuccessListener(
                        unused ->
                                loadAssignments()
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Could not update assignment",
                                        Toast.LENGTH_SHORT
                                ).show()
                );
    }

    // =========================================================
    // DELETE ASSIGNMENT
    // =========================================================

    private void confirmDeleteAssignment(
            String documentId,
            String title
    ) {

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Delete Assignment?"
                        )
                        .setMessage(
                                "Remove \"" +
                                        title +
                                        "\" permanently?"
                        )
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .setPositiveButton(
                                "DELETE",
                                (dialogInterface, which) ->
                                        deleteAssignment(
                                                documentId
                                        )
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    styleDialogWindow(
                            dialog
                    );

                    dialog.getButton(
                            AlertDialog.BUTTON_NEGATIVE
                    ).setTextColor(
                            isDarkModeEnabled()
                                    ? Color.rgb(203, 213, 225)
                                    : Color.rgb(71, 85, 105)
                    );

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setTextColor(
                            Color.rgb(
                                    220,
                                    38,
                                    38
                            )
                    );
                }
        );

        dialog.show();
    }

    private void deleteAssignment(
            String documentId
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
                .collection(
                        "students"
                )
                .document(
                        userId
                )
                .collection(
                        "assignments"
                )
                .document(
                        documentId
                )
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Assignment deleted",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadAssignments();
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Could not delete assignment",
                                        Toast.LENGTH_SHORT
                                ).show()
                );
    }

    // =========================================================
    // ADD NOTE - PROFESSIONAL LIGHT DIALOG
    // =========================================================

    private void showAddNoteDialog() {

        LinearLayout root =
                createDialogRoot();

        TextView icon =
                createDialogIcon(
                        "📝"
                );

        root.addView(
                icon,
                wrapParams(0, 10)
        );

        TextView title =
                createDialogTitle(
                        "Add Note"
                );

        root.addView(
                title,
                wrapParams(14, 0)
        );

        TextView subtitle =
                createDialogSubtitle(
                        "Save a quick study note so you can revisit it later."
                );

        root.addView(
                subtitle,
                wrapParams(5, 0)
        );

        EditText noteInput =
                createModernInput(
                        "Write your study note...",
                        false
                );

        noteInput.setGravity(
                Gravity.TOP |
                        Gravity.START
        );

        noteInput.setMinHeight(
                dpToPx(150)
        );

        noteInput.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        LinearLayout.LayoutParams noteParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(155)
                );

        noteParams.topMargin =
                dpToPx(20);

        root.addView(
                noteInput,
                noteParams
        );

        TextView helper =
                createDialogSubtitle(
                        "Tip: Keep notes short and focused for easier revision."
                );

        root.addView(
                helper,
                wrapParams(8, 0)
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setView(root)
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .setPositiveButton(
                                "SAVE NOTE",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    styleDialogWindow(
                            dialog
                    );

                    dialog.getButton(
                            AlertDialog.BUTTON_NEGATIVE
                    ).setTextColor(
                            isDarkModeEnabled()
                                    ? Color.rgb(203, 213, 225)
                                    : Color.rgb(71, 85, 105)
                    );

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setTextColor(
                            isDarkModeEnabled()
                                    ? Color.rgb(96, 165, 250)
                                    : Color.rgb(37, 99, 235)
                    );

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setOnClickListener(
                            v -> {

                                String note =
                                        noteInput
                                                .getText()
                                                .toString()
                                                .trim();

                                if (
                                        note.isEmpty()
                                ) {

                                    noteInput.setError(
                                            "Enter a note"
                                    );

                                    noteInput.requestFocus();

                                    return;
                                }

                                addNote(
                                        note
                                );

                                dialog.dismiss();
                            }
                    );
                }
        );

        dialog.show();
    }

    // =========================================================
    // SAVE NOTE
    // =========================================================

    private void addNote(
            String note
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

        Map<String, Object> noteData =
                new HashMap<>();

        noteData.put(
                "text",
                note
        );

        noteData.put(
                "createdAt",
                System.currentTimeMillis()
        );

        firestore
                .collection(
                        "students"
                )
                .document(
                        userId
                )
                .collection(
                        "notes"
                )
                .add(
                        noteData
                )
                .addOnSuccessListener(
                        documentReference -> {

                            Toast.makeText(
                                    this,
                                    "Note saved",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadNotes();
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Could not save note",
                                        Toast.LENGTH_SHORT
                                ).show()
                );
    }

    // =========================================================
    // LOAD NOTES
    // =========================================================

    private void loadNotes() {

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
                .collection(
                        "students"
                )
                .document(
                        userId
                )
                .collection(
                        "notes"
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            listNotes.removeAllViews();

                            if (
                                    snapshot.isEmpty()
                            ) {

                                TextView empty =
                                        new TextView(this);

                                empty.setText(
                                        "No notes yet.\n\n" +
                                                "Tap + ADD to save quick study notes."
                                );

                                empty.setTextSize(
                                        14
                                );

                                empty.setTextColor(
                                        Color.rgb(
                                                100,
                                                116,
                                                139
                                        )
                                );

                                empty.setGravity(
                                        Gravity.CENTER
                                );

                                empty.setPadding(
                                        20,
                                        30,
                                        20,
                                        30
                                );

                                listNotes.addView(
                                        empty
                                );

                                applyThemeMode();

                                return;
                            }

                            for (
                                    var document :
                                    snapshot.getDocuments()
                            ) {

                                String note =
                                        document.getString(
                                                "text"
                                        );

                                if (
                                        note == null
                                                ||
                                                note.trim()
                                                        .isEmpty()
                                ) {

                                    continue;
                                }

                                addNoteCard(
                                        document.getId(),
                                        note
                                );
                            }

                            applyThemeMode();
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Unable to load notes",
                                        Toast.LENGTH_SHORT
                                ).show()
                );
    }

    // =========================================================
    // NOTE CARD
    // =========================================================

    private void addNoteCard(
            String documentId,
            String note
    ) {

        MaterialCardView card =
                new MaterialCardView(this);

        card.setCardBackgroundColor(
                Color.WHITE
        );

        card.setRadius(
                dpToPx(18)
        );

        card.setCardElevation(
                dpToPx(2)
        );

        card.setStrokeWidth(
                dpToPx(1)
        );

        card.setStrokeColor(
                Color.rgb(
                        226,
                        232,
                        240
                )
        );

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                dpToPx(17),
                dpToPx(17),
                dpToPx(17),
                dpToPx(17)
        );

        TextView heading =
                new TextView(this);

        heading.setText(
                "📝 Study Note"
        );

        heading.setTextSize(
                14
        );

        heading.setTextColor(
                Color.rgb(
                        30,
                        41,
                        59
                )
        );

        heading.setTypeface(
                null,
                Typeface.BOLD
        );

        layout.addView(
                heading
        );

        TextView noteText =
                new TextView(this);

        noteText.setText(
                note
        );

        noteText.setTextSize(
                14
        );

        noteText.setTextColor(
                Color.rgb(
                        71,
                        85,
                        105
                )
        );

        noteText.setLineSpacing(
                0,
                1.2f
        );

        LinearLayout.LayoutParams noteParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        noteParams.topMargin =
                dpToPx(8);

        layout.addView(
                noteText,
                noteParams
        );

        MaterialButton deleteButton =
                createActionButton(
                        "🗑  DELETE NOTE",
                        Color.rgb(
                                220,
                                38,
                                38
                        ),
                        Color.rgb(
                                220,
                                38,
                                38
                        )
                );

        deleteButton.setTextSize(
                11
        );

        deleteButton.setOnClickListener(
                v -> deleteNote(
                        documentId
                )
        );

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(55)
                );

        buttonParams.topMargin =
                dpToPx(12);

        layout.addView(
                deleteButton,
                buttonParams
        );

        card.addView(
                layout
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin =
                dpToPx(10);

        listNotes.addView(
                card,
                cardParams
        );
    }

    // =========================================================
    // DELETE NOTE
    // =========================================================

    private void deleteNote(
            String documentId
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
                .collection(
                        "students"
                )
                .document(
                        userId
                )
                .collection(
                        "notes"
                )
                .document(
                        documentId
                )
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Note deleted",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadNotes();
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Could not delete note",
                                        Toast.LENGTH_SHORT
                                ).show()
                );
    }

    // =========================================================
    // FILE NAME
    // =========================================================

    private String getFileName(
            Uri uri
    ) {

        String result = null;

        try {

            android.database.Cursor cursor =
                    getContentResolver().query(
                            uri,
                            null,
                            null,
                            null,
                            null
                    );

            if (
                    cursor != null
            ) {

                int nameIndex =
                        cursor.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME
                        );

                if (
                        nameIndex >= 0
                                &&
                                cursor.moveToFirst()
                ) {

                    result =
                            cursor.getString(
                                    nameIndex
                            );
                }

                cursor.close();
            }

        } catch (Exception ignored) {
        }

        if (
                result == null
        ) {

            result =
                    uri.getLastPathSegment();
        }

        if (
                result == null
                        ||
                        result.isEmpty()
        ) {

            result =
                    "study_material";
        }

        return result;
    }

    // =========================================================
    // FILE EXTENSION
    // =========================================================

    private String getFileExtension(
            String fileName
    ) {

        if (
                fileName == null
                        ||
                        !fileName.contains(".")
        ) {

            return "";
        }

        return fileName
                .substring(
                        fileName.lastIndexOf(".") + 1
                )
                .toLowerCase(
                        Locale.getDefault()
                );
    }

    // =========================================================
    // MIME TYPE
    // =========================================================

    private String getMimeTypeFromUri(
            Uri uri
    ) {

        String mime =
                getContentResolver()
                        .getType(uri);

        if (
                mime != null
                        &&
                        !mime.isEmpty()
        ) {

            return mime;
        }

        return getMimeTypeFromExtension(
                getFileExtension(
                        getFileName(uri)
                )
        );
    }

    private String getMimeTypeFromExtension(
            String extension
    ) {

        switch (extension) {

            case "pdf":
                return "application/pdf";

            case "doc":
                return "application/msword";

            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            case "jpg":
            case "jpeg":
                return "image/jpeg";

            case "png":
                return "image/png";

            case "gif":
                return "image/gif";

            case "webp":
                return "image/webp";

            default:
                return "application/octet-stream";
        }
    }

    // =========================================================
    // FILE ICON
    // =========================================================

    private String getFileIcon(
            String extension
    ) {

        if (
                extension == null
        ) {

            return "📎";
        }

        switch (extension) {

            case "pdf":
                return "📄";

            case "doc":
            case "docx":
                return "📝";

            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "webp":
                return "🖼";

            default:
                return "📎";
        }
    }

    // =========================================================
    // OPEN FILE
    // =========================================================

    private void openFile(
            String fileUriString,
            String mimeType,
            String fileName
    ) {

        try {

            Uri uri =
                    Uri.parse(
                            fileUriString
                    );

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW
                    );

            intent.setDataAndType(
                    uri,
                    mimeType == null
                            ||
                            mimeType.isEmpty()
                            ? "*/*"
                            : mimeType
            );

            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            startActivity(
                    intent
            );

        } catch (
                ActivityNotFoundException e
        ) {

            Toast.makeText(
                    this,
                    "No app is available to open " +
                            fileName,
                    Toast.LENGTH_LONG
            ).show();

        } catch (
                Exception e
        ) {

            Toast.makeText(
                    this,
                    "Unable to open file",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =========================================================
    // PARSE DATE
    // =========================================================

    private long parseDate(
            String dateString
    ) {

        if (
                dateString == null
                        ||
                        dateString.trim().isEmpty()
        ) {

            return -1;
        }

        try {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "dd-MM-yyyy",
                            Locale.getDefault()
                    );

            format.setLenient(
                    false
            );

            Date date =
                    format.parse(
                            dateString.trim()
                    );

            if (
                    date == null
            ) {

                return -1;
            }

            Calendar calendar =
                    Calendar.getInstance();

            calendar.setTime(
                    date
            );

            calendar.set(
                    Calendar.HOUR_OF_DAY,
                    23
            );

            calendar.set(
                    Calendar.MINUTE,
                    59
            );

            calendar.set(
                    Calendar.SECOND,
                    59
            );

            calendar.set(
                    Calendar.MILLISECOND,
                    999
            );

            return calendar.getTimeInMillis();

        } catch (
                Exception e
        ) {

            return -1;
        }
    }

    // =========================================================
    // CLEAR TIME
    // =========================================================

    private void clearTime(
            Calendar calendar
    ) {

        calendar.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        calendar.set(
                Calendar.MINUTE,
                0
        );

        calendar.set(
                Calendar.SECOND,
                0
        );

        calendar.set(
                Calendar.MILLISECOND,
                0
        );
    }

    // =========================================================
    // LIGHT / DARK MODE
    // =========================================================

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
                        ? Color.rgb(
                        11,
                        17,
                        32
                )
                        : Color.rgb(
                        248,
                        250,
                        252
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

        final int darkCard =
                Color.rgb(
                        17,
                        24,
                        39
                );

        final int darkText =
                Color.rgb(
                        248,
                        250,
                        252
                );

        final int darkSecondary =
                Color.rgb(
                        203,
                        213,
                        225
                );

        final int darkMuted =
                Color.rgb(
                        148,
                        163,
                        184
                );

        final int darkBorder =
                Color.rgb(
                        51,
                        65,
                        85
                );

        final int darkPrimary =
                Color.rgb(
                        96,
                        165,
                        250
                );

        final int darkPrimaryDark =
                Color.rgb(
                        59,
                        130,
                        246
                );

        final int darkSoftBlue =
                Color.rgb(
                        23,
                        37,
                        84
                );

        final int darkSoftGreen =
                Color.rgb(
                        20,
                        83,
                        45
                );

        final int darkSoftOrange =
                Color.rgb(
                        120,
                        53,
                        15
                );

        final int darkSoftRed =
                Color.rgb(
                        127,
                        29,
                        29
                );

        final int darkSoftGray =
                Color.rgb(
                        30,
                        41,
                        59
                );

        if (view instanceof MaterialCardView) {

            MaterialCardView card =
                    (MaterialCardView) view;

            int current =
                    card.getCardBackgroundColor()
                            .getDefaultColor();

            if (current ==
                    Color.rgb(
                            37,
                            99,
                            235
                    )
                    || current ==
                    Color.rgb(
                            29,
                            78,
                            216
                    )
                    || current ==
                    Color.rgb(
                            96,
                            165,
                            250
                    )) {

                card.setCardBackgroundColor(
                        darkPrimaryDark
                );

            } else if (current ==
                    Color.rgb(
                            239,
                            246,
                            255
                    )) {

                card.setCardBackgroundColor(
                        darkSoftBlue
                );

            } else if (current ==
                    Color.rgb(
                            240,
                            253,
                            244
                    )) {

                card.setCardBackgroundColor(
                        darkSoftGreen
                );

            } else if (current ==
                    Color.rgb(
                            255,
                            247,
                            237
                    )) {

                card.setCardBackgroundColor(
                        darkSoftOrange
                );

            } else if (current ==
                    Color.rgb(
                            254,
                            242,
                            242
                    )) {

                card.setCardBackgroundColor(
                        darkSoftRed
                );

            } else {

                card.setCardBackgroundColor(
                        darkCard
                );
            }

            card.setStrokeColor(
                    darkBorder
            );

            card.setStrokeWidth(
                    dpToPx(1)
            );
        }

        if (view instanceof MaterialButton) {

            MaterialButton button =
                    (MaterialButton) view;

            int current =
                    button.getBackgroundTintList() == null
                            ? Color.TRANSPARENT
                            : button.getBackgroundTintList()
                            .getDefaultColor();

            if (current ==
                    Color.rgb(
                            37,
                            99,
                            235
                    )
                    || current ==
                    Color.rgb(
                            29,
                            78,
                            216
                    )
                    || current ==
                    Color.rgb(
                            96,
                            165,
                            250
                    )) {

                button.setBackgroundTintList(
                        ColorStateList.valueOf(
                                darkPrimaryDark
                        )
                );

                button.setTextColor(
                        Color.WHITE
                );

            } else if (current ==
                    Color.rgb(
                            239,
                            246,
                            255
                    )) {

                button.setBackgroundTintList(
                        ColorStateList.valueOf(
                                darkSoftBlue
                        )
                );

                button.setTextColor(
                        darkPrimary
                );

            } else if (current ==
                    Color.rgb(
                            241,
                            245,
                            249
                    )) {

                button.setBackgroundTintList(
                        ColorStateList.valueOf(
                                darkSoftGray
                        )
                );

                button.setTextColor(
                        darkSecondary
                );

            } else {

                button.setBackgroundTintList(
                        ColorStateList.valueOf(
                                darkCard
                        )
                );

                button.setTextColor(
                        darkText
                );

                button.setStrokeColor(
                        ColorStateList.valueOf(
                                darkBorder
                        )
                );

                button.setStrokeWidth(
                        dpToPx(1)
                );
            }
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
                && !(view instanceof MaterialButton)) {

            TextView text =
                    (TextView) view;

            int current =
                    text.getCurrentTextColor();

            if (current == Color.WHITE
                    || current ==
                    Color.rgb(
                            15,
                            23,
                            42
                    )
                    || current ==
                    Color.rgb(
                            30,
                            41,
                            59
                    )
                    || current ==
                    Color.rgb(
                            51,
                            65,
                            85
                    )
                    || current ==
                    Color.BLACK) {

                text.setTextColor(
                        darkText
                );

            } else if (current ==
                    Color.rgb(
                            71,
                            85,
                            105
                    )
                    || current ==
                    Color.rgb(
                            100,
                            116,
                            139
                    )) {

                text.setTextColor(
                        darkSecondary
                );

            } else if (current ==
                    Color.rgb(
                            148,
                            163,
                            184
                    )) {

                text.setTextColor(
                        darkMuted
                );

            } else if (current ==
                    Color.rgb(
                            37,
                            99,
                            235
                    )) {

                text.setTextColor(
                        darkPrimary
                );

            } else if (current ==
                    Color.rgb(
                            22,
                            163,
                            74
                    )) {

                text.setTextColor(
                        Color.rgb(
                                74,
                                222,
                                128
                        )
                );

            } else if (current ==
                    Color.rgb(
                            217,
                            119,
                            6
                    )) {

                text.setTextColor(
                        Color.rgb(
                                251,
                                191,
                                36
                        )
                );

            } else if (current ==
                    Color.rgb(
                            220,
                            38,
                            38
                    )) {

                text.setTextColor(
                        Color.rgb(
                                248,
                                113,
                                113
                        )
                );

            } else if (current ==
                    Color.rgb(
                            22,
                            101,
                            52
                    )) {

                text.setTextColor(
                        Color.rgb(
                                134,
                                239,
                                172
                        )
                );
            }
        }

        if (view.getBackground() instanceof ColorDrawable) {

            int backgroundColor =
                    ((ColorDrawable) view.getBackground())
                            .getColor();

            if (backgroundColor == Color.WHITE) {

                view.setBackgroundColor(
                        darkCard
                );

            } else if (backgroundColor == Color.rgb(248, 250, 252)) {

                view.setBackgroundColor(
                        Color.rgb(11, 17, 32)
                );
            }
        }

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

    // =========================================================
    // DP TO PX
    // =========================================================

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
