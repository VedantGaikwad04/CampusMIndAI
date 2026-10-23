
package com.example.campusmindai;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Live view of the teacher's active attendance session.
 * Displays student names and roll numbers.
 * A student who has not checked in is shown as not yet marked,
 * not confirmed absent.
 */
public class TeacherLiveAttendanceActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView tvSessionSubject;
    private TextView tvSessionState;
    private TextView tvSessionTime;
    private TextView tvPresentCount;
    private TextView tvPendingCount;
    private TextView tvEmpty;

    private LinearLayout presentContainer;
    private LinearLayout pendingContainer;

    private ListenerRegistration activeSessionListener;
    private ListenerRegistration participantListener;

    private final Handler timerHandler =
            new Handler(Looper.getMainLooper());

    private final Runnable timerTick = new Runnable() {
        @Override
        public void run() {
            if (tvSessionTime != null && currentSessionId != null) {
                tvSessionTime.setText(
                        formatExpiry(currentExpiryMillis)
                );
            }
            timerHandler.postDelayed(this, 1000L);
        }
    };

    private String currentSessionId;
    private long currentExpiryMillis;
    private boolean rosterLoaded;

    // Student UID -> name
    private final Map<String, String> rosterNames =
            new LinkedHashMap<>();

    // Student UID -> roll number
    private final Map<String, String> rosterRollNumbers =
            new LinkedHashMap<>();

    // Student UID -> name of the checked-in participant
    private final Map<String, String> presentNames =
            new LinkedHashMap<>();

    // Student UID -> roll number of the checked-in participant
    private final Map<String, String> presentRollNumbers =
            new LinkedHashMap<>();

    private int backgroundColor;
    private int cardColor;
    private int textColor;
    private int secondaryColor;
    private int mutedColor;
    private int borderColor;
    private int primaryColor;
    private int successColor;
    private int errorColor;
    private int softBlue;
    private int softGreen;
    private int softRed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadColors();
        createScreen();
        timerHandler.post(timerTick);

        if (auth.getCurrentUser() == null) {
            Toast.makeText(
                    this,
                    "Please log in again",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        verifyTeacherThenLoad();
    }

    // ---------------------------------------------------------
    // CREATE SCREEN
    // ---------------------------------------------------------

    private void createScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(backgroundColor);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(20), dp(24), dp(20), dp(30));
        scroll.addView(main);

        TextView title = text(
                "Live Attendance",
                25,
                textColor,
                Typeface.BOLD
        );
        main.addView(title);

        TextView subtitle = text(
                "Monitor students checking in for the active class",
                13,
                secondaryColor,
                Typeface.NORMAL
        );
        addTopMargin(subtitle, 5);
        main.addView(subtitle);

        MaterialCardView sessionCard = card();
        LinearLayout sessionContent = column();

        sessionContent.addView(
                text(
                        "CURRENT SESSION",
                        11,
                        mutedColor,
                        Typeface.BOLD
                )
        );

        tvSessionSubject = text(
                "Looking for an active session...",
                19,
                textColor,
                Typeface.BOLD
        );
        addTopMargin(tvSessionSubject, 10);
        sessionContent.addView(tvSessionSubject);

        tvSessionState = text(
                "Connecting to Firestore",
                13,
                secondaryColor,
                Typeface.NORMAL
        );
        addTopMargin(tvSessionState, 6);
        sessionContent.addView(tvSessionState);

        tvSessionTime = text(
                "",
                13,
                primaryColor,
                Typeface.BOLD
        );
        addTopMargin(tvSessionTime, 6);
        sessionContent.addView(tvSessionTime);

        sessionCard.addView(sessionContent);

        LinearLayout.LayoutParams sessionCardParams = matchWrap();
        sessionCardParams.topMargin = dp(20);
        main.addView(sessionCard, sessionCardParams);

        // SUMMARY COUNTS
        LinearLayout counts = new LinearLayout(this);
        counts.setOrientation(LinearLayout.HORIZONTAL);
        counts.setGravity(Gravity.CENTER_VERTICAL);

        MaterialCardView presentSummary = card();
        LinearLayout presentSummaryContent = column();
        presentSummaryContent.setGravity(Gravity.CENTER_VERTICAL);

        tvPresentCount = text(
                "0",
                26,
                successColor,
                Typeface.BOLD
        );

        TextView presentLabel = text(
                "PRESENT",
                11,
                secondaryColor,
                Typeface.BOLD
        );

        presentSummaryContent.addView(tvPresentCount);
        addTopMargin(presentLabel, 4);
        presentSummaryContent.addView(presentLabel);
        presentSummary.addView(presentSummaryContent);

        MaterialCardView pendingSummary = card();
        LinearLayout pendingSummaryContent = column();
        pendingSummaryContent.setGravity(Gravity.CENTER_VERTICAL);

        tvPendingCount = text(
                "0",
                26,
                errorColor,
                Typeface.BOLD
        );

        TextView pendingLabel = text(
                "NOT YET MARKED",
                11,
                secondaryColor,
                Typeface.BOLD
        );

        pendingSummaryContent.addView(tvPendingCount);
        addTopMargin(pendingLabel, 4);
        pendingSummaryContent.addView(pendingLabel);
        pendingSummary.addView(pendingSummaryContent);

        LinearLayout.LayoutParams summaryParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );

        summaryParams.topMargin = dp(14);
        summaryParams.rightMargin = dp(6);
        counts.addView(presentSummary, summaryParams);

        LinearLayout.LayoutParams secondSummaryParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );

        secondSummaryParams.topMargin = dp(14);
        secondSummaryParams.leftMargin = dp(6);
        counts.addView(pendingSummary, secondSummaryParams);

        main.addView(counts);

        // PRESENT LIST
        TextView presentHeading = text(
                "✓  PRESENT STUDENTS",
                13,
                successColor,
                Typeface.BOLD
        );

        addTopMargin(presentHeading, 25);
        main.addView(presentHeading);

        presentContainer = column();
        addTopMargin(presentContainer, 10);
        main.addView(presentContainer);

        // NOT YET MARKED LIST
        TextView pendingHeading = text(
                "○  ABSENT / NOT YET MARKED",
                13,
                errorColor,
                Typeface.BOLD
        );

        addTopMargin(pendingHeading, 24);
        main.addView(pendingHeading);

        TextView pendingHint = text(
                "Students in this list have not checked in for the current session yet.",
                12,
                mutedColor,
                Typeface.NORMAL
        );

        addTopMargin(pendingHint, 4);
        main.addView(pendingHint);

        pendingContainer = column();
        addTopMargin(pendingContainer, 10);
        main.addView(pendingContainer);

        tvEmpty = text(
                "",
                13,
                secondaryColor,
                Typeface.NORMAL
        );

        tvEmpty.setGravity(Gravity.CENTER);
        addTopMargin(tvEmpty, 20);
        main.addView(tvEmpty);

        setContentView(scroll);
    }

    // ---------------------------------------------------------
    // VERIFY TEACHER
    // ---------------------------------------------------------

    private void verifyTeacherThenLoad() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("students")
                .document(uid)
                .get()
                .addOnSuccessListener(profile -> {

                    String role = profile.getString("role");

                    if (role == null
                            || !"Teacher".equalsIgnoreCase(role)) {

                        Toast.makeText(
                                this,
                                "Teacher access required",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                        return;
                    }

                    loadStudentRoster();
                    listenForActiveSession(uid);
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to verify teacher access",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                });
    }

    // ---------------------------------------------------------
    // LOAD STUDENT NAMES AND ROLL NUMBERS
    // ---------------------------------------------------------

    private void loadStudentRoster() {

        db.collection("students")
                .get()
                .addOnSuccessListener(snapshot -> {

                    rosterNames.clear();
                    rosterRollNumbers.clear();

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        String role = document.getString("role");

                        if (role != null
                                && "Teacher".equalsIgnoreCase(role)) {
                            continue;
                        }

                        String name = document.getString("name");

                        if (name == null || name.trim().isEmpty()) {

                            String email = document.getString("email");

                            name = (email == null || email.trim().isEmpty())
                                    ? "Student"
                                    : email.trim();
                        }

                        String rollNumber =
                                document.getString("rollNumber");

                        if (rollNumber == null
                                || rollNumber.trim().isEmpty()) {
                            rollNumber = "Not assigned";
                        }

                        rosterNames.put(
                                document.getId(),
                                name.trim()
                        );

                        rosterRollNumbers.put(
                                document.getId(),
                                rollNumber.trim()
                        );
                    }

                    rosterLoaded = true;
                    renderLists();
                })
                .addOnFailureListener(e -> {

                    rosterLoaded = false;

                    tvEmpty.setText(
                            "Unable to load the student roster. Check the Firestore rules."
                    );

                    tvEmpty.setTextColor(errorColor);
                });
    }

    // ---------------------------------------------------------
    // LISTEN FOR ACTIVE TEACHER SESSION
    // ---------------------------------------------------------

    private void listenForActiveSession(String teacherUid) {

        activeSessionListener = db.collection("attendanceSessions")
                .whereEqualTo("teacherUid", teacherUid)
                .whereEqualTo("active", true)
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null) {

                        tvSessionState.setText(
                                "Unable to load the active session"
                        );

                        tvSessionState.setTextColor(errorColor);
                        return;
                    }

                    if (snapshot == null || snapshot.isEmpty()) {

                        clearCurrentSession();

                        tvSessionSubject.setText("No active session");

                        tvSessionState.setText(
                                "Start Attendance from the Teacher Dashboard first."
                        );

                        tvSessionState.setTextColor(secondaryColor);
                        tvSessionTime.setText("");

                        tvEmpty.setText(
                                "There is no live attendance session right now."
                        );

                        tvPresentCount.setText("0");

                        tvPendingCount.setText(
                                String.valueOf(rosterNames.size())
                        );

                        presentContainer.removeAllViews();
                        pendingContainer.removeAllViews();
                        return;
                    }

                    DocumentSnapshot chosen = null;
                    long newest = Long.MIN_VALUE;

                    for (DocumentSnapshot candidate :
                            snapshot.getDocuments()) {

                        Long created =
                                candidate.getLong("createdAtMillis");

                        long time = created == null ? 0L : created;

                        if (chosen == null || time > newest) {
                            chosen = candidate;
                            newest = time;
                        }
                    }

                    if (chosen == null) {
                        return;
                    }

                    String sessionId = chosen.getId();
                    String subject = chosen.getString("subject");
                    Long expires = chosen.getLong("expiresAtMillis");

                    boolean sessionChanged =
                            !sessionId.equals(currentSessionId);

                    currentSessionId = sessionId;

                    currentExpiryMillis =
                            expires == null ? 0L : expires;

                    tvSessionSubject.setText(
                            subject == null ? "Class session" : subject
                    );

                    tvSessionState.setText(
                            "● LIVE — updates automatically"
                    );

                    tvSessionState.setTextColor(successColor);

                    tvSessionTime.setText(
                            formatExpiry(currentExpiryMillis)
                    );

                    tvEmpty.setText("");

                    if (sessionChanged) {

                        if (participantListener != null) {
                            participantListener.remove();
                            participantListener = null;
                        }

                        presentNames.clear();
                        presentRollNumbers.clear();

                        listenForParticipants(sessionId);
                    }

                    renderLists();
                });
    }

    // ---------------------------------------------------------
    // LISTEN FOR PARTICIPANTS
    // ---------------------------------------------------------

    private void listenForParticipants(String sessionId) {

        participantListener = db.collection("attendanceSessions")
                .document(sessionId)
                .collection("participants")
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null) {

                        tvEmpty.setText(
                                "Unable to load live check-ins. Check participant Firestore rules."
                        );

                        tvEmpty.setTextColor(errorColor);
                        return;
                    }

                    presentNames.clear();
                    presentRollNumbers.clear();

                    if (snapshot != null) {

                        for (DocumentSnapshot participant :
                                snapshot.getDocuments()) {

                            String uid =
                                    participant.getString("studentUid");

                            if (uid == null || uid.trim().isEmpty()) {
                                uid = participant.getId();
                            }

                            String name =
                                    participant.getString("name");

                            if (name == null || name.trim().isEmpty()) {
                                name = rosterNames.get(uid);
                            }

                            if (name == null || name.trim().isEmpty()) {
                                name = "Student";
                            }

                            String rollNumber =
                                    participant.getString("rollNumber");

                            if (rollNumber == null
                                    || rollNumber.trim().isEmpty()) {

                                rollNumber =
                                        rosterRollNumbers.get(uid);
                            }

                            if (rollNumber == null
                                    || rollNumber.trim().isEmpty()) {
                                rollNumber = "Not assigned";
                            }

                            presentNames.put(uid, name);
                            presentRollNumbers.put(uid, rollNumber);
                        }
                    }

                    renderLists();
                });
    }

    // ---------------------------------------------------------
    // RENDER PRESENT AND NOT-YET-MARKED LISTS
    // ---------------------------------------------------------

    private void renderLists() {

        if (presentContainer == null
                || pendingContainer == null) {
            return;
        }

        presentContainer.removeAllViews();
        pendingContainer.removeAllViews();

        for (Map.Entry<String, String> entry :
                presentNames.entrySet()) {

            String uid = entry.getKey();
            String name = entry.getValue();

            String rollNumber =
                    presentRollNumbers.get(uid);

            addStudentRow(
                    presentContainer,
                    name,
                    rollNumber,
                    true
            );
        }

        int notMarked = 0;

        if (rosterLoaded) {

            for (Map.Entry<String, String> entry :
                    rosterNames.entrySet()) {

                String uid = entry.getKey();

                if (!presentNames.containsKey(uid)) {

                    String name = entry.getValue();

                    String rollNumber =
                            rosterRollNumbers.get(uid);

                    addStudentRow(
                            pendingContainer,
                            name,
                            rollNumber,
                            false
                    );

                    notMarked++;
                }
            }
        }

        tvPresentCount.setText(
                String.valueOf(presentNames.size())
        );

        tvPendingCount.setText(
                rosterLoaded
                        ? String.valueOf(notMarked)
                        : "—"
        );

        if (presentNames.isEmpty() && currentSessionId != null) {

            tvEmpty.setText(
                    "No students have checked in yet."
            );

            tvEmpty.setTextColor(secondaryColor);

        } else if (!presentNames.isEmpty()) {

            tvEmpty.setText("");
        }

        if (rosterLoaded && rosterNames.isEmpty()) {

            tvEmpty.setText(
                    "No student accounts were found in the roster."
            );
        }
    }

    // ---------------------------------------------------------
    // STUDENT ROW WITH ROLL NUMBER
    // ---------------------------------------------------------

    private void addStudentRow(
            LinearLayout parent,
            String name,
            String rollNumber,
            boolean present
    ) {

        MaterialCardView rowCard = card();

        LinearLayout row = new LinearLayout(this);

        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        row.setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(12)
        );

        TextView marker = text(
                present ? "✓" : "○",
                18,
                present ? successColor : mutedColor,
                Typeface.BOLD
        );

        marker.setGravity(Gravity.CENTER);

        row.addView(
                marker,
                new LinearLayout.LayoutParams(
                        dp(30),
                        dp(40)
                )
        );

        // Student name and roll number
        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);

        TextView studentName = text(
                name,
                14,
                textColor,
                Typeface.BOLD
        );

        details.addView(studentName);

        String displayedRoll =
                rollNumber == null || rollNumber.trim().isEmpty()
                        ? "Roll No: Not assigned"
                        : "Roll No: " + rollNumber;

        TextView studentRoll = text(
                displayedRoll,
                12,
                secondaryColor,
                Typeface.NORMAL
        );

        addTopMargin(studentRoll, 3);
        details.addView(studentRoll);

        LinearLayout.LayoutParams detailsParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );

        detailsParams.leftMargin = dp(8);

        row.addView(details, detailsParams);

        TextView status = text(
                present ? "PRESENT" : "WAITING",
                10,
                present ? successColor : secondaryColor,
                Typeface.BOLD
        );

        row.addView(status);

        rowCard.addView(row);

        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(8);

        parent.addView(rowCard, params);
    }

    // ---------------------------------------------------------
    // SESSION CLEANUP
    // ---------------------------------------------------------

    private void clearCurrentSession() {

        currentSessionId = null;
        currentExpiryMillis = 0L;

        presentNames.clear();
        presentRollNumbers.clear();

        if (participantListener != null) {
            participantListener.remove();
            participantListener = null;
        }
    }

    private String formatExpiry(long expiryMillis) {

        if (expiryMillis <= 0L) {
            return "Expiry time unavailable";
        }

        long remaining = Math.max(
                0L,
                expiryMillis - System.currentTimeMillis()
        );

        long seconds = remaining / 1000L;

        return seconds == 0
                ? "Session time reached — awaiting closure"
                : String.format(
                Locale.getDefault(),
                "Time remaining: %02d:%02d",
                seconds / 60,
                seconds % 60
        );
    }

    // ---------------------------------------------------------
    // THEME AND UI HELPERS
    // ---------------------------------------------------------

    private void loadColors() {

        backgroundColor = getColor(R.color.app_background);
        cardColor = getColor(R.color.app_card);
        textColor = getColor(R.color.app_text);
        secondaryColor = getColor(R.color.app_secondary_text);
        mutedColor = getColor(R.color.app_muted_text);
        borderColor = getColor(R.color.app_border);
        primaryColor = getColor(R.color.primary);
        successColor = getColor(R.color.success);
        errorColor = getColor(R.color.error);
        softBlue = getColor(R.color.app_blue_soft);
        softGreen = getColor(R.color.success);
        softRed = getColor(R.color.error);
    }

    private MaterialCardView card() {

        MaterialCardView card = new MaterialCardView(this);

        card.setCardBackgroundColor(
                ColorStateList.valueOf(cardColor)
        );

        card.setStrokeColor(
                ColorStateList.valueOf(borderColor)
        );

        card.setStrokeWidth(dp(1));
        card.setRadius(dp(17));
        card.setCardElevation(dp(1));

        return card;
    }

    private LinearLayout column() {

        LinearLayout layout = new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );

        return layout;
    }

    private TextView text(
            String value,
            float size,
            int color,
            int style
    ) {

        TextView view = new TextView(this);

        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);

        view.setTypeface(
                Typeface.DEFAULT,
                style
        );

        return view;
    }

    private LinearLayout.LayoutParams matchWrap() {

        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void addTopMargin(
            View view,
            int marginDp
    ) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(marginDp);

        view.setLayoutParams(params);
    }

    private int dp(int value) {

        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // ---------------------------------------------------------
    // CLEAN UP LISTENERS
    // ---------------------------------------------------------

    @Override
    protected void onDestroy() {

        timerHandler.removeCallbacksAndMessages(null);

        if (activeSessionListener != null) {
            activeSessionListener.remove();
        }

        if (participantListener != null) {
            participantListener.remove();
        }

        super.onDestroy();
    }
}
