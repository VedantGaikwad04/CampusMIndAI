package com.example.campusmindai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfilePhoto;
    private MaterialButton btnAddProfilePhoto;
    private MaterialButton btnRemoveProfilePhoto;

    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileCompletion;
    private ProgressBar progressProfile;
    private TextView tvProfileCollege;
    private TextView tvProfileCourse;

    private MaterialButton btnEditProfile;
    private MaterialButton btnProfileLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private SharedPreferences preferences;

    private ActivityResultLauncher<String> imagePickerLauncher;

    private static final String PREF_NAME =
            "CampusMindProfile";

    private static final String PROFILE_IMAGE_URI =
            "profile_image_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.proflieactivity);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        preferences = getSharedPreferences(
                PREF_NAME,
                MODE_PRIVATE
        );

        initializeViews();
        setupImagePicker();
        setupButtons();
        loadProfilePhoto();
        loadProfile();
    }

    private void initializeViews() {

        ivProfilePhoto =
                findViewById(R.id.ivProfilePhoto);

        btnAddProfilePhoto =
                findViewById(R.id.btnAddProfilePhoto);

        btnRemoveProfilePhoto =
                findViewById(R.id.btnRemoveProfilePhoto);

        tvProfileName =
                findViewById(R.id.tvProfileName);

        tvProfileEmail =
                findViewById(R.id.tvProfileEmail);

        tvProfileCompletion =
                findViewById(R.id.tvProfileCompletion);

        progressProfile =
                findViewById(R.id.progressProfile);

        tvProfileCollege =
                findViewById(R.id.tvProfileCollege);

        tvProfileCourse =
                findViewById(R.id.tvProfileCourse);

        btnEditProfile =
                findViewById(R.id.btnEditProfile);

        btnProfileLogout =
                findViewById(R.id.btnProfileLogout);
    }

    private void setupImagePicker() {

        imagePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {

                            if (uri != null) {

                                try {

                                    /*
                                     * Keep permission to access the
                                     * selected image after the app
                                     * is restarted.
                                     */
                                    getContentResolver()
                                            .takePersistableUriPermission(
                                                    uri,
                                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            );

                                } catch (Exception ignored) {
                                }

                                saveProfilePhoto(uri);
                            }
                        }
                );
    }

    private void setupButtons() {

        btnAddProfilePhoto.setOnClickListener(v ->
                openImagePicker()
        );

        btnRemoveProfilePhoto.setOnClickListener(v ->
                removeProfilePhoto()
        );

        btnEditProfile.setOnClickListener(v ->
                showEditProfileDialog()
        );

        btnProfileLogout.setOnClickListener(v ->
                showLogoutDialog()
        );
    }

    private void openImagePicker() {

        imagePickerLauncher.launch(
                "image/*"
        );
    }

    private void saveProfilePhoto(Uri uri) {

        preferences.edit()
                .putString(
                        PROFILE_IMAGE_URI,
                        uri.toString()
                )
                .apply();

        loadProfilePhoto();

        Toast.makeText(
                this,
                "Profile photo updated",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void loadProfilePhoto() {

        String savedUri =
                preferences.getString(
                        PROFILE_IMAGE_URI,
                        ""
                );

        if (savedUri != null &&
                !savedUri.isEmpty()) {

            try {

                Uri uri =
                        Uri.parse(savedUri);

                ivProfilePhoto.setImageURI(uri);

                btnAddProfilePhoto.setText(
                        "CHANGE PROFILE PHOTO"
                );

                btnRemoveProfilePhoto.setVisibility(
                        View.VISIBLE
                );

            } catch (Exception e) {

                showDefaultProfilePhoto();
            }

        } else {

            showDefaultProfilePhoto();
        }
    }

    private void showDefaultProfilePhoto() {

        ivProfilePhoto.setImageResource(
                R.drawable.profile_circle
        );

        btnAddProfilePhoto.setText(
                "ADD PROFILE PHOTO"
        );

        btnRemoveProfilePhoto.setVisibility(
                View.GONE
        );
    }

    private void removeProfilePhoto() {

        new AlertDialog.Builder(this)
                .setTitle("Remove Profile Photo")
                .setMessage(
                        "Do you want to remove your profile photo?"
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "REMOVE",
                        (dialog, which) -> {

                            preferences.edit()
                                    .remove(
                                            PROFILE_IMAGE_URI
                                    )
                                    .apply();

                            showDefaultProfilePhoto();

                            Toast.makeText(
                                    this,
                                    "Profile photo removed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .show();
    }

    private void loadProfile() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId =
                auth.getCurrentUser().getUid();
        String email =
                auth.getCurrentUser().getEmail();

        if (email != null) {
            tvProfileEmail.setText(email);
        }

        db.collection("students")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {

                        tvProfileName.setText(
                                "Student"
                        );

                        tvProfileCollege.setText(
                                "Not added"
                        );

                        tvProfileCourse.setText(
                                "Not added"
                        );

                        updateCompletion(
                                "",
                                email,
                                "",
                                ""
                        );

                        return;
                    }

                    String name =
                            documentSnapshot.getString(
                                    "name"
                            );

                    String college =
                            documentSnapshot.getString(
                                    "college"
                            );

                    String course =
                            documentSnapshot.getString(
                                    "course"
                            );

                    if (name == null ||
                            name.trim().isEmpty()) {

                        name = "Student";
                    }

                    if (college == null ||
                            college.trim().isEmpty()) {

                        college = "Not added";
                    }

                    if (course == null ||
                            course.trim().isEmpty()) {

                        course = "Not added";
                    }

                    tvProfileName.setText(name);

                    tvProfileEmail.setText(
                            email != null
                                    ? email
                                    : "Not available"
                    );

                    tvProfileCollege.setText(
                            college
                    );

                    tvProfileCourse.setText(
                            course
                    );

                    updateCompletion(
                            name,
                            email,
                            college,
                            course
                    );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load profile",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void updateCompletion(
            String name,
            String email,
            String college,
            String course
    ) {

        int completed = 0;

        if (name != null &&
                !name.trim().isEmpty() &&
                !name.equalsIgnoreCase("Student")) {

            completed++;
        }

        if (email != null &&
                !email.trim().isEmpty()) {

            completed++;
        }

        if (college != null &&
                !college.trim().isEmpty() &&
                !college.equalsIgnoreCase("Not added")) {

            completed++;
        }

        if (course != null &&
                !course.trim().isEmpty() &&
                !course.equalsIgnoreCase("Not added")) {

            completed++;
        }

        int percentage =
                completed * 25;

        progressProfile.setProgress(
                percentage
        );

        tvProfileCompletion.setText(
                "Profile " +
                        percentage +
                        "% complete"
        );
    }

    private void showEditProfileDialog() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        View dialogView =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.dialog_edit_profile,
                                null
                        );

        EditText etName =
                dialogView.findViewById(
                        R.id.etEditName
                );

        EditText etCollege =
                dialogView.findViewById(
                        R.id.etEditCollege
                );

        EditText etCourse =
                dialogView.findViewById(
                        R.id.etEditCourse
                );

        etName.setText(
                tvProfileName.getText().toString()
        );

        String college =
                tvProfileCollege
                        .getText()
                        .toString();

        String course =
                tvProfileCourse
                        .getText()
                        .toString();

        if (!college.equalsIgnoreCase("Not added")) {
            etCollege.setText(college);
        }

        if (!course.equalsIgnoreCase("Not added")) {
            etCourse.setText(course);
        }

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Edit Profile")
                        .setView(dialogView)
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .setPositiveButton(
                                "SAVE",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                dialogInterface -> {

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setOnClickListener(v -> {

                        String name =
                                etName.getText()
                                        .toString()
                                        .trim();

                        String collegeValue =
                                etCollege.getText()
                                        .toString()
                                        .trim();

                        String courseValue =
                                etCourse.getText()
                                        .toString()
                                        .trim();

                        if (name.isEmpty()) {

                            etName.setError(
                                    "Enter your name"
                            );

                            return;
                        }

                        saveProfileData(
                                name,
                                collegeValue,
                                courseValue,
                                dialog
                        );
                    });
                }
        );

        dialog.show();
    }

    private void saveProfileData(
            String name,
            String college,
            String course,
            AlertDialog dialog
    ) {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId =
                auth.getCurrentUser().getUid();

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "name",
                name
        );

        updates.put(
                "college",
                college
        );

        updates.put(
                "course",
                course
        );

        db.collection("students")
                .document(userId)
                .set(
                        updates,
                        com.google.firebase.firestore.SetOptions.merge()
                )
                .addOnSuccessListener(unused -> {

                    loadProfile();

                    dialog.dismiss();

                    Toast.makeText(
                            this,
                            "Profile updated",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to update profile",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void showLogoutDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage(
                        "Are you sure you want to logout?"
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "LOGOUT",
                        (dialog, which) -> {

                            auth.signOut();

                            Intent intent =
                                    new Intent(
                                            ProfileActivity.this,
                                            LoginActivity.class
                                    );

                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);

                            finish();
                        }
                )
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (auth != null &&
                auth.getCurrentUser() != null) {

            loadProfile();
            loadProfilePhoto();
        }
    }
}