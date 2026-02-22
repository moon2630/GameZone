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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Random;

public class car_racing extends AppCompatActivity {

    // Game Views
    private RelativeLayout gameArea;
    private ImageView playerCar;
    private TextView scoreText, highScoreText, livesText,btnPause,btnLeft, btnRight;
    private TextView finalScoreText, finalHighScoreText, levelText;
    private CardView gameOverLayout;
    private CardView startMenuLayout;
    private AppCompatButton  btnRestart, btnMenu;
    private AppCompatButton btnDifficulty, btnCarSelect, btnStart;

    // Game Variables
    private int score = 0;
    private int highScore = 0;
    private int lives = 3;
    private int level = 1;
    private int gameSpeed = 15;
    private int enemySpeed = 10;
    private int spawnRate = 100;
    private int maxEnemies = 5;
    private boolean gameRunning = false;
    private boolean gameOver = false;
    private boolean isPaused = false;
    private String difficulty = "easy";
    private String selectedCar = "red";

    // Player position
    private float playerX = 0;
    private float playerY = 0;
    private int currentLane = 1;

    // Game Objects
    private ArrayList<ImageView> enemyCars = new ArrayList<>();
    private ArrayList<Float> lanePositions = new ArrayList<>();

    // Game Loop
    private Handler gameHandler = new Handler();
    private Runnable gameRunnable;

    // Random
    private Random random = new Random();

    // Screen dimensions
    private int screenWidth, screenHeight;

    // Shared Preferences
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "CarRacingPrefs";

    // Car drawable resources
    private int[] carDrawables = {
            R.drawable.vector_car_red,
            R.drawable.vector_car_blue,
            R.drawable.vector_car_green,
            R.drawable.vector_car_yellow,
            R.drawable.vector_car_purple
    };
    private String[] carColors = {"red", "blue", "green", "yellow", "purple"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_racing);


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

        initViews();
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        loadGameData();
        setupLanes();
        setupButtonListeners();
        showStartMenu();
    }

    private void initViews() {
        gameArea = findViewById(R.id.gameArea);
        playerCar = findViewById(R.id.playerCar);
        scoreText = findViewById(R.id.scoreText);
        highScoreText = findViewById(R.id.highScoreText);
        livesText = findViewById(R.id.livesText);
        levelText = findViewById(R.id.levelText);
        gameOverLayout = findViewById(R.id.gameOverLayout);
        startMenuLayout = findViewById(R.id.startMenuLayout);

        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnStart = findViewById(R.id.btnStart);
        btnRestart = findViewById(R.id.btnRestart);
        btnMenu = findViewById(R.id.btnMenu);
        btnPause = findViewById(R.id.btnPause); // Add this ID in your XML
        btnDifficulty = findViewById(R.id.btnDifficulty);
        btnCarSelect = findViewById(R.id.btnCarSelect);

        finalScoreText = findViewById(R.id.finalScoreText);
        finalHighScoreText = findViewById(R.id.finalHighScoreText);
    }

    private void loadGameData() {
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        highScore = prefs.getInt("highScore", 0);
        selectedCar = prefs.getString("selectedCar", "red");
        difficulty = prefs.getString("difficulty", "easy");

        applyCarColor();
        updateUI();

        // Update the car select button text
        btnCarSelect.setText("CAR: " + selectedCar.toUpperCase());

        // Also update difficulty button text
        btnDifficulty.setText("DIFFICULTY: " + difficulty.toUpperCase());
    }

    private void applyCarColor() {
        for (int i = 0; i < carColors.length; i++) {
            if (carColors[i].equals(selectedCar)) {
                playerCar.setImageResource(carDrawables[i]);
                break;
            }
        }
    }

    private void saveGameData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("highScore", highScore);
        editor.putString("selectedCar", selectedCar);
        editor.putString("difficulty", difficulty);
        editor.apply();
    }

    private void setupLanes() {
        lanePositions.clear();
        int laneCount = 5; // Always 5 lanes now (instead of 3 or 4 based on difficulty)




        for (int i = 1; i <= laneCount; i++) {
            float lanePosition = (screenWidth * i) / (laneCount + 1) - playerCar.getWidth() / 2;
            lanePositions.add(lanePosition);
        }

        currentLane = laneCount / 2; // This will be lane 2 (0-indexed) or the middle lane
        playerX = lanePositions.get(currentLane);

        // Ensure player car is visible and positioned
        playerCar.setVisibility(View.VISIBLE);
        playerCar.setX(playerX);
        playerY = screenHeight - playerCar.getHeight() - 100;
        playerCar.setY(playerY);

        // Force layout update
        playerCar.requestLayout();
    }
    private void setupButtonListeners() {
        btnLeft.setOnClickListener(v -> movePlayerLeft());
        btnRight.setOnClickListener(v -> movePlayerRight());
        btnStart.setOnClickListener(v -> startGame());
        btnRestart.setOnClickListener(v -> restartGame());
        btnMenu.setOnClickListener(v -> showStartMenu());
        btnPause.setOnClickListener(v -> togglePause());
        btnDifficulty.setOnClickListener(v -> toggleDifficulty());
        btnCarSelect.setOnClickListener(v -> toggleCar());

        btnLeft.setOnLongClickListener(v -> {
            movePlayerLeft();
            return true;
        });

        btnRight.setOnLongClickListener(v -> {
            movePlayerRight();
            return true;
        });
    }

    private void togglePause() {
        if (gameOver) return;

        if (!isPaused) {
            // Pause the game
            isPaused = true;
            btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_resume, 0, 0, 0);
            showPauseOverlay();
            Toast.makeText(this, "Game Paused", Toast.LENGTH_SHORT).show();

            // Stop the game loop
            gameHandler.removeCallbacks(gameRunnable);
        } else {
            // Resume the game
            isPaused = false;
            btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_pause, 0, 0, 0);
            hidePauseOverlay();

            // Restart the game loop
            startGameLoop();
            Toast.makeText(this, "Game Resumed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showStartMenu() {
        gameOver = false;
        gameRunning = false;
        isPaused = false;
        startMenuLayout.setVisibility(View.VISIBLE);
        gameOverLayout.setVisibility(View.GONE);

        // Reset pause button to pause icon
        btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_pause, 0, 0, 0);

        // Disable game control buttons when in start menu
        btnPause.setEnabled(false);
        btnPause.setAlpha(0.5f);
        btnLeft.setEnabled(false);
        btnLeft.setAlpha(0.5f);
        btnRight.setEnabled(false);
        btnRight.setAlpha(0.5f);

        hidePauseOverlay();
        setupLanes();
        clearEnemies();
        updateUI();

        // Make sure player car is visible and properly positioned
        playerCar.setVisibility(View.VISIBLE);

        // Force position update
        playerCar.post(new Runnable() {
            @Override
            public void run() {
                setupLanes();
            }
        });
    }
    private void startGame() {
        gameRunning = true;
        gameOver = false;
        isPaused = false;
        score = 0;
        lives = 3;
        level = 1;

        startMenuLayout.setVisibility(View.GONE);
        gameOverLayout.setVisibility(View.GONE);

        btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_pause, 0, 0, 0);
        btnPause.setEnabled(true);
        btnPause.setAlpha(1f);
        btnLeft.setEnabled(true);
        btnLeft.setAlpha(1f);
        btnRight.setEnabled(true);
        btnRight.setAlpha(1f);

        hidePauseOverlay();

        // JUST SET VISIBLE - DON'T CHANGE POSITION
        playerCar.setVisibility(View.VISIBLE);

        clearEnemies();
        startPlayerIdleAnimation();
        startGameLoop();

        Toast.makeText(this, "GO! Avoid the cars!", Toast.LENGTH_SHORT).show();
    }

    private void gameOver() {
        gameRunning = false;
        gameOver = true;
        isPaused = false;

        if (score > highScore) {
            highScore = score;
            showNewHighScoreAnimation();
        }

        saveGameData();

        new Handler().postDelayed(() -> {
            gameOverLayout.setVisibility(View.VISIBLE);
            finalScoreText.setText("Score: " + score);
            finalHighScoreText.setText("High Score: " + highScore);

            // Reset pause button to pause icon and disable game controls
            btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_pause, 0, 0, 0);
            btnPause.setEnabled(false);
            btnPause.setAlpha(0.5f);
            btnLeft.setEnabled(false);
            btnLeft.setAlpha(0.5f);
            btnRight.setEnabled(false);
            btnRight.setAlpha(0.5f);

            hidePauseOverlay();
        }, 1000);

        clearEnemies();
    }
    private void showPauseOverlay() {
        // Create and show pause overlay
        TextView pauseText = new TextView(this);
        pauseText.setText("⏸️ GAME PAUSED ⏸️");
        pauseText.setTextSize(30);
        pauseText.setTextColor(0xFFFFFFFF);
        pauseText.setBackgroundColor(0x80000000);
        pauseText.setGravity(android.view.Gravity.CENTER);

        // Set a tag for easy identification
        pauseText.setTag("pauseOverlay");

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        pauseText.setLayoutParams(params);

        // Add to game area
        gameArea.addView(pauseText);

        // Disable movement buttons while paused
        btnLeft.setEnabled(false);
        btnRight.setEnabled(false);
    }
    private void hidePauseOverlay() {
        // Remove pause overlay by tag
        for (int i = 0; i < gameArea.getChildCount(); i++) {
            View child = gameArea.getChildAt(i);
            if ("pauseOverlay".equals(child.getTag())) {
                gameArea.removeView(child);
                break;
            }
        }

        // Re-enable movement buttons
        btnLeft.setEnabled(true);
        btnRight.setEnabled(true);
    }
    private void movePlayerLeft() {
        if (!gameRunning || gameOver || isPaused) return;

        if (currentLane > 0) {
            currentLane--;
            playerX = lanePositions.get(currentLane);

            playerCar.animate()
                    .x(playerX)
                    .setDuration(200)
                    .start();
        }
    }
    private void movePlayerRight() {
        if (!gameRunning || gameOver || isPaused) return;

        if (currentLane < lanePositions.size() - 1) {
            currentLane++;
            playerX = lanePositions.get(currentLane);

            playerCar.animate()
                    .x(playerX)
                    .setDuration(200)
                    .start();
        }
    }

    // In toggleDifficulty() method:
    private void toggleDifficulty() {
        switch (difficulty) {
            case "easy":
                difficulty = "medium";
                gameSpeed = 15;
                enemySpeed = 8; // Reduced from 12
                spawnRate = 80;
                maxEnemies = 6;
                break;
            case "medium":
                difficulty = "hard";
                gameSpeed = 18;
                enemySpeed = 10; // Reduced from 15
                spawnRate = 60;
                maxEnemies = 8;
                break;
            case "hard":
                difficulty = "expert";
                gameSpeed = 22;
                enemySpeed = 12; // Reduced from 18
                spawnRate = 40;
                maxEnemies = 10;
                break;
            case "expert":
                difficulty = "easy";
                gameSpeed = 12;
                enemySpeed = 6; // Reduced from 8
                spawnRate = 100;
                maxEnemies = 5;
                break;
        }

        btnDifficulty.setText("DIFFICULTY: " + difficulty.toUpperCase());

        // Still setup 5 lanes regardless of difficulty
        setupLanes();

        saveGameData();
        Toast.makeText(this, difficulty.toUpperCase() + " mode selected", Toast.LENGTH_SHORT).show();
    }
    private void toggleCar() {
        int currentIndex = -1;
        for (int i = 0; i < carColors.length; i++) {
            if (carColors[i].equals(selectedCar)) {
                currentIndex = i;
                break;
            }
        }

        int nextIndex = (currentIndex + 1) % carColors.length;
        selectedCar = carColors[nextIndex];
        playerCar.setImageResource(carDrawables[nextIndex]);

        playerCar.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(200)
                .withEndAction(() -> playerCar.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start())
                .start();

        // Update the button text
        btnCarSelect.setText("CAR: " + selectedCar.toUpperCase());
        saveGameData();
    }


    private void startPlayerIdleAnimation() {
        if (!gameRunning || gameOver) return;

        // Remove or comment out translationY:
        playerCar.animate()
                // .translationY(-5) // ← REMOVE THIS LINE
                .setDuration(300)
                .withEndAction(() -> playerCar.animate()
                        // .translationY(0) // ← REMOVE THIS LINE
                        .setDuration(300)
                        .withEndAction(() -> {
                            if (gameRunning && !gameOver && !isPaused) {
                                startPlayerIdleAnimation();
                            }
                        })
                        .start())
                .start();
    }

    private void startGameLoop() {
        if (gameRunnable != null) {
            gameHandler.removeCallbacks(gameRunnable);
        }

        gameRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameRunning && !gameOver && !isPaused) {  // Added !isPaused check
                    updateGame();
                    gameHandler.postDelayed(this, 16);
                }
            }
        };
        gameHandler.post(gameRunnable);
    }

    private void updateGame() {
        if (isPaused) return;  // Don't update game if paused

        score += 1;

        if (enemyCars.size() < maxEnemies && random.nextInt(spawnRate) < getSpawnChance()) {
            spawnEnemyCar();
        }

        updateEnemyCars();
        checkCollisions();
        checkLevelUp();
        updateUI();
    }

    private int getSpawnChance() {
        switch (difficulty) {
            case "easy": return 2;
            case "medium": return 3;
            case "hard": return 4;
            case "expert": return 5;
            default: return 2;
        }
    }

    private void spawnEnemyCar() {
        ImageView enemyCar = new ImageView(this);

        // Random enemy car from vector_car31 to vector_car35
        int enemyType = random.nextInt(6);
        int carDrawable;
        switch (enemyType) {
            case 0: carDrawable = R.drawable.vector_car32; break;
            case 1: carDrawable = R.drawable.vector_car33; break;
            case 2: carDrawable = R.drawable.vector_car34; break;
            case 3: carDrawable = R.drawable.vector_car35; break;
            case 4: carDrawable = R.drawable.vector_bus; break;
            case 5: carDrawable = R.drawable.vector_car37; break;
            default: carDrawable = R.drawable.vector_car32; break;
        }
        enemyCar.setImageResource(carDrawable);

        // Set enemy car size
        int carWidth = 70;  // Reduced from 80
        int carHeight = 100; // Reduced from 120
        if (difficulty.equals("hard") || difficulty.equals("expert")) {
            carWidth = 80;
            carHeight = 90;
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(carWidth, carHeight);
        enemyCar.setLayoutParams(params);

        // Now we have 5 lanes (0-4)
        int laneIndex;

        // Try to spawn in a different lane than player
        do {
            laneIndex = random.nextInt(5);
        } while (laneIndex == currentLane && random.nextInt(2) == 0); // 50% chance to avoid same lane

        float laneX = lanePositions.get(laneIndex);

        // Adjust X position to be centered in lane
        laneX += (playerCar.getWidth() - carWidth) / 2;

        enemyCar.setX(laneX);
        enemyCar.setY(-carHeight);
        enemyCar.setAlpha(0f);
        enemyCar.setScaleX(0.5f);
        enemyCar.setScaleY(0.5f);

        gameArea.addView(enemyCar);
        enemyCars.add(enemyCar);

        enemyCar.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start();
    }
    private void updateEnemyCars() {
        if (isPaused) return;  // Don't update enemy positions if paused

        ArrayList<ImageView> enemiesToRemove = new ArrayList<>();

        for (ImageView enemyCar : enemyCars) {
            float currentY = enemyCar.getY();
            float newY = currentY + enemySpeed + (level * 0.5f);
            enemyCar.setY(newY);

            if (currentY > screenHeight) {
                enemiesToRemove.add(enemyCar);
                gameArea.removeView(enemyCar);

                if (gameRunning && !isPaused) {  // Added !isPaused check
                    score += 10;
                    showScorePopup(enemyCar, "+10");
                }
            }
        }

        enemyCars.removeAll(enemiesToRemove);
    }

    private void showScorePopup(View view, String text) {
        TextView scorePopup = new TextView(this);
        scorePopup.setText(text);
        scorePopup.setTextSize(18);
        scorePopup.setTextColor(0xFF4CAF50);
        scorePopup.setShadowLayer(2, 0, 0, 0xFF000000);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = (int) view.getX() + 20;
        params.topMargin = (int) view.getY() - 30;
        scorePopup.setLayoutParams(params);

        gameArea.addView(scorePopup);

        scorePopup.animate()
                .translationY(-50)
                .alpha(0)
                .setDuration(800)
                .withEndAction(() -> gameArea.removeView(scorePopup))
                .start();
    }

    private void checkCollisions() {
        if (gameOver || isPaused) return;

        int[] playerPos = new int[2];
        playerCar.getLocationOnScreen(playerPos);

        // Use tighter collision bounds (60% of car size)
        int playerWidth = playerCar.getWidth();
        int playerHeight = playerCar.getHeight();

        int playerLeft = playerPos[0] + (int)(playerWidth * 0.2);
        int playerRight = playerPos[0] + (int)(playerWidth * 0.8);
        int playerTop = playerPos[1] + (int)(playerHeight * 0.2);
        int playerBottom = playerPos[1] + (int)(playerHeight * 0.8);

        for (ImageView enemyCar : enemyCars) {
            int[] enemyPos = new int[2];
            enemyCar.getLocationOnScreen(enemyPos);

            int enemyWidth = enemyCar.getWidth();
            int enemyHeight = enemyCar.getHeight();

            // Use tighter collision bounds for enemy too (60% of car size)
            int enemyLeft = enemyPos[0] + (int)(enemyWidth * 0.2);
            int enemyRight = enemyPos[0] + (int)(enemyWidth * 0.8);
            int enemyTop = enemyPos[1] + (int)(enemyHeight * 0.2);
            int enemyBottom = enemyPos[1] + (int)(enemyHeight * 0.8);

            if (playerLeft < enemyRight &&
                    playerRight > enemyLeft &&
                    playerTop < enemyBottom &&
                    playerBottom > enemyTop) {

                handleCollision(enemyCar);
                break;
            }
        }
    }
    private void handleCollision(ImageView collidedCar) {
        lives--;

        crashAnimation(collidedCar);

        if (lives <= 0) {
            new Handler().postDelayed(() -> gameOver(), 500);
        } else {
            updateLivesDisplay();
            showCrashWarning();

            if (collidedCar != null && enemyCars.contains(collidedCar)) {
                gameArea.removeView(collidedCar);
                enemyCars.remove(collidedCar);
            }
        }
    }

    private void crashAnimation(ImageView crashedCar) {
        playerCar.animate()
                .translationX(-20)
                .setDuration(50)
                .withEndAction(() -> playerCar.animate()
                        .translationX(20)
                        .setDuration(50)
                        .withEndAction(() -> playerCar.animate()
                                .translationX(0)
                                .setDuration(50)
                                .start())
                        .start())
                .start();

        if (crashedCar != null) {
            crashedCar.animate()
                    .scaleX(1.5f)
                    .scaleY(1.5f)
                    .alpha(0)
                    .rotation(45)
                    .setDuration(300)
                    .start();
        }

        // Create full-screen red flash
        View flash = new View(this);
        flash.setBackgroundColor(0x80FF0000); // Semi-transparent red

        // Use MATCH_PARENT for both width and height
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );

        // Position at top-left corner of the screen
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        flash.setLayoutParams(params);
        gameArea.addView(flash);

        flash.animate()
                .alpha(0)
                .setDuration(300)
                .withEndAction(() -> gameArea.removeView(flash))
                .start();
    }
    private void showCrashWarning() {
        TextView warning = new TextView(this);
        warning.setText("⚠️ CRASH! ⚠️");
        warning.setTextSize(24);
        warning.setTextColor(0xFFFF0000);
        warning.setShadowLayer(3, 0, 0, 0xFF000000);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        warning.setLayoutParams(params);

        gameArea.addView(warning);

        warning.setAlpha(0f);
        warning.animate()
                .alpha(1f)
                .setDuration(200)
                .withEndAction(() -> warning.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .setStartDelay(300)
                        .withEndAction(() -> gameArea.removeView(warning))
                        .start())
                .start();

        Toast.makeText(this, "Crash! Lives: " + lives, Toast.LENGTH_SHORT).show();
    }

    private void checkLevelUp() {
        int newLevel = (score / 1000) + 1;
        if (newLevel > level) {
            level = newLevel;
            levelUpAnimation();

            enemySpeed = Math.min(enemySpeed + 1, 25);
            spawnRate = Math.max(20, spawnRate - 5);
        }
    }

    private void levelUpAnimation() {
        TextView levelUp = new TextView(this);
        levelUp.setText("LEVEL " + level + "!");
        levelUp.setTextSize(30);
        levelUp.setTextColor(0xFFFFFF00);
        levelUp.setShadowLayer(3, 0, 0, 0xFF000000);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        levelUp.setLayoutParams(params);

        gameArea.addView(levelUp);

        levelUp.setScaleX(0.1f);
        levelUp.setScaleY(0.1f);
        levelUp.setAlpha(0f);

        levelUp.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .alpha(1f)
                .setDuration(500)
                .withEndAction(() -> levelUp.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .withEndAction(() -> new Handler().postDelayed(() -> {
                            levelUp.animate()
                                    .alpha(0f)
                                    .setDuration(500)
                                    .withEndAction(() -> gameArea.removeView(levelUp))
                                    .start();
                        }, 1000))
                        .start())
                .start();

        Toast.makeText(this, "Level Up! Speed Increased!", Toast.LENGTH_SHORT).show();
    }


    private void showNewHighScoreAnimation() {
        TextView newHighScore = new TextView(this);
        newHighScore.setText("🏆 NEW HIGH SCORE! 🏆");
        newHighScore.setTextSize(26);
        newHighScore.setTextColor(0xFFFFD700);
        newHighScore.setShadowLayer(3, 0, 0, 0xFF000000);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        newHighScore.setLayoutParams(params);

        gameArea.addView(newHighScore);

        newHighScore.setScaleX(0.1f);
        newHighScore.setScaleY(0.1f);
        newHighScore.setAlpha(0f);

        newHighScore.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .alpha(1f)
                .setDuration(600)
                .withEndAction(() -> newHighScore.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .withEndAction(() -> {
                            new Handler().postDelayed(() -> {
                                newHighScore.animate()
                                        .alpha(0f)
                                        .setDuration(500)
                                        .withEndAction(() -> gameArea.removeView(newHighScore))
                                        .start();
                            }, 2000);
                        })
                        .start())
                .start();
    }

    private void restartGame() {
        // Enable controls again when restarting
        btnPause.setEnabled(true);
        btnPause.setAlpha(1f);
        btnLeft.setEnabled(true);
        btnLeft.setAlpha(1f);
        btnRight.setEnabled(true);
        btnRight.setAlpha(1f);

        clearEnemies();
        startGame();
    }
    private void clearEnemies() {
        for (ImageView enemyCar : enemyCars) {
            gameArea.removeView(enemyCar);
        }
        enemyCars.clear();
    }

    private void updateUI() {
        scoreText.setText("Score: " + score);
        highScoreText.setText("High: " + highScore);
        levelText.setText("Level: " + level);
        updateLivesDisplay();
    }





    private void updateLivesDisplay() {
        StringBuilder livesString = new StringBuilder();

        // Ensure lives is not negative
        int displayLives = Math.max(lives, 0); // Don't allow negative values
        displayLives = Math.min(displayLives, 3); // Don't allow more than 3

        // Show filled hearts for actual lives
        for (int i = 0; i < displayLives; i++) {
            livesString.append("❤️");
        }

        // Show empty hearts for remaining lives up to 3
        for (int i = displayLives; i < 3; i++) {
            livesString.append("🤍");
        }

        livesText.setText(livesString.toString());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameRunning && !gameOver && !isPaused) {
            togglePause(); // Auto-pause when app loses focus
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Don't auto-resume, let user decide
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameHandler != null) {
            gameHandler.removeCallbacksAndMessages(null);
        }
        saveGameData();
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