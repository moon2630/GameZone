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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

public class space_shooter extends AppCompatActivity {

    // Game Views
    private SpaceShooterView spaceView;
    private TextView tvScore, tvLives, tvLevel, tvGameStatus;
    private TextView tvFinalScore, tvHighScore;
    private CardView startMenu, gameOverMenu;
    private TextView btnPauseResume; // New pause/resume button

    private LinearLayout powerupLayout;
    private TextView tvPowerup;

    // Game State
    private boolean gameRunning = false;
    private boolean isPaused = false;
    private int highScore = 0;
    private String difficulty = "normal";

    // Game Handler
    private Handler gameHandler = new Handler();
    private static final long UPDATE_INTERVAL = 16; // ~60 FPS

    // Preferences
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_shooter);

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

        // Initialize preferences
        preferences = getSharedPreferences("SpaceShooterPrefs", MODE_PRIVATE);
        highScore = preferences.getInt("highScore", 0);

        // Initialize views
        initViews();

        // Setup button listeners
        setupButtonListeners();

        // Show start menu
        showStartMenu();
    }

    private void initViews() {
        spaceView = findViewById(R.id.spaceView);

        // HUD elements
        tvScore = findViewById(R.id.tvScore);
        tvLives = findViewById(R.id.tvLives);
        tvLevel = findViewById(R.id.tvLevel);
        tvGameStatus = findViewById(R.id.tvGameStatus);

        // Pause/Resume button
        btnPauseResume = findViewById(R.id.btnPauseResume);
        btnPauseResume.setVisibility(View.VISIBLE);
        btnPauseResume.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_pause, 0, 0, 0);

        // Power-up indicator
        powerupLayout = findViewById(R.id.powerupLayout);
        tvPowerup = findViewById(R.id.tvPowerup);

        // Menus
        startMenu = findViewById(R.id.startMenu);
        gameOverMenu = findViewById(R.id.gameOverMenu);

        // Game over elements
        tvFinalScore = findViewById(R.id.tvFinalScore);
        tvHighScore = findViewById(R.id.tvHighScore);

        // Buttons
        TextView btnLeft = findViewById(R.id.btnLeft);
        TextView btnRight = findViewById(R.id.btnRight);
        TextView btnShoot = findViewById(R.id.btnShoot);

        // Set button listeners for controls
        btnLeft.setOnClickListener(v -> moveLeft());
        btnRight.setOnClickListener(v -> moveRight());
        btnShoot.setOnClickListener(v -> shoot());

        // Long press for continuous movement
        btnLeft.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startContinuousMove(-1);
                return true;
            }
        });

        btnRight.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startContinuousMove(1);
                return true;
            }
        });

        // Stop movement when button is released
        btnLeft.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                stopContinuousMove();
            }
            return false;
        });

        btnRight.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                stopContinuousMove();
            }
            return false;
        });
    }

    private void setupButtonListeners() {
        // Start menu buttons
        findViewById(R.id.btnStartGame).setOnClickListener(v -> startGame());
        findViewById(R.id.btnDifficulty).setOnClickListener(v -> toggleDifficulty());

        // Game over buttons
        findViewById(R.id.btnPlayAgain).setOnClickListener(v -> playAgain());
        findViewById(R.id.btnMainMenu).setOnClickListener(v -> showStartMenu());

        // Pause/Resume button
        btnPauseResume.setOnClickListener(v -> togglePauseResume());
    }

    private void showStartMenu() {
        stopGame();
        isPaused = false;

        startMenu.setVisibility(View.VISIBLE);
        gameOverMenu.setVisibility(View.GONE);
        tvGameStatus.setVisibility(View.GONE);
        powerupLayout.setVisibility(View.GONE);
        btnPauseResume.setVisibility(View.VISIBLE);
        btnPauseResume.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_pause, 0, 0, 0);

        // Update high score display
        updateHighScoreDisplay();
    }

    private void toggleDifficulty() {
        String[] difficulties = {"easy", "normal", "hard", "expert"};
        String[] displayNames = {"EASY", "NORMAL", "HARD", "EXPERT"};

        for (int i = 0; i < difficulties.length; i++) {
            if (difficulty.equals(difficulties[i])) {
                difficulty = difficulties[(i + 1) % difficulties.length];
                String displayName = displayNames[(i + 1) % difficulties.length];
                ((Button)findViewById(R.id.btnDifficulty)).setText("DIFFICULTY: " + displayName);

                Toast.makeText(this, displayName + " mode selected", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    private void startGame() {
        startMenu.setVisibility(View.GONE);
        gameOverMenu.setVisibility(View.GONE);
        tvGameStatus.setVisibility(View.GONE);
        powerupLayout.setVisibility(View.GONE);
        btnPauseResume.setVisibility(View.VISIBLE);
        isPaused = false;
        btnPauseResume.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_pause, 0, 0, 0);

        // Set difficulty
        spaceView.setDifficulty(difficulty);

        // Start game
        spaceView.startGame();
        gameRunning = true;

        // Start game loop
        gameHandler.post(gameUpdateRunnable);

        // Show countdown
        showCountdown();
    }

    private void showCountdown() {
        tvGameStatus.setVisibility(View.VISIBLE);
        tvGameStatus.setText("3");
        tvGameStatus.setTextColor(Color.parseColor("#FF5555"));

        gameHandler.postDelayed(() -> {
            tvGameStatus.setText("2");
            tvGameStatus.setTextColor(Color.parseColor("#FFAA00"));
        }, 1000);

        gameHandler.postDelayed(() -> {
            tvGameStatus.setText("1");
            tvGameStatus.setTextColor(Color.parseColor("#FFFF00"));
        }, 2000);

        gameHandler.postDelayed(() -> {
            tvGameStatus.setText("GO!");
            tvGameStatus.setTextColor(Color.parseColor("#00FF00"));
        }, 3000);

        gameHandler.postDelayed(() -> {
            tvGameStatus.setVisibility(View.GONE);
        }, 3500);
    }

    private void togglePauseResume() {
        if (!gameRunning || spaceView.isGameOver()) return;

        if (!isPaused) {
            // Pause the game
            isPaused = true;
            gameHandler.removeCallbacks(gameUpdateRunnable);
            gameHandler.removeCallbacks(continuousMoveRunnable);
            continuousMoveDirection = 0;
            spaceView.pauseGame();

            btnPauseResume.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_resume, 0, 0, 0);

            // REMOVE THIS LINE - Don't show pause text here
            // tvGameStatus.setVisibility(View.VISIBLE);
            // tvGameStatus.setText("PAUSED");
            // tvGameStatus.setTextColor(Color.parseColor("#FFAA00"));

            Toast.makeText(this, "Game Paused", Toast.LENGTH_SHORT).show();
        } else {
            // Resume the game
            isPaused = false;
            spaceView.resumeGame();
            btnPauseResume.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_pause, 0, 0, 0);

            // REMOVE THIS LINE - Don't hide game status here
            // tvGameStatus.setVisibility(View.GONE);

            // Restart game loop
            gameHandler.post(gameUpdateRunnable);

            Toast.makeText(this, "Game Resumed", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveLeft() {
        if (!isPaused && gameRunning && !spaceView.isGameOver()) {
            spaceView.movePlayer(-1);
        }
    }

    private void moveRight() {
        if (!isPaused && gameRunning && !spaceView.isGameOver()) {
            spaceView.movePlayer(1);
        }
    }

    private void shoot() {
        if (!isPaused && gameRunning && !spaceView.isGameOver()) {
            spaceView.shoot();
        }
    }

    private int continuousMoveDirection = 0;

    private void startContinuousMove(int direction) {
        if (!isPaused && gameRunning && !spaceView.isGameOver()) {
            continuousMoveDirection = direction;
            gameHandler.post(continuousMoveRunnable);
        }
    }

    private void stopContinuousMove() {
        continuousMoveDirection = 0;
    }

    private Runnable continuousMoveRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isPaused && continuousMoveDirection != 0 && gameRunning && !spaceView.isGameOver()) {
                spaceView.movePlayer(continuousMoveDirection);
                gameHandler.postDelayed(this, 50); // Move every 50ms
            }
        }
    };

    private Runnable gameUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (gameRunning && !isPaused) {
                // Update game view
                spaceView.update();

                // Update HUD
                updateHUD();

                // Check if game over
                if (spaceView.isGameOver()) {
                    gameOver();
                }

                // Schedule next update
                gameHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        }
    };

    private void updateHUD() {
        // Update score
        tvScore.setText("Score: " + spaceView.getScore());

        // Update lives
        int lives = spaceView.getLives();
        StringBuilder livesText = new StringBuilder();
        for (int i = 0; i < lives; i++) {
            livesText.append("❤");
        }
        for (int i = lives; i < 3; i++) { // Show empty hearts for missing lives
            livesText.append("🤍");
        }
        tvLives.setText(livesText.toString());

        // Update level
        tvLevel.setText("Level: " + spaceView.getLevel());

        // Update power-up indicator
        String activePowerup = spaceView.getActivePowerup();
        if (activePowerup != null) {
            powerupLayout.setVisibility(View.VISIBLE);
            tvPowerup.setText("⚡ " + activePowerup);

            // Blink effect
            if (System.currentTimeMillis() % 1000 < 500) {
                tvPowerup.setAlpha(1.0f);
            } else {
                tvPowerup.setAlpha(0.5f);
            }
        } else {
            powerupLayout.setVisibility(View.GONE);
        }
    }

    private void gameOver() {
        stopGame();
        btnPauseResume.setVisibility(View.GONE);

        gameOverMenu.setVisibility(View.VISIBLE);

        int finalScore = spaceView.getScore();
        tvFinalScore.setText("Score: " + finalScore);

        // Update high score
        if (finalScore > highScore) {
            highScore = finalScore;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("highScore", highScore);
            editor.apply();

            tvHighScore.setText("NEW HIGH SCORE!");
            tvHighScore.setTextColor(Color.parseColor("#FFFF00"));

            Toast.makeText(this, "NEW HIGH SCORE: " + highScore + "!", Toast.LENGTH_LONG).show();
        } else {
            tvHighScore.setText("High Score: " + highScore);
            tvHighScore.setTextColor(Color.parseColor("#FFAA00"));
        }
    }

    private void playAgain() {
        gameOverMenu.setVisibility(View.GONE);
        startGame();
    }

    private void stopGame() {
        gameRunning = false;
        isPaused = false;
        gameHandler.removeCallbacks(gameUpdateRunnable);
        gameHandler.removeCallbacks(continuousMoveRunnable);
        continuousMoveDirection = 0;
    }

    private void updateHighScoreDisplay() {
        highScore = preferences.getInt("highScore", 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameRunning && !isPaused && !spaceView.isGameOver()) {
            togglePauseResume(); // Auto-pause when app loses focus
        }
        stopGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Don't auto-resume, let user decide
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