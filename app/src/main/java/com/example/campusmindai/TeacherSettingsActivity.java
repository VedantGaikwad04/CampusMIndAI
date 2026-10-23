package com.example.campusmindai;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TeacherSettingsActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private int backgroundColor;
    private int cardColor;
    private int textColor;
    private int secondaryTextColor;
    private int mutedTextColor;
    private int borderColor;
    private int primaryBlue;
    private int softBlue;
    private int errorColor;
    private int softRed;

    private ActivityResultLauncher<String[]> profileImagePicker;
    private ImageView teacherProfileImage;
    private TextView teacherNameText;
    private TextView teacherEmailText;
    private TextView teacherCollegeText;
    private TextView teacherCourseText;
    private TextView profileCompletionText;
    private ProgressBar profileCompletionBar;

    private String currentTeacherName = "";
    private String currentTeacherCollege = "";
    private String currentTeacherCourse = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        loadColors();

        firebaseAuth =
                FirebaseAuth.getInstance();

        firestore =
                FirebaseFirestore.getInstance();

        profileImagePicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null || firebaseAuth.getCurrentUser() == null) {
                        return;
                    }

                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (SecurityException ignored) {
                        // The image can still be used for the current session.
                    }

                    String key = getTeacherPhotoPreferenceKey();
                    if (key != null) {
                        getSharedPreferences("CampusMindTeacherProfile", MODE_PRIVATE)
                                .edit()
                                .putString(key, uri.toString())
                                .apply();
                    }

                    loadTeacherPhoto();
                }
        );

        createScreen();
    }

    // =============================================================
    // CREATE SCREEN
    // =============================================================

    private void createScreen() {

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

        TextView title =
                createText(
                        "Teacher Settings",
                        24,
                        textColor,
                        Typeface.BOLD
                );

        main.addView(
                title
        );

        TextView subtitle =
                createText(
                        "Teacher portal preferences and account controls",
                        13,
                        secondaryTextColor,
                        Typeface.NORMAL
                );

        subtitle.setPadding(
                0,
                dp(5),
                0,
                0
        );

        main.addView(
                subtitle
        );

        // =========================================================
        // TEACHER PROFILE (kept inside Settings, not the dashboard)
        // =========================================================

        addSectionTitle(
                main,
                "TEACHER PROFILE"
        );

        MaterialCardView photoCard = createCard();
        LinearLayout photoContent = createContent();
        photoContent.setGravity(Gravity.CENTER_HORIZONTAL);

        teacherProfileImage = new ImageView(this);
        teacherProfileImage.setImageResource(R.drawable.profile_circle);
        teacherProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        teacherProfileImage.setContentDescription("Teacher profile photo");

        LinearLayout.LayoutParams photoParams =
                new LinearLayout.LayoutParams(dp(124), dp(124));
        photoContent.addView(teacherProfileImage, photoParams);

        TextView photoLabel = createText(
                "TEACHER PROFILE PHOTO",
                12,
                mutedTextColor,
                Typeface.BOLD
        );
        photoLabel.setPadding(0, dp(12), 0, dp(12));
        photoContent.addView(photoLabel);

        MaterialButton changePhotoButton = new MaterialButton(this);
        changePhotoButton.setText("CHANGE PROFILE PHOTO");
        changePhotoButton.setAllCaps(false);
        changePhotoButton.setTextColor(Color.WHITE);
        changePhotoButton.setBackgroundTintList(
                ColorStateList.valueOf(primaryBlue)
        );
        changePhotoButton.setCornerRadius(dp(12));
        changePhotoButton.setInsetTop(0);
        changePhotoButton.setInsetBottom(0);
        photoContent.addView(
                changePhotoButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(50)
                )
        );
        changePhotoButton.setOnClickListener(v -> {
            if (profileImagePicker != null) {
                profileImagePicker.launch(new String[]{"image/*"});
            }
        });

        MaterialButton removePhotoButton = new MaterialButton(this);
        removePhotoButton.setText("REMOVE PHOTO");
        removePhotoButton.setAllCaps(false);
        removePhotoButton.setTextColor(errorColor);
        removePhotoButton.setBackgroundTintList(
                ColorStateList.valueOf(softRed)
        );
        removePhotoButton.setCornerRadius(dp(12));
        removePhotoButton.setInsetTop(0);
        removePhotoButton.setInsetBottom(0);
        LinearLayout.LayoutParams removePhotoParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(48)
                );
        removePhotoParams.topMargin = dp(10);
        photoContent.addView(removePhotoButton, removePhotoParams);
        removePhotoButton.setOnClickListener(v -> removeTeacherPhoto());

        photoCard.addView(photoContent);
        LinearLayout.LayoutParams photoCardParams = matchWrapParams();
        photoCardParams.topMargin = dp(2);
        main.addView(photoCard, photoCardParams);

        // Teacher identity and editable information.
        MaterialCardView teacherInfoCard = createCard();
        LinearLayout teacherInfoContent = createContent();

        teacherNameText = createText(
                "Loading teacher profile...",
                22,
                textColor,
                Typeface.BOLD
        );
        teacherInfoContent.addView(teacherNameText);

        teacherEmailText = createText(
                "",
                14,
                secondaryTextColor,
                Typeface.NORMAL
        );
        teacherEmailText.setPadding(0, dp(6), 0, 0);
        teacherInfoContent.addView(teacherEmailText);

        profileCompletionText = createText(
                "Profile completion: 0%",
                14,
                primaryBlue,
                Typeface.BOLD
        );
        profileCompletionText.setPadding(0, dp(18), 0, dp(8));
        teacherInfoContent.addView(profileCompletionText);

        profileCompletionBar = new ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
        );
        profileCompletionBar.setMax(100);
        profileCompletionBar.setProgress(0);
        profileCompletionBar.setProgressTintList(
                ColorStateList.valueOf(primaryBlue)
        );
        teacherInfoContent.addView(
                profileCompletionBar,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(6)
                )
        );

        TextView academicHeading = createText(
                "ACADEMIC INFORMATION",
                12,
                mutedTextColor,
                Typeface.BOLD
        );
        academicHeading.setPadding(0, dp(22), 0, dp(12));
        teacherInfoContent.addView(academicHeading);

        teacherInfoContent.addView(createText(
                "College",
                13,
                secondaryTextColor,
                Typeface.NORMAL
        ));
        teacherCollegeText = createText(
                "Not added",
                16,
                textColor,
                Typeface.BOLD
        );
        teacherCollegeText.setPadding(0, dp(4), 0, dp(14));
        teacherInfoContent.addView(teacherCollegeText);

        teacherInfoContent.addView(createText(
                "Course / Department",
                13,
                secondaryTextColor,
                Typeface.NORMAL
        ));
        teacherCourseText = createText(
                "Not added",
                16,
                textColor,
                Typeface.BOLD
        );
        teacherCourseText.setPadding(0, dp(4), 0, dp(14));
        teacherInfoContent.addView(teacherCourseText);

        MaterialButton editDetailsButton = new MaterialButton(this);
        editDetailsButton.setText("EDIT DETAILS");
        editDetailsButton.setAllCaps(false);
        editDetailsButton.setTextColor(Color.WHITE);
        editDetailsButton.setBackgroundTintList(
                ColorStateList.valueOf(primaryBlue)
        );
        editDetailsButton.setCornerRadius(dp(12));
        editDetailsButton.setInsetTop(0);
        editDetailsButton.setInsetBottom(0);
        LinearLayout.LayoutParams editButtonParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(50)
                );
        editButtonParams.topMargin = dp(8);
        teacherInfoContent.addView(editDetailsButton, editButtonParams);
        editDetailsButton.setOnClickListener(v -> showEditTeacherDetailsDialog());

        teacherInfoCard.addView(teacherInfoContent);
        LinearLayout.LayoutParams teacherInfoParams = matchWrapParams();
        teacherInfoParams.topMargin = dp(14);
        main.addView(teacherInfoCard, teacherInfoParams);

        loadTeacherDetails();
        loadTeacherPhoto();

        // =========================================================
        // APPEARANCE
        // =========================================================

        addSectionTitle(
                main,
                "APPEARANCE"
        );

        MaterialCardView appearanceCard =
                createCard();

        LinearLayout appearanceRow =
                createHorizontalRow();

        TextView appearanceIcon =
                createText(
                        "🌙",
                        24,
                        primaryBlue,
                        Typeface.NORMAL
                );

        appearanceRow.addView(
                appearanceIcon,
                new LinearLayout.LayoutParams(
                        dp(45),
                        dp(50)
                )
        );

        LinearLayout appearanceText =
                new LinearLayout(this);

        appearanceText.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView appearanceTitle =
                createText(
                        "Dark Mode",
                        16,
                        textColor,
                        Typeface.BOLD
                );

        TextView appearanceDescription =
                createText(
                        "Use dark appearance for the teacher portal",
                        12,
                        secondaryTextColor,
                        Typeface.NORMAL
                );

        appearanceDescription.setPadding(
                0,
                dp(3),
                0,
                0
        );

        appearanceText.addView(
                appearanceTitle
        );

        appearanceText.addView(
                appearanceDescription
        );

        LinearLayout.LayoutParams appearanceTextParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                );

        appearanceTextParams.leftMargin =
                dp(12);

        appearanceRow.addView(
                appearanceText,
                appearanceTextParams
        );

        SwitchMaterial darkModeSwitch =
                new SwitchMaterial(this);

        boolean darkMode =
                getSharedPreferences(
                        "CampusMindSettings",
                        MODE_PRIVATE
                ).getBoolean(
                        "dark_mode",
                        false
                );

        darkModeSwitch.setChecked(
                darkMode
        );

        darkModeSwitch.setButtonTintList(
                ColorStateList.valueOf(
                        primaryBlue
                )
        );

        appearanceRow.addView(
                darkModeSwitch
        );

        appearanceCard.addView(
                appearanceRow
        );

        main.addView(
                appearanceCard
        );

        darkModeSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    getSharedPreferences(
                            "CampusMindSettings",
                            MODE_PRIVATE
                    )
                            .edit()
                            .putBoolean(
                                    "dark_mode",
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

        // =========================================================
        // ATTENDANCE
        // =========================================================

        addSectionTitle(
                main,
                "ATTENDANCE SETTINGS"
        );

        MaterialCardView attendanceCard =
                createCard();

        LinearLayout attendanceContent =
                createContent();

        TextView attendanceTitle =
                createText(
                        "📋  Classroom Attendance",
                        16,
                        textColor,
                        Typeface.BOLD
                );

        attendanceContent.addView(
                attendanceTitle
        );

        TextView duration =
                createText(
                        "Session duration: 90 seconds",
                        13,
                        secondaryTextColor,
                        Typeface.NORMAL
                );

        duration.setPadding(
                0,
                dp(8),
                0,
                0
        );

        attendanceContent.addView(
                duration
        );

        TextView bluetooth =
                createText(
                        "Bluetooth proximity verification: Enabled",
                        13,
                        secondaryTextColor,
                        Typeface.NORMAL
                );

        bluetooth.setPadding(
                0,
                dp(5),
                0,
                0
        );

        attendanceContent.addView(
                bluetooth
        );

        TextView code =
                createText(
                        "Temporary 6-digit attendance code: Enabled",
                        13,
                        secondaryTextColor,
                        Typeface.NORMAL
                );

        code.setPadding(
                0,
                dp(5),
                0,
                0
        );

        attendanceContent.addView(
                code
        );

        attendanceCard.addView(
                attendanceContent
        );

        main.addView(
                attendanceCard
        );

        // =========================================================
        // APP INFORMATION
        // =========================================================

        addSectionTitle(
                main,
                "APP INFORMATION"
        );

        MaterialCardView aboutCard =
                createCard();

        LinearLayout aboutContent =
                createContent();

        TextView aboutTitle =
                createText(
                        "ℹ️  CampusMind AI Teacher Portal",
                        16,
                        textColor,
                        Typeface.BOLD
                );

        aboutContent.addView(
                aboutTitle
        );

        TextView aboutDescription =
                createText(
                        "Teacher-side tools for classroom attendance and attendance management.",
                        13,
                        secondaryTextColor,
                        Typeface.NORMAL
                );

        aboutDescription.setPadding(
                0,
                dp(7),
                0,
                0
        );

        aboutContent.addView(
                aboutDescription
        );

        TextView version =
                createText(
                        "Version 1.0",
                        12,
                        mutedTextColor,
                        Typeface.NORMAL
                );

        version.setPadding(
                0,
                dp(7),
                0,
                0
        );

        aboutContent.addView(
                version
        );

        aboutCard.addView(
                aboutContent
        );

        main.addView(
                aboutCard
        );

        // =========================================================
        // ACCOUNT ACTIONS
        // =========================================================

        addSectionTitle(
                main,
                "ACCOUNT ACTIONS"
        );

        MaterialCardView logoutCard =
                createCard();

        LinearLayout logoutRow =
                createHorizontalRow();

        TextView logoutIcon =
                createText(
                        "🚪",
                        24,
                        errorColor,
                        Typeface.NORMAL
                );

        logoutRow.addView(
                logoutIcon,
                new LinearLayout.LayoutParams(
                        dp(45),
                        dp(50)
                )
        );

        LinearLayout logoutText =
                new LinearLayout(this);

        logoutText.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView logoutTitle =
                createText(
                        "Log Out",
                        16,
                        errorColor,
                        Typeface.BOLD
                );

        TextView logoutDescription =
                createText(
                        "Sign out from the teacher account",
                        12,
                        secondaryTextColor,
                        Typeface.NORMAL
                );

        logoutDescription.setPadding(
                0,
                dp(3),
                0,
                0
        );

        logoutText.addView(
                logoutTitle
        );

        logoutText.addView(
                logoutDescription
        );

        logoutRow.addView(
                logoutText,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        TextView arrow =
                createText(
                        "›",
                        28,
                        mutedTextColor,
                        Typeface.NORMAL
                );

        arrow.setGravity(
                Gravity.CENTER
        );

        logoutRow.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(30),
                        dp(50)
                )
        );

        logoutCard.addView(
                logoutRow
        );

        main.addView(
                logoutCard
        );

        logoutCard.setOnClickListener(
                v -> showLogoutDialog()
        );

        // =========================================================
        // FOOTER
        // =========================================================

        TextView footer =
                createText(
                        "CampusMind AI\nTeacher Portal",
                        12,
                        mutedTextColor,
                        Typeface.NORMAL
                );

        footer.setGravity(
                Gravity.CENTER
        );

        footer.setPadding(
                0,
                dp(28),
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
    // LOAD TEACHER PROFILE DETAILS
    // =============================================================

    private void loadTeacherDetails() {

        if (firebaseAuth.getCurrentUser() == null) {
            teacherNameText.setText("Teacher profile unavailable");
            teacherEmailText.setText("");
            return;
        }

        String uid = firebaseAuth.getCurrentUser().getUid();
        String authEmail = firebaseAuth.getCurrentUser().getEmail();
        if (authEmail != null) {
            teacherEmailText.setText(authEmail);
        }

        firestore.collection("students")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        teacherNameText.setText("Teacher profile not found");
                        teacherCollegeText.setText("Not added");
                        teacherCourseText.setText("Not added");
                        updateProfileCompletion(authEmail);
                        return;
                    }

                    // The page heading is always Teacher Profile. Do not show
                    // the word Student as the profile title in teacher settings.
                    String name = document.getString("name");
                    currentTeacherName = name == null ? "" : name.trim();
                    if (currentTeacherName.isEmpty()) {
                        currentTeacherName = "Teacher";
                    }
                    teacherNameText.setText(currentTeacherName);

                    currentTeacherCollege = document.getString("college");
                    if (currentTeacherCollege == null) currentTeacherCollege = "";
                    currentTeacherCollege = currentTeacherCollege.trim();
                    teacherCollegeText.setText(
                            currentTeacherCollege.isEmpty()
                                    ? "Not added" : currentTeacherCollege
                    );

                    currentTeacherCourse = document.getString("course");
                    if (currentTeacherCourse == null) currentTeacherCourse = "";
                    currentTeacherCourse = currentTeacherCourse.trim();
                    teacherCourseText.setText(
                            currentTeacherCourse.isEmpty()
                                    ? "Not added" : currentTeacherCourse
                    );

                    String profileEmail = document.getString("email");
                    if ((authEmail == null || authEmail.trim().isEmpty())
                            && profileEmail != null) {
                        teacherEmailText.setText(profileEmail);
                    }

                    updateProfileCompletion(authEmail != null ? authEmail : profileEmail);
                })
                .addOnFailureListener(e -> {
                    teacherNameText.setText("Unable to load teacher profile");
                    Toast.makeText(
                            this,
                            "Unable to load teacher information. Check your connection and Firestore rules.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void updateProfileCompletion(String email) {
        int completed = 0;
        if (currentTeacherName != null && !currentTeacherName.trim().isEmpty()
                && !"Teacher".equals(currentTeacherName)) completed++;
        if (email != null && !email.trim().isEmpty()) completed++;
        if (currentTeacherCollege != null && !currentTeacherCollege.trim().isEmpty()) completed++;
        if (currentTeacherCourse != null && !currentTeacherCourse.trim().isEmpty()) completed++;
        if (hasSavedTeacherPhoto()) completed++;

        int percent = completed * 20;
        if (profileCompletionText != null) {
            profileCompletionText.setText("Profile " + percent + "% complete");
        }
        if (profileCompletionBar != null) {
            profileCompletionBar.setProgress(percent);
        }
    }

    // =============================================================
    // TEACHER PROFILE PHOTO (saved locally for this account/device)
    // =============================================================

    private String getTeacherPhotoPreferenceKey() {
        if (firebaseAuth.getCurrentUser() == null) return null;
        return "teacher_photo_" + firebaseAuth.getCurrentUser().getUid();
    }

    private boolean hasSavedTeacherPhoto() {
        String key = getTeacherPhotoPreferenceKey();
        return key != null && getSharedPreferences(
                "CampusMindTeacherProfile", MODE_PRIVATE
        ).contains(key);
    }

    private void loadTeacherPhoto() {
        if (teacherProfileImage == null) return;

        teacherProfileImage.setImageResource(R.drawable.profile_circle);
        String key = getTeacherPhotoPreferenceKey();
        if (key == null) return;

        String savedUri = getSharedPreferences(
                "CampusMindTeacherProfile", MODE_PRIVATE
        ).getString(key, null);

        if (savedUri != null && !savedUri.trim().isEmpty()) {
            try {
                teacherProfileImage.setImageURI(Uri.parse(savedUri));
            } catch (Exception e) {
                teacherProfileImage.setImageResource(R.drawable.profile_circle);
            }
        }
        updateProfileCompletion(firebaseAuth.getCurrentUser() == null
                ? null : firebaseAuth.getCurrentUser().getEmail());
    }

    private void removeTeacherPhoto() {
        String key = getTeacherPhotoPreferenceKey();
        if (key == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Remove profile photo?")
                .setMessage("Your saved teacher profile photo will be removed from this device.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dialog, which) -> {
                    getSharedPreferences(
                            "CampusMindTeacherProfile", MODE_PRIVATE
                    ).edit().remove(key).apply();
                    loadTeacherPhoto();
                })
                .show();
    }

    // =============================================================
    // EDIT TEACHER DETAILS
    // =============================================================

    private void showEditTeacherDetailsDialog() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(24), dp(8), dp(24), dp(4));

        EditText nameInput = new EditText(this);
        nameInput.setSingleLine(true);
        nameInput.setHint("Teacher name");
        nameInput.setText(currentTeacherName.equals("Teacher") ? "" : currentTeacherName);
        form.addView(nameInput, matchWrapParams());

        EditText collegeInput = new EditText(this);
        collegeInput.setSingleLine(true);
        collegeInput.setHint("College");
        collegeInput.setText(currentTeacherCollege);
        LinearLayout.LayoutParams collegeParams = matchWrapParams();
        collegeParams.topMargin = dp(8);
        form.addView(collegeInput, collegeParams);

        EditText courseInput = new EditText(this);
        courseInput.setSingleLine(true);
        courseInput.setHint("Course / Department");
        courseInput.setText(currentTeacherCourse);
        LinearLayout.LayoutParams courseParams = matchWrapParams();
        courseParams.topMargin = dp(8);
        form.addView(courseInput, courseParams);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Teacher Profile")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(unused -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = nameInput.getText().toString().trim();
                String college = collegeInput.getText().toString().trim();
                String course = courseInput.getText().toString().trim();

                if (name.isEmpty()) {
                    nameInput.setError("Enter your name");
                    nameInput.requestFocus();
                    return;
                }

                saveTeacherDetails(name, college, course, dialog);
            });
        });

        dialog.show();
    }

    private void saveTeacherDetails(
            String name,
            String college,
            String course,
            AlertDialog dialog
    ) {
        if (firebaseAuth.getCurrentUser() == null) return;

        String uid = firebaseAuth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("college", college);
        updates.put("course", course);
        // Intentionally do not write to the role or email fields.

        firestore.collection("students")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    currentTeacherName = name;
                    currentTeacherCollege = college;
                    currentTeacherCourse = course;

                    teacherNameText.setText(name);
                    teacherCollegeText.setText(
                            college.isEmpty() ? "Not added" : college
                    );
                    teacherCourseText.setText(
                            course.isEmpty() ? "Not added" : course
                    );
                    String email = firebaseAuth.getCurrentUser().getEmail();
                    updateProfileCompletion(email);
                    dialog.dismiss();

                    Toast.makeText(
                            this,
                            "Teacher profile updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Unable to save profile. Check Firestore update rules.",
                        Toast.LENGTH_LONG
                ).show());
    }

    private LinearLayout.LayoutParams matchWrapParams() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    // =============================================================
    // LOGOUT
    // =============================================================

    private void showLogoutDialog() {

        new androidx.appcompat.app.AlertDialog.Builder(this)
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

                            Intent intent =
                                    new Intent(
                                            TeacherSettingsActivity.this,
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
                )
                .show();
    }

    // =============================================================
    // CARD
    // =============================================================

    private MaterialCardView createCard() {

        MaterialCardView card =
                new MaterialCardView(this);

        card.setCardBackgroundColor(
                cardColor
        );

        card.setStrokeColor(
                borderColor
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
    // CONTENT
    // =============================================================

    private LinearLayout createContent() {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
        );

        return layout;
    }

    // =============================================================
    // HORIZONTAL ROW
    // =============================================================

    private LinearLayout createHorizontalRow() {

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
                dp(14),
                dp(16),
                dp(14)
        );

        return row;
    }

    // =============================================================
    // SECTION TITLE
    // =============================================================

    private void addSectionTitle(
            LinearLayout parent,
            String text
    ) {

        TextView section =
                createText(
                        text,
                        12,
                        mutedTextColor,
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
                dp(24);

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
    // COLORS
    // =============================================================

    private void loadColors() {

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

        mutedTextColor =
                getColor(
                        R.color.app_muted_text
                );

        borderColor =
                getColor(
                        R.color.app_border
                );

        primaryBlue =
                getColor(
                        R.color.primary
                );

        softBlue =
                getColor(
                        R.color.app_blue_soft
                );

        errorColor =
                getColor(
                        R.color.error
                );

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
                        & android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        return mode ==
                android.content.res.Configuration
                        .UI_MODE_NIGHT_YES;
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
