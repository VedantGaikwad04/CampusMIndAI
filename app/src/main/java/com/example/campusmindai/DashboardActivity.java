package com.example.campusmindai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvDashboardAttendance;
    private TextView tvDashboardSessions;
    private TextView tvPendingAssignments;
    private TextView tvUpcomingAssignment;

    private MaterialButton btnAI;
    private MaterialButton btnSubjects;
    private MaterialButton btnAttendance;
    private MaterialButton btnAssignments;
    private MaterialButton btnFocus;
    private MaterialButton btnAnalytics;

    private MaterialCardView dashboardProfileShortcut;
    private ImageView dashboardProfileImage;

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    // =============================================================
    // THEME COLORS
    // =============================================================

    private boolean darkMode;

    private int backgroundColor;
    private int cardColor;
    private int textColor;
    private int secondaryTextColor;
    private int mutedTextColor;
    private int borderColor;
    private int primaryBlue;
    private int primaryDarkBlue;
    private int softBlue;

    // =============================================================
    // CREATE
    // =============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dashboardactivitty);

        applyThemeColors();

        // ---------------------------------------------------------
        // TEXT VIEWS
        // ---------------------------------------------------------

        tvWelcome =
                findViewById(R.id.tvWelcome);

        tvDashboardAttendance =
                findViewById(R.id.tvDashboardAttendance);

        tvDashboardSessions =
                findViewById(R.id.tvDashboardSessions);

        tvPendingAssignments =
                findViewById(R.id.tvPendingAssignments);

        tvUpcomingAssignment =
                findViewById(R.id.tvUpcomingAssignment);

        // ---------------------------------------------------------
        // BUTTONS
        // ---------------------------------------------------------

        btnAI =
                findViewById(R.id.btnAI);

        btnSubjects =
                findViewById(R.id.btnSubjects);

        btnAttendance =
                findViewById(R.id.btnAttendance);

        btnAssignments =
                findViewById(R.id.btnAssignments);

        btnFocus =
                findViewById(R.id.btnFocus);

        btnAnalytics =
                findViewById(R.id.btnAnalytics);

        // ---------------------------------------------------------
        // PROFILE
        // ---------------------------------------------------------

        dashboardProfileShortcut =
                findViewById(
                        R.id.dashboardProfileShortcut
                );

        dashboardProfileImage =
                findViewById(
                        R.id.ivDashboardProfile
                );

        // ---------------------------------------------------------
        // FIREBASE
        // ---------------------------------------------------------

        firestore =
                FirebaseFirestore.getInstance();

        firebaseAuth =
                FirebaseAuth.getInstance();

        // ---------------------------------------------------------
        // THEME
        // ---------------------------------------------------------

        applyDashboardTheme();

        // ---------------------------------------------------------
        // HEADER
        // ---------------------------------------------------------

        setupHeaderButtons();

        applyDashboardTheme();

        // ---------------------------------------------------------
        // NAVIGATION
        // ---------------------------------------------------------

        btnAI.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                DashboardActivity.this,
                                AIActivity.class
                        )
                )
        );

        btnSubjects.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                DashboardActivity.this,
                                SubjectsActivity.class
                        )
                )
        );

        btnAttendance.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                DashboardActivity.this,
                                AttendanceActivity.class
                        )
                )
        );

        btnAssignments.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                DashboardActivity.this,
                                AssignmentsActivity.class
                        )
                )
        );

        btnFocus.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                DashboardActivity.this,
                                FocusActivity.class
                        )
                )
        );

        btnAnalytics.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                DashboardActivity.this,
                                AnalyticsActivity.class
                        )
                )
        );

        if (dashboardProfileShortcut != null) {

            dashboardProfileShortcut.setOnClickListener(
                    v -> startActivity(
                            new Intent(
                                    DashboardActivity.this,
                                    ProfileActivity.class
                            )
                    )
            );
        }

        // ---------------------------------------------------------
        // LOAD DATA
        // ---------------------------------------------------------

        loadDashboardData();
    }

    // =============================================================
    // THEME COLORS
    // =============================================================

    private void applyThemeColors() {

        int nightMode =
                getResources()
                        .getConfiguration()
                        .uiMode
                        & Configuration.UI_MODE_NIGHT_MASK;

        darkMode =
                nightMode ==
                        Configuration.UI_MODE_NIGHT_YES;

        if (darkMode) {

            backgroundColor =
                    Color.rgb(
                            11,
                            17,
                            32
                    );

            cardColor =
                    Color.rgb(
                            17,
                            24,
                            39
                    );

            textColor =
                    Color.rgb(
                            248,
                            250,
                            252
                    );

            secondaryTextColor =
                    Color.rgb(
                            203,
                            213,
                            225
                    );

            mutedTextColor =
                    Color.rgb(
                            148,
                            163,
                            184
                    );

            borderColor =
                    Color.rgb(
                            51,
                            65,
                            85
                    );

            primaryBlue =
                    Color.rgb(
                            96,
                            165,
                            250
                    );

            primaryDarkBlue =
                    Color.rgb(
                            59,
                            130,
                            246
                    );

            softBlue =
                    Color.rgb(
                            23,
                            37,
                            84
                    );

        } else {

            backgroundColor =
                    Color.rgb(
                            248,
                            250,
                            252
                    );

            cardColor =
                    Color.WHITE;

            textColor =
                    Color.rgb(
                            15,
                            23,
                            42
                    );

            secondaryTextColor =
                    Color.rgb(
                            71,
                            85,
                            105
                    );

            mutedTextColor =
                    Color.rgb(
                            100,
                            116,
                            139
                    );

            borderColor =
                    Color.rgb(
                            226,
                            232,
                            240
                    );

            primaryBlue =
                    Color.rgb(
                            37,
                            99,
                            235
                    );

            primaryDarkBlue =
                    Color.rgb(
                            29,
                            78,
                            216
                    );

            softBlue =
                    Color.rgb(
                            239,
                            246,
                            255
                    );
        }
    }

    // =============================================================
    // DASHBOARD THEME
    // =============================================================

    private void applyDashboardTheme() {

        View root =
                findViewById(
                        android.R.id.content
                );

        if (root == null) {
            return;
        }

        root.setBackgroundColor(
                backgroundColor
        );

        // ---------------------------------------------------------
        // MAIN TEXT
        // ---------------------------------------------------------

        if (tvWelcome != null) {

            tvWelcome.setTextColor(
                    textColor
            );
        }

        if (tvDashboardAttendance != null) {

            tvDashboardAttendance.setTextColor(
                    primaryBlue
            );
        }

        if (tvDashboardSessions != null) {

            tvDashboardSessions.setTextColor(
                    primaryBlue
            );
        }

        if (tvPendingAssignments != null) {

            tvPendingAssignments.setTextColor(
                    primaryBlue
            );
        }

        if (tvUpcomingAssignment != null) {

            tvUpcomingAssignment.setTextColor(
                    secondaryTextColor
            );
        }

        // ---------------------------------------------------------
        // BUTTONS
        // ---------------------------------------------------------

        styleButton(btnAI);
        styleButton(btnSubjects);
        styleButton(btnAttendance);
        styleButton(btnAssignments);
        styleButton(btnFocus);
        styleButton(btnAnalytics);

        // ---------------------------------------------------------
        // PROFILE
        // ---------------------------------------------------------

        if (dashboardProfileShortcut != null) {

            dashboardProfileShortcut
                    .setCardBackgroundColor(
                            darkMode
                                    ? softBlue
                                    : Color.rgb(
                                    219,
                                    234,
                                    254
                            )
                    );

            dashboardProfileShortcut
                    .setStrokeColor(
                            borderColor
                    );

            dashboardProfileShortcut
                    .setStrokeWidth(
                            dp(1)
                    );
        }

        // ---------------------------------------------------------
        // CARDS
        // ---------------------------------------------------------

        styleAllCards(root);

        // ---------------------------------------------------------
        // TEXT
        // ---------------------------------------------------------

        styleAllTextViews(root);
    }

    // =============================================================
    // BUTTON STYLE
    // =============================================================

    private void styleButton(
            MaterialButton button
    ) {

        if (button == null) {
            return;
        }

        button.setCornerRadius(
                dp(15)
        );

        button.setMinHeight(
                dp(52)
        );

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        // =========================================================
        // ASK CAMPUSMIND BUTTON
        // =========================================================

        if (button == btnAI) {

            button.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            Color.WHITE
                    )
            );

            button.setTextColor(
                    primaryDarkBlue
            );

            return;
        }

        // =========================================================
        // NORMAL DASHBOARD BUTTONS
        // =========================================================

        button.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        primaryBlue
                )
        );

        button.setTextColor(
                Color.WHITE
        );
    }

    // =============================================================
    // CARD STYLE
    // =============================================================

    private void styleAllCards(
            View view
    ) {

        if (view == null) {
            return;
        }

        if (view instanceof MaterialCardView) {

            MaterialCardView card =
                    (MaterialCardView) view;

            int currentColor =
                    card.getCardBackgroundColor()
                            .getDefaultColor();

            boolean isHeroCard =
                    currentColor ==
                            Color.rgb(
                                    37,
                                    99,
                                    235
                            )
                            ||
                            currentColor ==
                                    Color.rgb(
                                            29,
                                            78,
                                            216
                                    );

            // -----------------------------------------------------
            // AI HERO
            // -----------------------------------------------------

            if (isHeroCard) {

                card.setCardBackgroundColor(
                        Color.rgb(
                                37,
                                99,
                                235
                        )
                );

                card.setStrokeColor(
                        Color.rgb(
                                37,
                                99,
                                235
                        )
                );
            }

            // -----------------------------------------------------
            // PROFILE
            // -----------------------------------------------------

            else if (
                    card ==
                            dashboardProfileShortcut
            ) {

                card.setCardBackgroundColor(
                        darkMode
                                ? softBlue
                                : Color.rgb(
                                219,
                                234,
                                254
                        )
                );

                card.setStrokeColor(
                        borderColor
                );
            }

            // -----------------------------------------------------
            // NORMAL CARD
            // -----------------------------------------------------

            else {

                card.setCardBackgroundColor(
                        cardColor
                );

                card.setStrokeColor(
                        borderColor
                );
            }

            card.setStrokeWidth(
                    dp(1)
            );
        }

        // ---------------------------------------------------------
        // CHILDREN
        // ---------------------------------------------------------

        if (view instanceof ViewGroup) {

            ViewGroup group =
                    (ViewGroup) view;

            for (
                    int i = 0;
                    i < group.getChildCount();
                    i++
            ) {

                styleAllCards(
                        group.getChildAt(i)
                );
            }
        }
    }

    // =============================================================
    // TEXT STYLE
    // ONLY THIS FUNCTION IS CHANGED
    // =============================================================

    private void styleAllTextViews(
            View view
    ) {

        if (view == null) {
            return;
        }

        if (
                view instanceof TextView
                        &&
                        !(view instanceof MaterialButton)
        ) {

            TextView textView =
                    (TextView) view;

            // -----------------------------------------------------
            // CHECK WHETHER TEXT IS INSIDE A CARD
            // -----------------------------------------------------

            boolean insideCard = false;

            View parentView = textView;

            while (
                    parentView.getParent()
                            instanceof View
            ) {

                if (
                        parentView.getParent()
                                instanceof MaterialCardView
                ) {

                    insideCard = true;
                    break;
                }

                parentView =
                        (View)
                                parentView.getParent();
            }

            // -----------------------------------------------------
            // BLUE AI HERO
            // -----------------------------------------------------

            if (isInsideBlueHero(textView)) {

                String value =
                        textView.getText()
                                .toString()
                                .trim();

                if (
                        value.contains(
                                "CAMPUSMIND"
                        )
                                ||
                                value.contains(
                                        "What do you want"
                                )
                ) {

                    textView.setTextColor(
                            Color.WHITE
                    );

                } else {

                    textView.setTextColor(
                            Color.rgb(
                                    219,
                                    234,
                                    254
                            )
                    );
                }
            }

            // -----------------------------------------------------
            // DARK MODE
            // -----------------------------------------------------

            else if (darkMode) {

                /*
                 * Text outside cards belongs to the dashboard
                 * header/section area, which is light in the
                 * supplied layout. Keep that text dark.
                 *
                 * Text inside cards belongs to dark cards.
                 * Keep those texts light.
                 */

                if (!insideCard) {

                    // -------------------------------------------------
                    // WELCOME
                    // -------------------------------------------------

                    if (textView == tvWelcome) {

                        textView.setTextColor(
                                Color.rgb(
                                        15,
                                        23,
                                        42
                                )
                        );

                    } else {

                        int currentColor =
                                textView.getCurrentTextColor();

                        // Primary blue
                        if (
                                currentColor ==
                                        Color.rgb(
                                                37,
                                                99,
                                                235
                                        )
                                        ||
                                        currentColor ==
                                                Color.rgb(
                                                        29,
                                                        78,
                                                        216
                                                )
                        ) {

                            textView.setTextColor(
                                    primaryBlue
                            );
                        }

                        // Secondary text
                        else if (
                                currentColor ==
                                        Color.rgb(
                                                71,
                                                85,
                                                105
                                        )
                        ) {

                            textView.setTextColor(
                                    Color.rgb(
                                            71,
                                            85,
                                            105
                                    )
                            );
                        }

                        // Muted text
                        else if (
                                currentColor ==
                                        Color.rgb(
                                                100,
                                                116,
                                                139
                                        )
                        ) {

                            textView.setTextColor(
                                    Color.rgb(
                                            100,
                                            116,
                                            139
                                    )
                            );
                        }

                        // Any light/white text outside a card
                        // becomes dark and visible.
                        else {

                            textView.setTextColor(
                                    Color.rgb(
                                            15,
                                            23,
                                            42
                                    )
                            );
                        }
                    }
                }

                // -----------------------------------------------------
                // TEXT INSIDE NORMAL DARK CARD
                // -----------------------------------------------------

                else {

                    int currentColor =
                            textView.getCurrentTextColor();

                    // Main text
                    if (
                            currentColor ==
                                    Color.rgb(
                                            15,
                                            23,
                                            42
                                    )
                                    ||
                                    currentColor ==
                                            Color.rgb(
                                                    30,
                                                    41,
                                                    59
                                            )
                                    ||
                                    currentColor ==
                                            Color.rgb(
                                                    51,
                                                    65,
                                                    85
                                            )
                                    ||
                                    currentColor ==
                                            Color.WHITE
                                    ||
                                    currentColor ==
                                            Color.rgb(
                                                    255,
                                                    255,
                                                    255
                                            )
                    ) {

                        textView.setTextColor(
                                textColor
                        );
                    }

                    // Secondary
                    else if (
                            currentColor ==
                                    Color.rgb(
                                            71,
                                            85,
                                            105
                                    )
                    ) {

                        textView.setTextColor(
                                secondaryTextColor
                        );
                    }

                    // Muted
                    else if (
                            currentColor ==
                                    Color.rgb(
                                            100,
                                            116,
                                            139
                                    )
                                    ||
                                    currentColor ==
                                            Color.rgb(
                                                    148,
                                                    163,
                                                    184
                                            )
                    ) {

                        textView.setTextColor(
                                mutedTextColor
                        );
                    }

                    // Primary blue
                    else if (
                            currentColor ==
                                    Color.rgb(
                                            37,
                                            99,
                                            235
                                    )
                                    ||
                                    currentColor ==
                                            Color.rgb(
                                                    29,
                                                    78,
                                                    216
                                            )
                    ) {

                        textView.setTextColor(
                                primaryBlue
                        );
                    }

                    // Unknown text color inside dark card
                    else {

                        textView.setTextColor(
                                textColor
                        );
                    }
                }
            }

            // -----------------------------------------------------
            // LIGHT MODE
            // -----------------------------------------------------

            else {

                int currentColor =
                        textView.getCurrentTextColor();

                // Main dark text
                if (
                        currentColor ==
                                Color.rgb(
                                        15,
                                        23,
                                        42
                                )
                                ||
                                currentColor ==
                                        Color.rgb(
                                                30,
                                                41,
                                                59
                                        )
                                ||
                                currentColor ==
                                        Color.rgb(
                                                51,
                                                65,
                                                85
                                        )
                ) {

                    textView.setTextColor(
                            textColor
                    );
                }

                // Secondary
                else if (
                        currentColor ==
                                Color.rgb(
                                        71,
                                        85,
                                        105
                                )
                ) {

                    textView.setTextColor(
                            secondaryTextColor
                    );
                }

                // Muted
                else if (
                        currentColor ==
                                Color.rgb(
                                        100,
                                        116,
                                        139
                                )
                ) {

                    textView.setTextColor(
                            mutedTextColor
                    );
                }

                // Soft gray
                else if (
                        currentColor ==
                                Color.rgb(
                                        148,
                                        163,
                                        184
                                )
                ) {

                    textView.setTextColor(
                            mutedTextColor
                    );
                }

                // Primary blue
                else if (
                        currentColor ==
                                Color.rgb(
                                        37,
                                        99,
                                        235
                                )
                                ||
                                currentColor ==
                                        Color.rgb(
                                                29,
                                                78,
                                                216
                                        )
                ) {

                    textView.setTextColor(
                            primaryBlue
                    );
                }
            }
        }

        // ---------------------------------------------------------
        // CHILDREN
        // ---------------------------------------------------------

        if (view instanceof ViewGroup) {

            ViewGroup group =
                    (ViewGroup) view;

            for (
                    int i = 0;
                    i < group.getChildCount();
                    i++
            ) {

                styleAllTextViews(
                        group.getChildAt(i)
                );
            }
        }
    }

    // =============================================================
    // CHECK BLUE HERO
    // =============================================================

    private boolean isInsideBlueHero(
            View view
    ) {

        View current =
                view;

        while (
                current.getParent()
                        instanceof View
        ) {

            if (
                    current.getParent()
                            instanceof MaterialCardView
            ) {

                MaterialCardView card =
                        (MaterialCardView)
                                current.getParent();

                int cardBackground =
                        card.getCardBackgroundColor()
                                .getDefaultColor();

                if (
                        cardBackground ==
                                Color.rgb(
                                        37,
                                        99,
                                        235
                                )
                                ||
                                cardBackground ==
                                        Color.rgb(
                                                29,
                                                78,
                                                216
                                        )
                ) {

                    return true;
                }
            }

            current =
                    (View)
                            current.getParent();
        }

        return false;
    }

    // =============================================================
    // HEADER
    // =============================================================

    private void setupHeaderButtons() {

        if (dashboardProfileShortcut == null) {
            return;
        }

        ViewParentHolder holder =
                findHeaderParent(
                        dashboardProfileShortcut
                );

        if (holder == null) {
            return;
        }

        ViewGroup header =
                holder.parent;

        // =========================================================
        // REMOVE CURRENT PROFILE BUTTON
        // =========================================================

        if (dashboardProfileShortcut.getParent()
                instanceof ViewGroup) {

            ((ViewGroup)
                    dashboardProfileShortcut.getParent())
                    .removeView(
                            dashboardProfileShortcut
                    );
        }

        // =========================================================
        // REMOVE OLD SETTINGS BUTTON IF IT ALREADY EXISTS
        // =========================================================

        View oldSettings =
                header.findViewWithTag(
                        "campusmind_settings"
                );

        if (oldSettings != null) {

            if (oldSettings.getParent()
                    instanceof ViewGroup) {

                ((ViewGroup)
                        oldSettings.getParent())
                        .removeView(
                                oldSettings
                        );
            }
        }

        // =========================================================
        // PROFILE CARD
        // =========================================================

        MaterialCardView profileCard =
                new MaterialCardView(this);

        profileCard.setId(
                R.id.dashboardProfileShortcut
        );

        profileCard.setCardBackgroundColor(
                darkMode
                        ? softBlue
                        : Color.rgb(
                        219,
                        234,
                        254
                )
        );

        profileCard.setRadius(
                dp(13)
        );

        profileCard.setStrokeWidth(
                dp(1)
        );

        profileCard.setStrokeColor(
                borderColor
        );

        profileCard.setClickable(
                true
        );

        profileCard.setFocusable(
                true
        );

        // =========================================================
        // PROFILE IMAGE
        // =========================================================

        ImageView profileImage =
                new ImageView(this);

        dashboardProfileImage =
                profileImage;

        profileImage.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        profileImage.setContentDescription(
                "Profile Picture"
        );

        profileImage.setImageResource(
                R.drawable.profile_circle
        );

        loadDashboardProfileImage(
                profileImage
        );

        // Safety: detach image if Android reports a parent.
        if (profileImage.getParent()
                instanceof ViewGroup) {

            ((ViewGroup)
                    profileImage.getParent())
                    .removeView(
                            profileImage
                    );
        }

        profileCard.addView(
                profileImage,
                new ViewGroup.LayoutParams(
                        dp(52),
                        dp(52)
                )
        );

        profileCard.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                DashboardActivity.this,
                                ProfileActivity.class
                        )
                )
        );

        // =========================================================
        // PROFILE AT LEFT
        // =========================================================

        header.addView(
                profileCard,
                0,
                new LinearLayout.LayoutParams(
                        dp(52),
                        dp(52)
                )
        );

        dashboardProfileShortcut =
                profileCard;

        // =========================================================
        // SETTINGS CARD
        // =========================================================

        MaterialCardView settingsCard =
                new MaterialCardView(this);

        settingsCard.setTag(
                "campusmind_settings"
        );

        settingsCard.setCardBackgroundColor(
                cardColor
        );

        settingsCard.setRadius(
                dp(13)
        );

        settingsCard.setStrokeWidth(
                dp(1)
        );

        settingsCard.setStrokeColor(
                borderColor
        );

        settingsCard.setClickable(
                true
        );

        settingsCard.setFocusable(
                true
        );

        // =========================================================
        // SETTINGS ICON
        // =========================================================

        TextView settingsIcon =
                new TextView(this);

        settingsIcon.setText(
                "⚙"
        );

        settingsIcon.setTextSize(
                20
        );

        settingsIcon.setTextColor(
                primaryBlue
        );

        settingsIcon.setGravity(
                Gravity.CENTER
        );

        // Safety: detach icon if it somehow already has a parent.
        if (settingsIcon.getParent()
                instanceof ViewGroup) {

            ((ViewGroup)
                    settingsIcon.getParent())
                    .removeView(
                            settingsIcon
                    );
        }

        settingsCard.addView(
                settingsIcon,
                new ViewGroup.LayoutParams(
                        dp(52),
                        dp(52)
                )
        );

        settingsCard.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                DashboardActivity.this,
                                SettingsActivity.class
                        )
                )
        );

        // =========================================================
        // SETTINGS AT RIGHT
        // =========================================================

        header.addView(
                settingsCard,
                header.getChildCount(),
                new LinearLayout.LayoutParams(
                        dp(52),
                        dp(52)
                )
        );
    }

    // =============================================================
    // LOAD PROFILE IMAGE
    // =============================================================

    private void loadDashboardProfileImage(
            ImageView imageView
    ) {

        if (imageView == null) {
            return;
        }

        // Default image
        imageView.setImageResource(
                R.drawable.profile_circle
        );

        SharedPreferences preferences =
                getSharedPreferences(
                        "CampusMindProfile",
                        MODE_PRIVATE
                );

        String uriString =
                preferences.getString(
                        "profile_image_uri",
                        null
                );

        if (
                uriString == null
                        ||
                        uriString.trim().isEmpty()
        ) {

            return;
        }

        try {

            Uri imageUri =
                    Uri.parse(
                            uriString
                    );

            // Verify that the URI can actually be opened.
            getContentResolver()
                    .openInputStream(
                            imageUri
                    )
                    .close();

            imageView.setImageURI(
                    imageUri
            );

        } catch (Exception e) {

            imageView.setImageResource(
                    R.drawable.profile_circle
            );
        }
    }

    // =============================================================
    // FIND HEADER PARENT
    // =============================================================

    private ViewParentHolder findHeaderParent(
            View target
    ) {

        View root =
                findViewById(
                        android.R.id.content
                );

        if (!(root instanceof ViewGroup)) {
            return null;
        }

        ViewParentHolder holder =
                new ViewParentHolder();

        holder.parent =
                findParentRecursive(
                        (ViewGroup) root,
                        target,
                        holder
                );

        if (holder.parent == null) {
            return null;
        }

        return holder;
    }

    private ViewGroup findParentRecursive(
            ViewGroup parent,
            View target,
            ViewParentHolder holder
    ) {

        for (
                int i = 0;
                i < parent.getChildCount();
                i++
        ) {

            View child =
                    parent.getChildAt(i);

            if (child == target) {

                holder.index =
                        i;

                return parent;
            }

            if (child instanceof ViewGroup) {

                ViewGroup result =
                        findParentRecursive(
                                (ViewGroup) child,
                                target,
                                holder
                        );

                if (result != null) {

                    return result;
                }
            }
        }

        return null;
    }

    private static class ViewParentHolder {

        ViewGroup parent;
        int index;
    }

    // =============================================================
    // DASHBOARD DATA
    // =============================================================

    private void loadDashboardData() {

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

        loadStudentName(
                userId
        );

        loadAttendance(
                userId
        );

        loadStudySessions(
                userId
        );

        loadAssignmentSummary(
                userId
        );
    }

    // =============================================================
    // STUDENT NAME
    // =============================================================

    private void loadStudentName(
            String userId
    ) {

        firestore
                .collection("students")
                .document(userId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            String name =
                                    documentSnapshot
                                            .getString(
                                                    "name"
                                            );

                            if (
                                    name != null
                                            &&
                                            !name
                                                    .trim()
                                                    .isEmpty()
                            ) {

                                tvWelcome.setText(
                                        "Welcome, "
                                                + name.trim()
                                                + " 👋"
                                );

                            } else {

                                tvWelcome.setText(
                                        "Welcome 👋"
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e ->
                                tvWelcome.setText(
                                        "Welcome 👋"
                                )
                );
    }

    // =============================================================
    // ATTENDANCE
    // =============================================================

    private void loadAttendance(
            String userId
    ) {

        firestore
                .collection("students")
                .document(userId)
                .collection("attendance")
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            int totalClasses =
                                    snapshot.size();

                            int presentClasses =
                                    0;

                            for (
                                    DocumentSnapshot document
                                    : snapshot.getDocuments()
                            ) {

                                String status =
                                        document.getString(
                                                "status"
                                        );

                                if (
                                        "Present"
                                                .equalsIgnoreCase(
                                                        status
                                                )
                                ) {

                                    presentClasses++;
                                }
                            }

                            int percentage =
                                    0;

                            if (
                                    totalClasses > 0
                            ) {

                                percentage =
                                        (
                                                presentClasses
                                                        * 100
                                        )
                                                / totalClasses;
                            }

                            tvDashboardAttendance
                                    .setText(
                                            percentage
                                                    + "%"
                                    );
                        }
                )
                .addOnFailureListener(
                        e ->
                                tvDashboardAttendance
                                        .setText(
                                                "--"
                                        )
                );
    }

    // =============================================================
    // FOCUS SESSIONS
    // =============================================================

    private void loadStudySessions(
            String userId
    ) {

        firestore
                .collection("students")
                .document(userId)
                .collection("studySessions")
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            int totalMinutes =
                                    0;

                            for (
                                    DocumentSnapshot document
                                    : snapshot.getDocuments()
                            ) {

                                Long seconds =
                                        document.getLong(
                                                "durationSeconds"
                                        );

                                if (
                                        seconds != null
                                ) {

                                    totalMinutes +=
                                            seconds / 60;

                                    continue;
                                }

                                Long millis =
                                        document.getLong(
                                                "durationMillis"
                                        );

                                if (
                                        millis != null
                                ) {

                                    totalMinutes +=
                                            millis / 60000;

                                    continue;
                                }

                                Map<String, Object> data =
                                        document.getData();

                                if (
                                        data != null
                                ) {

                                    Object minutesValue =
                                            data.get(
                                                    "durationMinutes"
                                            );

                                    if (
                                            minutesValue
                                                    instanceof Number
                                    ) {

                                        totalMinutes +=
                                                (
                                                        (Number)
                                                                minutesValue
                                                )
                                                        .intValue();
                                    }
                                }
                            }

                            tvDashboardSessions
                                    .setText(
                                            totalMinutes
                                                    + " min"
                                    );
                        }
                )
                .addOnFailureListener(
                        e ->
                                tvDashboardSessions
                                        .setText(
                                                "0 min"
                                        )
                );
    }

    // =============================================================
    // ASSIGNMENTS
    // =============================================================

    private void loadAssignmentSummary(
            String userId
    ) {

        firestore
                .collection("students")
                .document(userId)
                .collection("assignments")
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            int pending =
                                    0;

                            String upcoming =
                                    "No upcoming assignments";

                            long nearestDate =
                                    Long.MAX_VALUE;

                            for (
                                    DocumentSnapshot document
                                    : snapshot.getDocuments()
                            ) {

                                String status =
                                        document.getString(
                                                "status"
                                        );

                                if (
                                        "Completed"
                                                .equalsIgnoreCase(
                                                        status
                                                )
                                ) {

                                    continue;
                                }

                                pending++;

                                Long dueDate =
                                        document.getLong(
                                                "dueDateMillis"
                                        );

                                if (
                                        dueDate != null
                                                &&
                                                dueDate < nearestDate
                                ) {

                                    nearestDate =
                                            dueDate;

                                    String title =
                                            document.getString(
                                                    "title"
                                            );

                                    if (
                                            title != null
                                    ) {

                                        upcoming =
                                                title;
                                    }
                                }
                            }

                            tvPendingAssignments
                                    .setText(
                                            String.valueOf(
                                                    pending
                                            )
                                    );

                            tvUpcomingAssignment
                                    .setText(
                                            upcoming
                                    );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            tvPendingAssignments
                                    .setText(
                                            "0"
                                    );

                            tvUpcomingAssignment
                                    .setText(
                                            "Unable to load"
                                    );
                        }
                );
    }

    // =============================================================
    // RESUME
    // =============================================================

    @Override
    protected void onResume() {

        super.onResume();

        // Refresh theme.
        applyThemeColors();

        applyDashboardTheme();

        // Refresh profile image.
        if (
                dashboardProfileImage != null
        ) {

            loadDashboardProfileImage(
                    dashboardProfileImage
            );
        }

        // Refresh Firebase data.
        loadDashboardData();
    }

    // =============================================================
    // DP
    // =============================================================

    private int dp(
            int value
    ) {

        return (int)
                (
                        value
                                *
                                getResources()
                                        .getDisplayMetrics()
                                        .density
                                + 0.5f
                );
    }
}