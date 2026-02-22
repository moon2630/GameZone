package com.example.gamezone;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.res.ResourcesCompat;

public class baloon_game extends AppCompatActivity {

    private BalloonGameView balloonView;
    private CardView layoutDifficulty;
    private TextView tvScore, tvHighScore, tvTime, tvDifficultyDesc, tvGameStatus, tvBalloonsPopped, tvLevel;

    // Change these to AppCompatButton for proper button functionality
    private TextView btnStart, btnPause, btnReset, btnBack;

    private AppCompatButton btnEasy, btnMedium, btnHard;
    private Handler handler = new Handler();
    private int timeLeft;
    private int score = 0;
    private int highScore = 0;
    private int balloonsPopped = 0;
    private boolean gameRunning = false;
    private boolean gamePaused = false;
    private int selectedDifficulty = 1;
    private String[] difficultyNames = {"Easy", "Med", "Hard"};
    private int[] highScores = new int[3];
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baloon_game);

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

        preferences = getSharedPreferences("BalloonGamePrefs", MODE_PRIVATE);
        loadHighScores();
        // Call this whenever you want to check
        initializeViews();
        setupButtonListeners();
        updateDifficultyDescription();
    }

    private void initializeViews() {
        balloonView = findViewById(R.id.balloonView);
        layoutDifficulty = findViewById(R.id.layoutDifficulty);

        tvScore = findViewById(R.id.tvScore);
        tvHighScore = findViewById(R.id.tvHighScore);
        tvTime = findViewById(R.id.tvTime);
        tvGameStatus = findViewById(R.id.tvGameStatus);
        tvBalloonsPopped = findViewById(R.id.tvBalloonsPopped);
        tvLevel = findViewById(R.id.tvLevel);
        tvDifficultyDesc = findViewById(R.id.tvDifficultyDesc);

        // Initialize as AppCompatButton
        btnStart = findViewById(R.id.btnStart);
        btnPause = findViewById(R.id.btnPause);
        btnReset = findViewById(R.id.btnReset);
        btnBack = findViewById(R.id.btnBack);

        btnEasy = findViewById(R.id.btnEasy);
        btnMedium = findViewById(R.id.btnMedium);
        btnHard = findViewById(R.id.btnHard);

        // Set initial state
        btnPause.setEnabled(false);
        btnReset.setEnabled(false);
    }

    private void setupButtonListeners() {
        // Difficulty selection buttons
        btnEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDifficulty(1);
            }
        });

        btnMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDifficulty(2);
            }
        });

        btnHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDifficulty(3);
            }
        });

        // Game control buttons
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameRunning) {
                    startGame();
                }
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameRunning) {
                    if (gamePaused) {
                        resumeGame();
                    } else {
                        pauseGame();
                    }
                }
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
                startGame(); // Restart game immediately
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDifficultySelection();
            }
        });

        // Touch controls for popping balloons
        balloonView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!gameRunning || gamePaused) return false;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    float y = event.getY();

                    int points = balloonView.popBalloon(x, y);

                    if (points > 0) {
                        score += points;
                        balloonsPopped++;
                        updateScoreDisplay();
                    }
                }
                return true;
            }
        });
    }

    private void selectDifficulty(int difficulty) {
        selectedDifficulty = difficulty;
        balloonView.setDifficulty(difficulty);

        // Show game UI and hide difficulty selection
        layoutDifficulty.setVisibility(View.GONE);

        tvScore.setVisibility(View.VISIBLE);
        tvHighScore.setVisibility(View.VISIBLE);
        tvTime.setVisibility(View.VISIBLE);
        tvLevel.setVisibility(View.VISIBLE);
        tvBalloonsPopped.setVisibility(View.VISIBLE);
        btnStart.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.VISIBLE);
        btnReset.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        balloonView.setVisibility(View.VISIBLE);

        // Update level display
        tvLevel.setText("Lvl: " + difficultyNames[difficulty - 1]);

        // Update high score for selected difficulty
        updateHighScoreDisplay();

        // Show difficulty description
        String[] descriptions = {
                "Easy: Slow balloons, more time\nPerfect for beginners!",
                "Medium: Faster balloons, normal time\nGood challenge!",
                "Hard: Very fast, many balloons\nExpert mode!"
        };
        Toast.makeText(this, descriptions[difficulty - 1], Toast.LENGTH_LONG).show();
    }


    // Example: Show current spawn info
    private void showSpawnInfo() {
        String info = balloonView.getSpawnInfo();
        Log.d("Game", info);

        // Optional: Show as toast when multiplier increases
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }


    private void showDifficultySelection() {
        resetGame();

        // Show difficulty selection and hide game UI
        layoutDifficulty.setVisibility(View.VISIBLE);

        tvScore.setVisibility(View.GONE);
        tvHighScore.setVisibility(View.GONE);
        tvTime.setVisibility(View.GONE);
        tvLevel.setVisibility(View.GONE);
        tvGameStatus.setVisibility(View.GONE);
        tvBalloonsPopped.setVisibility(View.GONE);
        btnStart.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);
        balloonView.setVisibility(View.GONE);

        updateDifficultyDescription();
    }

    private void updateDifficultyDescription() {
        String desc = "EASY: Slow balloons, 90 seconds\n" +
                "MEDIUM: Faster balloons, 60 seconds\n" +
                "HARD: Very fast, many balloons, 45 seconds\n\n" +
                "High Scores:\n" +
                "Easy: " + highScores[0] + "\n" +
                "Medium: " + highScores[1] + "\n" +
                "Hard: " + highScores[2];
        tvDifficultyDesc.setText(desc);
    }

    private void pauseGame() {
        gamePaused = true;
        btnPause.setCompoundDrawablesWithIntrinsicBounds(
                getResources().getDrawable(R.drawable.vector_resume),
                null, null, null
        );
        tvGameStatus.setText("Game Paused");
        tvGameStatus.setVisibility(View.VISIBLE);

        // Stop game loop
        handler.removeCallbacks(gameRunnable);
        handler.removeCallbacks(timerRunnable);
    }

    private void resumeGame() {
        gamePaused = false;
        btnPause.setCompoundDrawablesWithIntrinsicBounds(
                getResources().getDrawable(R.drawable.vector_pause),
                null, null, null
        );
        tvGameStatus.setVisibility(View.INVISIBLE);

        // Resume game loop
        handler.post(gameRunnable);
        handler.postDelayed(timerRunnable, 1000);
    }

    private void resetGame() {
        gameRunning = false;
        gamePaused = false;

        handler.removeCallbacks(gameRunnable);
        handler.removeCallbacks(timerRunnable);

        btnStart.setEnabled(true);
        btnPause.setEnabled(false);
        btnReset.setEnabled(false);

        // Reset pause button to default state
        btnPause.setCompoundDrawablesWithIntrinsicBounds(
                getResources().getDrawable(R.drawable.vector_pause),
                null, null, null
        );

        tvGameStatus.setVisibility(View.GONE);
        balloonView.reset();

        // Reset score display
        score = 0;
        balloonsPopped = 0;
        updateScoreDisplay();
    }

    private void gameOver() {
        gameRunning = false;

        handler.removeCallbacks(gameRunnable);
        handler.removeCallbacks(timerRunnable);

        btnStart.setEnabled(true);
        btnPause.setEnabled(false);
        btnReset.setEnabled(true);

        tvGameStatus.setText("GAME OVER!\nScore: " + score);
        tvGameStatus.setVisibility(View.VISIBLE);

        // Save high score for current difficulty
        if (score > highScores[selectedDifficulty - 1]) {
            highScores[selectedDifficulty - 1] = score;
            saveHighScores();
            updateHighScoreDisplay();

            String message = "NEW " + difficultyNames[selectedDifficulty - 1].toUpperCase() +
                    " HIGH SCORE: " + score + "!";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (gameRunning && !gamePaused) {
                timeLeft--;
                updateTimeDisplay();

                // Optional: Show notification when quantity increases
                if (timeLeft % 10 == 0 && timeLeft > 0) {
                    // Every 10 seconds, show a message
                    runOnUiThread(() -> {

                        showSpawnInfo();

                        Toast.makeText(baloon_game.this,
                                "More balloons incoming!", Toast.LENGTH_SHORT).show();
                    });
                }

                if (timeLeft <= 0) {
                    gameOver();
                } else {
                    handler.postDelayed(this, 1000);
                }
            }
        }
    };

    private void updateScoreDisplay() {
        tvScore.setText("Score: " + score);
        tvBalloonsPopped.setText("x" + balloonsPopped);
    }

    private void updateHighScoreDisplay() {
        int currentHighScore = highScores[selectedDifficulty - 1];
        tvHighScore.setText("High Score: " + currentHighScore);
    }

    private void updateTimeDisplay() {
        tvTime.setText("Time: " + timeLeft);

    }

    // Update the gameRunnable to check for missed balloons
    private Runnable gameRunnable = new Runnable() {
        @Override
        public void run() {
            if (gameRunning && !gamePaused) {
                balloonView.update();
                balloonView.invalidate();

                handler.postDelayed(this, 16);
            }
        }
    };

    // Reset missed count when game starts
    private void startGame() {
        gameRunning = true;
        gamePaused = false;
        score = 0;
        balloonsPopped = 0;
        timeLeft = balloonView.getGameTime();

        btnStart.setEnabled(false);
        btnPause.setEnabled(true);
        btnReset.setEnabled(true);

        // Set pause button to default state
        btnPause.setCompoundDrawablesWithIntrinsicBounds(
                getResources().getDrawable(R.drawable.vector_pause),
                null, null, null
        );

        tvGameStatus.setVisibility(View.INVISIBLE);
        balloonView.reset();
        balloonView.resetMissedCount();

        updateScoreDisplay();
        updateTimeDisplay();

        handler.post(gameRunnable);
        handler.post(timerRunnable);

        String[] diffTips = {
                "Easy: Take your time!",
                "Medium: Be quick!",
                "Hard: Don't miss any!"
        };
        Toast.makeText(this, diffTips[selectedDifficulty - 1], Toast.LENGTH_SHORT).show();
    }

    private void loadHighScores() {
        highScores[0] = preferences.getInt("highScoreEasy", 0);
        highScores[1] = preferences.getInt("highScoreMedium", 0);
        highScores[2] = preferences.getInt("highScoreHard", 0);
        highScore = highScores[0];
    }

    private void saveHighScores() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("highScoreEasy", highScores[0]);
        editor.putInt("highScoreMedium", highScores[1]);
        editor.putInt("highScoreHard", highScores[2]);
        editor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameRunning && !gamePaused) {
            pauseGame();
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