package com.example.gamezone;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class guess_the_number extends AppCompatActivity {
    private int displayRound = 1;
    private LinearLayout  gameLayout;
    private LinearLayout answerContainer, inputContainer;
    private View leftContainer, rightContainer;
    private CardView difficultyLayout,gameOverLayout;
    private TextView scoreTextView, timerTextView, streakTextView, hintTextView;
    private TextView gameOverText, resultText;
    private AppCompatButton easyButton, mediumButton, hardButton;
    private AppCompatButton startButton, restartButton, playAgainButton;

   // Animation speeds for different difficulties (in milliseconds)
    private final float EASY_SPEEDS[] = {1500f, 1000f, 800f};     // 1.5s, 1s, 0.8s
    private final float MEDIUM_SPEEDS[] = {1200f, 800f, 600f};    // 1.2s, 0.8s, 0.6s
    private final float HARD_SPEEDS[] = {1000f, 700f, 500f};      // 1s, 0.7s, 0.5s
    // Game State
    private int currentDifficulty = 3; // 3, 4, or 5 numbers
    private int score = 0;
    private int streak = 0;
    private int currentRound = 1;
    private boolean gameActive = false;
    private boolean numbersVisible = false;
    private List<Integer> targetNumbers = new ArrayList<>();
    private List<EditText> inputFields = new ArrayList<>();
    private List<TextView> numberDisplays = new ArrayList<>();

    // Timer
    private CountDownTimer gameTimer;
    private long GAME_TIME = 30000; // Default 30 seconds
    // Add these with other arrays at the top
    private final long EASY_REVEAL_TIMES[] = {1500, 1400, 1300, 1200, 1100, 1000, 900, 800, 700, 600};
    private final long MEDIUM_REVEAL_TIMES[] = {1200, 1100, 1000, 900, 800, 700, 600, 500, 400, 300};
    private final long HARD_REVEAL_TIMES[] = {1000, 900, 800, 700, 600, 500, 400, 300, 200, 100};

    private int speedLevel = 1; // Speed multiplier (1 = normal)
    private final int BASE_ANIMATION_TIME = 500; // Base animation time in ms

    // Random
    private Random random = new Random();
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_the_number);


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


        // Initialize views
        initializeViews();

        // Set up button listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        // Layouts
        difficultyLayout = findViewById(R.id.difficultyLayout);
        gameLayout = findViewById(R.id.gameLayout);
        gameOverLayout = findViewById(R.id.gameOverLayout);

        // Answer and input containers
        answerContainer = findViewById(R.id.answerContainer);
        inputContainer = findViewById(R.id.inputContainer);
        leftContainer = findViewById(R.id.leftContainer);
        rightContainer = findViewById(R.id.rightContainer);

        // Text views
        scoreTextView = findViewById(R.id.scoreTextView);
        timerTextView = findViewById(R.id.timerTextView);
        streakTextView = findViewById(R.id.streakTextView);
        hintTextView = findViewById(R.id.hintTextView);
        gameOverText = findViewById(R.id.gameOverText);
        resultText = findViewById(R.id.resultText);

        // Buttons
        easyButton = findViewById(R.id.easyButton);
        mediumButton = findViewById(R.id.mediumButton);
        hardButton = findViewById(R.id.hardButton);
        startButton = findViewById(R.id.startButton);
        restartButton = findViewById(R.id.restartButton);
        playAgainButton = findViewById(R.id.playAgainButton);
    }

    private void setupButtonListeners() {
        // Difficulty selection
        easyButton.setOnClickListener(v -> startGameWithDifficulty(3));
        mediumButton.setOnClickListener(v -> startGameWithDifficulty(4));
        hardButton.setOnClickListener(v -> startGameWithDifficulty(5));

        // Game buttons
        startButton.setOnClickListener(v -> startRevealAnimation());
        restartButton.setOnClickListener(v -> restartGame());
        playAgainButton.setOnClickListener(v -> restartGame());

        // Add try again button listener
        AppCompatButton tryAgainButton = findViewById(R.id.tryAgainButton);
        tryAgainButton.setOnClickListener(v -> {
            // Try again with same difficulty
            gameOverLayout.setVisibility(View.GONE);
            gameLayout.setAlpha(1.0f);

            // Reset game state but keep same difficulty
            score = 0;
            streak = 0;
            currentRound = 1;
            displayRound = 1; // Add this
            speedLevel = 1;
            gameActive = false;
            numbersVisible = false;

            // Update UI
            scoreTextView.setText("Score: " + score);
            streakTextView.setText("Streak: " + streak);

            // Reset containers
            leftContainer.setTranslationX(0);
            rightContainer.setTranslationX(0);
            leftContainer.setVisibility(View.VISIBLE);
            rightContainer.setVisibility(View.VISIBLE);

            // Generate new numbers
            generateNumbers();
            setupNumberDisplays();
            setupInputFields();

            // Reset input backgrounds
            for (EditText inputField : inputFields) {
                inputField.setBackgroundResource(R.drawable.input_background_game);
                inputField.setText("");
            }

            // Enable start button
            startButton.setEnabled(true);

            // Reset timer based on difficulty
            updateTimeForDifficulty();

            hintTextView.setText("Click START to reveal numbers!");
        });
    }


    // Remove this line: private final long REVEAL_TIME = 1500; // 2 seconds to memorize
// Replace with method below

    private long getRevealTime() {
        long[] revealTimes;

        // Select reveal time array based on difficulty
        switch (currentDifficulty) {
            case 3: // Easy
                revealTimes = EASY_REVEAL_TIMES;
                break;
            case 4: // Medium
                revealTimes = MEDIUM_REVEAL_TIMES;
                break;
            case 5: // Hard
                revealTimes = HARD_REVEAL_TIMES;
                break;
            default:
                revealTimes = EASY_REVEAL_TIMES;
        }

        // FIX: Calculate speed level based on currentRound, not streak
        // Every 3 rounds increase speed level
        int speedLevelIndex = Math.min((currentRound - 1), revealTimes.length - 1);

        return revealTimes[speedLevelIndex];
    }
    private void startGameWithDifficulty(int difficulty) {
        currentDifficulty = difficulty;
        speedLevel = 1; // Reset speed level
        gameActive = false;
        numbersVisible = false;

        // Hide difficulty layout, show game layout
        difficultyLayout.setVisibility(View.GONE);
        gameLayout.setVisibility(View.VISIBLE);
        gameOverLayout.setVisibility(View.GONE);

        // Reset game state
        score = 0;
        streak = 0;
        currentRound = 1;
        displayRound = 1; // Reset display round too

        // Update UI
        scoreTextView.setText("Score: " + score);
        streakTextView.setText("Streak: " + streak);

        // Set time based on difficulty
        updateTimeForDifficulty();

        // Make sure containers are visible
        leftContainer.setVisibility(View.VISIBLE);
        rightContainer.setVisibility(View.VISIBLE);
        leftContainer.setTranslationX(0);
        rightContainer.setTranslationX(0);

        // Generate first numbers
        generateNumbers();
        setupNumberDisplays();
        setupInputFields();

        // Enable start button
        startButton.setEnabled(true);

        hintTextView.setText("Click START to begin!");
    }    private void updateTimeForDifficulty() {
        switch (currentDifficulty) {
            case 3: // Easy
                GAME_TIME = 30000; // 30 seconds
                break;
            case 4: // Medium
                GAME_TIME = 20000; // 20 seconds
                break;
            case 5: // Hard
                GAME_TIME = 12000; // 12 seconds
                break;
        }
        timerTextView.setText("Time: " + (GAME_TIME / 1000));
        timerTextView.setTextColor(0xFFFFD700);
    }

    private void generateNumbers() {
        targetNumbers.clear();
        for (int i = 0; i < currentDifficulty; i++) {
            // Generate random numbers between 0-9
            targetNumbers.add(random.nextInt(10));
        }
    }

    private void setupNumberDisplays() {
        answerContainer.removeAllViews();
        numberDisplays.clear();

        // Create text views for each number
        for (int i = 0; i < currentDifficulty; i++) {
            TextView numberView = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    80, 80
            );
            params.setMargins(15, 8, 15, 8);
            numberView.setLayoutParams(params);

            // Set number text
            if (numbersVisible) {
                numberView.setText(String.valueOf(targetNumbers.get(i)));
            } else {
                numberView.setText("?");
            }

            numberView.setTextSize(25);
            numberView.setTextColor(0xFFFFFFFF);
            numberView.setBackgroundResource(R.drawable.number_background);
            numberView.setTypeface(ResourcesCompat.getFont(this, R.font.caudex)); // Add this line
            numberView.setGravity(Gravity.CENTER);  // Fixed here

            answerContainer.addView(numberView);
            numberDisplays.add(numberView);
        }
    }

    private void setupInputFields() {
        inputContainer.removeAllViews();
        inputFields.clear();

        // Create input fields for each number
        for (int i = 0; i < currentDifficulty; i++) {
            EditText inputField = new EditText(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    100, 100
            );
            params.setMargins(15, 5, 15, 5);
            inputField.setLayoutParams(params);

            // Set input properties
            inputField.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            inputField.setMaxLines(1);
            inputField.setTextSize(20);
            inputField.setTextColor(0xFF000000);
            inputField.setBackgroundResource(R.drawable.input_background_game);
            inputField.setTypeface(ResourcesCompat.getFont(this, R.font.caudex)); // Add this line

            inputField.setGravity(Gravity.CENTER);  // Fixed here
            inputField.setEnabled(false); // Disable until reveal is complete
            inputField.setTag(i); // Store index

            // Add text change listener
            inputField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1) {
                        int currentIndex = (int) inputField.getTag();

                        // Auto-focus to next field
                        if (currentIndex < inputFields.size() - 1) {
                            inputFields.get(currentIndex + 1).requestFocus();
                        } else {
                            // Last field - hide keyboard and check if all filled
                            inputField.clearFocus();
                            checkIfAllFilled();
                        }

                        // Check if all fields are filled
                        checkIfAllFilled();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            inputContainer.addView(inputField);
            inputFields.add(inputField);
        }
    }
    private void checkSingleInput(int index, String input) {
        if (input.isEmpty()) {
            inputFields.get(index).setBackgroundResource(R.drawable.input_background_game);
            return;
        }

        try {
            int userNumber = Integer.parseInt(input);
            if (userNumber == targetNumbers.get(index)) {
                inputFields.get(index).setBackgroundResource(R.drawable.input_correct);
            } else {
                inputFields.get(index).setBackgroundResource(R.drawable.input_wrong);
            }
        } catch (NumberFormatException e) {
            inputFields.get(index).setBackgroundResource(R.drawable.input_wrong);
        }
    }

    private void checkIfAllFilled() {
        boolean allFilled = true;

        // Check if all fields have input
        for (EditText inputField : inputFields) {
            String input = inputField.getText().toString().trim();
            if (input.isEmpty()) {
                allFilled = false;
                break;
            }
        }

        if (allFilled) {
            // All fields are filled, check answer
            checkAnswer();
        }
    }

    private void startRevealAnimation() {
        if (gameActive) return;

        gameActive = true;
        startButton.setEnabled(false);

        // Get current animation speed
        float animationSpeed = getCurrentAnimationSpeed();

        // Get current reveal time based on round
        long revealTime = getRevealTime();

        // Show speed level in hint with reveal time
        hintTextView.setText("Memorize the numbers! (" + (revealTime/1000f) + "s)");

        // Make sure containers are covering numbers initially
        leftContainer.setTranslationX(0);
        rightContainer.setTranslationX(0);
        leftContainer.setVisibility(View.VISIBLE);
        rightContainer.setVisibility(View.VISIBLE);

        // Show numbers
        revealNumbers();

        // Animate containers sliding away with current speed
        animateContainers(true, (int) animationSpeed);

        // After reveal time, hide numbers and bring containers back
        handler.postDelayed(() -> {
            hideNumbers();

            // Reset positions for animation back
            leftContainer.setTranslationX(-leftContainer.getWidth());
            rightContainer.setTranslationX(rightContainer.getWidth());

            // Animate containers back to cover numbers with same speed
            ObjectAnimator leftBackAnim = ObjectAnimator.ofFloat(leftContainer, "translationX", 0);
            ObjectAnimator rightBackAnim = ObjectAnimator.ofFloat(rightContainer, "translationX", 0);

            leftBackAnim.setDuration((int) animationSpeed);
            rightBackAnim.setDuration((int) animationSpeed);
            leftBackAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            rightBackAnim.setInterpolator(new AccelerateDecelerateInterpolator());

            leftBackAnim.start();
            rightBackAnim.start();

            // Enable input fields and start timer
            handler.postDelayed(() -> {
                enableInputFields();
                startGameTimer();
                hintTextView.setText("Enter the numbers!");
            }, (int) animationSpeed);

        }, revealTime); // Use dynamic reveal time
    }

    private void revealNumbers() {
        numbersVisible = true;
        for (int i = 0; i < numberDisplays.size(); i++) {
            TextView numberView = numberDisplays.get(i);
            numberView.setText(String.valueOf(targetNumbers.get(i)));
        }
    }

    private void hideNumbers() {
        numbersVisible = false;
        for (TextView numberView : numberDisplays) {
            numberView.setText("?");
        }
    }

    private void animateContainers(final boolean reveal, int animationTime) {
        float containerWidth = leftContainer.getWidth();

        float leftTargetX = reveal ? -containerWidth : 0;
        float rightTargetX = reveal ? containerWidth : 0;

        // Animate left container
        ObjectAnimator leftAnim = ObjectAnimator.ofFloat(leftContainer, "translationX", leftTargetX);
        leftAnim.setDuration(animationTime);
        leftAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        // Animate right container
        ObjectAnimator rightAnim = ObjectAnimator.ofFloat(rightContainer, "translationX", rightTargetX);
        rightAnim.setDuration(animationTime);
        rightAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        leftAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!reveal) {
                    leftContainer.setTranslationX(0);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        rightAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!reveal) {
                    rightContainer.setTranslationX(0);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        leftAnim.start();
        rightAnim.start();
    }

    private void enableInputFields() {
        for (EditText inputField : inputFields) {
            inputField.setEnabled(true);
            inputField.setText("");
            inputField.setBackgroundResource(R.drawable.input_background_game); // Reset to normal background
        }

        // Focus on first input field
        if (!inputFields.isEmpty()) {
            inputFields.get(0).requestFocus();
        }
    }

    private void disableInputFields() {
        for (EditText inputField : inputFields) {
            inputField.setEnabled(false);
        }
    }

    private void startGameTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        gameTimer = new CountDownTimer(GAME_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                timerTextView.setText("Time: " + seconds);

                // Change color when time is low
                if (seconds <= 3) {
                    timerTextView.setTextColor(0xFFFF0000); // Red
                } else if (seconds <= 5) {
                    timerTextView.setTextColor(0xFFFFFF00); // Yellow
                } else {
                    timerTextView.setTextColor(0xFFFFD700); // Gold
                }
            }

            @Override
            public void onFinish() {
                timerTextView.setText("Time: 0");
                timerTextView.setTextColor(0xFFFF0000);

                // Time's up - auto check answer
                checkAnswer();
            }
        }.start();
    }

    private void checkAnswer() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        gameActive = false;
        disableInputFields();

        // Check each input
        boolean allCorrect = true;
        List<Integer> userAnswers = new ArrayList<>();

        for (int i = 0; i < inputFields.size(); i++) {
            EditText inputField = inputFields.get(i);
            String userInput = inputField.getText().toString().trim();

            if (userInput.isEmpty()) {
                userAnswers.add(-1);
                allCorrect = false;
                continue;
            }

            try {
                int userNumber = Integer.parseInt(userInput);
                userAnswers.add(userNumber);

                if (userNumber != targetNumbers.get(i)) {
                    allCorrect = false;
                }
            } catch (NumberFormatException e) {
                userAnswers.add(-1);
                allCorrect = false;
            }
        }

        // Highlight results
        highlightResults(allCorrect, userAnswers);

        if (allCorrect) {
            // Correct answer
            score += currentDifficulty * 10;
            streak++;

            // Update displayRound BEFORE incrementing currentRound
            displayRound = currentRound;
            currentRound++;

            // Update animation speed based on streak
            updateAnimationSpeed();

            scoreTextView.setText("Score: " + score);
            streakTextView.setText("Streak: " + streak);

            // Get current animation speed
            float animationSpeed = getCurrentAnimationSpeed();

            // Show containers moving away
            animateContainers(true, (int) animationSpeed);
            revealNumbers();

            hintTextView.setText("✅ CORRECT! +" + (currentDifficulty * 10) + " points");

            // Prepare for next round after 2 seconds
            handler.postDelayed(() -> {
                // Generate new numbers
                generateNumbers();

                // Reset containers with current speed
                animateContainers(false, (int) animationSpeed);
                hideNumbers();

                // Reset input fields
                for (EditText inputField : inputFields) {
                    inputField.setBackgroundResource(R.drawable.input_background);
                    inputField.setText("");
                }

                // Enable start button for next round
                startButton.setEnabled(true);

                // Show correct round number (displayRound shows current completed round)
                hintTextView.setText("Round " + displayRound + " - Click START!");
            }, 2000);

        } else {
            // Wrong answer - show game over after 5 seconds
            streak = 0;
            speedLevel = 1; // Reset speed level
            streakTextView.setText("Streak: 0");

            // Show containers permanently with base speed
            animateContainers(true, BASE_ANIMATION_TIME);
            revealNumbers();

            hintTextView.setText("❌ WRONG ANSWER!");

            // Wait 5 seconds before showing game over
            handler.postDelayed(() -> {
                showGameOver(userAnswers);
            }, 5000); // 5 seconds delay
        }
    }
    private void updateAnimationSpeed() {
        // Determine speed level based on streak
        if (streak >= 10) {
            speedLevel = 3; // Fastest speed
        } else if (streak >= 3) {
            speedLevel = 2; // Medium speed
        } else {
            speedLevel = 1; // Normal speed
        }

        // Show speed update notification
        if (streak == 3 || streak == 10) {
            Toast.makeText(this, "⚡ Animation Speed Level " + speedLevel + "!", Toast.LENGTH_SHORT).show();
        }
    }


    private float getCurrentAnimationSpeed() {
        float[] speeds;

        // Select speed array based on difficulty
        switch (currentDifficulty) {
            case 3: // Easy
                speeds = EASY_SPEEDS;
                break;
            case 4: // Medium
                speeds = MEDIUM_SPEEDS;
                break;
            case 5: // Hard
                speeds = HARD_SPEEDS;
                break;
            default:
                speeds = EASY_SPEEDS;
        }

        // Get speed based on current level (1-3)
        int index = Math.min(speedLevel - 1, speeds.length - 1);
        return speeds[index];
    }

     private void highlightResults(boolean allCorrect, List<Integer> userAnswers) {
        // Highlight each field based on correctness
        for (int i = 0; i < inputFields.size(); i++) {
            EditText inputField = inputFields.get(i);

            if (userAnswers.get(i) == -1) {
                // Empty field
                inputField.setBackgroundResource(R.drawable.input_wrong);
            } else {
                // Check if this specific number is correct
                if (userAnswers.get(i) == targetNumbers.get(i)) {
                    inputField.setBackgroundResource(R.drawable.input_correct);
                } else {
                    inputField.setBackgroundResource(R.drawable.input_wrong);
                }
            }
        }

        // Example: If target is [1, 3, 5] and user enters [1, 9, 5]:
        // Field 0: 1 = correct → green
        // Field 1: 9 = wrong → red
        // Field 2: 5 = correct → green
    }
    private void showGameOver(List<Integer> userAnswers) {
        // Build result message
        StringBuilder resultMessage = new StringBuilder();
        resultMessage.append("Your answer: ");
        for (int i = 0; i < userAnswers.size(); i++) {
            if (userAnswers.get(i) == -1) {
                resultMessage.append("_ ");
            } else {
                resultMessage.append(userAnswers.get(i)).append(" ");
            }
        }

        resultMessage.append("\nCorrect answer: ");
        for (int num : targetNumbers) {
            resultMessage.append(num).append(" ");
        }

        // Disable all game controls
        gameActive = false; // ← ADD THIS LINE
        startButton.setEnabled(false);
        disableInputFields();

        // Show game over layout
        gameLayout.setAlpha(0.3f);
        gameOverLayout.setVisibility(View.VISIBLE);
        resultText.setText(resultMessage.toString());

        // Add final score and speed level
        gameOverText.setText("GAME OVER\nFinal Score: " + score + "\nSpeed Level: " + speedLevel);
    }
    private void restartGame() {
        // Hide game over layout
        gameOverLayout.setVisibility(View.GONE);

        // Reset game layout alpha
        gameLayout.setAlpha(1.0f);

        // Hide game layout, show difficulty selection
        gameLayout.setVisibility(View.GONE);
        difficultyLayout.setVisibility(View.VISIBLE);

        // Reset game state
        gameActive = false; // ← ADD THIS LINE
        numbersVisible = false; // ← ADD THIS LINE

        // Reset containers position
        leftContainer.setTranslationX(0);
        rightContainer.setTranslationX(0);
        leftContainer.setVisibility(View.VISIBLE);
        rightContainer.setVisibility(View.VISIBLE);

        // Cancel timer
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        // Reset timer display
        timerTextView.setText("Time: 30");
        timerTextView.setTextColor(0xFFFFD700);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBackPressed() {
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
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85), // 90% of screen width
                    ViewGroup.LayoutParams.WRAP_CONTENT // Height: wrap content
            );

            // Set dialog background and button text colors
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);

            Typeface customFont = ResourcesCompat.getFont(this, R.font.caudex);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(customFont, Typeface.BOLD);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(customFont, Typeface.BOLD);
        } else {
            super.onBackPressed();
        }

    }
}