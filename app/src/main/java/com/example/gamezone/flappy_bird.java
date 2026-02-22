package com.example.gamezone;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
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

public class flappy_bird extends AppCompatActivity {

    // Game Views
    private RelativeLayout gameArea;
    private ImageView bird;
    private TextView scoreText, highScoreText, btnPause;
    private TextView finalScoreText, finalBestText, instructionsText;

    private CardView startMenuLayout,birdSelectLayout,gameOverLayout;
    private AppCompatButton btnStart, btnRestart, btnMenu, btnChangeBird;
    private View ground;
    private AppCompatButton[] birdButtons = new AppCompatButton[5];

    // Add this variable with other game variables
    private int selectedBirdIndex = 0;

    // Game Variables
    private int score = 0;
    private int highScore = 0;
    private float birdY = 0;
    private float birdVelocity = 0;
    private float gravity = 0.8f;
    private float flapStrength = -18f;
    private int gameSpeed = 8;
    private int pipeGap = 650;
    private int pipeDistance = 1000;
    private int pipeWidth = 400;
    private boolean gameRunning = false;
    private boolean gameOver = false;
    private boolean isPaused = false;
    private boolean isFlapping = false;
    private boolean hasStarted = false;
    private String selectedBird = "yellow";

    // Pipes
    private ArrayList<ImageView> topPipes = new ArrayList<>();
    private ArrayList<ImageView> bottomPipes = new ArrayList<>();
    private ArrayList<Boolean> pipeScored = new ArrayList<>();

    // Screen dimensions
    private int screenWidth, screenHeight;
    private int groundHeight;

    // Game Loop
    private Handler gameHandler = new Handler();
    private Runnable gameRunnable;

    // Random
    private Random random = new Random();

    // Shared Preferences
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "FlappyBirdPrefs";

    // Game state
    private int gameState = 0; // 0: menu, 1: ready, 2: playing, 3: game over

    // Bird drawable resources
    private int[] birdDrawables = {
            R.drawable.vector_bird_yellow,
            R.drawable.vector_bird_green,
            R.drawable.vector_bird_blue,
            R.drawable.vector_bird_brown,
            R.drawable.vector_bird_pink
    };
    private String[] birdColors = {"yellow", "green", "blue", "brown", "red"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flappy_bird);


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
        setupTouchListener();
        setupButtonListeners();
        showStartMenu();
        startBirdIdleAnimation();
    }

    private void initViews() {
        gameArea = findViewById(R.id.gameArea);
        bird = findViewById(R.id.bird);
        scoreText = findViewById(R.id.scoreText);
        highScoreText = findViewById(R.id.highScoreText);
        btnPause = findViewById(R.id.btnPause);
        startMenuLayout = findViewById(R.id.startMenuLayout);
        gameOverLayout = findViewById(R.id.gameOverLayout);
        birdSelectLayout = findViewById(R.id.birdSelectLayout);
        btnStart = findViewById(R.id.btnStart);
        btnRestart = findViewById(R.id.btnRestart);
        btnMenu = findViewById(R.id.btnMenu);
        btnChangeBird = findViewById(R.id.btnChangeBird);
        finalScoreText = findViewById(R.id.finalScoreText);
        finalBestText = findViewById(R.id.finalBestText);
        instructionsText = findViewById(R.id.instructionsText);

        // REMOVE ground initialization
        // ground = findViewById(R.id.ground);

        // Initialize bird selection buttons
        birdButtons[0] = findViewById(R.id.btnBirdYellow);
        birdButtons[1] = findViewById(R.id.btnBirdGreen);
        birdButtons[2] = findViewById(R.id.btnBirdBlue);
        birdButtons[3] = findViewById(R.id.btnBirdBrown);
        birdButtons[4] = findViewById(R.id.btnBirdRed);

        // Set initial button backgrounds
        updateBirdButtonBackgrounds();

        // Initialize the change_bird ImageView if it exists
        ImageView changeBirdImageView = findViewById(R.id.change_bird);
        if (changeBirdImageView != null) {
            changeBirdImageView.setImageResource(R.drawable.vector_bird_yellow);
        }
    }
    private void loadGameData() {
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        highScore = prefs.getInt("highScore", 0);
        selectedBird = prefs.getString("selectedBird", "yellow");
        highScoreText.setText("Best: " + highScore);

        // Find the index of the selected bird
        for (int i = 0; i < birdColors.length; i++) {
            if (birdColors[i].equals(selectedBird)) {
                selectedBirdIndex = i;
                break;
            }
        }

        applyBirdColor();

        // Also update the change_bird ImageView
        ImageView changeBirdImageView = findViewById(R.id.change_bird);
        if (changeBirdImageView != null) {
            changeBirdImageView.setImageResource(birdDrawables[selectedBirdIndex]);
        }
    }

    private void applyBirdColor() {
        for (int i = 0; i < birdColors.length; i++) {
            if (birdColors[i].equals(selectedBird)) {
                bird.setImageResource(birdDrawables[i]);
                break;
            }
        }
    }

    private void saveGameData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("highScore", highScore);
        editor.putString("selectedBird", selectedBird);
        editor.apply();
    }

    private void setupTouchListener() {
        gameArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (birdSelectLayout.getVisibility() == View.VISIBLE) {
                        // Close bird selection if clicked outside
                        birdSelectLayout.setVisibility(View.GONE);
                        return true;
                    }

                    if (gameState == 0) {
                        startGame();
                    } else if (gameState == 1) {
                        gameState = 2;
                        hasStarted = true;
                        flapBird();
                    } else if (gameState == 2) {
                        flapBird();
                    }
                    return true;
                }
                return false;
            }
        });
    }
    private void setupButtonListeners() {
        btnStart.setOnClickListener(v -> startGame());
        btnRestart.setOnClickListener(v -> restartGame());
        btnMenu.setOnClickListener(v -> showStartMenu());
        btnChangeBird.setOnClickListener(v -> toggleBirdSelectLayout());
        btnPause.setOnClickListener(v -> togglePause());

        for (int i = 0; i < birdButtons.length; i++) {
            final int index = i;
            birdButtons[i].setOnClickListener(v -> selectBird(index));
        }
    }

    private void togglePause() {
        if (gameOver || gameState != 2) return;

        if (!isPaused) {
            isPaused = true;
            btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_resume, 0, 0, 0);
            showPauseOverlay();
            Toast.makeText(this, "Game Paused", Toast.LENGTH_SHORT).show();
            gameHandler.removeCallbacks(gameRunnable);
        } else {
            isPaused = false;
            btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_pause, 0, 0, 0);
            hidePauseOverlay();
            startGameLoop();
            Toast.makeText(this, "Game Resumed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPauseOverlay() {
        TextView pauseText = new TextView(this);
        pauseText.setText("⏸️ GAME PAUSED ⏸️");
        pauseText.setTextSize(30);
        pauseText.setTextColor(0xFFFFFFFF);
        pauseText.setBackgroundColor(0x80000000);
        pauseText.setGravity(android.view.Gravity.CENTER);
        pauseText.setTag("pauseOverlay");

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        pauseText.setLayoutParams(params);
        gameArea.addView(pauseText);
    }

    private void hidePauseOverlay() {
        for (int i = 0; i < gameArea.getChildCount(); i++) {
            View child = gameArea.getChildAt(i);
            if ("pauseOverlay".equals(child.getTag())) {
                gameArea.removeView(child);
                break;
            }
        }
    }

    private void toggleBirdSelectLayout() {
        if (birdSelectLayout.getVisibility() == View.VISIBLE) {
            birdSelectLayout.setVisibility(View.GONE);
        } else {
            birdSelectLayout.setVisibility(View.VISIBLE);

            // Update all bird button backgrounds when layout opens
            updateBirdButtonBackgrounds();
        }
    }

    private void updateBirdButtonBackgrounds() {
        for (int i = 0; i < birdButtons.length; i++) {
            if (i == selectedBirdIndex) {
                // Selected bird gets special background
                birdButtons[i].setBackgroundResource(R.drawable.button_action_blue);
                birdButtons[i].setTextColor(getResources().getColor(android.R.color.white));
            } else {
                // Other birds get default background
                birdButtons[i].setBackgroundResource(R.drawable.button_action);
                birdButtons[i].setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }




    private void selectBird(int index) {
        selectedBirdIndex = index;
        selectedBird = birdColors[index];

        // Update main bird
        bird.setImageResource(birdDrawables[index]);

        // Also update the change_bird ImageView to show selected bird
        ImageView changeBirdImageView = findViewById(R.id.change_bird);
        if (changeBirdImageView != null) {
            changeBirdImageView.setImageResource(birdDrawables[index]);
        }

        // Update button backgrounds
        updateBirdButtonBackgrounds();

        // Save data but DON'T hide the layout immediately
        saveGameData();

        // Animation for feedback
        bird.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(200)
                .withEndAction(() -> bird.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start())
                .start();

        Toast.makeText(this, selectedBird.toUpperCase() + " bird selected", Toast.LENGTH_SHORT).show();

        // Don't hide layout automatically - let user click outside
    }


    private void startBirdIdleAnimation() {
        if (gameState == 0) {
            bird.animate()
                    .translationY(-20)
                    .setDuration(1500)
                    .withEndAction(() -> bird.animate()
                            .translationY(20)
                            .setDuration(1500)
                            .withEndAction(() -> {
                                if (gameState == 0) {
                                    startBirdIdleAnimation();
                                }
                            })
                            .start())
                    .start();
        }
    }

    private void startGame() {
        gameState = 1;
        gameRunning = true;
        gameOver = false;
        isPaused = false;
        hasStarted = false;
        score = 0;

        startMenuLayout.setVisibility(View.GONE);
        gameOverLayout.setVisibility(View.GONE);
        birdSelectLayout.setVisibility(View.GONE);

        btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_pause, 0, 0, 0);
        btnPause.setEnabled(true);

        birdY = screenHeight / 2f - bird.getHeight() / 2f;
        birdVelocity = 0;
        bird.setY(birdY);
        bird.setRotation(0);
        bird.setTranslationY(0);

        clearPipes();
        pipeScored.clear();
        gameSpeed = 8;

        instructionsText.setText("Tap to start flying!");
        instructionsText.setVisibility(View.VISIBLE);

        startGameLoop();

        // REMOVE THIS ANIMATION - it was causing height decrease
    /*
    bird.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(300)
            .withEndAction(() -> bird.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start())
            .start();
    */

        showCountdown();
    }


    private void showCountdown() {
        TextView countdownText = new TextView(this);
        countdownText.setText("READY");
        countdownText.setTextSize(40);
        countdownText.setTextColor(0xFFFFFF00);
        countdownText.setShadowLayer(3, 0, 0, 0xFF000000);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        countdownText.setLayoutParams(params);

        gameArea.addView(countdownText);

        countdownText.setScaleX(0.1f);
        countdownText.setScaleY(0.1f);
        countdownText.setAlpha(0f);

        countdownText.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .alpha(1f)
                .setDuration(500)
                .withEndAction(() -> countdownText.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .withEndAction(() -> new Handler().postDelayed(() -> {
                            countdownText.animate()
                                    .alpha(0f)
                                    .setDuration(300)
                                    .withEndAction(() -> gameArea.removeView(countdownText))
                                    .start();

                            if (gameState == 1) {
                                instructionsText.setText("Tap to flap!");
                            }
                        }, 1000))
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
                if (gameRunning && !gameOver && !isPaused) {
                    updateGame();
                    gameHandler.postDelayed(this, 16);
                }
            }
        };
        gameHandler.post(gameRunnable);
    }

    private void updateGame() {
        if (gameState == 2) {
            updateBird();
            updatePipes();
            spawnPipes();
            checkCollisions();
            updateScore();
        }
        updateUI();
    }

    private void updateBird() {
        birdVelocity += gravity;
        birdY += birdVelocity;
        birdVelocity = Math.min(birdVelocity, 25f);

        // Prevent bird from going below screen
        birdY = Math.min(birdY, screenHeight - bird.getHeight() - 10);

        bird.setY(birdY);

        float rotation = Math.max(-30, Math.min(90, birdVelocity * 3));
        bird.setRotation(rotation);

        // Just reset isFlapping flag
        if (isFlapping) {
            isFlapping = false;
        }
    }
    private void flapBird() {
        if (!gameRunning || gameOver || isPaused) return;

        isFlapping = true;
        birdVelocity = flapStrength;

        if (instructionsText.getVisibility() == View.VISIBLE) {
            instructionsText.setVisibility(View.GONE);
        }

        // REMOVE background color change
    /*
    gameArea.setBackgroundColor(0xFF4FC3F7);
    new Handler().postDelayed(() -> {
        if (gameRunning) {
            gameArea.setBackgroundResource(R.drawable.flappy_bird_bg);
        }
    }, 50);
    */
    }

    private void spawnPipes() {
        if (gameState != 2) return;

        if (topPipes.isEmpty() ||
                (int) topPipes.get(topPipes.size() - 1).getX() < screenWidth - pipeDistance) {

            int currentPipeGap = pipeGap;
            if (score > 5) {
                currentPipeGap = Math.max(500, pipeGap - (score - 5) * 10);
            }

            int minGapY, maxGapY;

            if (score <= 5) {
                minGapY = 300;
                maxGapY = screenHeight - currentPipeGap - 300;
            } else if (score <= 10) {
                minGapY = 250;
                maxGapY = screenHeight - currentPipeGap - 250;
            } else if (score <= 20) {
                minGapY = 200;
                maxGapY = screenHeight - currentPipeGap - 200;
            } else {
                minGapY = 150;
                maxGapY = screenHeight - currentPipeGap - 150;
            }

            // Ensure maxGapY > minGapY
            if (maxGapY <= minGapY) {
                maxGapY = minGapY + 100;
            }

            int gapY = random.nextInt(maxGapY - minGapY) + minGapY;

            // Create top pipe
            ImageView topPipe = new ImageView(this);
            topPipe.setImageResource(R.drawable.vector_top_side_pillar);
            topPipe.setScaleType(ImageView.ScaleType.FIT_XY);

            int topPipeHeight = gapY;
            if (score > 10) {
                topPipeHeight = (int)(gapY * 1.1f);
            }

            RelativeLayout.LayoutParams topParams = new RelativeLayout.LayoutParams(pipeWidth, topPipeHeight);
            topPipe.setLayoutParams(topParams);
            topPipe.setX(screenWidth);
            topPipe.setY(0);
            topPipe.setElevation(5f);

            gameArea.addView(topPipe);
            topPipes.add(topPipe);

            // Create bottom pipe
            ImageView bottomPipe = new ImageView(this);
            bottomPipe.setImageResource(R.drawable.vector_bottom_side_pillar);

            // CRITICAL FIX: Use FIT_XY to stretch the image properly
            bottomPipe.setScaleType(ImageView.ScaleType.FIT_XY);

            int bottomPipeStartY = gapY + currentPipeGap;
            int bottomPipeHeight = screenHeight - bottomPipeStartY;

            // FIX: Ensure bottom pipe always touches bottom
            // Add enough height to ensure it reaches bottom of screen
            bottomPipeHeight = screenHeight - bottomPipeStartY + 70; // Extra 50px to ensure coverage

            RelativeLayout.LayoutParams bottomParams = new RelativeLayout.LayoutParams(pipeWidth, bottomPipeHeight);
            bottomPipe.setLayoutParams(bottomParams);
            bottomPipe.setX(screenWidth);
            bottomPipe.setY(bottomPipeStartY);
            bottomPipe.setElevation(5f);

            gameArea.addView(bottomPipe);
            bottomPipes.add(bottomPipe);

            pipeScored.add(false);

            topPipe.setAlpha(0f);
            bottomPipe.setAlpha(0f);

            topPipe.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
            bottomPipe.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        }
    }

    private void debugCollision(Rect birdRect, Rect topPipeRect, Rect bottomPipeRect) {
        // This is just for debugging - remove in production
        // Draw bird collision box
        View birdDebug = new View(this);
        birdDebug.setBackgroundColor(0x60FF0000); // Red with 60% opacity
        RelativeLayout.LayoutParams birdParams = new RelativeLayout.LayoutParams(
                birdRect.width(), birdRect.height()
        );
        birdParams.leftMargin = birdRect.left;
        birdParams.topMargin = birdRect.top;
        birdDebug.setLayoutParams(birdParams);
        birdDebug.setTag("debug");
        gameArea.addView(birdDebug);

        // Draw top pipe collision box
        View topPipeDebug = new View(this);
        topPipeDebug.setBackgroundColor(0x6000FF00); // Green with 60% opacity
        RelativeLayout.LayoutParams topParams = new RelativeLayout.LayoutParams(
                topPipeRect.width(), topPipeRect.height()
        );
        topParams.leftMargin = topPipeRect.left;
        topParams.topMargin = topPipeRect.top;
        topPipeDebug.setLayoutParams(topParams);
        topPipeDebug.setTag("debug");
        gameArea.addView(topPipeDebug);

        // Draw bottom pipe collision box
        View bottomPipeDebug = new View(this);
        bottomPipeDebug.setBackgroundColor(0x600000FF); // Blue with 60% opacity
        RelativeLayout.LayoutParams bottomParams = new RelativeLayout.LayoutParams(
                bottomPipeRect.width(), bottomPipeRect.height()
        );
        bottomParams.leftMargin = bottomPipeRect.left;
        bottomParams.topMargin = bottomPipeRect.top;
        bottomPipeDebug.setLayoutParams(bottomParams);
        bottomPipeDebug.setTag("debug");
        gameArea.addView(bottomPipeDebug);

        // Remove debug views after 50ms
        new Handler().postDelayed(() -> {
            for (int i = gameArea.getChildCount() - 1; i >= 0; i--) {
                View child = gameArea.getChildAt(i);
                if ("debug".equals(child.getTag())) {
                    gameArea.removeView(child);
                }
            }
        }, 50);
    }
    private void updatePipes() {
        for (int i = 0; i < topPipes.size(); i++) {
            ImageView topPipe = topPipes.get(i);
            ImageView bottomPipe = bottomPipes.get(i);

            float currentX = topPipe.getX();
            float newX = currentX - gameSpeed;

            topPipe.setX(newX);
            bottomPipe.setX(newX);

            if (newX + pipeWidth < 0) {
                gameArea.removeView(topPipe);
                gameArea.removeView(bottomPipe);
                topPipes.remove(i);
                bottomPipes.remove(i);
                pipeScored.remove(i);
                i--;
            }
        }
    }

    private void checkCollisions() {
        if (gameOver || gameState != 2 || isPaused) return;

        // Get bird rectangle with slight padding
        Rect birdRect = new Rect();
        bird.getHitRect(birdRect);

        // Add padding to bird rectangle (make it smaller than actual bird)
        int birdPadding = 10;
        birdRect.left += birdPadding;
        birdRect.top += birdPadding;
        birdRect.right -= birdPadding;
        birdRect.bottom -= birdPadding;

        // Check TOP screen collision
        if (birdRect.top <= 0) {
            birdY = 5;
            birdVelocity = Math.abs(birdVelocity) * 0.5f;
        }

        // Check BOTTOM screen collision
        if (birdRect.bottom >= screenHeight) {
            bottomScreenCollision();
            return;
        }

        // Check PIPE collisions
        for (int i = 0; i < topPipes.size(); i++) {
            ImageView topPipe = topPipes.get(i);
            ImageView bottomPipe = bottomPipes.get(i);

            // Get pipe rectangles
            Rect topPipeRect = new Rect();
            topPipe.getHitRect(topPipeRect);

            Rect bottomPipeRect = new Rect();
            bottomPipe.getHitRect(bottomPipeRect);

            // Adjust pipe collision boxes for better accuracy
            // Top pipe: reduce from bottom (collide with pipe opening)
            topPipeRect.bottom -= 15;  // Allow passing through the gap

            // Bottom pipe: increase from top (collide with pipe opening)
            bottomPipeRect.top += 15;  // Allow passing through the gap

            // Add side padding to pipes
            int pipeSidePadding = 5;
            topPipeRect.left += pipeSidePadding;
            topPipeRect.right -= pipeSidePadding;
            bottomPipeRect.left += pipeSidePadding;
            bottomPipeRect.right -= pipeSidePadding;

            // Check collision with top pipe
            if (Rect.intersects(birdRect, topPipeRect)) {
                pipeCollision(i);
                return;
            }

            // Check collision with bottom pipe
            if (Rect.intersects(birdRect, bottomPipeRect)) {
                pipeCollision(i);
                return;
            }
        }
    }


    private void bottomScreenCollision() {
        // Bird hits bottom of screen
        showCollisionEffect();
        new Handler().postDelayed(() -> {
            if (gameRunning) {
                gameOver();
            }
        }, 300);
    }



    private void pipeCollision(int pipeIndex) {
        showCollisionEffect();
        new Handler().postDelayed(() -> {
            if (gameRunning) {
                gameOver();
            }
        }, 300);
    }

    private void showCollisionEffect() {
        // Red flash effect - FULL SCREEN
        View flash = new View(this);
        flash.setBackgroundColor(0x80FF0000); // 80 = 50% opacity

        // Use MATCH_PARENT for full screen
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );

        // Make it cover entire parent (gameArea's parent)
        flash.setLayoutParams(params);

        // Add to the root view, not gameArea
        View rootView = findViewById(android.R.id.content);
        if (rootView instanceof ViewGroup) {
            ((ViewGroup) rootView).addView(flash);
        }

        flash.animate()
                .alpha(0)
                .setDuration(400)
                .withEndAction(() -> {
                    if (rootView instanceof ViewGroup) {
                        ((ViewGroup) rootView).removeView(flash);
                    }
                })
                .start();

        // Shake effect
        gameArea.animate()
                .translationX(-20)
                .setDuration(50)
                .withEndAction(() -> gameArea.animate()
                        .translationX(20)
                        .setDuration(50)
                        .withEndAction(() -> gameArea.animate()
                                .translationX(-20)
                                .setDuration(50)
                                .withEndAction(() -> gameArea.animate()
                                        .translationX(0)
                                        .setDuration(50)
                                        .start())
                                .start())
                        .start())
                .start();
    }
    private void updateScore() {
        for (int i = 0; i < topPipes.size(); i++) {
            if (!pipeScored.get(i)) {
                ImageView topPipe = topPipes.get(i);

                if (bird.getX() > topPipe.getX() + pipeWidth) {
                    pipeScored.set(i, true);
                    score++;

                    showScoreAnimation(topPipe);

                    if (score == 5) {
                        showDifficultyMessage("⚠️ Pipes getting smaller!");
                    } else if (score == 10) {
                        showDifficultyMessage("⚠️ Pipes getting taller!");
                    } else if (score == 15) {
                        showDifficultyMessage("⚡ Super challenge mode!");
                    }

                    if (score % 3 == 0) {
                        gameSpeed = Math.min(gameSpeed + 1, 15);
                        if (score % 6 == 0) {
                            showSpeedIncreaseAnimation();
                        }
                    }

                    break;
                }
            }
        }
    }

    private void showDifficultyMessage(String message) {
        TextView difficultyText = new TextView(this);
        difficultyText.setText(message);
        difficultyText.setTextSize(22);
        difficultyText.setTextColor(0xFFFF9800);
        difficultyText.setShadowLayer(2, 0, 0, 0xFF000000);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = screenWidth / 2 - 150;
        params.topMargin = 200;
        difficultyText.setLayoutParams(params);

        gameArea.addView(difficultyText);

        difficultyText.setAlpha(0f);
        difficultyText.animate()
                .alpha(1f)
                .setDuration(400)
                .withEndAction(() -> difficultyText.animate()
                        .alpha(0f)
                        .setDuration(400)
                        .setStartDelay(1500)
                        .withEndAction(() -> gameArea.removeView(difficultyText))
                        .start())
                .start();
    }

    private void showScoreAnimation(ImageView pipe) {
        TextView scorePopup = new TextView(this);
        scorePopup.setText("+1");
        scorePopup.setTextSize(24);
        scorePopup.setTextColor(0xFF4CAF50);
        scorePopup.setShadowLayer(2, 0, 0, 0xFF000000);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = (int) pipe.getX() + pipeWidth / 2 - 30;
        params.topMargin = screenHeight / 2 - 100;
        scorePopup.setLayoutParams(params);

        gameArea.addView(scorePopup);

        scorePopup.animate()
                .translationY(-100)
                .alpha(0)
                .setDuration(800)
                .withEndAction(() -> gameArea.removeView(scorePopup))
                .start();

        scoreText.animate()
                .scaleX(1.4f)
                .scaleY(1.4f)
                .setDuration(150)
                .withEndAction(() -> scoreText.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start())
                .start();
    }

    private void showSpeedIncreaseAnimation() {
        TextView speedText = new TextView(this);
        speedText.setText("⚡ Faster! ⚡");
        speedText.setTextSize(20);
        speedText.setTextColor(0xFFFFFF00);
        speedText.setShadowLayer(2, 0, 0, 0xFF000000);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = screenWidth / 2 - 100;
        params.topMargin = 150;
        speedText.setLayoutParams(params);

        gameArea.addView(speedText);

        speedText.setAlpha(0f);
        speedText.animate()
                .alpha(1f)
                .setDuration(300)
                .withEndAction(() -> speedText.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .setStartDelay(1000)
                        .withEndAction(() -> gameArea.removeView(speedText))
                        .start())
                .start();
    }

    private void gameOver() {
        gameState = 3;
        gameRunning = false;
        gameOver = true;
        isPaused = false;

        btnPause.setEnabled(false);
        btnPause.setAlpha(0.5f);
        btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_pause, 0, 0, 0);

        boolean isNewHighScore = false;
        if (score > highScore) {
            highScore = score;
            saveGameData();
            isNewHighScore = true;
        }

        bird.animate()
                .rotation(90)
                .translationY(100)
                .setDuration(800)
                .start();

        boolean finalIsNewHighScore = isNewHighScore;
        new Handler().postDelayed(() -> {
            gameOverLayout.setVisibility(View.VISIBLE);
            finalScoreText.setText("Score: " + score);
            finalBestText.setText("Best: " + highScore);

            String message;
            if (score == 0) {
                message = "Try again! Tap to flap!";
            } else if (score < 3) {
                message = "Good start! Keep practicing!";
            } else if (score < 5) {
                message = "Nice! Getting better!";
            } else if (score < 10) {
                message = "Great! Survived small pipes!";
            } else if (score < 15) {
                message = "Awesome! Handled tall pipes!";
            } else if (score < 25) {
                message = "Amazing! Mastered challenges!";
            } else {
                message = "LEGENDARY! Unstoppable bird!";
            }
            if (finalIsNewHighScore) {
                message = "🏆 NEW HIGH SCORE! 🏆\n" + message;
            }

            instructionsText.setText(message);

            if (finalIsNewHighScore) {
                showNewHighScoreAnimation();
            }
        }, 1200);

        fadeOutPipes();
    }

    private void fadeOutPipes() {
        for (ImageView pipe : topPipes) {
            pipe.animate()
                    .alpha(0)
                    .setDuration(500)
                    .start();
        }
        for (ImageView pipe : bottomPipes) {
            pipe.animate()
                    .alpha(0)
                    .setDuration(500)
                    .start();
        }

        new Handler().postDelayed(this::clearPipes, 600);
    }

    private void showNewHighScoreAnimation() {
        TextView newHighScore = new TextView(this);
        newHighScore.setText("🎉 NEW BEST! 🎉");
        newHighScore.setTextSize(28);
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
                .scaleX(1.3f)
                .scaleY(1.3f)
                .alpha(1f)
                .setDuration(700)
                .withEndAction(() -> newHighScore.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .withEndAction(() -> {
                            new Handler().postDelayed(() -> {
                                newHighScore.animate()
                                        .scaleX(1.1f)
                                        .scaleY(1.1f)
                                        .setDuration(300)
                                        .withEndAction(() -> newHighScore.animate()
                                                .scaleX(1f)
                                                .scaleY(1f)
                                                .setDuration(300)
                                                .start())
                                        .start();
                            }, 500);

                            new Handler().postDelayed(() -> {
                                newHighScore.animate()
                                        .alpha(0f)
                                        .setDuration(500)
                                        .withEndAction(() -> gameArea.removeView(newHighScore))
                                        .start();
                            }, 3000);
                        })
                        .start())
                .start();
    }

    private void restartGame() {
        btnPause.setEnabled(true);
        btnPause.setAlpha(1f);
        clearPipes();
        startGame();
    }

    private void clearPipes() {
        for (ImageView pipe : topPipes) {
            gameArea.removeView(pipe);
        }
        for (ImageView pipe : bottomPipes) {
            gameArea.removeView(pipe);
        }
        topPipes.clear();
        bottomPipes.clear();
        pipeScored.clear();
    }

    private void showStartMenu() {
        gameState = 0;
        gameOver = false;
        gameRunning = false;
        isPaused = false;
        startMenuLayout.setVisibility(View.VISIBLE);
        gameOverLayout.setVisibility(View.GONE);
        birdSelectLayout.setVisibility(View.GONE);

        btnPause.setEnabled(false);
        btnPause.setAlpha(0.5f);
        btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_pause, 0, 0, 0);
        hidePauseOverlay();

        birdY = screenHeight / 2f - bird.getHeight() / 2f;
        bird.setY(birdY);
        bird.setRotation(0);
        bird.setTranslationY(0);

        startBirdIdleAnimation();
        clearPipes();
        updateUI();

        // Update bird button backgrounds when returning to menu
        updateBirdButtonBackgrounds();
    }
    private void updateUI() {
        scoreText.setText(String.valueOf(score));
        highScoreText.setText("Best: " + highScore);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameRunning && !gameOver && !isPaused) {
            togglePause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Don't auto-resume
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