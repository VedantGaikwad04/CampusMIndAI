package com.example.campusmindai;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class RoleSelectionActivity extends AppCompatActivity {

    private int primaryBlue;
    private int backgroundColor;
    private int cardColor;
    private int textColor;
    private int secondaryTextColor;
    private int borderColor;
    private int softBlue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadColors();

        // =========================================================
        // ROOT SCROLL
        // =========================================================

        ScrollView scrollView = new ScrollView(this);

        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(backgroundColor);

        // =========================================================
        // MAIN CONTAINER
        // =========================================================

        LinearLayout main =
                new LinearLayout(this);

        main.setOrientation(
                LinearLayout.VERTICAL
        );

        main.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        main.setPadding(
                dp(24),
                dp(40),
                dp(24),
                dp(30)
        );

        scrollView.addView(main);

        // =========================================================
        // APP NAME
        // =========================================================

        TextView appName =
                new TextView(this);

        appName.setText(
                "CampusMind AI"
        );

        appName.setTextColor(
                primaryBlue
        );

        appName.setTextSize(27);

        appName.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        appName.setGravity(
                Gravity.CENTER
        );

        main.addView(
                appName,
                createWrapParams(0, 0)
        );

        // =========================================================
        // TITLE
        // =========================================================

        TextView title =
                new TextView(this);

        title.setText(
                "Welcome to CampusMind"
        );

        title.setTextColor(
                textColor
        );

        title.setTextSize(24);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams titleParams =
                createWrapParams(0, dp(8));

        main.addView(
                title,
                titleParams
        );

        // =========================================================
        // SUBTITLE
        // =========================================================

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "Choose how you will use the application"
        );

        subtitle.setTextColor(
                secondaryTextColor
        );

        subtitle.setTextSize(14);

        subtitle.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams subtitleParams =
                createWrapParams(0, dp(5));

        main.addView(
                subtitle,
                subtitleParams
        );

        // =========================================================
        // SPACING
        // =========================================================

        addSpace(
                main,
                dp(28)
        );

        // =========================================================
        // STUDENT CARD
        // =========================================================

        MaterialCardView studentCard =
                createRoleCard();

        LinearLayout studentContent =
                createRoleContent();

        TextView studentIcon =
                createIcon(
                        "🎓"
                );

        studentContent.addView(
                studentIcon,
                createWrapParams(0, 0)
        );

        TextView studentTitle =
                createRoleTitle(
                        "Student"
                );

        studentContent.addView(
                studentTitle,
                createWrapParams(0, dp(10))
        );

        TextView studentDescription =
                createRoleDescription(
                        "Access your AI study assistant, subjects, attendance, assignments, notes, focus mode and analytics."
                );

        studentContent.addView(
                studentDescription,
                createWrapParams(0, dp(5))
        );

        MaterialButton studentButton =
                createPrimaryButton(
                        "CONTINUE AS STUDENT"
                );

        LinearLayout.LayoutParams studentButtonParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(52)
                );

        studentButtonParams.topMargin =
                dp(18);

        studentContent.addView(
                studentButton,
                studentButtonParams
        );

        studentCard.addView(
                studentContent
        );

        LinearLayout.LayoutParams studentCardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        main.addView(
                studentCard,
                studentCardParams
        );

        // =========================================================
        // TEACHER CARD
        // =========================================================

        LinearLayout.LayoutParams teacherSpacing =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        teacherSpacing.topMargin =
                dp(18);

        MaterialCardView teacherCard =
                createRoleCard();

        LinearLayout teacherContent =
                createRoleContent();

        TextView teacherIcon =
                createIcon(
                        "👨‍🏫"
                );

        teacherContent.addView(
                teacherIcon,
                createWrapParams(0, 0)
        );

        TextView teacherTitle =
                createRoleTitle(
                        "Teacher"
                );

        teacherContent.addView(
                teacherTitle,
                createWrapParams(0, dp(10))
        );

        TextView teacherDescription =
                createRoleDescription(
                        "Start classroom attendance sessions and manage teacher-side attendance features."
                );

        teacherContent.addView(
                teacherDescription,
                createWrapParams(0, dp(5))
        );

        MaterialButton teacherButton =
                createSecondaryButton(
                        "CONTINUE AS TEACHER"
                );

        LinearLayout.LayoutParams teacherButtonParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(52)
                );

        teacherButtonParams.topMargin =
                dp(18);

        teacherContent.addView(
                teacherButton,
                teacherButtonParams
        );

        teacherCard.addView(
                teacherContent
        );

        main.addView(
                teacherCard,
                teacherSpacing
        );

        // =========================================================
        // FOOTER
        // =========================================================

        addSpace(
                main,
                dp(28)
        );

        TextView footer =
                new TextView(this);

        footer.setText(
                "Your academic companion"
        );

        footer.setTextColor(
                secondaryTextColor
        );

        footer.setTextSize(12);

        footer.setGravity(
                Gravity.CENTER
        );

        main.addView(
                footer,
                createWrapParams(0, 0)
        );

        // =========================================================
        // CLICK EVENTS
        // =========================================================

        studentButton.setOnClickListener(
                v -> openLogin("Student")
        );

        teacherButton.setOnClickListener(
                v -> openLogin("Teacher")
        );

        // =========================================================
        // SHOW SCREEN
        // =========================================================

        setContentView(
                scrollView
        );
    }

    // =============================================================
    // OPEN LOGIN
    // =============================================================

    private void openLogin(String role) {

        Intent intent =
                new Intent(
                        RoleSelectionActivity.this,
                        LoginActivity.class
                );

        intent.putExtra(
                "role",
                role
        );

        startActivity(
                intent
        );
    }

    // =============================================================
    // COLORS
    // =============================================================

    private void loadColors() {

        primaryBlue =
                getColor(
                        R.color.primary
                );

        backgroundColor =
                getColor(
                        R.color.app_background
                );

        cardColor =
                getColor(
                        R.color.app_card
                );

        textColor =
                getColor(
                        R.color.app_text
                );

        secondaryTextColor =
                getColor(
                        R.color.app_secondary_text
                );

        borderColor =
                getColor(
                        R.color.app_border
                );

        softBlue =
                getColor(
                        R.color.app_blue_soft
                );
    }

    // =============================================================
    // ROLE CARD
    // =============================================================

    private MaterialCardView createRoleCard() {

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
                dp(20)
        );

        card.setCardElevation(
                dp(2)
        );

        card.setUseCompatPadding(
                true
        );

        return card;
    }

    // =============================================================
    // ROLE CONTENT
    // =============================================================

    private LinearLayout createRoleContent() {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        layout.setPadding(
                dp(20),
                dp(22),
                dp(20),
                dp(20)
        );

        return layout;
    }

    // =============================================================
    // ICON
    // =============================================================

    private TextView createIcon(String text) {

        TextView icon =
                new TextView(this);

        icon.setText(
                text
        );

        icon.setTextSize(
                35
        );

        icon.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        dp(58),
                        dp(58)
                );

        icon.setBackground(
                createRoundedBackground(
                        softBlue,
                        dp(16)
                )
        );

        return icon;
    }

    // =============================================================
    // ROLE TITLE
    // =============================================================

    private TextView createRoleTitle(String text) {

        TextView title =
                new TextView(this);

        title.setText(
                text
        );

        title.setTextColor(
                textColor
        );

        title.setTextSize(
                20
        );

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setGravity(
                Gravity.CENTER
        );

        return title;
    }

    // =============================================================
    // ROLE DESCRIPTION
    // =============================================================

    private TextView createRoleDescription(
            String text
    ) {

        TextView description =
                new TextView(this);

        description.setText(
                text
        );

        description.setTextColor(
                secondaryTextColor
        );

        description.setTextSize(
                13
        );

        description.setGravity(
                Gravity.CENTER
        );

        description.setLineSpacing(
                dp(2),
                1.0f
        );

        return description;
    }

    // =============================================================
    // PRIMARY BUTTON
    // =============================================================

    private MaterialButton createPrimaryButton(
            String text
    ) {

        MaterialButton button =
                new MaterialButton(this);

        button.setText(
                text
        );

        button.setTextSize(
                13
        );

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setAllCaps(
                false
        );

        button.setTextColor(
                Color.WHITE
        );

        button.setBackgroundTintList(
                ColorStateList.valueOf(
                        primaryBlue
                )
        );

        button.setCornerRadius(
                dp(14)
        );

        button.setInsetTop(
                0
        );

        button.setInsetBottom(
                0
        );

        return button;
    }

    // =============================================================
    // SECONDARY BUTTON
    // =============================================================

    private MaterialButton createSecondaryButton(
            String text
    ) {

        MaterialButton button =
                new MaterialButton(this);

        button.setText(
                text
        );

        button.setTextSize(
                13
        );

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setAllCaps(
                false
        );

        button.setTextColor(
                primaryBlue
        );

        button.setBackgroundTintList(
                ColorStateList.valueOf(
                        softBlue
                )
        );

        button.setStrokeColor(
                ColorStateList.valueOf(
                        primaryBlue
                )
        );

        button.setStrokeWidth(
                dp(1)
        );

        button.setCornerRadius(
                dp(14)
        );

        button.setInsetTop(
                0
        );

        button.setInsetBottom(
                0
        );

        return button;
    }

    // =============================================================
    // ROUNDED BACKGROUND
    // =============================================================

    private android.graphics.drawable.GradientDrawable
    createRoundedBackground(
            int color,
            int radius
    ) {

        android.graphics.drawable.GradientDrawable
                drawable =
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
    // WRAP PARAMETERS
    // =============================================================

    private LinearLayout.LayoutParams
    createWrapParams(
            int topMargin,
            int bottomMargin
    ) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin =
                topMargin;

        params.bottomMargin =
                bottomMargin;

        return params;
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