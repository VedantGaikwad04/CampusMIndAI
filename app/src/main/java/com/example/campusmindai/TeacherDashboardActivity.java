package com.example.campusmindai;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Toast;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class TeacherDashboardActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private int primaryBlue;
    private int backgroundColor;
    private int cardColor;
    private int textColor;
    private int secondaryTextColor;
    private int mutedTextColor;
    private int borderColor;
    private int softBlue;
    private int errorColor;
    private int softRed;

    private TextView tvTeacherName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // =========================================================
        // COLORS
        // =========================================================

        loadColors();

        // =========================================================
        // FIREBASE
        // =========================================================

        firebaseAuth =
                FirebaseAuth.getInstance();

        firestore =
                FirebaseFirestore.getInstance();

        // =========================================================
        // CREATE UI
        // =========================================================

        createDashboard();

        // =========================================================
        // VERIFY TEACHER + LOAD NAME
        // =========================================================

        verifyTeacher();
    }

    // =============================================================
    // CREATE DASHBOARD
    // =============================================================

    private void createDashboard() {

        ScrollView scrollView =
                new ScrollView(this);

        scrollView.setFillViewport(true);

        scrollView.setBackgroundColor(
                backgroundColor
        );

        LinearLayout main =
                new LinearLayout(this);

        main.setOrientation(
                LinearLayout.VERTICAL
        );

        main.setPadding(
                dp(20),
                dp(24),
                dp(20),
                dp(30)
        );

        scrollView.addView(main);

        // =========================================================
        // HEADER
        // =========================================================

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        LinearLayout headerText =
                new LinearLayout(this);

        headerText.setOrientation(
                LinearLayout.VERTICAL
        );

        tvTeacherName =
                new TextView(this);

        tvTeacherName.setText(
                "Welcome, Teacher"
        );

        tvTeacherName.setTextColor(
                textColor
        );

        tvTeacherName.setTextSize(
                22
        );

        tvTeacherName.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        TextView headerSubtitle =
                new TextView(this);

        headerSubtitle.setText(
                "Teacher Dashboard"
        );

        headerSubtitle.setTextColor(
                secondaryTextColor
        );

        headerSubtitle.setTextSize(
                13
        );

        headerSubtitle.setPadding(
                0,
                dp(4),
                0,
                0
        );

        headerText.addView(
                tvTeacherName
        );

        headerText.addView(
                headerSubtitle
        );

        header.addView(
                headerText,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        // =========================================================
        // LOGOUT
        // =========================================================

        MaterialButton logoutButton =
                new MaterialButton(this);

        logoutButton.setText(
                "LOG OUT"
        );

        logoutButton.setAllCaps(
                false
        );

        logoutButton.setTextSize(
                12
        );

        logoutButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        logoutButton.setTextColor(
                errorColor
        );

        logoutButton.setBackgroundTintList(
                ColorStateList.valueOf(
                        softRed
                )
        );

        logoutButton.setCornerRadius(
                dp(12)
        );

        logoutButton.setInsetTop(0);
        logoutButton.setInsetBottom(0);

        logoutButton.setOnClickListener(
                v -> showLogoutDialog()
        );

        header.addView(
                logoutButton,
                new LinearLayout.LayoutParams(
                        dp(90),
                        dp(44)
                )
        );

        main.addView(
                header
        );

        // =========================================================
        // WELCOME CARD
        // =========================================================

        addSpace(
                main,
                dp(24)
        );

        MaterialCardView welcomeCard =
                createCard();

        LinearLayout welcomeContent =
                new LinearLayout(this);

        welcomeContent.setOrientation(
                LinearLayout.VERTICAL
        );

        welcomeContent.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
        );

        TextView welcomeTitle =
                createText(
                        "👨‍🏫  Teacher Portal",
                        18,
                        textColor,
                        Typeface.BOLD
                );

        TextView welcomeDescription =
                createText(
                        "Manage classroom attendance and conduct verified attendance sessions.",
                        13,
                        secondaryTextColor,
                        Typeface.NORMAL
                );

        welcomeDescription.setPadding(
                0,
                dp(8),
                0,
                0
        );

        welcomeContent.addView(
                welcomeTitle
        );

        welcomeContent.addView(
                welcomeDescription
        );

        welcomeCard.addView(
                welcomeContent
        );

        main.addView(
                welcomeCard
        );

        // =========================================================
        // ATTENDANCE
        // =========================================================

        addSectionTitle(
                main,
                "ATTENDANCE"
        );

        // =========================================================
        // START ATTENDANCE
        // =========================================================

        MaterialCardView startAttendance =
                createNavigationCard(
                        "📋",
                        "Start Attendance",
                        "Start a new classroom attendance session"
                );

        startAttendance.setOnClickListener(
                v -> openAttendance()
        );

        main.addView(
                startAttendance
        );

        // =========================================================
        // LIVE ATTENDANCE
        // =========================================================

        MaterialCardView liveAttendance =
                createNavigationCard(
                        "🟢",
                        "Live Attendance",
                        "Open your current attendance session"
                );

        liveAttendance.setOnClickListener(
                v -> {
                    Intent intent =
                            new Intent(
                                    TeacherDashboardActivity.this,
                                    TeacherLiveAttendanceActivity.class
                            );

                    startActivity(intent);
                }
        );

        LinearLayout.LayoutParams liveParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        liveParams.topMargin = dp(12);

        main.addView(liveAttendance, liveParams);


        // =========================================================
        // ATTENDANCE HISTORY
        // =========================================================

        MaterialCardView attendanceHistory =
                createNavigationCard(
                        "📊",
                        "Attendance History",
                        "View previous attendance records"
                );

        attendanceHistory.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    TeacherDashboardActivity.this,
                                    TeacherAttendanceHistoryActivity.class
                            );

                    startActivity(intent);
                }
        );
        LinearLayout.LayoutParams historyParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        historyParams.topMargin =
                dp(12);

        main.addView(
                attendanceHistory,
                historyParams
        );

        // =========================================================
        // APP
        // =========================================================

        addSectionTitle(
                main,
                "APP"
        );

        MaterialCardView settingsCard =
                createNavigationCard(
                        "⚙️",
                        "Settings",
                        "Appearance and application preferences"
                );

        settingsCard.setOnClickListener(
                v -> {

                    startActivity(
                            new Intent(
                                    TeacherDashboardActivity.this,
                                    TeacherSettingsActivity.class
                            )
                    );
                }
        );

        main.addView(
                settingsCard
        );

        // =========================================================
        // FOOTER
        // =========================================================

        TextView footer =
                new TextView(this);

        footer.setText(
                "CampusMind AI\nTeacher Portal"
        );

        footer.setTextColor(
                mutedTextColor
        );

        footer.setTextSize(
                12
        );

        footer.setGravity(
                Gravity.CENTER
        );

        footer.setPadding(
                0,
                dp(30),
                0,
                0
        );

        main.addView(
                footer,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(
                scrollView
        );
    }

    // =============================================================
    // OPEN ATTENDANCE
    // =============================================================

    private void openAttendance() {

        Intent intent =
                new Intent(
                        TeacherDashboardActivity.this,
                        TeacherAttendanceActivity.class
                );

        startActivity(intent);
    }

    // =============================================================
    // VERIFY TEACHER
    // =============================================================

    private void verifyTeacher() {

        if (firebaseAuth.getCurrentUser() == null) {

            goToRoleSelection();

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
                .addOnSuccessListener(
                        document -> {

                            String role =
                                    document.getString(
                                            "role"
                                    );

                            // -------------------------------------------------
                            // NOT A TEACHER
                            // -------------------------------------------------

                            if (role == null
                                    || !role.equalsIgnoreCase("Teacher")) {

                                firebaseAuth.signOut();

                                goToRoleSelection();

                                return;
                            }

                            // -------------------------------------------------
                            // LOAD NAME
                            // -------------------------------------------------

                            String name =
                                    document.getString(
                                            "name"
                                    );

                            if (name != null
                                    && !name.trim().isEmpty()
                                    && tvTeacherName != null) {

                                tvTeacherName.setText(
                                        "Welcome, " + name
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Unable to verify teacher account",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =============================================================
    // GO TO ROLE SELECTION
    // =============================================================

    private void goToRoleSelection() {

        Intent intent =
                new Intent(
                        TeacherDashboardActivity.this,
                        RoleSelectionActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(
                intent
        );

        finish();
    }

    // =============================================================
    // LOGOUT DIALOG
    // =============================================================

    private void showLogoutDialog() {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Log Out?"
                )
                .setMessage(
                        "Are you sure you want to log out?"
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "LOG OUT",
                        (dialog, which) -> {

                            firebaseAuth.signOut();

                            goToRoleSelection();
                        }
                )
                .show();
    }

    // =============================================================
    // HISTORY MESSAGE
    // =============================================================

    private void showHistoryMessage() {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Attendance History"
                )
                .setMessage(
                        "Attendance history will be connected to the teacher's previous sessions next."
                )
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    // =============================================================
    // CREATE CARD
    // =============================================================

    private MaterialCardView createCard() {

        MaterialCardView card =
                new MaterialCardView(this);

        card.setCardBackgroundColor(
                ColorStateList.valueOf(
                        cardColor
                )
        );

        card.setStrokeColor(
                ColorStateList.valueOf(
                        borderColor
                )
        );

        card.setStrokeWidth(
                dp(1)
        );

        card.setRadius(
                dp(18)
        );

        card.setCardElevation(
                dp(2)
        );

        return card;
    }

    // =============================================================
    // NAVIGATION CARD
    // =============================================================

    private MaterialCardView createNavigationCard(
            String icon,
            String title,
            String description
    ) {

        MaterialCardView card =
                createCard();

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );

        TextView iconView =
                new TextView(this);

        iconView.setText(
                icon
        );

        iconView.setTextSize(
                25
        );

        iconView.setGravity(
                Gravity.CENTER
        );

        iconView.setBackground(
                createRoundedBackground(
                        softBlue,
                        dp(14)
                )
        );

        row.addView(
                iconView,
                new LinearLayout.LayoutParams(
                        dp(54),
                        dp(54)
                )
        );

        LinearLayout textColumn =
                new LinearLayout(this);

        textColumn.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView titleView =
                createText(
                        title,
                        16,
                        textColor,
                        Typeface.BOLD
                );

        TextView descriptionView =
                createText(
                        description,
                        12,
                        secondaryTextColor,
                        Typeface.NORMAL
                );

        descriptionView.setPadding(
                0,
                dp(4),
                0,
                0
        );

        textColumn.addView(
                titleView
        );

        textColumn.addView(
                descriptionView
        );

        LinearLayout.LayoutParams textParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                );

        textParams.leftMargin =
                dp(14);

        row.addView(
                textColumn,
                textParams
        );

        TextView arrow =
                new TextView(this);

        arrow.setText(
                "›"
        );

        arrow.setTextColor(
                mutedTextColor
        );

        arrow.setTextSize(
                28
        );

        arrow.setGravity(
                Gravity.CENTER
        );

        row.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(28),
                        dp(45)
                )
        );

        card.addView(
                row
        );

        return card;
    }

    // =============================================================
    // SECTION TITLE
    // =============================================================

    private void addSectionTitle(
            LinearLayout parent,
            String title
    ) {

        TextView section =
                new TextView(this);

        section.setText(
                title
        );

        section.setTextColor(
                mutedTextColor
        );

        section.setTextSize(
                12
        );

        section.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        section.setLetterSpacing(
                0.08f
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin =
                dp(26);

        params.bottomMargin =
                dp(10);

        parent.addView(
                section,
                params
        );
    }

    // =============================================================
    // TEXT
    // =============================================================

    private TextView createText(
            String text,
            float size,
            int color,
            int style
    ) {

        TextView view =
                new TextView(this);

        view.setText(
                text
        );

        view.setTextSize(
                size
        );

        view.setTextColor(
                color
        );

        view.setTypeface(
                Typeface.DEFAULT,
                style
        );

        return view;
    }

    // =============================================================
    // BACKGROUND
    // =============================================================

    private android.graphics.drawable.GradientDrawable
    createRoundedBackground(
            int color,
            int radius
    ) {

        android.graphics.drawable.GradientDrawable drawable =
                new android.graphics.drawable.GradientDrawable();

        drawable.setColor(
                color
        );

        drawable.setCornerRadius(
                radius
        );

        return drawable;
    }

    // =============================================================
    // COLORS
    // =============================================================

    private void loadColors() {

        primaryBlue =
                getColor(R.color.primary);

        backgroundColor =
                getColor(R.color.app_background);

        cardColor =
                getColor(R.color.app_card);

        textColor =
                getColor(R.color.app_text);

        secondaryTextColor =
                getColor(R.color.app_secondary_text);

        mutedTextColor =
                getColor(R.color.app_muted_text);

        borderColor =
                getColor(R.color.app_border);

        softBlue =
                getColor(R.color.app_blue_soft);

        errorColor =
                getColor(R.color.error);

        if (isDarkMode()) {

            softRed =
                    Color.rgb(
                            69,
                            10,
                            10
                    );

        } else {

            softRed =
                    Color.rgb(
                            254,
                            242,
                            242
                    );
        }
    }

    // =============================================================
    // DARK MODE
    // =============================================================

    private boolean isDarkMode() {

        int mode =
                getResources()
                        .getConfiguration()
                        .uiMode
                        & android.content.res.Configuration
                        .UI_MODE_NIGHT_MASK;

        return mode ==
                android.content.res.Configuration
                        .UI_MODE_NIGHT_YES;
    }

    // =============================================================
    // SPACE
    // =============================================================

    private void addSpace(
            LinearLayout parent,
            int height
    ) {

        View space =
                new View(this);

        parent.addView(
                space,
                new LinearLayout.LayoutParams(
                        1,
                        height
                )
        );
    }

    // =============================================================
    // DP
    // =============================================================

    private int dp(int value) {

        return (int) (
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
        );
    }
}
