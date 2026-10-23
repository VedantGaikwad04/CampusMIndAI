package com.example.campusmindai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final long SPLASH_TIME = 2500L;

    private static final String SETTINGS_PREFS =
            "CampusMindSettings";

    private static final String DARK_MODE_KEY =
            "dark_mode";

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // =========================================================
        // LOAD DARK MODE BEFORE UI
        // =========================================================

        SharedPreferences preferences =
                getSharedPreferences(
                        SETTINGS_PREFS,
                        MODE_PRIVATE
                );

        boolean darkMode =
                preferences.getBoolean(
                        DARK_MODE_KEY,
                        false
                );

        if (darkMode) {

            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
            );

        } else {

            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
            );
        }

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_main
        );

        // =========================================================
        // FIREBASE
        // =========================================================

        firebaseAuth =
                FirebaseAuth.getInstance();

        firestore =
                FirebaseFirestore.getInstance();

        // =========================================================
        // SPLASH TIMER
        // =========================================================

        handler =
                new Handler(
                        Looper.getMainLooper()
                );

        handler.postDelayed(
                this::checkLoginSession,
                SPLASH_TIME
        );
    }

    // =============================================================
    // CHECK LOGIN SESSION
    // =============================================================

    private void checkLoginSession() {

        if (isFinishing()) {
            return;
        }

        // =========================================================
        // NO USER LOGGED IN
        // =========================================================

        if (firebaseAuth.getCurrentUser() == null) {

            openRoleSelection();

            return;
        }

        // =========================================================
        // USER IS ALREADY LOGGED IN
        // =========================================================

        routeLoggedInUser();
    }

    // =============================================================
    // ROUTE LOGGED-IN USER BY ROLE
    // =============================================================

    private void routeLoggedInUser() {

        if (firebaseAuth.getCurrentUser() == null) {

            openRoleSelection();

            return;
        }

        String userId =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        firestore
                .collection("students")
                .document(userId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (isFinishing()) {
                                return;
                            }

                            String role =
                                    document.getString(
                                            "role"
                                    );

                            // =============================================
                            // TEACHER
                            // =============================================

                            if ("Teacher"
                                    .equalsIgnoreCase(role)) {

                                openTeacherSide();

                            }

                            // =============================================
                            // STUDENT
                            // =============================================

                            else {

                                openStudentSide();
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (isFinishing()) {
                                return;
                            }

                            /*
                             * If role information cannot be read,
                             * keep the existing student experience as
                             * the fallback instead of showing teacher
                             * functionality.
                             */

                            Toast.makeText(
                                    MainActivity.this,
                                    "Unable to verify account role",
                                    Toast.LENGTH_LONG
                            ).show();

                            openStudentSide();
                        }
                );
    }

    // =============================================================
    // ROLE SELECTION
    // =============================================================

    private void openRoleSelection() {

        Intent intent =
                new Intent(
                        MainActivity.this,
                        RoleSelectionActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }

    // =============================================================
    // STUDENT SIDE
    // =============================================================

    private void openStudentSide() {

        Intent intent =
                new Intent(
                        MainActivity.this,
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

    // =============================================================
    // TEACHER SIDE
    // =============================================================

    private void openTeacherSide() {

        Intent intent =
                new Intent(
                        MainActivity.this,
                        TeacherDashboardActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }

    // =============================================================
    // CLEANUP
    // =============================================================

    @Override
    protected void onDestroy() {

        if (handler != null) {

            handler.removeCallbacksAndMessages(
                    null
            );
        }

        super.onDestroy();
    }
}