package com.example.gamezone;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Random;

public class cup_and_ball_game extends AppCompatActivity {
    // Add these with other variables at the top
    private TextView timerTextView;
    private CountDownTimer roundTimer;
    private long roundTimeRemaining = 0;
    private int cupMargin = 15;
    private int cupWidth = 150;

    private int cupHeight = 200;
    private final long SHOW_BALL_TIME = 3000; // 3 seconds to show ball
    private final long HIDE_BALL_TIME = 500; // 0.5 seconds to hide
    private final long GUESS_TIME = 10000; // 10 seconds to guess
    private LinearLayout cupsContainer;
    private TextView scoreTextView, streakTextView, statusTextView;
    private AppCompatButton startButton, resetButton;
    private RadioGroup difficultyRadioGroup;

    private int score = 0;
    private int streak = 0;
    private int totalCups = 3;
    private int ballPosition = 0;
    private int shuffleCount = 5;
    private int shuffleSpeed = 800;
    private int baseShuffleSpeed = 800;
    private int speedIncrement = 0;
    private boolean gameActive = false;
    private boolean shuffling = false;
    private boolean showingBall = false;
    private String currentDifficulty = "easy";

    private ArrayList<ImageView> cups = new ArrayList<>();
    private ArrayList<ImageView> balls = new ArrayList<>();
    private Random random = new Random();
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cup_and_ball_game);

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
        cupsContainer = findViewById(R.id.cupsContainer);
        scoreTextView = findViewById(R.id.scoreTextView);
        streakTextView = findViewById(R.id.streakTextView);
        statusTextView = findViewById(R.id.statusTextView);
        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);
        // Add this in onCreate() after initializing other views
        timerTextView = findViewById(R.id.timerTextView);
        difficultyRadioGroup = findViewById(R.id.difficultyRadioGroup);

        // Set initial difficulty
        ((RadioButton) findViewById(R.id.easyRadio)).setChecked(true);
        currentDifficulty = "easy";

        // Set up cups
        setupCups();

        // Button listeners
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!shuffling && !showingBall) {
                    startGame();
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });

        // Difficulty selection
        difficultyRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.easyRadio) {
                    if (!currentDifficulty.equals("easy") && (gameActive || shuffling || showingBall || streak > 0 || score > 0)) {
                        showDifficultyChangeDialog("easy");
                    } else {
                        changeDifficulty("easy");
                    }
                } else if (checkedId == R.id.mediumRadio) {
                    if (!currentDifficulty.equals("medium") && (gameActive || shuffling || showingBall || streak > 0 || score > 0)) {
                        showDifficultyChangeDialog("medium");
                    } else {
                        changeDifficulty("medium");
                    }
                } else if (checkedId == R.id.hardRadio) {
                    if (!currentDifficulty.equals("hard") && (gameActive || shuffling || showingBall || streak > 0 || score > 0)) {
                        showDifficultyChangeDialog("hard");
                    } else {
                        changeDifficulty("hard");
                    }
                }
            }
        });
    }

    private void showDifficultyChangeDialog(final String newDifficulty) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Difficulty");
        builder.setMessage("Changing difficulty will reset your current game and all progress. Are you sure?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Reset game and change difficulty
            resetGame();
            changeDifficulty(newDifficulty);
            // Update radio button selection
            if (newDifficulty.equals("easy")) {
                ((RadioButton) findViewById(R.id.easyRadio)).setChecked(true);
            } else if (newDifficulty.equals("medium")) {
                ((RadioButton) findViewById(R.id.mediumRadio)).setChecked(true);
            } else if (newDifficulty.equals("hard")) {
                ((RadioButton) findViewById(R.id.hardRadio)).setChecked(true);
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            // Revert to previous difficulty selection
            dialog.dismiss();
            if (currentDifficulty.equals("easy")) {
                ((RadioButton) findViewById(R.id.easyRadio)).setChecked(true);
            } else if (currentDifficulty.equals("medium")) {
                ((RadioButton) findViewById(R.id.mediumRadio)).setChecked(true);
            } else if (currentDifficulty.equals("hard")) {
                ((RadioButton) findViewById(R.id.hardRadio)).setChecked(true);
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void changeDifficulty(String difficulty) {
        currentDifficulty = difficulty;

        // Store current speed before changing base speed
        int currentSpeedBonus = (streak / 3) * 250;

        switch (difficulty) {
            case "easy":
                totalCups = 3;
                shuffleCount = 5;
                baseShuffleSpeed = 700;
                cupWidth = 150;
                cupHeight = 200;
                cupMargin = 15;
                break;
            case "medium":
                totalCups = 4;
                shuffleCount = 7;
                baseShuffleSpeed = 500;
                cupWidth = 130;
                cupHeight = 180;
                cupMargin = 10;
                break;
            case "hard":
                totalCups = 5;
                shuffleCount = 10;
                baseShuffleSpeed = 400;
                cupWidth = 110;
                cupHeight = 160;
                cupMargin = 8;
                break;
        }

        // Recalculate shuffle speed with preserved bonus
        updateShuffleSpeed();

        setupCups();
    }
    private void setupCups() {
        // Clear everything
        cupsContainer.removeAllViews();
        cups.clear();
        balls.clear();

        // Call resetCupsDisplay which now handles creation
        resetCupsDisplay();

        // Update status text
        statusTextView.setText("Ready to play! Select difficulty and click START.");
    }

    private void checkCupSelection(int selectedCupNumber) {
        gameActive = false;
        startButton.setEnabled(true);
        startButton.setText("START GAME");

        // Cancel timer when user makes a guess
        if (roundTimer != null) {
            roundTimer.cancel();
            timerTextView.setText("Time: --");
            timerTextView.setTextColor(0xFFD700);
        }

        // Debug log
        Log.d("CupSelection", "Selected cup: " + selectedCupNumber +
                ", Ball under cup: " + ballPosition);

        // Show result
        if (selectedCupNumber == ballPosition) {
            // Correct guess
            score += 10;
            streak++;

            // Check for speed increase
            if (streak % 3 == 0 && streak > 0) {
                Toast.makeText(this, "⚡ Speed Increased! " + streak + " streak!", Toast.LENGTH_SHORT).show();
            }

            statusTextView.setText("🎉 CORRECT! You found the ball under cup " + ballPosition + "! 🎉");

            // Show ball and highlight correct cup
            showBallResult(selectedCupNumber, true);

            // Update score display
            scoreTextView.setText("Score: " + score);
            streakTextView.setText("Streak: " + streak);

            // Check for streak achievements
            checkAchievements();
        } else {
            // Wrong guess
            streak = 0;

            // Show correct cup numbers
            statusTextView.setText("❌ WRONG! You selected cup " + selectedCupNumber +
                    " but ball was under cup " + ballPosition);

            // Show where user guessed (wrong) and actual ball position
            showBallResult(selectedCupNumber, false);

            streakTextView.setText("Streak: 0");

            // Reset shuffle speed to base when streak is broken
            shuffleSpeed = baseShuffleSpeed;
        }
    }
    private void showBallResult(int selectedCupNumber, boolean isCorrect) {
        // Cancel timer when result is shown
        if (roundTimer != null) {
            roundTimer.cancel();
            timerTextView.setText("Time: --");
            timerTextView.setTextColor(0xFFD700);
        }

        // Find which container has the selected cup and which has the ball
        int selectedContainerIndex = -1;
        int ballContainerIndex = -1;

        for (int i = 0; i < totalCups; i++) {
            View container = cupsContainer.getChildAt(i);
            int cupNumber = (int) container.getTag();

            if (cupNumber == selectedCupNumber) {
                selectedContainerIndex = i;
            }
            if (cupNumber == ballPosition) {
                ballContainerIndex = i;
            }
        }

        // Debug log
        Log.d("ShowBallResult", "Selected container: " + selectedContainerIndex +
                " (cup " + selectedCupNumber + ")" +
                ", Ball container: " + ballContainerIndex +
                " (cup " + ballPosition + ")");

        // Show the actual ball from the correct container
        if (ballContainerIndex != -1) {
            LinearLayout ballContainer = (LinearLayout) cupsContainer.getChildAt(ballContainerIndex);
            ImageView ball = (ImageView) ballContainer.getChildAt(0);
            ball.setVisibility(View.VISIBLE);
        }

        // Highlight selected cup
        if (selectedContainerIndex != -1) {
            LinearLayout selectedContainer = (LinearLayout) cupsContainer.getChildAt(selectedContainerIndex);
            ImageView selectedCup = (ImageView) selectedContainer.getChildAt(1);

            try {
                if (isCorrect) {
                    selectedCup.setImageResource(R.drawable.cup_correct);
                } else {
                    selectedCup.setImageResource(R.drawable.cup_wrong);
                }
            } catch (Exception e) {
                if (isCorrect) {
                    selectedCup.setBackgroundColor(0xFF4CAF50);
                } else {
                    selectedCup.setBackgroundColor(0xFFF44336);
                }
            }
        }

        // Also highlight the correct cup if wrong guess
        if (!isCorrect && ballContainerIndex != -1) {
            LinearLayout correctContainer = (LinearLayout) cupsContainer.getChildAt(ballContainerIndex);
            ImageView correctCup = (ImageView) correctContainer.getChildAt(1);

            try {
                correctCup.setImageResource(R.drawable.cup_correct);
            } catch (Exception e) {
                correctCup.setBackgroundColor(0xFF4CAF50);
            }
        }

        // Reset after 3 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetCupsDisplay();
            }
        }, 3000);
    }
    private void resetCupsDisplay() {
        // Clear all containers
        cupsContainer.removeAllViews();

        // Create new containers in proper order
        for (int i = 0; i < totalCups; i++) {
            // Create cup
            ImageView cup = new ImageView(this);
            LinearLayout.LayoutParams cupParams = new LinearLayout.LayoutParams(cupWidth, cupHeight);
            cupParams.setMargins(cupMargin, 0, cupMargin, 0);
            cup.setLayoutParams(cupParams);

            try {
                cup.setImageResource(R.drawable.cup);
            } catch (Exception e) {
                cup.setBackgroundColor(0xFF795548);
            }

            cup.setTag("cup_" + i);
            cup.setId(View.generateViewId());

            // Create ball (initially hidden)
            ImageView ball = new ImageView(this);
            LinearLayout.LayoutParams ballParams = new LinearLayout.LayoutParams(60, 60);
            ball.setLayoutParams(ballParams);

            try {
                ball.setImageResource(R.drawable.ball);
            } catch (Exception e) {
                ball.setBackgroundColor(0xFFFFC107);
            }

            ball.setVisibility(View.INVISIBLE);
            ball.setTag("ball_" + i);
            ball.setId(View.generateViewId());

            // Create a container for cup and ball
            LinearLayout container = new LinearLayout(this);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            containerParams.setMargins(5, 0, 5, 0);
            container.setLayoutParams(containerParams);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

            // Store the cup number in container tag (1, 2, 3...)
            container.setTag(i + 1);

            // Add ball first (so it appears under cup)
            container.addView(ball);
            container.addView(cup);

            // Store references
            cups.add(cup);
            balls.add(ball);

            // Set click listener for container
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (gameActive && !shuffling && !showingBall) {
                        // Get the cup number from container tag
                        int selectedCupNumber = (int) v.getTag();
                        checkCupSelection(selectedCupNumber);
                    }
                }
            });

            cupsContainer.addView(container);
        }

        // Set new random ball position for next round
        ballPosition = random.nextInt(totalCups) + 1;

        // Reset timer display
        timerTextView.setText("Time: --");
        timerTextView.setTextColor(0xFFD700);

        // Debug log
        Log.d("ResetCups", "New ball position: cup " + ballPosition);
        Log.d("ResetCups", "Created " + totalCups + " new containers with cup size: " + cupWidth + "x" + cupHeight);

        statusTextView.setText("Ready for next round! Click START.");
    }

    private void startGame() {
        gameActive = true;
        shuffling = false;
        showingBall = true;
        startButton.setEnabled(false);
        startButton.setText("WATCH CAREFULLY...");

        // Show ball position message
        statusTextView.setText("Ball is under cup " + ballPosition + ". Remember it!");

        // DO NOT start timer here - timer will start AFTER shuffling

        // Debug log
        Log.d("StartGame", "Starting game - Ball at cup " + ballPosition);

        // Debug all container states
        for (int i = 0; i < totalCups; i++) {
            View container = cupsContainer.getChildAt(i);
            Log.d("StartGameDebug", "Container " + i + " has tag: " + container.getTag() +
                    " (cup " + container.getTag() + ")");
        }

        // Show ball under correct cup
        showBall();

        // Start shuffling after 3 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hidingBall();
                statusTextView.setText("Shuffling cups... Follow the ball!");

                // Start shuffling after hiding ball
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        shuffling = true;

                        // Calculate current shuffle speed with streak bonus
                        updateShuffleSpeed();
                        shuffleCups(0);
                    }
                }, HIDE_BALL_TIME);
            }
        }, SHOW_BALL_TIME);
    }

    private void startRoundTimer(long durationMillis) {
        if (roundTimer != null) {
            roundTimer.cancel();
        }

        roundTimeRemaining = durationMillis;

        roundTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                roundTimeRemaining = millisUntilFinished;
                int seconds = (int) (millisUntilFinished / 1000);
                timerTextView.setText("Time: " + String.format("%02d", seconds));

                // Change color when time is low
                if (seconds <= 3) {
                    timerTextView.setTextColor(Color.RED);
                } else if (seconds <= 5) {
                    timerTextView.setTextColor(Color.BLACK);
                } else {
                    timerTextView.setTextColor(0xFFD700);
                }
            }

            @Override
            public void onFinish() {
                timerTextView.setText("Time: 00");
                timerTextView.setTextColor(Color.RED);

                // Time's up - player loses the round
                if (gameActive && !shuffling) {
                    gameActive = false;
                    streak = 0;
                    score = Math.max(0, score - 5); // Penalty for running out of time
                    statusTextView.setText("⏰ TIME'S UP! You lost the round! -5 points");
                    scoreTextView.setText("Score: " + score);
                    streakTextView.setText("Streak: 0");

                    // Show correct position
                    showBallResult(-1, false);

                    // Reset shuffle speed to base when streak is broken
                    shuffleSpeed = baseShuffleSpeed;

                    // Enable start button for next round
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startButton.setEnabled(true);
                            startButton.setText("START GAME");
                        }
                    }, 3000);
                }
            }
        }.start();
    }
    private void showBall() {
        // Find which container has the ball
        // The ball is hidden in all containers, but we need to find the one where ballPosition matches
        int ballContainerIndex = -1;

        // Check all containers to find which one has the ball (ball is visible or has ball)
        for (int i = 0; i < totalCups; i++) {
            LinearLayout container = (LinearLayout) cupsContainer.getChildAt(i);
            ImageView ball = (ImageView) container.getChildAt(0);

            // Check if this container should have the ball based on ballPosition
            // We need to check the container tag to see which cup it represents
            int cupNumber = (int) container.getTag();
            if (cupNumber == ballPosition) {
                ballContainerIndex = i;
                break;
            }
        }

        if (ballContainerIndex == -1) {
            Log.e("ShowBall", "Could not find container with ball! ballPosition=" + ballPosition);
            return;
        }

        // Hide all balls first
        for (int i = 0; i < totalCups; i++) {
            LinearLayout container = (LinearLayout) cupsContainer.getChildAt(i);
            ImageView ball = (ImageView) container.getChildAt(0);
            ball.setVisibility(View.INVISIBLE);
        }

        // Show ball from the correct container
        LinearLayout ballContainer = (LinearLayout) cupsContainer.getChildAt(ballContainerIndex);
        ImageView ball = (ImageView) ballContainer.getChildAt(0);
        ball.setVisibility(View.VISIBLE);

        // Highlight the correct cup
        ImageView correctCup = (ImageView) ballContainer.getChildAt(1);
        try {
            correctCup.setImageResource(R.drawable.cup_correct);
        } catch (Exception e) {
            correctCup.setBackgroundColor(0xFF4CAF50);
        }

        // Debug log
        Log.d("ShowBall", "Showing ball at container " + ballContainerIndex +
                " (cup " + ballPosition + ")");

        // Also debug all container tags
        for (int i = 0; i < totalCups; i++) {
            View container = cupsContainer.getChildAt(i);
            Log.d("ShowBallDebug", "Container " + i + " has tag: " + container.getTag());
        }
    }
    // Update swapContainers to handle cup numbers (1, 2, 3)
    private void shuffleCups(final int currentShuffle) {
        if (currentShuffle >= shuffleCount) {
            shuffling = false;

            // Don't enable button here - user should guess immediately
            statusTextView.setText("Cups shuffled! Tap on the cup you think has the ball!");

            // Start guess timer ONLY AFTER shuffling is complete
            startRoundTimer(GUESS_TIME);

            // Debug log
            Log.d("ShuffleComplete", "Shuffling complete. Final ball position: cup " + ballPosition);
            return;
        }

        // Select two random cups to swap
        int cup1 = random.nextInt(totalCups);
        int cup2;
        do {
            cup2 = random.nextInt(totalCups);
        } while (cup2 == cup1);

        // Debug log
        Log.d("ShuffleStep", "Step " + (currentShuffle + 1) + "/" + shuffleCount +
                ": Swapping containers at positions " + cup1 + " and " + cup2);

        // Swap cup positions
        swapContainers(cup1, cup2, currentShuffle);
    }
    private void swapContainers(final int cup1Index, final int cup2Index, final int currentShuffle) {
        // Get the container views
        View container1 = cupsContainer.getChildAt(cup1Index);
        View container2 = cupsContainer.getChildAt(cup2Index);

        // Debug before swap
        Log.d("SwapDebug", "Before swap - Container " + cup1Index + " has cup: " + container1.getTag() +
                ", Container " + cup2Index + " has cup: " + container2.getTag() +
                ", Ball under cup: " + ballPosition);

        // Get positions
        final float container1X = container1.getX();
        final float container2X = container2.getX();

        // Create animations
        TranslateAnimation moveContainer1 = new TranslateAnimation(0, container2X - container1X, 0, 0);
        TranslateAnimation moveContainer2 = new TranslateAnimation(0, container1X - container2X, 0, 0);

        moveContainer1.setDuration(shuffleSpeed);
        moveContainer2.setDuration(shuffleSpeed);
        moveContainer1.setFillAfter(true);
        moveContainer2.setFillAfter(true);

        // Start animations
        container1.startAnimation(moveContainer1);
        container2.startAnimation(moveContainer2);

        // Get the cup numbers from the containers (1, 2, 3)
        int cup1Number = (int) container1.getTag();
        int cup2Number = (int) container2.getTag();

        // Update ball position if it was under swapped cups
        if (ballPosition == cup1Number) {
            ballPosition = cup2Number;
            Log.d("BallMove", "Ball moved from cup " + cup1Number + " to cup " + cup2Number);
        } else if (ballPosition == cup2Number) {
            ballPosition = cup1Number;
            Log.d("BallMove", "Ball moved from cup " + cup2Number + " to cup " + cup1Number);
        }

        // Swap the cup numbers in container tags
        container1.setTag(cup2Number);
        container2.setTag(cup1Number);

        // Animation listener
        moveContainer1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Clear animations
                container1.clearAnimation();
                container2.clearAnimation();

                // Update positions
                container1.setX(container2X);
                container2.setX(container1X);

                // Debug after swap
                Log.d("SwapDebug", "After swap - Container " + cup1Index + " has cup: " + container1.getTag() +
                        ", Container " + cup2Index + " has cup: " + container2.getTag() +
                        ", Ball under cup: " + ballPosition);

                // Continue shuffling after delay
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        shuffleCups(currentShuffle + 1);
                    }
                }, 200);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }
    private void hidingBall() {
        showingBall = false;

        // Find which container has the ball
        int ballContainerIndex = -1;
        for (int i = 0; i < totalCups; i++) {
            View container = cupsContainer.getChildAt(i);
            int cupNumber = (int) container.getTag();
            if (cupNumber == ballPosition) {
                ballContainerIndex = i;
                break;
            }
        }

        if (ballContainerIndex == -1) return;

        // Hide the ball from the correct container
        LinearLayout ballContainer = (LinearLayout) cupsContainer.getChildAt(ballContainerIndex);
        ImageView ball = (ImageView) ballContainer.getChildAt(0);
        ball.setVisibility(View.INVISIBLE);

        // Reset cup color
        ImageView cup = (ImageView) ballContainer.getChildAt(1);
        try {
            cup.setImageResource(R.drawable.cup);
        } catch (Exception e) {
            cup.setBackgroundColor(0xFF795548);
        }
    }

    private void updateShuffleSpeed() {
        // Calculate speed bonus based on streak (every 3 correct answers)
        // 150ms decrease per 3 streaks for more noticeable speed increase
        int speedBonus = (streak / 3) * 250;

        // Apply the bonus to ALL difficulty levels
        // But ensure minimum speed is 100ms (fast but still visible)
        int calculatedSpeed = baseShuffleSpeed - speedBonus;

        // Set minimum and maximum bounds
        shuffleSpeed = Math.max(calculatedSpeed, 100); // Minimum 100ms
        shuffleSpeed = Math.min(shuffleSpeed, baseShuffleSpeed); // Don't go slower than base

        // Debug log
        Log.d("SpeedUpdate", "Streak: " + streak +
                ", Base speed: " + baseShuffleSpeed +
                ", Bonus: -" + speedBonus + "ms" +
                ", Final speed: " + shuffleSpeed + "ms");

        // Show notification when speed increases (at streaks 3, 6, 9, etc.)
        if (streak > 0 && streak % 3 == 0) {
            int speedLevel = streak / 3;
            String speedMessage;

            switch (speedLevel) {
                case 1:
                    speedMessage = "⚡ Speed Level 1! Cups moving faster!";
                    break;
                case 2:
                    speedMessage = "⚡⚡ Speed Level 2! Really fast now!";
                    break;
                case 3:
                    speedMessage = "⚡⚡⚡ Speed Level 3! Lightning speed!";
                    break;
                case 4:
                    speedMessage = "⚡⚡⚡⚡ Speed Level 4! Super speed!";
                    break;
                case 5:
                    speedMessage = "⚡⚡⚡⚡⚡ Speed Level 5! Extreme speed!";
                    break;
                default:
                    speedMessage = "⚡⚡⚡ Speed Level " + speedLevel + "! Insane speed!";
                    break;
            }

            Toast.makeText(this, speedMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAchievements() {
        if (streak == 3) {
            Toast.makeText(this, "🔥 Good! 3 in a row!", Toast.LENGTH_SHORT).show();
        } else if (streak == 5) {
            Toast.makeText(this, "🔥🔥 AMAZING! 5 in a row! 🔥🔥", Toast.LENGTH_LONG).show();
        } else if (streak == 10) {
            Toast.makeText(this, "🏆 LEGENDARY! 10 straight wins! 🏆", Toast.LENGTH_LONG).show();
        }
    }

    private void resetGame() {
        score = 0;
        streak = 0;
        gameActive = false;
        shuffling = false;
        showingBall = false;
        speedIncrement = 0;

        // Cancel any running timer
        if (roundTimer != null) {
            roundTimer.cancel();
            timerTextView.setText("Time: --");
            timerTextView.setTextColor(0xFFD700);
        }

        // Reset to base speed for current difficulty
        updateShuffleSpeed();

        scoreTextView.setText("Score: 0");
        streakTextView.setText("Streak: 0");
        statusTextView.setText("Game reset! Select difficulty and click START.");
        startButton.setText("START GAME");
        startButton.setEnabled(true);

        // Reset cups display
        resetCupsDisplay();

        Toast.makeText(this, "Game reset - Speed back to normal", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (roundTimer != null) {
            roundTimer.cancel();
        }
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