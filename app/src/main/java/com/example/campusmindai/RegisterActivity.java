
package com.example.campusmindai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etCollege;
    private TextInputEditText etCourse;
    private TextInputEditText etRollNumber;

    private MaterialButton btnRegister;
    private TextView tvLogin;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    // Default role remains Student.
    private String selectedRole = "Student";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.registeractivity);

        // GET SELECTED ROLE
        Intent intent = getIntent();

        if (intent != null && intent.hasExtra("role")) {
            String roleFromIntent = intent.getStringExtra("role");

            if (roleFromIntent != null
                    && !roleFromIntent.trim().isEmpty()) {
                selectedRole = roleFromIntent.trim();
            }
        }

        if (selectedRole.equalsIgnoreCase("Teacher")) {
            selectedRole = "Teacher";
        } else {
            selectedRole = "Student";
        }

        // FIND VIEWS
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etCollege = findViewById(R.id.etCollege);
        etCourse = findViewById(R.id.etCourse);
        etRollNumber = findViewById(R.id.etRollNumber);

        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // Show roll number only for students.
        if (etRollNumber != null) {
            etRollNumber.setVisibility(
                    selectedRole.equals("Student")
                            ? View.VISIBLE
                            : View.GONE
            );
        }

        // FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // REGISTER
        btnRegister.setOnClickListener(v -> registerUser());

        // LOGIN
        tvLogin.setOnClickListener(v -> finish());
    }

    // REGISTER USER
    private void registerUser() {



        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String college = etCollege.getText().toString().trim();
        String course = etCourse.getText().toString().trim();

        String rollNumber = etRollNumber == null
                ? ""
                : etRollNumber.getText().toString().trim();

        // NAME VALIDATION
        if (name.isEmpty()) {
            etName.setError("Enter your full name");
            etName.requestFocus();
            return;
        }

        // EMAIL VALIDATION
        if (email.isEmpty()) {
            etEmail.setError("Enter your email");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // PASSWORD VALIDATION
        if (password.isEmpty()) {
            etPassword.setError("Create a password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError(
                    "Password must be at least 6 characters"
            );
            etPassword.requestFocus();
            return;
        }

        // COLLEGE VALIDATION
        if (college.isEmpty()) {
            etCollege.setError("Enter your college");
            etCollege.requestFocus();
            return;
        }

        // COURSE VALIDATION
        if (course.isEmpty()) {
            etCourse.setError("Enter your course");
            etCourse.requestFocus();
            return;
        }

        // ROLL NUMBER VALIDATION FOR STUDENTS ONLY
        if (selectedRole.equals("Student")) {

            if (etRollNumber == null) {
                Toast.makeText(
                        this,
                        "Roll number field is missing from registeractivity.xml",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            if (rollNumber.isEmpty()) {
                etRollNumber.setError("Enter your roll number");
                etRollNumber.requestFocus();
                return;
            }
        }

        // NORMALIZE ROLE
        if (!selectedRole.equalsIgnoreCase("Teacher")
                && !selectedRole.equalsIgnoreCase("Student")) {
            selectedRole = "Student";
        }

        selectedRole = selectedRole.equalsIgnoreCase("Teacher")
                ? "Teacher"
                : "Student";

        // LOADING STATE
        btnRegister.setEnabled(false);
        btnRegister.setText("CREATING ACCOUNT...");

        // CREATE FIREBASE AUTH ACCOUNT
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    // AUTH FAILED
                    if (!task.isSuccessful()) {

                        resetRegisterButton();

                        String message = "Registration failed";

                        if (task.getException() != null) {
                            message = "Registration failed: "
                                    + task.getException().getMessage();
                        }

                        Toast.makeText(
                                this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    // CHECK CURRENT USER
                    if (firebaseAuth.getCurrentUser() == null) {

                        resetRegisterButton();

                        Toast.makeText(
                                this,
                                "Unable to create account",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    // GET UID
                    String userId = firebaseAuth.getCurrentUser().getUid();

                    // CREATE USER DOCUMENT
                    Map<String, Object> user = new HashMap<>();

                    user.put("name", name);
                    user.put("email", email);
                    user.put("college", college);
                    user.put("course", course);
                    user.put("role", selectedRole);

                    // SAVE ROLL NUMBER FOR STUDENTS ONLY
                    if (selectedRole.equals("Student")) {
                        user.put("rollNumber", rollNumber);
                        user.put(
                                "rollNumberNormalized",
                                rollNumber.toLowerCase(Locale.ROOT)
                        );
                    }

                    // SAVE TO FIRESTORE
                    firestore.collection("students")
                            .document(userId)
                            .set(user)
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(
                                        RegisterActivity.this,
                                        "Account created successfully!",
                                        Toast.LENGTH_LONG
                                ).show();

                                // GO TO CORRECT SIDE
                                if (selectedRole.equals("Teacher")) {

                                    Intent teacherIntent = new Intent(
                                            RegisterActivity.this,
                                            TeacherAttendanceActivity.class
                                    );

                                    teacherIntent.setFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK
                                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    );

                                    startActivity(teacherIntent);

                                } else {

                                    Intent studentIntent = new Intent(
                                            RegisterActivity.this,
                                            DashboardActivity.class
                                    );

                                    studentIntent.setFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK
                                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    );

                                    startActivity(studentIntent);
                                }

                                finish();
                            })
                            .addOnFailureListener(e -> {

                                resetRegisterButton();

                                Toast.makeText(
                                        RegisterActivity.this,
                                        "Database error: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                });
    }

    // RESTORE REGISTER BUTTON
    private void resetRegisterButton() {
        btnRegister.setEnabled(true);
        btnRegister.setText("CREATE ACCOUNT");
    }
}
