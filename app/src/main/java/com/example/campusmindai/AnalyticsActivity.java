package com.example.campusmindai;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnalyticsActivity extends AppCompatActivity {

    TextView tvOverallAttendance;
    TextView tvAttendanceCount;
    TextView tvTotalSubjects;
    TextView tvCompletedSessions;
    TextView tvStudyMinutes;
    TextView tvAssignmentsDone;
    TextView tvAssignmentsPending;
    TextView tvAnalyticsMessage;
    TextView tvAiInsights;

    MaterialButton btnGenerateInsights;

    ProgressBar progressAttendance;

    LinearLayout subjectAnalyticsContainer;

    FirebaseFirestore firestore;
    FirebaseAuth firebaseAuth;

    int totalLectures = 0;
    int presentLectures = 0;

    int totalSubjects = 0;

    int completedSessions = 0;
    int totalStudyMinutes = 0;

    int completedAssignments = 0;
    int pendingAssignments = 0;

    ArrayList<String> subjectAnalyticsData =
            new ArrayList<>();

    ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.analyticsactivity
        );

        tvOverallAttendance =
                findViewById(
                        R.id.tvOverallAttendance
                );

        tvAttendanceCount =
                findViewById(
                        R.id.tvAttendanceCount
                );

        tvTotalSubjects =
                findViewById(
                        R.id.tvTotalSubjects
                );

        tvCompletedSessions =
                findViewById(
                        R.id.tvCompletedSessions
                );

        tvStudyMinutes =
                findViewById(
                        R.id.tvStudyMinutes
                );

        tvAssignmentsDone =
                findViewById(
                        R.id.tvAssignmentsDone
                );

        tvAssignmentsPending =
                findViewById(
                        R.id.tvAssignmentsPending
                );

        tvAnalyticsMessage =
                findViewById(
                        R.id.tvAnalyticsMessage
                );

        tvAiInsights =
                findViewById(
                        R.id.tvAiInsights
                );

        btnGenerateInsights =
                findViewById(
                        R.id.btnGenerateInsights
                );

        progressAttendance =
                findViewById(
                        R.id.progressAttendance
                );

        subjectAnalyticsContainer =
                findViewById(
                        R.id.subjectAnalyticsContainer
                );

        firestore =
                FirebaseFirestore.getInstance();

        firebaseAuth =
                FirebaseAuth.getInstance();

        applyProfessionalTheme();

        btnGenerateInsights.setOnClickListener(
                v -> generateAiInsights()
        );

        loadAnalytics();
    }

    // =========================================================
    // PROFESSIONAL LIGHT UI
    // =========================================================

    private void applyProfessionalTheme() {

        final int pageBackground =
                Color.rgb(
                        248,
                        250,
                        252
                );

        final int white =
                Color.WHITE;

        final int primary =
                Color.rgb(
                        37,
                        99,
                        235
                );

        final int primaryDark =
                Color.rgb(
                        29,
                        78,
                        216
                );

        final int text =
                Color.rgb(
                        15,
                        23,
                        42
                );

        final int secondary =
                Color.rgb(
                        100,
                        116,
                        139
                );

        final int border =
                Color.rgb(
                        226,
                        232,
                        240
                );

        final int inputBackground =
                Color.rgb(
                        248,
                        250,
                        252
                );

        getWindow().setStatusBarColor(
                pageBackground
        );

        getWindow().setNavigationBarColor(
                pageBackground
        );

        getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );

        getWindow()
                .getDecorView()
                .setBackgroundColor(
                        pageBackground
                );

        View contentRoot =
                findViewById(
                        android.R.id.content
                );

        if (contentRoot != null) {

            contentRoot.setBackgroundColor(
                    pageBackground
            );
        }

        styleCardsAndViews(
                contentRoot,
                white,
                primary,
                text,
                secondary,
                border,
                inputBackground
        );

        // Overall attendance

        tvOverallAttendance.setTextColor(
                primary
        );

        tvOverallAttendance.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        tvAttendanceCount.setTextColor(
                secondary
        );

        // Statistics

        tvTotalSubjects.setTextColor(
                primary
        );

        tvCompletedSessions.setTextColor(
                primary
        );

        tvStudyMinutes.setTextColor(
                primary
        );

        tvAssignmentsDone.setTextColor(
                Color.rgb(
                        22,
                        163,
                        74
                )
        );

        tvAssignmentsPending.setTextColor(
                Color.rgb(
                        217,
                        119,
                        6
                )
        );

        tvAnalyticsMessage.setTextColor(
                secondary
        );

        tvAiInsights.setTextColor(
                text
        );

        // AI button

        btnGenerateInsights.setTextColor(
                white
        );

        btnGenerateInsights.setBackgroundTintList(
                ColorStateList.valueOf(
                        primary
                )
        );

        btnGenerateInsights.setRippleColor(
                ColorStateList.valueOf(
                        Color.rgb(
                                191,
                                219,
                                254
                        )
                )
        );

        btnGenerateInsights.setStrokeColor(
                ColorStateList.valueOf(
                        primary
                )
        );

        btnGenerateInsights.setCornerRadius(
                dpToPx(14)
        );

        // Overall progress

        if (progressAttendance != null) {

            progressAttendance.setProgressTintList(
                    ColorStateList.valueOf(
                            primary
                    )
            );

            progressAttendance.setProgressBackgroundTintList(
                    ColorStateList.valueOf(
                            border
                    )
            );
        }
    }

    private void styleCardsAndViews(
            View view,
            int white,
            int primary,
            int text,
            int secondary,
            int border,
            int inputBackground
    ) {

        if (view == null) {
            return;
        }

        // All cards become clean white cards

        if (view instanceof MaterialCardView) {

            MaterialCardView card =
                    (MaterialCardView) view;

            card.setCardBackgroundColor(
                    white
            );

            card.setStrokeColor(
                    border
            );

            card.setStrokeWidth(
                    dpToPx(1)
            );

            card.setRadius(
                    dpToPx(18)
            );

            card.setCardElevation(
                    dpToPx(1)
            );
        }

        // Buttons

        if (view instanceof MaterialButton) {

            MaterialButton button =
                    (MaterialButton) view;

            button.setTextColor(
                    text
            );

            button.setCornerRadius(
                    dpToPx(14)
            );

            if (button ==
                    btnGenerateInsights) {

                button.setTextColor(
                        white
                );

                button.setBackgroundTintList(
                        ColorStateList.valueOf(
                                primary
                        )
                );

            } else {

                button.setBackgroundTintList(
                        ColorStateList.valueOf(
                                Color.rgb(
                                        241,
                                        245,
                                        249
                                )
                        )
                );

                button.setStrokeColor(
                        ColorStateList.valueOf(
                                border
                        )
                );
            }

        } else if (view instanceof TextView) {

            TextView textView =
                    (TextView) view;

            textView.setTextColor(
                    text
            );
        }

        // Progress bars

        if (view instanceof ProgressBar) {

            ProgressBar progressBar =
                    (ProgressBar) view;

            progressBar.setProgressTintList(
                    ColorStateList.valueOf(
                            primary
                    )
            );

            progressBar.setProgressBackgroundTintList(
                    ColorStateList.valueOf(
                            border
                    )
            );
        }

        // Recursively style children

        if (view instanceof ViewGroup) {

            ViewGroup group =
                    (ViewGroup) view;

            for (
                    int i = 0;
                    i < group.getChildCount();
                    i++
            ) {

                styleCardsAndViews(
                        group.getChildAt(i),
                        white,
                        primary,
                        text,
                        secondary,
                        border,
                        inputBackground
                );
            }
        }
    }

    private int dpToPx(int dp) {

        return Math.round(
                dp *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    // =========================================================
    // LOAD ANALYTICS
    // =========================================================

    private void loadAnalytics() {

        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        String userId =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        tvAnalyticsMessage.setText(
                "Your academic performance overview"
        );

        loadSubjects(userId);

        loadAttendance(userId);

        loadStudySessions(userId);

        loadAssignments(userId);
    }

    // =========================================================
    // SUBJECTS
    // =========================================================

    private void loadSubjects(
            String userId
    ) {

        firestore
                .collection("students")
                .document(userId)
                .collection("subjects")
                .get()
                .addOnSuccessListener(snapshot -> {

                    Set<String> uniqueSubjects =
                            new HashSet<>();

                    for (
                            var document :
                            snapshot.getDocuments()
                    ) {

                        String name =
                                document.getString(
                                        "name"
                                );

                        if (name == null) {
                            continue;
                        }

                        name =
                                name.trim();

                        if (name.isEmpty()) {
                            continue;
                        }

                        uniqueSubjects.add(
                                name.toLowerCase(
                                        Locale.getDefault()
                                )
                        );
                    }

                    totalSubjects =
                            uniqueSubjects.size();

                    tvTotalSubjects.setText(
                            String.valueOf(
                                    totalSubjects
                            )
                    );
                });
    }

    // =========================================================
    // ATTENDANCE
    // =========================================================

    private void loadAttendance(
            String userId
    ) {

        firestore
                .collection("students")
                .document(userId)
                .collection("attendance")
                .get()
                .addOnSuccessListener(snapshot -> {

                    totalLectures = 0;

                    presentLectures = 0;

                    Map<String, Integer> totalMap =
                            new HashMap<>();

                    Map<String, Integer> presentMap =
                            new HashMap<>();

                    Map<String, String> displayNames =
                            new HashMap<>();

                    for (
                            var document :
                            snapshot.getDocuments()
                    ) {

                        String subject =
                                document.getString(
                                        "subject"
                                );

                        String status =
                                document.getString(
                                        "status"
                                );

                        if (
                                subject == null ||
                                        status == null
                        ) {
                            continue;
                        }

                        subject =
                                subject.trim();

                        if (subject.isEmpty()) {
                            continue;
                        }

                        String key =
                                subject.toLowerCase(
                                        Locale.getDefault()
                                );

                        int subjectTotal =
                                totalMap.containsKey(key)
                                        ? totalMap.get(key)
                                        : 0;

                        totalMap.put(
                                key,
                                subjectTotal + 1
                        );

                        displayNames.put(
                                key,
                                subject
                        );

                        if (
                                status.equalsIgnoreCase(
                                        "Present"
                                )
                        ) {

                            int subjectPresent =
                                    presentMap.containsKey(
                                            key
                                    )
                                            ? presentMap.get(key)
                                            : 0;

                            presentMap.put(
                                    key,
                                    subjectPresent + 1
                            );
                        }
                    }

                    totalLectures =
                            snapshot.size();

                    for (
                            Integer value :
                            presentMap.values()
                    ) {

                        presentLectures +=
                                value;
                    }

                    updateOverallAttendance();

                    loadSubjectAttendance(
                            totalMap,
                            presentMap,
                            displayNames
                    );
                });
    }

    // =========================================================
    // SUBJECT-WISE ATTENDANCE
    // =========================================================

    private void loadSubjectAttendance(
            Map<String, Integer> totalMap,
            Map<String, Integer> presentMap,
            Map<String, String> displayNames
    ) {

        subjectAnalyticsContainer
                .removeAllViews();

        subjectAnalyticsData
                .clear();

        for (
                String key :
                totalMap.keySet()
        ) {

            int total =
                    totalMap.get(key);

            int present =
                    presentMap.containsKey(key)
                            ? presentMap.get(key)
                            : 0;

            double percentage =
                    total == 0
                            ? 0
                            : (present * 100.0)
                              / total;

            String displayName =
                    displayNames.get(key);

            addSubjectAnalyticsCard(
                    displayName,
                    present,
                    total,
                    percentage
            );

            subjectAnalyticsData.add(
                    displayName +
                            ": " +
                            String.format(
                                    Locale.getDefault(),
                                    "%.1f%%",
                                    percentage
                            ) +
                            " (" +
                            present +
                            "/" +
                            total +
                            " present)"
            );
        }
    }

    // =========================================================
    // SUBJECT ANALYTICS CARD
    // =========================================================

    private void addSubjectAnalyticsCard(
            String subject,
            int present,
            int total,
            double percentage
    ) {

        MaterialCardView card =
                new MaterialCardView(this);

        card.setRadius(
                dpToPx(18)
        );

        card.setCardElevation(
                dpToPx(1)
        );

        card.setUseCompatPadding(
                true
        );

        // PROFESSIONAL LIGHT CARD

        card.setCardBackgroundColor(
                Color.WHITE
        );

        card.setStrokeColor(
                Color.rgb(
                        226,
                        232,
                        240
                )
        );

        card.setStrokeWidth(
                dpToPx(1)
        );

        LinearLayout.LayoutParams
                cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin =
                dpToPx(12);

        card.setLayoutParams(
                cardParams
        );

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                dpToPx(18),
                dpToPx(16),
                dpToPx(18),
                dpToPx(16)
        );

        // -----------------------------------------------------
        // TITLE ROW
        // -----------------------------------------------------

        LinearLayout titleRow =
                new LinearLayout(this);

        titleRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        titleRow.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        TextView title =
                new TextView(this);

        title.setText(
                subject
        );

        title.setTextSize(
                16
        );

        title.setTextColor(
                Color.rgb(
                        15,
                        23,
                        42
                )
        );

        title.setTypeface(
                null,
                Typeface.BOLD
        );

        LinearLayout.LayoutParams
                titleParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        titleRow.addView(
                title,
                titleParams
        );

        int roundedPercentage =
                (int) Math.round(
                        percentage
                );

        TextView percentageView =
                new TextView(this);

        percentageView.setText(
                roundedPercentage +
                        "%"
        );

        percentageView.setTextColor(
                getAttendanceColor(
                        percentage
                )
        );

        percentageView.setTextSize(
                20
        );

        percentageView.setTypeface(
                null,
                Typeface.BOLD
        );

        titleRow.addView(
                percentageView
        );

        layout.addView(
                titleRow
        );

        // -----------------------------------------------------
        // COUNT
        // -----------------------------------------------------

        TextView count =
                new TextView(this);

        count.setText(
                present +
                        " present  •  " +
                        total +
                        " lectures"
        );

        count.setTextSize(
                13
        );

        count.setTextColor(
                Color.rgb(
                        100,
                        116,
                        139
                )
        );

        LinearLayout.LayoutParams
                countParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        countParams.topMargin =
                dpToPx(5);

        layout.addView(
                count,
                countParams
        );

        // -----------------------------------------------------
        // PROGRESS
        // -----------------------------------------------------

        ProgressBar progressBar =
                new ProgressBar(
                        this,
                        null,
                        android.R.attr.progressBarStyleHorizontal
                );

        progressBar.setMax(
                100
        );

        progressBar.setProgress(
                (int) Math.round(
                        percentage
                )
        );

        progressBar.setProgressTintList(
                ColorStateList.valueOf(
                        getAttendanceColor(
                                percentage
                        )
                )
        );

        progressBar.setProgressBackgroundTintList(
                ColorStateList.valueOf(
                        Color.rgb(
                                226,
                                232,
                                240
                        )
                )
        );

        LinearLayout.LayoutParams
                progressParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(7)
                );

        progressParams.topMargin =
                dpToPx(13);

        layout.addView(
                progressBar,
                progressParams
        );

        // -----------------------------------------------------
        // ATTENDANCE TEXT
        // -----------------------------------------------------

        TextView percentageText =
                new TextView(this);

        percentageText.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f%% attendance",
                        percentage
                )
        );

        percentageText.setTextSize(
                13
        );

        percentageText.setTextColor(
                getAttendanceColor(
                        percentage
                )
        );

        percentageText.setTypeface(
                null,
                Typeface.BOLD
        );

        LinearLayout.LayoutParams
                percentParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        percentParams.topMargin =
                dpToPx(8);

        layout.addView(
                percentageText,
                percentParams
        );

        card.addView(
                layout
        );

        subjectAnalyticsContainer.addView(
                card,
                cardParams
        );
    }

    // =========================================================
    // OVERALL ATTENDANCE
    // =========================================================

    private void updateOverallAttendance() {

        double percentage =
                totalLectures == 0
                        ? 0
                        : (presentLectures * 100.0)
                          / totalLectures;

        tvOverallAttendance.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f%%",
                        percentage
                )
        );

        tvAttendanceCount.setText(
                presentLectures +
                        " present out of " +
                        totalLectures +
                        " lectures"
        );

        progressAttendance.setMax(
                100
        );

        progressAttendance.setProgress(
                (int) Math.round(
                        percentage
                )
        );

        progressAttendance.setProgressTintList(
                ColorStateList.valueOf(
                        getAttendanceColor(
                                percentage
                        )
                )
        );

        progressAttendance.setProgressBackgroundTintList(
                ColorStateList.valueOf(
                        Color.rgb(
                                226,
                                232,
                                240
                        )
                )
        );

        tvOverallAttendance.setTextColor(
                getAttendanceColor(
                        percentage
                )
        );
    }

    // =========================================================
    // ATTENDANCE COLOR
    // =========================================================

    private int getAttendanceColor(
            double percentage
    ) {

        // Green = healthy

        if (percentage >= 75) {

            return Color.rgb(
                    22,
                    163,
                    74
            );
        }

        // Orange = warning

        else if (percentage >= 60) {

            return Color.rgb(
                    217,
                    119,
                    6
            );
        }

        // Red = low attendance

        else {

            return Color.rgb(
                    220,
                    38,
                    38
            );
        }
    }

    // =========================================================
    // STUDY SESSIONS
    // =========================================================

    private void loadStudySessions(
            String userId
    ) {

        firestore
                .collection("students")
                .document(userId)
                .collection("studySessions")
                .get()
                .addOnSuccessListener(snapshot -> {

                    completedSessions = 0;

                    totalStudyMinutes = 0;

                    for (
                            var document :
                            snapshot.getDocuments()
                    ) {

                        String status =
                                document.getString(
                                        "status"
                                );

                        Long duration =
                                document.getLong(
                                        "durationMinutes"
                                );

                        if (
                                status != null &&
                                        status.equalsIgnoreCase(
                                                "Completed"
                                        )
                        ) {

                            completedSessions++;

                            if (duration != null) {

                                totalStudyMinutes +=
                                        duration.intValue();
                            }
                        }
                    }

                    tvCompletedSessions.setText(
                            String.valueOf(
                                    completedSessions
                            )
                    );

                    tvStudyMinutes.setText(
                            String.valueOf(
                                    totalStudyMinutes
                            )
                    );
                });
    }

    // =========================================================
    // ASSIGNMENTS
    // =========================================================

    private void loadAssignments(
            String userId
    ) {

        firestore
                .collection("students")
                .document(userId)
                .collection("assignments")
                .get()
                .addOnSuccessListener(snapshot -> {

                    completedAssignments = 0;

                    pendingAssignments = 0;

                    for (
                            var document :
                            snapshot.getDocuments()
                    ) {

                        String status =
                                document.getString(
                                        "status"
                                );

                        if (
                                status != null &&
                                        status.equalsIgnoreCase(
                                                "Completed"
                                        )
                        ) {

                            completedAssignments++;

                        } else {

                            pendingAssignments++;
                        }
                    }

                    tvAssignmentsDone.setText(
                            String.valueOf(
                                    completedAssignments
                            )
                    );

                    tvAssignmentsPending.setText(
                            String.valueOf(
                                    pendingAssignments
                            )
                    );
                });
    }

    // =========================================================
    // AI INSIGHTS
    // =========================================================

    private void generateAiInsights() {

        if (firebaseAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnGenerateInsights.setEnabled(
                false
        );

        btnGenerateInsights.setText(
                "ANALYZING..."
        );

        tvAiInsights.setText(
                "Gemini is analyzing your academic data..."
        );

        String overallAttendance =
                tvOverallAttendance
                        .getText()
                        .toString();

        String assignmentData =
                "Completed: " +
                        completedAssignments +
                        ", Pending: " +
                        pendingAssignments;

        String studyData =
                completedSessions +
                        " completed focus sessions, " +
                        totalStudyMinutes +
                        " minutes of study time.";

        String subjectData =
                subjectAnalyticsData.isEmpty()
                        ? "No subject attendance recorded."
                        : joinSubjectData();

        String prompt =
                "You are CampusMind, an AI academic assistant " +
                        "for a college student. Analyze the following " +
                        "student academic data and give practical " +
                        "personalized recommendations.\n\n" +

                        "Overall attendance: " +
                        overallAttendance +
                        "\n" +

                        "Subjects: " +
                        totalSubjects +
                        "\n" +

                        "Subject attendance:\n" +
                        subjectData +
                        "\n\n" +

                        "Assignments: " +
                        assignmentData +
                        "\n" +

                        "Study activity: " +
                        studyData +
                        "\n\n" +

                        "Give a concise response with exactly these " +
                        "sections:\n" +
                        "PERFORMANCE\n" +
                        "PRIORITY\n" +
                        "ACTION PLAN\n" +
                        "MOTIVATION\n\n" +

                        "Keep the advice realistic for a college student. " +
                        "Do not invent academic data.";

        callGemini(
                prompt
        );
    }

    // =========================================================
    // SUBJECT DATA FOR AI
    // =========================================================

    private String joinSubjectData() {

        StringBuilder builder =
                new StringBuilder();

        for (
                String data :
                subjectAnalyticsData
        ) {

            builder.append(
                            "• "
                    )
                    .append(data)
                    .append("\n");
        }

        return builder.toString();
    }

    // =========================================================
    // GEMINI API
    // =========================================================

    private void callGemini(
            String prompt
    ) {

        executorService.execute(() -> {

            HttpURLConnection connection =
                    null;

            try {

                String apiKey =
                        getString(
                                R.string.gemini_api_key
                        );

                String endpoint =
                        "https://generativelanguage.googleapis.com/" +
                                "v1beta/models/gemini-3.6-flash:generateContent" +
                                "?key=" +
                                apiKey;

                URL url =
                        new URL(
                                endpoint
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod(
                        "POST"
                );

                connection.setConnectTimeout(
                        15000
                );

                connection.setReadTimeout(
                        30000
                );

                connection.setDoOutput(
                        true
                );

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                JSONObject textPart =
                        new JSONObject();

                textPart.put(
                        "text",
                        prompt
                );

                JSONArray parts =
                        new JSONArray();

                parts.put(
                        textPart
                );

                JSONObject content =
                        new JSONObject();

                content.put(
                        "parts",
                        parts
                );

                JSONArray contents =
                        new JSONArray();

                contents.put(
                        content
                );

                JSONObject request =
                        new JSONObject();

                request.put(
                        "contents",
                        contents
                );

                String requestBody =
                        request.toString();

                OutputStream outputStream =
                        connection.getOutputStream();

                outputStream.write(
                        requestBody.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

                outputStream.flush();

                outputStream.close();

                int responseCode =
                        connection.getResponseCode();

                InputStream inputStream;

                if (
                        responseCode >= 200 &&
                                responseCode < 300
                ) {

                    inputStream =
                            connection.getInputStream();

                } else {

                    inputStream =
                            connection.getErrorStream();
                }

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        inputStream
                                )
                        );

                StringBuilder response =
                        new StringBuilder();

                String line;

                while (
                        (line =
                                reader.readLine())
                                != null
                ) {

                    response.append(
                            line
                    );
                }

                reader.close();

                if (
                        responseCode < 200 ||
                                responseCode >= 300
                ) {

                    throw new Exception(
                            "API Error: " +
                                    responseCode
                    );
                }

                JSONObject json =
                        new JSONObject(
                                response.toString()
                        );

                JSONArray candidates =
                        json.getJSONArray(
                                "candidates"
                        );

                JSONObject firstCandidate =
                        candidates.getJSONObject(
                                0
                        );

                JSONObject responseContent =
                        firstCandidate.getJSONObject(
                                "content"
                        );

                JSONArray responseParts =
                        responseContent.getJSONArray(
                                "parts"
                        );

                String aiText =
                        responseParts
                                .getJSONObject(0)
                                .getString(
                                        "text"
                                );

                runOnUiThread(() -> {

                    tvAiInsights.setText(
                            aiText.trim()
                    );

                    btnGenerateInsights.setText(
                            "ANALYZE AGAIN"
                    );

                    btnGenerateInsights.setEnabled(
                            true
                    );
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    tvAiInsights.setText(
                            "Unable to generate AI insights right now.\n\n" +
                                    "Please check your internet connection " +
                                    "and try again."
                    );

                    btnGenerateInsights.setText(
                            "TRY AGAIN"
                    );

                    btnGenerateInsights.setEnabled(
                            true
                    );
                });

            } finally {

                if (connection != null) {

                    connection.disconnect();
                }
            }
        });
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        executorService.shutdownNow();

        super.onDestroy();
    }
}