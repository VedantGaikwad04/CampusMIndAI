package com.example.campusmindai;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AIActivity extends AppCompatActivity {

    private Spinner spinnerMode;
    private TextView tvChat;
    private EditText etQuestion;
    private Button btnAskAI;
    private Button btnClearAI;
    private NestedScrollView scrollChat;

    private String subjectName = "";
    private boolean subjectOnly = false;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    // =========================================================
    // COLORS
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

    private static final int COLOR_SOFT_GRAY =
            Color.rgb(241, 245, 249);

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.aiactivity
        );

        spinnerMode =
                findViewById(
                        R.id.spinnerMode
                );

        tvChat =
                findViewById(
                        R.id.tvChat
                );

        etQuestion =
                findViewById(
                        R.id.etQuestion
                );

        btnAskAI =
                findViewById(
                        R.id.btnAskAI
                );

        btnClearAI =
                findViewById(
                        R.id.btnClearAI
                );

        scrollChat =
                findViewById(
                        R.id.scrollChat
                );

        subjectName =
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

        if (subjectName == null) {
            subjectName = "";
        }

        setupProfessionalUI();

        setupModeSpinner();

        setupButtons();

        showInitialMessage();
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

        // AI RESPONSE

        if (tvChat != null) {

            tvChat.setTextColor(
                    COLOR_TEXT
            );

            tvChat.setTextSize(
                    14
            );

            tvChat.setLineSpacing(
                    0,
                    1.25f
            );

            tvChat.setGravity(
                    Gravity.TOP |
                            Gravity.START
            );

            tvChat.setPadding(
                    dpToPx(4),
                    dpToPx(4),
                    dpToPx(4),
                    dpToPx(4)
            );
        }

        // QUESTION INPUT

        if (etQuestion != null) {

            etQuestion.setTextColor(
                    COLOR_TEXT
            );

            etQuestion.setHintTextColor(
                    COLOR_MUTED
            );

            etQuestion.setTextSize(
                    14
            );

            etQuestion.setPadding(
                    dpToPx(16),
                    dpToPx(13),
                    dpToPx(16),
                    dpToPx(13)
            );

            etQuestion.setBackground(
                    createRoundedBackground(
                            COLOR_INPUT,
                            COLOR_BORDER,
                            14
                    )
            );
        }

        // ASK AI

        if (btnAskAI != null) {

            btnAskAI.setTextColor(
                    Color.WHITE
            );

            btnAskAI.setTextSize(
                    14
            );

            btnAskAI.setTypeface(
                    null,
                    Typeface.BOLD
            );

            btnAskAI.setGravity(
                    Gravity.CENTER
            );

            btnAskAI.setBackground(
                    createRoundedBackground(
                            COLOR_PRIMARY,
                            COLOR_PRIMARY,
                            14
                    )
            );

            btnAskAI.setBackgroundTintList(
                    ColorStateList.valueOf(
                            COLOR_PRIMARY
                    )
            );

            btnAskAI.setPadding(
                    dpToPx(12),
                    0,
                    dpToPx(12),
                    0
            );

            btnAskAI.setMinHeight(
                    dpToPx(52)
            );
        }

        // CLEAR

        if (btnClearAI != null) {

            btnClearAI.setTextColor(
                    COLOR_SECONDARY
            );

            btnClearAI.setTextSize(
                    13
            );

            btnClearAI.setTypeface(
                    null,
                    Typeface.BOLD
            );

            btnClearAI.setGravity(
                    Gravity.CENTER
            );

            btnClearAI.setBackground(
                    createRoundedBackground(
                            COLOR_SOFT_GRAY,
                            COLOR_BORDER,
                            14
                    )
            );

            btnClearAI.setPadding(
                    dpToPx(12),
                    0,
                    dpToPx(12),
                    0
            );

            btnClearAI.setMinHeight(
                    dpToPx(52)
            );
        }

        // SPINNER

        if (spinnerMode != null) {

            spinnerMode.setBackground(
                    createRoundedBackground(
                            COLOR_SOFT_BLUE,
                            COLOR_BORDER,
                            14
                    )
            );

            spinnerMode.setPadding(
                    dpToPx(14),
                    0,
                    dpToPx(14),
                    0
            );

            spinnerMode.setPopupBackgroundDrawable(
                    createRoundedBackground(
                            COLOR_CARD,
                            COLOR_BORDER,
                            14
                    )
            );
        }

        styleTextViews(root);
    }

    // =========================================================
    // TEXT STYLING
    // =========================================================

    private void styleTextViews(
            View view
    ) {

        if (view == null) {
            return;
        }

        if (view instanceof TextView) {

            TextView textView =
                    (TextView) view;

            int id =
                    textView.getId();

            if (
                    id ==
                            getResources()
                                    .getIdentifier(
                                            "tvTitle",
                                            "id",
                                            getPackageName()
                                    )
            ) {

                textView.setTextColor(
                        COLOR_TEXT
                );

                textView.setTypeface(
                        null,
                        Typeface.BOLD
                );

            } else if (
                    textView != tvChat &&
                            textView != btnAskAI &&
                            textView != btnClearAI &&
                            textView != etQuestion
            ) {

                String value =
                        textView
                                .getText()
                                .toString()
                                .trim();

                if (
                        value.equalsIgnoreCase(
                                "STUDY MODE"
                        )
                ) {

                    textView.setTextColor(
                            COLOR_SECONDARY
                    );

                    textView.setTypeface(
                            null,
                            Typeface.BOLD
                    );

                } else {

                    textView.setTextColor(
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

                styleTextViews(
                        group.getChildAt(i)
                );
            }
        }
    }

    // =========================================================
    // SPINNER
    // =========================================================

    private void setupModeSpinner() {

        String[] modes = {
                "Explain Simply",
                "5-Mark Answer",
                "Generate MCQs",
                "Summarize"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        modes
                ) {

                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            ViewGroup parent
                    ) {

                        TextView view =
                                (TextView) super.getView(
                                        position,
                                        convertView,
                                        parent
                                );

                        view.setTextColor(
                                COLOR_TEXT
                        );

                        view.setTextSize(
                                15
                        );

                        view.setTypeface(
                                null,
                                Typeface.BOLD
                        );

                        view.setGravity(
                                Gravity.CENTER_VERTICAL
                        );

                        view.setPadding(
                                dpToPx(2),
                                0,
                                dpToPx(2),
                                0
                        );

                        return view;
                    }

                    @Override
                    public View getDropDownView(
                            int position,
                            View convertView,
                            ViewGroup parent
                    ) {

                        TextView view =
                                (TextView) super
                                        .getDropDownView(
                                                position,
                                                convertView,
                                                parent
                                        );

                        view.setTextColor(
                                COLOR_TEXT
                        );

                        view.setTextSize(
                                14
                        );

                        view.setGravity(
                                Gravity.CENTER_VERTICAL
                        );

                        view.setPadding(
                                dpToPx(16),
                                dpToPx(14),
                                dpToPx(16),
                                dpToPx(14)
                        );

                        view.setBackgroundColor(
                                Color.WHITE
                        );

                        return view;
                    }
                };

        spinnerMode.setAdapter(
                adapter
        );
    }

    // =========================================================
    // INITIAL MESSAGE
    // =========================================================

    private void showInitialMessage() {

        if (
                subjectOnly &&
                        !subjectName.isEmpty()
        ) {

            tvChat.setText(
                    "AI Study Assistant\n\n" +
                            "Subject: " +
                            subjectName +
                            "\n\n" +
                            "Ask anything related to " +
                            subjectName +
                            "."
            );

        } else {

            tvChat.setText(
                    "AI Study Assistant\n\n" +
                            "Ask me anything related to your studies."
            );
        }

        scrollToBottom();
    }

    // =========================================================
    // BUTTONS
    // =========================================================

    private void setupButtons() {

        btnAskAI.setOnClickListener(
                v -> {

                    String question =
                            etQuestion
                                    .getText()
                                    .toString()
                                    .trim();

                    if (question.isEmpty()) {

                        etQuestion.setError(
                                "Enter your question"
                        );

                        etQuestion.requestFocus();

                        return;
                    }

                    askGemini(
                            question
                    );
                }
        );

        btnClearAI.setOnClickListener(
                v -> {

                    showInitialMessage();

                    etQuestion.setText("");

                    btnAskAI.setText(
                            "ASK AI"
                    );

                    btnAskAI.setEnabled(
                            true
                    );

                    scrollToBottom();
                }
        );
    }

    // =========================================================
    // ASK GEMINI
    // =========================================================

    private void askGemini(
            String question
    ) {

        Object selectedItem =
                spinnerMode
                        .getSelectedItem();

        String selectedMode;

        if (selectedItem == null) {

            selectedMode =
                    "Explain Simply";

        } else {

            selectedMode =
                    selectedItem
                            .toString();
        }

        btnAskAI.setEnabled(
                false
        );

        btnAskAI.setText(
                "THINKING..."
        );

        String prompt =
                createPrompt(
                        question,
                        selectedMode
                );

        executorService.execute(
                () -> {

                    String response;

                    try {

                        response =
                                callGeminiAPI(
                                        prompt
                                );

                    } catch (
                            Exception e
                    ) {

                        response =
                                "Unable to get AI response.\n\n" +
                                        "Please check your internet connection " +
                                        "and try again.";
                    }

                    final String finalResponse =
                            cleanAIResponse(
                                    response
                            );

                    mainHandler.post(
                            () -> {

                                btnAskAI.setEnabled(
                                        true
                                );

                                btnAskAI.setText(
                                        "ASK AI"
                                );

                                tvChat.append(
                                        "\n\nYou: " +
                                                question +
                                                "\n\nAI: " +
                                                finalResponse
                                );

                                scrollToBottom();

                                etQuestion.setText("");
                            }
                    );
                }
        );
    }

    // =========================================================
    // PROMPT
    // =========================================================

    private String createPrompt(
            String question,
            String selectedMode
    ) {

        StringBuilder prompt =
                new StringBuilder();

        prompt.append(
                "You are CampusMind AI, an academic study assistant " +
                        "for college students. "
        );

        if (
                subjectOnly &&
                        !subjectName.isEmpty()
        ) {

            prompt.append(
                    "The student is currently studying the subject: " +
                            subjectName +
                            ". "
            );

            prompt.append(
                    "Keep the answer specifically relevant to " +
                            subjectName +
                            ". "
            );
        }

        prompt.append(
                "The student selected this response mode: " +
                        selectedMode +
                        ". "
        );

        if (
                selectedMode.equals(
                        "Explain Simply"
                )
        ) {

            prompt.append(
                    "Explain the topic in very simple language. " +
                            "Use short paragraphs and numbered points where useful. " +
                            "Make it easy for a college student to understand."
            );

        } else if (
                selectedMode.equals(
                        "5-Mark Answer"
                )
        ) {

            prompt.append(
                    "Write a proper college examination answer suitable " +
                            "for approximately 5 marks. Include a short introduction, " +
                            "important points, and a conclusion where appropriate."
            );

        } else if (
                selectedMode.equals(
                        "Generate MCQs"
                )
        ) {

            prompt.append(
                    "Generate 5 multiple choice questions. " +
                            "Give four options for each question and clearly mention " +
                            "the correct answer."
            );

        } else if (
                selectedMode.equals(
                        "Summarize"
                )
        ) {

            prompt.append(
                    "Summarize the topic using important points only. " +
                            "Keep the explanation concise and useful for revision."
            );
        }

        prompt.append(
                "\n\nIMPORTANT FORMATTING RULES: " +
                        "Return the answer as clean plain text. " +
                        "Do not use Markdown. " +
                        "Do not use double asterisks. " +
                        "Do not use single asterisks as bullet points. " +
                        "Do not use hashtags. " +
                        "Do not use underscores for formatting. " +
                        "Do not use Markdown headings. " +
                        "Use simple numbered points such as 1., 2., 3. when needed. " +
                        "Do not put decorative symbols around words."
        );

        prompt.append(
                "\n\nStudent question: " +
                        question
        );

        return prompt.toString();
    }

    // =========================================================
    // GEMINI API
    // =========================================================

    private String callGeminiAPI(
            String prompt
    ) throws Exception {

        String apiKey =
                getString(
                        R.string.gemini_api_key
                );

        String endpoint =
                "https://generativelanguage.googleapis.com/" +
                        "v1beta/models/" +
                        "gemini-3.6-flash:generateContent?key=" +
                        apiKey;

        URL url =
                new URL(
                        endpoint
                );

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod(
                "POST"
        );

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        connection.setDoOutput(
                true
        );

        connection.setConnectTimeout(
                30000
        );

        connection.setReadTimeout(
                60000
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

        JSONObject requestBody =
                new JSONObject();

        requestBody.put(
                "contents",
                contents
        );

        OutputStream outputStream =
                connection.getOutputStream();

        outputStream.write(
                requestBody
                        .toString()
                        .getBytes(
                                StandardCharsets.UTF_8
                        )
        );

        outputStream.flush();

        outputStream.close();

        int responseCode =
                connection
                        .getResponseCode();

        InputStream inputStream;

        if (
                responseCode >= 200 &&
                        responseCode < 300
        ) {

            inputStream =
                    connection
                            .getInputStream();

        } else {

            inputStream =
                    connection
                            .getErrorStream();
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                inputStream,
                                StandardCharsets.UTF_8
                        )
                );

        StringBuilder responseBuilder =
                new StringBuilder();

        String line;

        while (
                (line =
                        reader.readLine())
                        != null
        ) {

            responseBuilder.append(
                    line
            );
        }

        reader.close();

        connection.disconnect();

        String rawResponse =
                responseBuilder.toString();

        if (
                responseCode < 200 ||
                        responseCode >= 300
        ) {

            try {

                JSONObject errorObject =
                        new JSONObject(
                                rawResponse
                        );

                JSONObject error =
                        errorObject.optJSONObject(
                                "error"
                        );

                if (error != null) {

                    String message =
                            error.optString(
                                    "message",
                                    "Gemini API error"
                            );

                    throw new Exception(
                            message
                    );
                }

            } catch (
                    org.json.JSONException ignored
            ) {
            }

            throw new Exception(
                    "API request failed. Code: " +
                            responseCode
            );
        }

        JSONObject jsonResponse =
                new JSONObject(
                        rawResponse
                );

        JSONArray candidates =
                jsonResponse.optJSONArray(
                        "candidates"
                );

        if (
                candidates == null ||
                        candidates.length() == 0
        ) {

            throw new Exception(
                    "No AI response received."
            );
        }

        JSONObject candidate =
                candidates.getJSONObject(
                        0
                );

        JSONObject contentResponse =
                candidate.optJSONObject(
                        "content"
                );

        if (contentResponse == null) {

            throw new Exception(
                    "Invalid AI response."
            );
        }

        JSONArray responseParts =
                contentResponse.optJSONArray(
                        "parts"
                );

        if (
                responseParts == null ||
                        responseParts.length() == 0
        ) {

            throw new Exception(
                    "Empty AI response."
            );
        }

        StringBuilder finalText =
                new StringBuilder();

        for (
                int i = 0;
                i < responseParts.length();
                i++
        ) {

            JSONObject part =
                    responseParts.getJSONObject(
                            i
                    );

            String text =
                    part.optString(
                            "text",
                            ""
                    );

            finalText.append(
                    text
            );
        }

        return finalText
                .toString()
                .trim();
    }

    // =========================================================
    // CLEAN AI RESPONSE
    // =========================================================

    private String cleanAIResponse(
            String response
    ) {

        if (response == null) {

            return "";
        }

        String cleaned =
                response;

        cleaned =
                cleaned.replace(
                        "**",
                        ""
                );

        cleaned =
                cleaned.replace(
                        "__",
                        ""
                );

        cleaned =
                cleaned.replace(
                        "###",
                        ""
                );

        cleaned =
                cleaned.replace(
                        "##",
                        ""
                );

        cleaned =
                cleaned.replace(
                        "#",
                        ""
                );

        cleaned =
                cleaned.replaceAll(
                        "(?m)^\\s*\\*\\s+",
                        ""
                );

        cleaned =
                cleaned.replaceAll(
                        "(?m)^\\s*-\\s+",
                        ""
                );

        cleaned =
                cleaned.replace(
                        "_",
                        ""
                );

        cleaned =
                cleaned.replaceAll(
                        "\\n{3,}",
                        "\n\n"
                );

        cleaned =
                cleaned.replaceAll(
                        "\\s+([,.!?])",
                        "$1"
                );

        return cleaned.trim();
    }

    // =========================================================
    // CREATE ROUNDED BACKGROUND
    // =========================================================

    private GradientDrawable createRoundedBackground(
            int fillColor,
            int strokeColor,
            int radiusDp
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(
                fillColor
        );

        drawable.setCornerRadius(
                dpToPx(
                        radiusDp
                )
        );

        drawable.setStroke(
                dpToPx(1),
                strokeColor
        );

        return drawable;
    }

    // =========================================================
    // SCROLL CHAT TO BOTTOM
    // =========================================================

    private void scrollToBottom() {

        if (scrollChat == null) {
            return;
        }

        scrollChat.postDelayed(
                () -> scrollChat.fullScroll(
                        View.FOCUS_DOWN
                ),
                150
        );
    }

    // =========================================================
    // DP TO PX
    // =========================================================

    private int dpToPx(
            int dp
    ) {

        return (int) (
                dp *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        if (
                executorService != null
        ) {

            executorService.shutdownNow();
        }

        super.onDestroy();
    }
}