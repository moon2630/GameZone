package com.example.gamezone;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.Random;

public class maths_game extends AppCompatActivity {
    private TextView maths_question_TXT, Timer, score_TXT, highScore_TXT, questionNumber_TXT;
    private EditText maths_answer;
    private Button leave_game_btn, restart_game_btn;
    private CountDownTimer countDownTimer;
    private int score = 0;
    private int questionNumber = 1;
    private String currentDifficulty = "";
    private Random random = new Random();
    private boolean isTimerRunning = false;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maths_game);


        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        // Initialize UI elements
        maths_question_TXT = findViewById(R.id.maths_question_TXT);
        maths_answer = findViewById(R.id.maths_answer);
        Timer = findViewById(R.id.Timer);
        score_TXT = findViewById(R.id.score_TXT);
        highScore_TXT = findViewById(R.id.High_score_TXT);
        questionNumber_TXT = findViewById(R.id.question_number);
        leave_game_btn = findViewById(R.id.leave_game_btn);
        restart_game_btn = findViewById(R.id.restart_game_btn);

        prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        // Set focus and show keyboard for answer input
        maths_answer.requestFocus();
        maths_answer.setCursorVisible(true);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(maths_answer, InputMethodManager.SHOW_IMPLICIT);

        // Button click listeners
        leave_game_btn.setOnClickListener(v -> showLeaveDialog());
        restart_game_btn.setOnClickListener(v -> showRestartDialog());

        // Answer input validation
        maths_answer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    maths_answer.setBackgroundResource(R.drawable.input_background);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String answer = s.toString().trim();
                if (!answer.isEmpty()) {
                    checkAnswer(answer);
                } else {
                    maths_answer.setBackgroundResource(R.drawable.input_background);
                }
            }
        });

        // Restore state or show difficulty dialog
        if (savedInstanceState != null) {
            currentDifficulty = savedInstanceState.getString("currentDifficulty", "");
            score = savedInstanceState.getInt("score", 0);
            questionNumber = savedInstanceState.getInt("questionNumber", 1);
            isTimerRunning = savedInstanceState.getBoolean("isTimerRunning", false);
            score_TXT.setText(String.valueOf(score));
            questionNumber_TXT.setText("Question: " + questionNumber);
            if (isTimerRunning) {
                long timeLeft = savedInstanceState.getLong("timeLeft", 120000);
                startTimer(timeLeft);
            }
        }

        if (currentDifficulty.isEmpty()) {
            showDifficultyDialog();
        } else {
            generateQuestion();
            updateHighScoreDisplay();
        }
    }

    private void updateHighScoreDisplay() {
        if (!currentDifficulty.isEmpty()) {
            int highScore = prefs.getInt("high_score_" + currentDifficulty, 0);
            highScore_TXT.setText("High Score : " + highScore);
        }
    }

    private void showDifficultyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.maths_level, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();




        AppCompatButton easyBtn = dialogView.findViewById(R.id.easy_level_btn);
        AppCompatButton mediumBtn = dialogView.findViewById(R.id.medium_level_btn);
        AppCompatButton hardBtn = dialogView.findViewById(R.id.hard_level_btn);



        dialog.setCancelable(true);
        dialog.setOnCancelListener(dialogInterface -> finish());

        // Set up button click listeners
        View.OnClickListener difficultyListener = v -> {
            if (v.getId() == R.id.easy_level_btn) {
                currentDifficulty = "Easy";
            } else if (v.getId() == R.id.medium_level_btn) {
                currentDifficulty = "Medium";
            } else if (v.getId() == R.id.hard_level_btn) {
                currentDifficulty = "Hard";
            }
            updateHighScoreDisplay();
            generateQuestion();
            dialog.dismiss();

            // Start timer immediately
            startTimer(120000);
        };

        easyBtn.setOnClickListener(difficultyListener);
        mediumBtn.setOnClickListener(difficultyListener);
        hardBtn.setOnClickListener(difficultyListener);

        dialog.show();
    }

    private void startTimer(long millis) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = true;
        countDownTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                Timer.setText(String.format("%d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                Timer.setText("0:00");
                isTimerRunning = false;
                showWinRoundDialog();
            }
        }.start();
    }

    private void generateQuestion() {
        int num1, num2;
        String operator;
        int answer;

        do {
            switch (currentDifficulty) {
                case "Easy":
                    int opIndexEasy = random.nextInt(4);
                    operator = opIndexEasy == 0 ? "+" : opIndexEasy == 1 ? "×" : opIndexEasy == 2 ? "÷" : "-";
                    if (operator.equals("-")) {
                        num1 = random.nextInt(51) + 50; // 50-100
                        num2 = random.nextInt(41) + 10; // 10-50
                    } else if (operator.equals("×")) {
                        num1 = random.nextInt(11) + 10; // 10-20
                        num2 = random.nextInt(6) + 1;   // 1-6
                    } else if (operator.equals("÷")) {
                        num2 = random.nextInt(5) + 1;   // 1-5
                        answer = random.nextInt(20) + 1; // 1-20
                        num1 = num2 * answer;           // Ensure remainder = 0
                    } else {
                        num1 = random.nextInt(51) + 50; // 50-100
                        num2 = random.nextInt(41) + 10; // 10-50
                    }
                    break;
                case "Medium":
                    int opIndexMedium = random.nextInt(4);
                    operator = opIndexMedium == 0 ? "+" : opIndexMedium == 1 ? "×" : opIndexMedium == 2 ? "÷" : "-";
                    if (operator.equals("-")) {
                        num1 = random.nextInt(51) + 150; // 150-200
                        num2 = random.nextInt(51) + 50;  // 50-100
                    } else if (operator.equals("×")) {
                        num1 = random.nextInt(11) + 10;  // 10-20
                        num2 = random.nextInt(6) + 2;    // 2-7
                    } else if (operator.equals("÷")) {
                        num2 = random.nextInt(6) + 5;    // 5-10
                        answer = random.nextInt(20) + 5; // 5-24
                        num1 = num2 * answer;            // Ensure remainder = 0
                    } else {
                        num1 = random.nextInt(51) + 150; // 150-200
                        num2 = random.nextInt(51) + 50;  // 50-100
                    }
                    break;
                case "Hard":
                    int opIndexHard = random.nextInt(4);
                    operator = opIndexHard == 0 ? "+" : opIndexHard == 1 ? "×" : opIndexHard == 2 ? "÷" : "-";
                    if (operator.equals("-")) {
                        num1 = random.nextInt(51) + 250; // 250-300
                        num2 = random.nextInt(51) + 100; // 100-150
                    } else if (operator.equals("×")) {
                        num1 = random.nextInt(11) + 15;  // 15-25
                        num2 = random.nextInt(6) + 3;    // 3-8
                    } else if (operator.equals("÷")) {
                        num2 = random.nextInt(6) + 10;   // 10-15
                        answer = random.nextInt(20) + 10;// 10-29
                        num1 = num2 * answer;            // Ensure remainder = 0
                    } else {
                        num1 = random.nextInt(51) + 250; // 250-300
                        num2 = random.nextInt(51) + 100; // 100-150
                    }
                    break;
                default:
                    return;
            }
            answer = calculateAnswer(num1, num2, operator);
        } while (answer >= 400 || (operator.equals("÷") && num2 != 0 && num1 % num2 != 0) ||
                (operator.equals("-") && num1 < num2) ||
                (operator.equals("×") && answer > 99) ||
                (operator.equals("-") && answer > 99) ||
                (num1 == 0 || num2 == 0));

        maths_question_TXT.setText(num1 + " " + operator + " " + num2);
        maths_answer.setText("");
    }

    private int calculateAnswer(int num1, int num2, String operator) {
        switch (operator) {
            case "+": return num1 + num2;
            case "-": return num1 - num2;
            case "×": return num1 * num2;
            case "÷": return num1 / num2;
            default: return 0;
        }
    }

    private void checkAnswer(String userAnswer) {
        try {
            if (userAnswer.isEmpty()) {
                maths_answer.setBackgroundResource(R.drawable.input_background);
                return;
            }

            int answer = Integer.parseInt(userAnswer);
            String question = maths_question_TXT.getText().toString();
            String[] parts = question.split(" ");
            int num1 = Integer.parseInt(parts[0]);
            String operator = parts[1];
            int num2 = Integer.parseInt(parts[2]);
            int correctAnswer = calculateAnswer(num1, num2, operator);

            if (answer == correctAnswer) {
                score++;
                questionNumber++;
                score_TXT.setText("Score : " + score);
                questionNumber_TXT.setText("Question: " + questionNumber);
                maths_answer.setBackgroundResource(R.drawable.input_background);
                generateQuestion();
            } else {
                maths_answer.setBackgroundResource(R.drawable.maths_wrong_answer_bg);
            }
        } catch (NumberFormatException e) {
            maths_answer.setBackgroundResource(R.drawable.maths_wrong_answer_bg);
        }
    }

    private void saveHighScore() {
        if (!currentDifficulty.isEmpty()) {
            int currentHighScore = prefs.getInt("high_score_" + currentDifficulty, 0);
            if (score > currentHighScore) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("high_score_" + currentDifficulty, score);
                editor.apply();
            }
        }
    }

    private void showWinRoundDialog() {
        saveHighScore();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.maths_win_round, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        LinearLayout linearLayout = dialogView.findViewById(R.id.linear_GameOver_card);
        AppCompatButton restartBtn = dialogView.findViewById(R.id.win_restart_game_btn);
        AppCompatButton leaveBtn = dialogView.findViewById(R.id.win_leave_game_btn);


        TextView winScoreTXT = dialogView.findViewById(R.id.win_score_TXT);
        TextView highScoreGameOverTXT = dialogView.findViewById(R.id.High_score_game_over_TXT);

        winScoreTXT.setText("Score : " + score);

        // Show high score for current difficulty
        if (!currentDifficulty.isEmpty()) {
            int highScore = prefs.getInt("high_score_" + currentDifficulty, 0);
            highScoreGameOverTXT.setText("High Score: " + highScore);
        }

        restartBtn.setOnClickListener(v -> {
            score = 0;
            questionNumber = 1;
            score_TXT.setText("Score : " + score);
            questionNumber_TXT.setText("Question: 1");
            startTimer(120000);
            generateQuestion();
            maths_answer.setText("");
            dialog.dismiss();
        });

        leaveBtn.setOnClickListener(v -> {
            dialog.dismiss();
            showLeaveDialog();
        });

        dialogView.setOnClickListener(v -> {
            // Do nothing - prevents clicks from passing through
        });

        dialog.show();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        showLeaveDialog();
    }

    private void showLeaveDialog() {
        // Create the dialog layout
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
        dialogLayout.setPadding(50, 50, 50, 35);

        // Title
        TextView title = new TextView(this);
        title.setText("Leave Game");
        title.setTypeface(ResourcesCompat.getFont(this, R.font.caudex), Typeface.BOLD);
        title.setTextSize(22);
        title.setPadding(0, 0, 10, 20);
        title.setTextColor(getResources().getColor(android.R.color.black));

        // Message
        TextView message = new TextView(this);
        message.setText("Do you want to leave the game?");
        message.setTypeface(ResourcesCompat.getFont(this, R.font.caudex));
        message.setTextSize(16);
        message.setPadding(0, 10, 0, 0);
        message.setTextColor(getResources().getColor(android.R.color.black));

        // Add views to the layout
        dialogLayout.addView(title);
        dialogLayout.addView(message);

        // Create and show dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .setPositiveButton("Leave", (dialogInterface, which) -> {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85), // 85% of screen width
                    ViewGroup.LayoutParams.WRAP_CONTENT // Height: wrap content
            );

            // Set dialog background and button text colors
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);

            Typeface customFont = ResourcesCompat.getFont(this, R.font.caudex);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(customFont, Typeface.BOLD);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(customFont, Typeface.BOLD);
        }
    }

    private void showRestartDialog() {
        // Create the dialog layout
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
        dialogLayout.setPadding(50, 50, 50, 35);

        // Title
        TextView title = new TextView(this);
        title.setText("Restart Game");
        title.setTypeface(ResourcesCompat.getFont(this, R.font.caudex), Typeface.BOLD);
        title.setTextSize(22);
        title.setPadding(0, 0, 10, 20);
        title.setTextColor(getResources().getColor(android.R.color.black));

        // Message
        TextView message = new TextView(this);
        message.setText("Do you want to restart your game?");
        message.setTypeface(ResourcesCompat.getFont(this, R.font.caudex));
        message.setTextSize(16);
        message.setPadding(0, 10, 0, 0);
        message.setTextColor(getResources().getColor(android.R.color.black));

        // Add views to the layout
        dialogLayout.addView(title);
        dialogLayout.addView(message);

        // Create and show dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .setPositiveButton("OK", (dialogInterface, which) -> {
                    score = 0;
                    questionNumber = 1;
                    score_TXT.setText("Score : " + score);
                    questionNumber_TXT.setText("Question: 1");
                    startTimer(120000);
                    generateQuestion();
                    maths_answer.setText("");
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85), // 85% of screen width
                    ViewGroup.LayoutParams.WRAP_CONTENT // Height: wrap content
            );

            // Set dialog background and button text colors
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);

            Typeface customFont = ResourcesCompat.getFont(this, R.font.caudex);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(customFont, Typeface.BOLD);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(customFont, Typeface.BOLD);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentDifficulty", currentDifficulty);
        outState.putInt("score", score);
        outState.putInt("questionNumber", questionNumber);
        outState.putBoolean("isTimerRunning", isTimerRunning);
        if (countDownTimer != null) {
            String timeText = Timer.getText().toString();
            String[] parts = timeText.split(":");
            long minutes = Long.parseLong(parts[0]);
            long seconds = Long.parseLong(parts[1]);
            long timeLeft = (minutes * 60 + seconds) * 1000;
            outState.putLong("timeLeft", timeLeft);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        // Update high score display when resuming
        if (!currentDifficulty.isEmpty()) {
            updateHighScoreDisplay();
        }
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}