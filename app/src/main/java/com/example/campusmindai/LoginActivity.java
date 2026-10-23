package com.example.campusmindai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    private MaterialButton btnLogin;
    private TextView tvRegister;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private String selectedRole = "Student";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.loginactivity);

        // =========================================================
        // GET ROLE
        // =========================================================

        if (getIntent() != null
                && getIntent().hasExtra("role")) {

            String role =
                    getIntent().getStringExtra("role");

            if (role != null
                    && !role.trim().isEmpty()) {

                selectedRole =
                        role.trim();
            }
        }

        if (!selectedRole.equalsIgnoreCase("Teacher")) {
            selectedRole = "Student";
        } else {
            selectedRole = "Teacher";
        }

        // =========================================================
        // FIREBASE
        // =========================================================

        firebaseAuth =
                FirebaseAuth.getInstance();

        firestore =
                FirebaseFirestore.getInstance();

        // =========================================================
        // FIND VIEWS
        // =========================================================

        etEmail =
                findViewById(R.id.etEmail);

        etPassword =
                findViewById(R.id.etPassword);

        btnLogin =
                findViewById(R.id.btnLogin);

        tvRegister =
                findViewById(R.id.tvRegister);

        // =========================================================
        // LOGIN
        // =========================================================

        btnLogin.setOnClickListener(
                v -> loginUser()
        );

        // =========================================================
        // REGISTER
        // =========================================================

        tvRegister.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    LoginActivity.this,
                                    RegisterActivity.class
                            );

                    intent.putExtra(
                            "role",
                            selectedRole
                    );

                    startActivity(intent);
                }
        );
    }


    private void routeUserAfterLogin() {

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(
                    this,
                    "Please log in again.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("students")
                .document(uid)
                .get()
                .addOnSuccessListener(profile -> {

                    if (!profile.exists()) {
                        auth.signOut();

                        Toast.makeText(
                                this,
                                "User profile not found. Please contact the administrator.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    String role = profile.getString("role");

                    Class<?> destination;

                    if ("Teacher".equalsIgnoreCase(role)) {

                        destination = TeacherDashboardActivity.class;

                    } else if ("Student".equalsIgnoreCase(role)) {

                        destination = DashboardActivity.class;

                    } else {

                        auth.signOut();

                        Toast.makeText(
                                this,
                                "Invalid account role. Please contact the administrator.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    Intent intent = new Intent(
                            LoginActivity.this,
                            destination
                    );

                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to load your profile. Check your internet connection and try again.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =============================================================
    // LOGIN USER
    // =============================================================

    private void loginUser() {

        String email =
                etEmail.getText()
                        .toString()
                        .trim();

        String password =
                etPassword.getText()
                        .toString()
                        .trim();

        if (email.isEmpty()) {

            etEmail.setError(
                    "Enter your email"
            );

            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {

            etPassword.setError(
                    "Enter your password"
            );

            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);

        btnLogin.setText(
                "Signing In..."
        );

        firebaseAuth
                .signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(
                        this,
                        task -> {

                            if (!task.isSuccessful()) {

                                btnLogin.setEnabled(true);

                                btnLogin.setText(
                                        "LOGIN"
                                );

                                String message =
                                        "Login failed";

                                if (task.getException() != null) {

                                    message =
                                            task.getException()
                                                    .getMessage();
                                }

                                Toast.makeText(
                                        this,
                                        message,
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            verifyRoleAndOpenDashboard();
                        }
                );
    }

    // =============================================================
    // VERIFY ROLE
    // =============================================================

    private void verifyRoleAndOpenDashboard() {

        if (firebaseAuth.getCurrentUser() == null) {
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

                            String actualRole =
                                    document.getString(
                                            "role"
                                    );

                            if (actualRole == null
                                    || actualRole.trim().isEmpty()) {

                                actualRole = "Student";
                            }

                            // =================================================
                            // ROLE MISMATCH
                            // =================================================

                            if (!actualRole.equalsIgnoreCase(
                                    selectedRole
                            )) {

                                firebaseAuth.signOut();

                                btnLogin.setEnabled(true);

                                btnLogin.setText(
                                        "LOGIN"
                                );

                                Toast.makeText(
                                        this,
                                        "This account is registered as "
                                                + actualRole
                                                + ". Please select "
                                                + actualRole
                                                + " login.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            // =================================================
                            // TEACHER
                            // =================================================

                            if (actualRole.equalsIgnoreCase(
                                    "Teacher"
                            )) {

                                Intent intent =
                                        new Intent(
                                                LoginActivity.this,
                                                TeacherDashboardActivity.class
                                        );

                                intent.setFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                                                |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                );

                                startActivity(intent);

                                finish();

                                return;
                            }

                            // =================================================
                            // STUDENT
                            // =================================================

                            Intent intent =
                                    new Intent(
                                            LoginActivity.this,
                                            DashboardActivity.class
                                    );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                            |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);

                            finish();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            btnLogin.setEnabled(true);

                            btnLogin.setText(
                                    "LOGIN"
                            );

                            Toast.makeText(
                                    this,
                                    "Unable to verify account role",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =============================================================
    // EXISTING SESSION
    // =============================================================

    @Override
    protected void onStart() {

        super.onStart();

        if (firebaseAuth != null
                && firebaseAuth.getCurrentUser() != null) {

            verifyRoleAndOpenDashboard();
        }
    }
}