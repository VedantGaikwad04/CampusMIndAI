package com.example.campusmindai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private SharedPreferences preferences;

    private static final String PREFS_NAME =
            "CampusMindSettings";

    private static final String DARK_MODE_KEY =
            "dark_mode";

    private static final String NOTIFICATIONS_KEY =
            "notifications_enabled";

    // ============================================================
    // LIGHT COLORS
    // ============================================================

    private static final int LIGHT_BLUE =
            Color.rgb(37, 99, 235);

    private static final int LIGHT_BLUE_SOFT =
            Color.rgb(239, 246, 255);

    private static final int LIGHT_TEXT =
            Color.rgb(15, 23, 42);

    private static final int LIGHT_SECONDARY =
            Color.rgb(71, 85, 105);

    private static final int LIGHT_MUTED =
            Color.rgb(100, 116, 139);

    private static final int LIGHT_BORDER =
            Color.rgb(226, 232, 240);

    private static final int LIGHT_BG =
            Color.rgb(248, 250, 252);

    private static final int LIGHT_CARD =
            Color.WHITE;

    // ============================================================
    // DARK COLORS
    // ============================================================

    private static final int DARK_BLUE =
            Color.rgb(96, 165, 250);

    private static final int DARK_BLUE_SOFT =
            Color.rgb(23, 37, 84);

    private static final int DARK_TEXT =
            Color.rgb(248, 250, 252);

    private static final int DARK_SECONDARY =
            Color.rgb(203, 213, 225);

    private static final int DARK_MUTED =
            Color.rgb(148, 163, 184);

    private static final int DARK_BORDER =
            Color.rgb(51, 65, 85);

    private static final int DARK_BG =
            Color.rgb(11, 17, 32);

    private static final int DARK_CARD =
            Color.rgb(17, 24, 39);

    // ============================================================
    // CURRENT THEME COLORS
    // ============================================================

    private int BLUE;
    private int BLUE_SOFT;
    private int TEXT;
    private int SECONDARY;
    private int MUTED;
    private int BORDER;
    private int BG;
    private int CARD;

    private final int RED =
            Color.rgb(220, 38, 38);

    private final int RED_SOFT =
            Color.rgb(254, 242, 242);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth =
                FirebaseAuth.getInstance();

        preferences =
                getSharedPreferences(
                        PREFS_NAME,
                        MODE_PRIVATE
                );

        applyThemeColors();

        createSettingsScreen();
    }

    // ============================================================
    // THEME COLORS
    // ============================================================

    private void applyThemeColors() {

        int nightMode =
                AppCompatDelegate.getDefaultNightMode();

        boolean darkMode;

        if (nightMode ==
                AppCompatDelegate.MODE_NIGHT_YES) {

            darkMode = true;

        } else if (nightMode ==
                AppCompatDelegate.MODE_NIGHT_NO) {

            darkMode = false;

        } else {

            darkMode =
                    preferences.getBoolean(
                            DARK_MODE_KEY,
                            false
                    );
        }

        if (darkMode) {

            BLUE = DARK_BLUE;
            BLUE_SOFT = DARK_BLUE_SOFT;
            TEXT = DARK_TEXT;
            SECONDARY = DARK_SECONDARY;
            MUTED = DARK_MUTED;
            BORDER = DARK_BORDER;
            BG = DARK_BG;
            CARD = DARK_CARD;

        } else {

            BLUE = LIGHT_BLUE;
            BLUE_SOFT = LIGHT_BLUE_SOFT;
            TEXT = LIGHT_TEXT;
            SECONDARY = LIGHT_SECONDARY;
            MUTED = LIGHT_MUTED;
            BORDER = LIGHT_BORDER;
            BG = LIGHT_BG;
            CARD = LIGHT_CARD;
        }
    }

    // ============================================================
    // SETTINGS SCREEN
    // ============================================================

    private void createSettingsScreen() {

        ScrollView scrollView =
                new ScrollView(this);

        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(BG);

        LinearLayout main =
                new LinearLayout(this);

        main.setOrientation(
                LinearLayout.VERTICAL
        );

        main.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(30)
        );

        scrollView.addView(main);

        // ========================================================
        // HEADER
        // ========================================================

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView back =
                new TextView(this);

        back.setText("‹");
        back.setTextColor(BLUE);
        back.setTextSize(38);
        back.setGravity(Gravity.CENTER);
        back.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        back.setOnClickListener(
                v -> finish()
        );

        header.addView(
                back,
                new LinearLayout.LayoutParams(
                        dp(45),
                        dp(50)
                )
        );

        LinearLayout titleBox =
                new LinearLayout(this);

        titleBox.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView title =
                new TextView(this);

        title.setText("Settings");
        title.setTextColor(TEXT);
        title.setTextSize(25);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        titleBox.addView(title);

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "Manage your CampusMind experience"
        );

        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(12);

        LinearLayout.LayoutParams
                subtitleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.topMargin =
                dp(2);

        titleBox.addView(
                subtitle,
                subtitleParams
        );

        header.addView(
                titleBox,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        main.addView(header);

        // ========================================================
        // ACCOUNT
        // ========================================================

        addSectionTitle(
                main,
                "ACCOUNT"
        );

        MaterialCardView profileCard =
                createNavigationCard(
                        "👤",
                        "Profile",
                        "Manage your name, college and course"
                );

        profileCard.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                this,
                                ProfileActivity.class
                        )
                )
        );

        addCard(
                main,
                profileCard
        );

        // ========================================================
        // APP PREFERENCES
        // ========================================================

        addSectionTitle(
                main,
                "APP PREFERENCES"
        );

        // --------------------------------------------------------
        // NOTIFICATIONS
        // --------------------------------------------------------

        MaterialCardView notificationCard =
                createCard();

        LinearLayout notificationRow =
                createRow();

        addIcon(
                notificationRow,
                "🔔"
        );

        LinearLayout notificationText =
                createTextColumn(
                        "Notifications",
                        "Control app notification preference"
                );

        notificationRow.addView(
                notificationText,
                createWeightParams()
        );

        Switch notificationSwitch =
                new Switch(this);

        notificationSwitch.setChecked(
                preferences.getBoolean(
                        NOTIFICATIONS_KEY,
                        true
                )
        );

        notificationSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    preferences
                            .edit()
                            .putBoolean(
                                    NOTIFICATIONS_KEY,
                                    isChecked
                            )
                            .apply();

                    Toast.makeText(
                            this,
                            isChecked
                                    ? "Notifications enabled"
                                    : "Notifications disabled",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        notificationRow.addView(
                notificationSwitch
        );

        notificationCard.addView(
                notificationRow
        );

        addCard(
                main,
                notificationCard
        );

        // --------------------------------------------------------
        // DARK MODE
        // --------------------------------------------------------

        MaterialCardView themeCard =
                createCard();

        LinearLayout themeRow =
                createRow();

        addIcon(
                themeRow,
                "🌙"
        );

        LinearLayout themeText =
                createTextColumn(
                        "Dark Mode",
                        "Switch between light and dark appearance"
                );

        themeRow.addView(
                themeText,
                createWeightParams()
        );

        Switch themeSwitch =
                new Switch(this);

        boolean darkMode =
                preferences.getBoolean(
                        DARK_MODE_KEY,
                        false
                );

        themeSwitch.setChecked(
                darkMode
        );

        themeSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (!buttonView.isPressed()) {
                        return;
                    }

                    preferences
                            .edit()
                            .putBoolean(
                                    DARK_MODE_KEY,
                                    isChecked
                            )
                            .apply();

                    if (isChecked) {

                        AppCompatDelegate
                                .setDefaultNightMode(
                                        AppCompatDelegate
                                                .MODE_NIGHT_YES
                                );

                    } else {

                        AppCompatDelegate
                                .setDefaultNightMode(
                                        AppCompatDelegate
                                                .MODE_NIGHT_NO
                                );
                    }
                }
        );

        themeRow.addView(
                themeSwitch
        );

        themeCard.addView(
                themeRow
        );

        addCard(
                main,
                themeCard
        );

        // ========================================================
        // ACADEMIC
        // ========================================================

        addSectionTitle(
                main,
                "ACADEMIC"
        );

        MaterialCardView subjectsCard =
                createNavigationCard(
                        "📚",
                        "Manage Subjects",
                        "Add, view or remove your subjects"
                );

        subjectsCard.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                this,
                                SubjectsActivity.class
                        )
                )
        );

        addCard(
                main,
                subjectsCard
        );

        MaterialCardView attendanceCard =
                createNavigationCard(
                        "📊",
                        "Attendance",
                        "View your attendance records"
                );

        attendanceCard.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                this,
                                AttendanceActivity.class
                        )
                )
        );

        addCard(
                main,
                attendanceCard
        );

        // ========================================================
        // AI ASSISTANT
        // ========================================================

        addSectionTitle(
                main,
                "AI ASSISTANT"
        );

        MaterialCardView aiCard =
                createNavigationCard(
                        "🤖",
                        "AI Study Assistant",
                        "Open CampusMind AI study assistant"
                );

        aiCard.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                this,
                                AIActivity.class
                        )
                )
        );

        addCard(
                main,
                aiCard
        );

        // ========================================================
        // ABOUT
        // ========================================================

        addSectionTitle(
                main,
                "ABOUT"
        );

        MaterialCardView aboutCard =
                createNavigationCard(
                        "ℹ️",
                        "About CampusMind AI",
                        "Application information"
                );

        aboutCard.setOnClickListener(
                v -> showAboutDialog()
        );

        addCard(
                main,
                aboutCard
        );

        MaterialCardView versionCard =
                createCard();

        LinearLayout versionRow =
                createRow();

        addIcon(
                versionRow,
                "📱"
        );

        LinearLayout versionText =
                createTextColumn(
                        "App Version",
                        "CampusMind AI"
                );

        versionRow.addView(
                versionText,
                createWeightParams()
        );

        TextView version =
                new TextView(this);

        version.setText("1.0");
        version.setTextColor(MUTED);
        version.setTextSize(13);
        version.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        versionRow.addView(version);

        versionCard.addView(versionRow);

        addCard(
                main,
                versionCard
        );

        // ========================================================
        // LOGOUT
        // ========================================================

        addSectionTitle(
                main,
                "ACCOUNT ACTIONS"
        );

        MaterialButton logoutButton =
                new MaterialButton(this);

        logoutButton.setText(
                "🚪  LOG OUT"
        );

        logoutButton.setTextColor(
                RED
        );

        logoutButton.setTextSize(14);

        logoutButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        logoutButton.setBackgroundTintList(
                ColorStateList.valueOf(
                        RED_SOFT
                )
        );

        logoutButton.setCornerRadius(
                dp(15)
        );

        logoutButton.setMinHeight(
                dp(56)
        );

        logoutButton.setInsetTop(0);
        logoutButton.setInsetBottom(0);

        logoutButton.setOnClickListener(
                v -> showLogoutDialog()
        );

        LinearLayout.LayoutParams
                logoutParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(56)
                );

        logoutParams.topMargin =
                dp(5);

        main.addView(
                logoutButton,
                logoutParams
        );

        // ========================================================
        // FOOTER
        // ========================================================

        TextView footer =
                new TextView(this);

        footer.setText(
                "CampusMind AI\nYour Academic Companion"
        );

        footer.setTextColor(MUTED);
        footer.setTextSize(12);
        footer.setGravity(
                Gravity.CENTER
        );

        footer.setLineSpacing(
                2,
                1
        );

        LinearLayout.LayoutParams
                footerParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        footerParams.topMargin =
                dp(24);

        main.addView(
                footer,
                footerParams
        );

        setContentView(scrollView);
    }

    // ============================================================
    // SECTION TITLE
    // ============================================================

    private void addSectionTitle(
            LinearLayout parent,
            String text
    ) {

        TextView section =
                new TextView(this);

        section.setText(text);
        section.setTextColor(BLUE);
        section.setTextSize(11);

        section.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        section.setLetterSpacing(
                0.08f
        );

        LinearLayout.LayoutParams
                params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin =
                dp(22);

        params.bottomMargin =
                dp(8);

        parent.addView(
                section,
                params
        );
    }

    // ============================================================
    // NAVIGATION CARD
    // ============================================================

    private MaterialCardView createNavigationCard(
            String icon,
            String title,
            String description
    ) {

        MaterialCardView card =
                createCard();

        LinearLayout row =
                createRow();

        addIcon(
                row,
                icon
        );

        LinearLayout text =
                createTextColumn(
                        title,
                        description
                );

        row.addView(
                text,
                createWeightParams()
        );

        addArrow(row);

        card.addView(row);

        return card;
    }

    // ============================================================
    // CARD
    // ============================================================

    private MaterialCardView createCard() {

        MaterialCardView card =
                new MaterialCardView(this);

        card.setCardBackgroundColor(
                CARD
        );

        card.setCardElevation(
                dp(1)
        );

        card.setRadius(
                dp(18)
        );

        card.setStrokeWidth(
                dp(1)
        );

        card.setStrokeColor(
                BORDER
        );

        return card;
    }

    private void addCard(
            LinearLayout parent,
            MaterialCardView card
    ) {

        LinearLayout.LayoutParams
                params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin =
                dp(10);

        parent.addView(
                card,
                params
        );
    }

    // ============================================================
    // ROW
    // ============================================================

    private LinearLayout createRow() {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                dp(15),
                dp(14),
                dp(15),
                dp(14)
        );

        return row;
    }

    // ============================================================
    // ICON
    // ============================================================

    private void addIcon(
            LinearLayout row,
            String icon
    ) {

        TextView iconView =
                new TextView(this);

        iconView.setText(icon);
        iconView.setTextSize(21);
        iconView.setGravity(
                Gravity.CENTER
        );

        iconView.setBackgroundColor(
                BLUE_SOFT
        );

        LinearLayout.LayoutParams
                params =
                new LinearLayout.LayoutParams(
                        dp(46),
                        dp(46)
                );

        params.rightMargin =
                dp(13);

        row.addView(
                iconView,
                params
        );
    }

    // ============================================================
    // TEXT COLUMN
    // ============================================================

    private LinearLayout createTextColumn(
            String title,
            String description
    ) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView titleView =
                new TextView(this);

        titleView.setText(title);
        titleView.setTextColor(
                TEXT
        );

        titleView.setTextSize(15);

        titleView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        box.addView(titleView);

        TextView descriptionView =
                new TextView(this);

        descriptionView.setText(
                description
        );

        descriptionView.setTextColor(
                MUTED
        );

        descriptionView.setTextSize(11);

        LinearLayout.LayoutParams
                params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin =
                dp(3);

        box.addView(
                descriptionView,
                params
        );

        return box;
    }

    // ============================================================
    // ARROW
    // ============================================================

    private void addArrow(
            LinearLayout row
    ) {

        TextView arrow =
                new TextView(this);

        arrow.setText("›");
        arrow.setTextColor(
                MUTED
        );

        arrow.setTextSize(28);

        arrow.setGravity(
                Gravity.CENTER
        );

        row.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(30),
                        dp(45)
                )
        );
    }

    // ============================================================
    // WEIGHT
    // ============================================================

    private LinearLayout.LayoutParams
    createWeightParams() {

        return new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
    }

    // ============================================================
    // ABOUT
    // ============================================================

    private void showAboutDialog() {

        new AlertDialog.Builder(this)
                .setTitle(
                        "CampusMind AI"
                )
                .setMessage(
                        "CampusMind AI is an AI-powered "
                                + "academic assistant designed "
                                + "for college students.\n\n"
                                + "Features include:\n"
                                + "• AI Study Assistant\n"
                                + "• Subjects\n"
                                + "• Assignments & Notes\n"
                                + "• Attendance\n"
                                + "• Focus Mode\n"
                                + "• Analytics\n\n"
                                + "Version 1.0"
                )
                .setPositiveButton(
                        "CLOSE",
                        null
                )
                .show();
    }

    // ============================================================
    // LOGOUT
    // ============================================================

    private void showLogoutDialog() {

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Log Out?"
                        )
                        .setMessage(
                                "Are you sure you want to log out of CampusMind AI?"
                        )
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .setPositiveButton(
                                "LOG OUT",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setTextColor(
                            RED
                    );

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setOnClickListener(
                            v -> {

                                firebaseAuth.signOut();

                                Intent intent =
                                        new Intent(
                                                this,
                                                LoginActivity.class
                                        );

                                intent.setFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                                                |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                );

                                startActivity(intent);

                                finish();
                            }
                    );
                }
        );

        dialog.show();
    }

    // ============================================================
    // DP
    // ============================================================

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