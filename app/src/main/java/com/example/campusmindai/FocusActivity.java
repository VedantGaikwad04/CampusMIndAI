package com.example.campusmindai;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FocusActivity extends AppCompatActivity {

    private static final long FOCUS_TIME =
            45 * 60 * 1000L;

    private TextView tvTimer;
    private TextView tvFocusStatus;
    private Spinner spinnerSubject;

    private Button btnStartPause;
    private Button btnReset;
    private Button btnFinishSession;

    private CountDownTimer countDownTimer;

    private boolean timerRunning = false;

    private long timeLeftMillis =
            FOCUS_TIME;

    private long focusedMillis = 0;

    private long currentRunStartMillis = 0;

    private String selectedSubject = "";

    private boolean subjectOnly = false;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final ArrayList<String> subjectList =
            new ArrayList<>();

    private ArrayAdapter<String> subjectAdapter;

    // =========================================================
    // LIGHT MODE COLORS
    // =========================================================

    private static final int COLOR_BACKGROUND =
            Color.rgb(248, 250, 252);

    private static final int COLOR_CARD =
            Color.WHITE;

    private static final int COLOR_PRIMARY =
            Color.rgb(37, 99, 235);

    private static final int COLOR_PRIMARY_DARK =
            Color.rgb(29, 78, 216);

    private static final int COLOR_TEXT =
            Color.rgb(15, 23, 42);

    private static final int COLOR_SECONDARY =
            Color.rgb(71, 85, 105);

    private static final int COLOR_MUTED =
            Color.rgb(100, 116, 139);

    private static final int COLOR_BORDER =
            Color.rgb(203, 213, 225);

    private static final int COLOR_INPUT =
            Color.rgb(248, 250, 252);

    private static final int COLOR_SOFT_BLUE =
            Color.rgb(239, 246, 255);

    private static final int COLOR_SOFT_GREEN =
            Color.rgb(240, 253, 244);

    private static final int COLOR_GREEN =
            Color.rgb(22, 163, 74);

    private static final int COLOR_SOFT_GRAY =
            Color.rgb(241, 245, 249);

    private static final int COLOR_RED =
            Color.rgb(220, 38, 38);

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.focusactivity
        );

        tvTimer =
                findViewById(
                        R.id.tvTimer
                );

        tvFocusStatus =
                findViewById(
                        R.id.tvFocusStatus
                );

        spinnerSubject =
                findViewById(
                        R.id.spinnerSubject
                );

        btnStartPause =
                findViewById(
                        R.id.btnStartPause
                );

        btnReset =
                findViewById(
                        R.id.btnReset
                );

        btnFinishSession =
                findViewById(
                        R.id.btnFinishSession
                );

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();

        selectedSubject =
                getIntent()
                        .getStringExtra(
                                "subjectName"
                        );

        subjectOnly =
                getIntent()
                        .getBooleanExtra(
                                "subjectOnly",
                                false
                        );

        if (
                selectedSubject == null
        ) {

            selectedSubject = "";
        }

        setupProfessionalUI();

        applyThemeMode();

        setupSpinner();

        setupButtons();

        if (
                subjectOnly
                        &&
                        !selectedSubject.isEmpty()
        ) {

            subjectList.clear();

            subjectList.add(
                    selectedSubject
            );

            subjectAdapter
                    .notifyDataSetChanged();

            spinnerSubject.setEnabled(
                    false
            );

            tvFocusStatus.setText(
                    "Ready to focus on " +
                            selectedSubject
            );

        } else {

            loadSubjects();
        }

        updateTimerText();
    }

    // =========================================================
    // PROFESSIONAL UI
    // =========================================================

    private void setupProfessionalUI() {

        getWindow().setStatusBarColor(
                COLOR_BACKGROUND
        );

        getWindow().setNavigationBarColor(
                COLOR_BACKGROUND
        );

        getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );

        View root =
                findViewById(
                        android.R.id.content
                );

        if (root != null) {

            root.setBackgroundColor(
                    COLOR_BACKGROUND
            );
        }

        if (tvTimer != null) {

            tvTimer.setTextColor(
                    COLOR_PRIMARY
            );

            tvTimer.setTextSize(
                    46
            );

            tvTimer.setTypeface(
                    Typeface.create(
                            "sans-serif",
                            Typeface.BOLD
                    )
            );

            tvTimer.setGravity(
                    Gravity.CENTER
            );

            tvTimer.setPadding(
                    dpToPx(12),
                    dpToPx(18),
                    dpToPx(12),
                    dpToPx(18)
            );

            tvTimer.setBackground(
                    createRoundedBackground(
                            COLOR_SOFT_BLUE,
                            COLOR_BORDER,
                            20
                    )
            );
        }

        if (tvFocusStatus != null) {

            tvFocusStatus.setTextColor(
                    COLOR_SECONDARY
            );

            tvFocusStatus.setTextSize(
                    14
            );

            tvFocusStatus.setTypeface(
                    null,
                    Typeface.BOLD
            );

            tvFocusStatus.setGravity(
                    Gravity.CENTER
            );
        }

        if (spinnerSubject != null) {

            spinnerSubject.setBackground(
                    createRoundedBackground(
                            COLOR_INPUT,
                            COLOR_BORDER,
                            14
                    )
            );

            spinnerSubject.setPadding(
                    dpToPx(14),
                    0,
                    dpToPx(14),
                    0
            );

            spinnerSubject.setPopupBackgroundDrawable(
                    createRoundedBackground(
                            COLOR_CARD,
                            COLOR_BORDER,
                            14
                    )
            );
        }

        if (btnStartPause != null) {

            btnStartPause.setTextColor(
                    Color.WHITE
            );

            btnStartPause.setTextSize(
                    14
            );

            btnStartPause.setTypeface(
                    null,
                    Typeface.BOLD
            );

            btnStartPause.setGravity(
                    Gravity.CENTER
            );

            btnStartPause.setMinHeight(
                    dpToPx(54)
            );

            btnStartPause.setPadding(
                    dpToPx(14),
                    0,
                    dpToPx(14),
                    0
            );

            btnStartPause.setBackground(
                    createRoundedBackground(
                            COLOR_PRIMARY,
                            COLOR_PRIMARY,
                            15
                    )
            );

            btnStartPause.setBackgroundTintList(
                    ColorStateList.valueOf(
                            COLOR_PRIMARY
                    )
            );
        }

        if (btnReset != null) {

            btnReset.setTextColor(
                    COLOR_SECONDARY
            );

            btnReset.setTextSize(
                    13
            );

            btnReset.setTypeface(
                    null,
                    Typeface.BOLD
            );

            btnReset.setGravity(
                    Gravity.CENTER
            );

            btnReset.setMinHeight(
                    dpToPx(52)
            );

            btnReset.setPadding(
                    dpToPx(12),
                    0,
                    dpToPx(12),
                    0
            );

            btnReset.setBackground(
                    createRoundedBackground(
                            COLOR_SOFT_GRAY,
                            COLOR_BORDER,
                            14
                    )
            );

            btnReset.setBackgroundTintList(
                    ColorStateList.valueOf(
                            COLOR_SOFT_GRAY
                    )
            );
        }

        if (btnFinishSession != null) {

            btnFinishSession.setTextColor(
                    COLOR_PRIMARY
            );

            btnFinishSession.setTextSize(
                    13
            );

            btnFinishSession.setTypeface(
                    null,
                    Typeface.BOLD
            );

            btnFinishSession.setGravity(
                    Gravity.CENTER
            );

            btnFinishSession.setMinHeight(
                    dpToPx(52)
            );

            btnFinishSession.setPadding(
                    dpToPx(12),
                    0,
                    dpToPx(12),
                    0
            );

            btnFinishSession.setBackground(
                    createRoundedBackground(
                            COLOR_SOFT_BLUE,
                            COLOR_PRIMARY,
                            14
                    )
            );

            btnFinishSession.setBackgroundTintList(
                    ColorStateList.valueOf(
                            COLOR_SOFT_BLUE
                    )
            );
        }

        styleAllTextViews(
                root
        );
    }

    // =========================================================
    // TEXT STYLING
    // =========================================================

    private void styleAllTextViews(
            View view
    ) {

        if (view == null) {
            return;
        }

        if (view instanceof TextView) {

            TextView text =
                    (TextView) view;

            if (
                    text != tvTimer
                            &&
                            text != tvFocusStatus
                            &&
                            text != btnStartPause
                            &&
                            text != btnReset
                            &&
                            text != btnFinishSession
            ) {

                String value =
                        text.getText()
                                .toString()
                                .trim();

                if (
                        value.equalsIgnoreCase(
                                "FOCUS MODE"
                        )
                                ||
                                value.equalsIgnoreCase(
                                        "FOCUS SESSION"
                                )
                                ||
                                value.equalsIgnoreCase(
                                        "SELECT SUBJECT"
                                )
                ) {

                    text.setTextColor(
                            COLOR_TEXT
                    );

                    text.setTypeface(
                            null,
                            Typeface.BOLD
                    );

                } else {

                    text.setTextColor(
                            COLOR_SECONDARY
                    );
                }
            }
        }

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

    // =========================================================
    // SUBJECT SPINNER
    // =========================================================

    private void setupSpinner() {

        subjectAdapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout
                                .simple_spinner_item,
                        subjectList
                ) {

                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            ViewGroup parent
                    ) {

                        TextView text =
                                (TextView)
                                        super.getView(
                                                position,
                                                convertView,
                                                parent
                                        );

                        text.setTextColor(
                                isDarkModeEnabled()
                                        ? Color.rgb(
                                        248,
                                        250,
                                        252
                                )
                                        : COLOR_TEXT
                        );

                        text.setTextSize(
                                15
                        );

                        text.setTypeface(
                                null,
                                Typeface.BOLD
                        );

                        text.setGravity(
                                Gravity.CENTER_VERTICAL
                        );

                        text.setPadding(
                                dpToPx(2),
                                0,
                                dpToPx(2),
                                0
                        );

                        return text;
                    }

                    @Override
                    public View getDropDownView(
                            int position,
                            View convertView,
                            ViewGroup parent
                    ) {

                        TextView text =
                                (TextView)
                                        super.getDropDownView(
                                                position,
                                                convertView,
                                                parent
                                        );

                        boolean dark =
                                isDarkModeEnabled();

                        text.setTextColor(
                                dark
                                        ? Color.rgb(
                                        248,
                                        250,
                                        252
                                )
                                        : COLOR_TEXT
                        );

                        text.setTextSize(
                                14
                        );

                        text.setGravity(
                                Gravity.CENTER_VERTICAL
                        );

                        text.setPadding(
                                dpToPx(16),
                                dpToPx(14),
                                dpToPx(16),
                                dpToPx(14)
                        );

                        text.setBackgroundColor(
                                dark
                                        ? Color.rgb(
                                        30,
                                        41,
                                        59
                                )
                                        : Color.WHITE
                        );

                        return text;
                    }
                };

        spinnerSubject.setAdapter(
                subjectAdapter
        );
    }

    // =========================================================
    // BUTTONS
    // =========================================================

    private void setupButtons() {

        btnStartPause.setOnClickListener(
                v -> {

                    if (timerRunning) {

                        pauseTimer();

                    } else {

                        startTimer();
                    }
                }
        );

        btnReset.setOnClickListener(
                v -> resetTimer()
        );

        btnFinishSession.setOnClickListener(
                v -> finishSession()
        );
    }

    // =========================================================
    // LOAD SUBJECTS
    // =========================================================

    private void loadSubjects() {

        if (
                auth.getCurrentUser() == null
        ) {

            return;
        }

        String userId =
                auth.getCurrentUser()
                        .getUid();

        db.collection("students")
                .document(userId)
                .collection("subjects")
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            subjectList.clear();

                            for (
                                    QueryDocumentSnapshot document :
                                    queryDocumentSnapshots
                            ) {

                                String name =
                                        document.getString(
                                                "name"
                                        );

                                if (
                                        name == null
                                ) {

                                    continue;
                                }

                                name =
                                        name.trim();

                                if (
                                        name.isEmpty()
                                ) {

                                    continue;
                                }

                                if (
                                        !containsSubject(
                                                name
                                        )
                                ) {

                                    subjectList.add(
                                            name
                                    );
                                }
                            }

                            subjectAdapter
                                    .notifyDataSetChanged();

                            if (
                                    !subjectList.isEmpty()
                                            &&
                                            selectedSubject
                                                    .isEmpty()
                            ) {

                                selectedSubject =
                                        subjectList.get(0);

                                tvFocusStatus.setText(
                                        "Ready to focus on " +
                                                selectedSubject
                                );
                            }

                            applyThemeMode();
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Unable to load subjects",
                                        Toast.LENGTH_SHORT
                                ).show()
                );
    }

    private boolean containsSubject(
            String name
    ) {

        for (
                String subject :
                subjectList
        ) {

            if (
                    subject.equalsIgnoreCase(
                            name
                    )
            ) {

                return true;
            }
        }

        return false;
    }

    // =========================================================
    // START TIMER
    // =========================================================

    private void startTimer() {

        if (
                spinnerSubject.getSelectedItem() == null
                        ||
                        spinnerSubject.getSelectedItem()
                                .toString()
                                .trim()
                                .isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Please select a subject",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        selectedSubject =
                spinnerSubject
                        .getSelectedItem()
                        .toString()
                        .trim();

        currentRunStartMillis =
                System.currentTimeMillis();

        timerRunning = true;

        btnStartPause.setText(
                "PAUSE"
        );

        tvFocusStatus.setText(
                "Focusing on " +
                        selectedSubject +
                        "..."
        );

        countDownTimer =
                new CountDownTimer(
                        timeLeftMillis,
                        1000
                ) {

                    @Override
                    public void onTick(
                            long millisUntilFinished
                    ) {

                        timeLeftMillis =
                                millisUntilFinished;

                        updateTimerText();
                    }

                    @Override
                    public void onFinish() {

                        addCurrentRunningTime();

                        timeLeftMillis =
                                0;

                        timerRunning =
                                false;

                        btnStartPause.setText(
                                "START"
                        );

                        tvFocusStatus.setText(
                                "45-minute focus session completed!"
                        );

                        saveStudySession();
                    }
                };

        countDownTimer.start();
    }

    // =========================================================
    // PAUSE
    // =========================================================

    private void pauseTimer() {

        if (
                countDownTimer != null
        ) {

            countDownTimer.cancel();
        }

        addCurrentRunningTime();

        timerRunning =
                false;

        btnStartPause.setText(
                "RESUME"
        );

        tvFocusStatus.setText(
                "Paused — " +
                        formatDuration(
                                focusedMillis
                        ) +
                        " focused"
        );
    }

    // =========================================================
    // ADD CURRENT RUNNING TIME
    // =========================================================

    private void addCurrentRunningTime() {

        if (
                currentRunStartMillis <= 0
        ) {

            return;
        }

        long now =
                System.currentTimeMillis();

        long elapsed =
                now -
                        currentRunStartMillis;

        if (
                elapsed > 0
        ) {

            focusedMillis +=
                    elapsed;
        }

        currentRunStartMillis =
                0;
    }

    // =========================================================
    // RESET
    // =========================================================

    private void resetTimer() {

        if (
                countDownTimer != null
        ) {

            countDownTimer.cancel();
        }

        timerRunning =
                false;

        timeLeftMillis =
                FOCUS_TIME;

        focusedMillis =
                0;

        currentRunStartMillis =
                0;

        btnStartPause.setText(
                "START"
        );

        tvFocusStatus.setText(
                selectedSubject.isEmpty()
                        ? "Ready to focus"
                        : "Ready to focus on " +
                          selectedSubject
        );

        updateTimerText();
    }

    // =========================================================
    // FINISH SESSION
    // =========================================================

    private void finishSession() {

        if (
                timerRunning
        ) {

            if (
                    countDownTimer != null
            ) {

                countDownTimer.cancel();
            }

            addCurrentRunningTime();

            timerRunning =
                    false;
        }

        if (
                focusedMillis <= 0
        ) {

            Toast.makeText(
                    this,
                    "No focused time to save",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        tvFocusStatus.setText(
                "Saving focused session..."
        );

        saveStudySession();
    }

    // =========================================================
    // SAVE SESSION
    // =========================================================

    private void saveStudySession() {

        if (
                auth.getCurrentUser() == null
        ) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (
                selectedSubject == null
                        ||
                        selectedSubject
                                .trim()
                                .isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Select a subject first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String userId =
                auth.getCurrentUser()
                        .getUid();

        long durationMillis =
                focusedMillis;

        long durationSeconds =
                TimeUnit.MILLISECONDS
                        .toSeconds(
                                durationMillis
                        );

        double durationMinutes =
                durationMillis /
                        60000.0;

        long endTime =
                System.currentTimeMillis();

        long startTime =
                endTime -
                        durationMillis;

        String date =
                new SimpleDateFormat(
                        "dd-MM-yyyy",
                        Locale.getDefault()
                ).format(
                        new Date(
                                endTime
                        )
                );

        Map<String, Object> session =
                new HashMap<>();

        session.put(
                "subject",
                selectedSubject
        );

        session.put(
                "durationMinutes",
                durationMinutes
        );

        session.put(
                "durationSeconds",
                durationSeconds
        );

        session.put(
                "durationMillis",
                durationMillis
        );

        session.put(
                "startTime",
                startTime
        );

        session.put(
                "endTime",
                endTime
        );

        session.put(
                "date",
                date
        );

        session.put(
                "status",
                "Completed"
        );

        session.put(
                "createdAt",
                System.currentTimeMillis()
        );

        db.collection("students")
                .document(userId)
                .collection("studySessions")
                .add(session)
                .addOnSuccessListener(
                        documentReference -> {

                            Toast.makeText(
                                    this,
                                    "Saved: " +
                                            formatDuration(
                                                    focusedMillis
                                            ),
                                    Toast.LENGTH_SHORT
                            ).show();

                            tvFocusStatus.setText(
                                    "Study session saved successfully"
                            );

                            timeLeftMillis =
                                    FOCUS_TIME;

                            focusedMillis =
                                    0;

                            currentRunStartMillis =
                                    0;

                            btnStartPause.setText(
                                    "START"
                            );

                            updateTimerText();
                        }
                )
                .addOnFailureListener(
                        e ->
                                tvFocusStatus.setText(
                                        "Unable to save study session"
                                )
                );
    }

    // =========================================================
    // TIMER TEXT
    // =========================================================

    private void updateTimerText() {

        long totalSeconds =
                TimeUnit.MILLISECONDS
                        .toSeconds(
                                timeLeftMillis
                        );

        long minutes =
                totalSeconds / 60;

        long seconds =
                totalSeconds % 60;

        tvTimer.setText(
                String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        minutes,
                        seconds
                )
        );
    }

    // =========================================================
    // FORMAT DURATION
    // =========================================================

    private String formatDuration(
            long millis
    ) {

        if (
                millis <= 0
        ) {

            return "0 sec";
        }

        long totalSeconds =
                TimeUnit.MILLISECONDS
                        .toSeconds(
                                millis
                        );

        long hours =
                totalSeconds / 3600;

        long minutes =
                (totalSeconds % 3600)
                        / 60;

        long seconds =
                totalSeconds % 60;

        if (
                hours > 0
        ) {

            return hours +
                    " hr " +
                    minutes +
                    " min";

        } else if (
                minutes > 0
        ) {

            return minutes +
                    " min " +
                    seconds +
                    " sec";

        } else {

            return seconds +
                    " sec";
        }
    }

    // =========================================================
    // DARK MODE
    // =========================================================

    private boolean isDarkModeEnabled() {

        return getSharedPreferences(
                "CampusMindSettings",
                MODE_PRIVATE
        ).getBoolean(
                "dark_mode",
                false
        );
    }

    private void applyThemeMode() {

        boolean darkMode =
                isDarkModeEnabled();

        int background =
                darkMode
                        ? Color.rgb(
                        11,
                        17,
                        32
                )
                        : COLOR_BACKGROUND;

        getWindow().setStatusBarColor(
                background
        );

        getWindow().setNavigationBarColor(
                background
        );

        getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                        darkMode
                                ? 0
                                : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );

        View root =
                findViewById(
                        android.R.id.content
                );

        if (
                root == null
        ) {

            return;
        }

        root.setBackgroundColor(
                background
        );

        if (
                root instanceof ViewGroup
                        &&
                        ((ViewGroup) root)
                                .getChildCount() > 0
        ) {

            ((ViewGroup) root)
                    .getChildAt(0)
                    .setBackgroundColor(
                            background
                    );
        }

        if (
                !darkMode
        ) {

            return;
        }

        applyDarkThemeTree(
                root
        );

        if (
                tvTimer != null
        ) {

            tvTimer.setTextColor(
                    Color.rgb(
                            96,
                            165,
                            250
                    )
            );

            tvTimer.setBackground(
                    createRoundedBackground(
                            Color.rgb(
                                    23,
                                    37,
                                    84
                            ),
                            Color.rgb(
                                    51,
                                    65,
                                    85
                            ),
                            20
                    )
            );
        }

        if (
                tvFocusStatus != null
        ) {

            tvFocusStatus.setTextColor(
                    Color.rgb(
                            203,
                            213,
                            225
                    )
            );
        }
    }

    private void applyDarkThemeTree(
            View view
    ) {

        if (
                view == null
        ) {

            return;
        }

        final int darkCard =
                Color.rgb(
                        17,
                        24,
                        39
                );

        final int darkText =
                Color.rgb(
                        248,
                        250,
                        252
                );

        final int darkSecondary =
                Color.rgb(
                        203,
                        213,
                        225
                );

        final int darkMuted =
                Color.rgb(
                        148,
                        163,
                        184
                );

        final int darkBorder =
                Color.rgb(
                        51,
                        65,
                        85
                );

        final int darkPrimary =
                Color.rgb(
                        96,
                        165,
                        250
                );

        final int darkPrimaryDark =
                Color.rgb(
                        59,
                        130,
                        246
                );

        final int darkSoftBlue =
                Color.rgb(
                        23,
                        37,
                        84
                );

        final int darkSoftGray =
                Color.rgb(
                        30,
                        41,
                        59
                );

        if (
                view instanceof MaterialCardView
        ) {

            MaterialCardView card =
                    (MaterialCardView)
                            view;

            int current =
                    card.getCardBackgroundColor()
                            .getDefaultColor();

            if (
                    current ==
                            Color.rgb(
                                    37,
                                    99,
                                    235
                            )
                            ||
                            current ==
                                    Color.rgb(
                                            29,
                                            78,
                                            216
                                    )
            ) {

                card.setCardBackgroundColor(
                        darkPrimaryDark
                );

            } else if (
                    current ==
                            Color.rgb(
                                    239,
                                    246,
                                    255
                            )
            ) {

                card.setCardBackgroundColor(
                        darkSoftBlue
                );

            } else {

                card.setCardBackgroundColor(
                        darkCard
                );
            }

            card.setStrokeColor(
                    darkBorder
            );

            card.setStrokeWidth(
                    dpToPx(1)
            );
        }

        if (
                view instanceof Button
        ) {

            Button button =
                    (Button)
                            view;

            String label =
                    button.getText()
                            == null
                            ? ""
                            : button.getText()
                            .toString()
                            .trim()
                            .toUpperCase(
                                    Locale.getDefault()
                            );

            if (
                    label.contains(
                            "START"
                    )
                            ||
                            label.contains(
                                    "RESUME"
                            )
            ) {

                button.setTextColor(
                        Color.WHITE
                );

                button.setBackground(
                        createRoundedBackground(
                                darkPrimaryDark,
                                darkPrimaryDark,
                                15
                        )
                );

            } else if (
                    label.contains(
                            "RESET"
                    )
            ) {

                button.setTextColor(
                        darkSecondary
                );

                button.setBackground(
                        createRoundedBackground(
                                darkSoftGray,
                                darkBorder,
                                14
                        )
                );

            } else if (
                    label.contains(
                            "FINISH"
                    )
            ) {

                button.setTextColor(
                        darkPrimary
                );

                button.setBackground(
                        createRoundedBackground(
                                darkSoftBlue,
                                darkPrimary,
                                14
                        )
                );
            }
        }

        if (
                view instanceof TextView
                        &&
                        !(view instanceof Button)
        ) {

            TextView text =
                    (TextView)
                            view;

            int current =
                    text.getCurrentTextColor();

            if (
                    current ==
                            Color.rgb(
                                    15,
                                    23,
                                    42
                            )
                            ||
                            current ==
                                    Color.rgb(
                                            30,
                                            41,
                                            59
                                    )
                            ||
                            current ==
                                    Color.BLACK
            ) {

                text.setTextColor(
                        darkText
                );

            } else if (
                    current ==
                            Color.rgb(
                                    71,
                                    85,
                                    105
                            )
                            ||
                            current ==
                                    Color.rgb(
                                            100,
                                            116,
                                            139
                                    )
            ) {

                text.setTextColor(
                        darkSecondary
                );

            } else if (
                    current ==
                            Color.rgb(
                                    148,
                                    163,
                                    184
                            )
            ) {

                text.setTextColor(
                        darkMuted
                );

            } else if (
                    current ==
                            Color.rgb(
                                    37,
                                    99,
                                    235
                            )
            ) {

                text.setTextColor(
                        darkPrimary
                );
            }
        }

        if (
                view instanceof Spinner
        ) {

            view.setBackground(
                    createRoundedBackground(
                            darkSoftGray,
                            darkBorder,
                            14
                    )
            );
        }

        if (
                view instanceof ViewGroup
        ) {

            ViewGroup group =
                    (ViewGroup)
                            view;

            for (
                    int i = 0;
                    i < group.getChildCount();
                    i++
            ) {

                applyDarkThemeTree(
                        group.getChildAt(i)
                );
            }
        }
    }

    // =========================================================
    // ROUNDED BACKGROUND
    // =========================================================

    private android.graphics.drawable.GradientDrawable
    createRoundedBackground(
            int backgroundColor,
            int strokeColor,
            int radius
    ) {

        android.graphics.drawable.GradientDrawable
                drawable =
                new android.graphics.drawable
                        .GradientDrawable();

        drawable.setColor(
                backgroundColor
        );

        drawable.setCornerRadius(
                dpToPx(radius)
        );

        drawable.setStroke(
                dpToPx(1),
                strokeColor
        );

        return drawable;
    }

    // =========================================================
    // DP
    // =========================================================

    private int dpToPx(
            int dp
    ) {

        return (int) (
                dp *
                        getResources()
                                .getDisplayMetrics()
                                .density
                        + 0.5f
        );
    }

    // =========================================================
    // RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        applyThemeMode();
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        if (
                countDownTimer != null
        ) {

            countDownTimer.cancel();
        }

        super.onDestroy();
    }
}