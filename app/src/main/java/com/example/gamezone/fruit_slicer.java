package com.example.gamezone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
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
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Random;

public class fruit_slicer extends AppCompatActivity {

    // Game Views
    private RelativeLayout gameArea;
    private TextView scoreText, highScoreText, livesText, comboText;
    private TextView finalScoreText, finalHighScoreText, btnPause;
    private CardView gameOverLayout, startMenuLayout;
    private AppCompatButton btnStart, btnRestart, btnMenu;

    // Game Variables
    private int score = 0;
    private int highScore = 0;
    private int lives = 3;
    private int combo = 0;
    private int maxCombo = 0;
    private boolean gameRunning = false;
    private boolean gameOver = false;
    private boolean isPaused = false;
    private boolean slicing = false;

    // Fruit Objects - Use CopyOnWriteArrayList to avoid ConcurrentModificationException
    private CopyOnWriteArrayList<GameObject> gameObjects = new CopyOnWriteArrayList<>();
    private ArrayList<BladePoint> bladePoints = new ArrayList<>();

    // Game Loop
    private Handler gameHandler = new Handler();
    private Runnable gameRunnable;
    private static final long UPDATE_INTERVAL = 16; // ~60 FPS

    // Random
    private Random random = new Random();

    // Screen dimensions
    private int screenWidth, screenHeight;

    // Shared Preferences
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "FruitSlicerPrefs";

    // Fruit and bomb drawables - Map fruit indices to colors for juice effects
    private int[] fruitDrawables = {
            R.drawable.vector_fruit_apple,           // index 0 - Apple
            R.drawable.vector_fruit_banana,          // index 1 - Banana
            R.drawable.vector_fruit_grapes,          // index 2 - Grapes
            R.drawable.vector_fruit_watermelon,      // index 3 - Watermelon
            R.drawable.vector_fruit_mango2,          // index 4 - Mango
            R.drawable.vector_fruit_strawberry1,     // index 5 - Strawberry
            R.drawable.vector_fruit_pineapple        // index 6 - Pineapple
    };

    private int[] fruitJuiceColors = {
            0xFFFF4444, // Bright Red for Apple
            0xFFFFFF00, // Bright Yellow for Banana
            0xFF7CFC00, // green for Grapes
            0xFFFF69B4, // Hot Pink for Watermelon
            0xFFFFA500, // Orange for Mango
            0xFFFF2D5C, // Strawberry Red
            0xFFFFD700  // Gold/Yellow for Pineapple
    };

    private int[] specialFruitDrawables = {
            R.drawable.vector_fruit_peach,   // Special fruit 1 - Peach
            R.drawable.vector_fruit_cherry4    // Special fruit 2 - Cherry
    };

    private int[] specialFruitJuiceColors = {
            0xFFFFB6C1, // Light Pink for Peach
            0xFF8B0000  // Dark Red for Cherry
    };

    private int bombDrawable = R.drawable.vector_fruit_bomb;
    private int bombJuiceColor = 0xFF000000; // Black for bomb
    // Spawn timers
    private long lastSpawnTime = 0;
    private int spawnRate = 800;
    private int bombChance = 15;
    private int specialChance = 5;

    // Game difficulty and speed
    private String difficulty = "normal";
    private float baseSpeed = 15;
    private float gravity = 0.5f;
    private long gameTime = 0;
    private int scoreForSpeedIncrease = 500;

    // Blade drawing
    private class BladePoint {
        float x, y;
        long time;
        BladePoint(float x, float y) {
            this.x = x;
            this.y = y;
            this.time = System.currentTimeMillis();
        }
    }

    // Game Object Class
    private class GameObject {
        ImageView image;
        float x, y;
        float velocityX, velocityY;
        float rotationSpeed;
        float scale = 1.0f;
        int type; // 0=normal fruit, 1=special fruit, 2=bomb
        int fruitIndex;
        boolean sliced = false;
        boolean active = true;
        int juiceColor;

        GameObject(int type, int drawableRes, int fruitIndex, int juiceColor) {
            this.type = type;
            this.fruitIndex = fruitIndex;
            this.juiceColor = juiceColor;
            this.image = new ImageView(fruit_slicer.this);
            this.image.setImageResource(drawableRes);

            // ===== FRUIT SIZE SETTINGS - ADJUST EACH FRUIT SIZE =====
            int size;

            if (type == 2) { // Bomb
                size = 140; // Bomb size
            } else if (type == 1) { // Special fruits (Peach, Cherry)
                // Special fruits based on fruitIndex (0=Peach, 1=Cherry)
                switch (fruitIndex) {
                    case 0: // Peach - INCREASE SIZE
                        size = 150; // Larger Peach
                        break;
                    case 1: // Cherry - INCREASE SIZE
                        size = 140; // Larger Cherry
                        break;
                    default:
                        size = 140;
                        break;
                }
            } else { // Normal fruits
                // Different sizes for different fruits based on index
                switch (fruitIndex) {
                    case 0: // Apple
                        size = 130;
                        break;
                    case 1: // Banana
                        size = 140; // Banana longer but we use square, so medium
                        break;
                    case 2: // Grapes
                        size = 150; // Grapes cluster - larger
                        break;
                    case 3: // Watermelon
                        size = 170; // Watermelon - biggest
                        break;
                    case 4: // Mango
                        size = 140; // Mango - medium
                        break;
                    case 5: // Strawberry - DECREASE SIZE
                        size = 110; // Smaller strawberry
                        break;
                    case 6: // Pineapple - INCREASE SIZE
                        size = 200; // Larger pineapple
                        break;
                    default:
                        size = 130;
                        break;
                }
            }
            // ========================================================

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
            image.setLayoutParams(params);

            // ===== PADDING SETTINGS - ADJUST THESE VALUES =====
            int horizontalPadding = 40;  // Padding from left/right edges
            int bottomPadding = 100;      // Padding from bottom
            // =================================================

            // Calculate safe boundaries
            int minX = horizontalPadding;
            int maxX = screenWidth - horizontalPadding - size;

            // Ensure minX is not greater than maxX
            if (minX > maxX) {
                minX = 0;
                maxX = screenWidth - size;
            }

            // Random X position within safe boundaries
            x = random.nextInt(maxX - minX + 1) + minX + size/2f;

            // Y position with bottom padding - ensure it's within screen
            y = screenHeight - bottomPadding - size/2f;
            if (y < size/2f) y = screenHeight - size/2f - 50;

            // Speed increases with game time
            float currentBaseSpeed = baseSpeed + (gameTime / 20000f) * 10;
            if (difficulty.equals("hard")) currentBaseSpeed += 5;
            if (difficulty.equals("expert")) currentBaseSpeed += 10;

            velocityY = -currentBaseSpeed - random.nextFloat() * 5;
            velocityX = (random.nextFloat() - 0.5f) * (8 + gameTime / 10000f);
            rotationSpeed = (random.nextFloat() - 0.5f) * 10;

            // Ensure image stays within bounds during placement
            float finalX = Math.max(minX, Math.min(x - size/2f, maxX));
            float finalY = Math.max(horizontalPadding, Math.min(y - size/2f, screenHeight - size - bottomPadding));

            image.setX(finalX);
            image.setY(finalY);
            image.setRotation(random.nextFloat() * 360);
            image.setScaleX(scale);
            image.setScaleY(scale);
        }
        void update() {
            if (!active || sliced) return;

            velocityY += gravity;
            x += velocityX;
            y += velocityY;

            // Add boundary checking to keep fruit on screen
            int size = image.getWidth();

            // Left boundary
            if (x - size/2f < 0) {
                x = size/2f;
                velocityX = -velocityX * 0.5f; // Bounce with reduced speed
            }

            // Right boundary
            if (x + size/2f > screenWidth) {
                x = screenWidth - size/2f;
                velocityX = -velocityX * 0.5f; // Bounce with reduced speed
            }

            // Top boundary (optional - fruits can go off top)
            if (y - size/2f < 0) {
                y = size/2f;
                velocityY = -velocityY * 0.3f; // Soft bounce at top
            }

            image.setRotation(image.getRotation() + rotationSpeed);
            image.setX(x - size / 2f);
            image.setY(y - size / 2f);

            // Check if object is off screen (only when completely gone)
            if (y > screenHeight + 200 || y < -200) {
                active = false;
                if (type != 2 && !sliced && gameRunning && !gameOver && !isPaused) {
                    runOnUiThread(() -> {
                        lives--;
                        updateLivesDisplay();
                        showMissEffect();
                        if (lives <= 0) gameOver();
                    });
                }
            }
        }
        void slice() {
            if (sliced || !active) return;

            sliced = true;
            active = false;

            runOnUiThread(() -> {
                if (type == 2) {
                    handleBombExplosion();
                } else {
                    int points = type == 1 ? 50 : 10;
                    combo++;
                    if (combo > maxCombo) maxCombo = combo;

                    int totalPoints = points * combo;
                    score += totalPoints;

                    // Update game time for speed increase
                    if (score > scoreForSpeedIncrease) {
                        scoreForSpeedIncrease += 500;
                        gameTime += 5000;
                    }

                    showSliceEffect(x, y, "+" + totalPoints, type == 1);
                    createSplashEffect(juiceColor);
                    createFruitHalves();
                }
                gameArea.removeView(image);
            });
        }

        private void createSplashEffect(int color) {
            for (int i = 0; i < 12; i++) {
                ImageView drop = new ImageView(fruit_slicer.this);
                drop.setImageResource(fruitDrawables[0]);
                drop.setColorFilter(color);

                int dropSize = 10 + random.nextInt(10);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dropSize, dropSize);
                drop.setLayoutParams(params);

                drop.setX(x - dropSize/2);
                drop.setY(y - dropSize/2);
                drop.setAlpha(0.9f);

                gameArea.addView(drop);

                float angle = (float) (i * Math.PI / 6);
                float destX = x + (float) Math.cos(angle) * (80 + random.nextInt(50));
                float destY = y + (float) Math.sin(angle) * (80 + random.nextInt(50));

                drop.animate()
                        .x(destX)
                        .y(destY)
                        .alpha(0)
                        .rotation(360 + random.nextInt(360))
                        .setDuration(600 + random.nextInt(200))
                        .withEndAction(() -> {
                            try {
                                gameArea.removeView(drop);
                            } catch (Exception e) {
                                // Ignore removal errors
                            }
                        })
                        .start();
            }
        }

        private void createFruitHalves() {
            for (int i = 0; i < 2; i++) {
                ImageView half = new ImageView(fruit_slicer.this);
                half.setImageResource(fruitDrawables[fruitIndex]);
                half.setColorFilter(i == 0 ? 0xFFFFAAAA : 0xFFAAFFAA);

                half.setX(image.getX());
                half.setY(image.getY());
                half.setRotation(image.getRotation());

                int halfSize = image.getWidth() / 2;
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(halfSize, image.getHeight());
                half.setLayoutParams(params);

                gameArea.addView(half);

                half.animate()
                        .x(image.getX() + (i == 0 ? -120 : 120))
                        .y(image.getY() - 70)
                        .rotation(image.getRotation() + (i == 0 ? -120 : 120))
                        .alpha(0)
                        .setDuration(600)
                        .withEndAction(() -> {
                            try {
                                gameArea.removeView(half);
                            } catch (Exception e) {
                                // Ignore removal errors
                            }
                        })
                        .start();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_slicer);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        initViews();
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        loadGameData();
        showStartMenu();

        gameArea.setOnTouchListener((v, event) -> {
            handleTouch(event);
            return true;
        });
    }

    private void initViews() {
        gameArea = findViewById(R.id.gameArea);
        scoreText = findViewById(R.id.scoreText);
        highScoreText = findViewById(R.id.highScoreText);
        livesText = findViewById(R.id.livesText);
        comboText = findViewById(R.id.comboText);
        gameOverLayout = findViewById(R.id.gameOverLayout);
        startMenuLayout = findViewById(R.id.startMenuLayout);
        btnPause = findViewById(R.id.btnPause);

        btnStart = findViewById(R.id.btnStart);
        btnRestart = findViewById(R.id.btnRestart);
        btnMenu = findViewById(R.id.btnMenu);

        finalScoreText = findViewById(R.id.finalScoreText);
        finalHighScoreText = findViewById(R.id.finalHighScoreText);

        findViewById(R.id.btnDifficulty).setOnClickListener(v -> toggleDifficulty());
        btnStart.setOnClickListener(v -> startGame());
        btnRestart.setOnClickListener(v -> restartGame());
        btnMenu.setOnClickListener(v -> showStartMenu());
        btnPause.setOnClickListener(v -> togglePause());

        findViewById(R.id.btnFruitSelect).setOnClickListener(v ->
                Toast.makeText(this, "Fruits change automatically!", Toast.LENGTH_SHORT).show());
    }

    private void loadGameData() {
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        highScore = prefs.getInt("highScore", 0);
        difficulty = prefs.getString("difficulty", "normal");

        TextView btnDifficulty = findViewById(R.id.btnDifficulty);
        btnDifficulty.setText("DIFFICULTY: " + difficulty.toUpperCase());
        updateUI();
    }

    private void saveGameData() {
        prefs.edit()
                .putInt("highScore", highScore)
                .putString("difficulty", difficulty)
                .apply();
    }

    private void toggleDifficulty() {
        switch (difficulty) {
            case "easy":
                difficulty = "normal";
                spawnRate = 700;
                bombChance = 15;
                specialChance = 5;
                baseSpeed = 15;
                gravity = 0.5f;
                break;
            case "normal":
                difficulty = "hard";
                spawnRate = 500;
                bombChance = 20;
                specialChance = 8;
                baseSpeed = 18;
                gravity = 0.6f;
                break;
            case "hard":
                difficulty = "expert";
                spawnRate = 350;
                bombChance = 25;
                specialChance = 10;
                baseSpeed = 22;
                gravity = 0.7f;
                break;
            case "expert":
                difficulty = "easy";
                spawnRate = 850;
                bombChance = 10;
                specialChance = 3;
                baseSpeed = 12;
                gravity = 0.4f;
                break;
        }

        TextView btnDifficulty = findViewById(R.id.btnDifficulty);
        btnDifficulty.setText("DIFFICULTY: " + difficulty.toUpperCase());
        saveGameData();
        Toast.makeText(this, difficulty.toUpperCase() + " mode selected", Toast.LENGTH_SHORT).show();
    }

    private void showStartMenu() {
        gameRunning = false;
        gameOver = false;
        isPaused = false;
        gameTime = 0;
        scoreForSpeedIncrease = 500;

        startMenuLayout.setVisibility(View.VISIBLE);
        gameOverLayout.setVisibility(View.GONE);

        btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_pause, 0, 0, 0);
        btnPause.setEnabled(false);
        btnPause.setAlpha(0.5f);

        clearGameObjects();
        updateUI();
    }

    private void startGame() {
        gameRunning = true;
        gameOver = false;
        isPaused = false;
        score = 0;
        lives = 3;
        combo = 0;
        maxCombo = 0;
        gameTime = 0;
        scoreForSpeedIncrease = 500;

        startMenuLayout.setVisibility(View.GONE);
        gameOverLayout.setVisibility(View.GONE);

        btnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_pause, 0, 0, 0);
        btnPause.setEnabled(true);
        btnPause.setAlpha(1f);

        clearGameObjects();
        lastSpawnTime = System.currentTimeMillis();
        startGameLoop();

        Toast.makeText(this, "Slice the fruits! Avoid bombs!", Toast.LENGTH_SHORT).show();
    }

    private void gameOver() {
        gameRunning = false;
        gameOver = true;

        if (score > highScore) {
            highScore = score;
            showNewHighScoreAnimation();
        }

        saveGameData();

        new Handler().postDelayed(() -> {
            gameOverLayout.setVisibility(View.VISIBLE);
            finalScoreText.setText("Score: " + score);
            finalHighScoreText.setText("High Score: " + highScore);

            btnPause.setEnabled(false);
            btnPause.setAlpha(0.5f);
            Toast.makeText(this, "Max Combo: " + maxCombo + "!", Toast.LENGTH_LONG).show();
        }, 1000);

        clearGameObjects();
    }

    private void togglePause() {
        if (gameOver) return;

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
        pauseText.setText("⏸️ PAUSED ⏸️");
        pauseText.setTextSize(40);
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

    private void handleTouch(MotionEvent event) {
        if (!gameRunning || gameOver || isPaused) return;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                slicing = true;
                bladePoints.clear();
                bladePoints.add(new BladePoint(x, y));
                checkSliceCollisions(x, y);
                break;

            case MotionEvent.ACTION_MOVE:
                if (slicing) {
                    bladePoints.add(new BladePoint(x, y));
                    checkSliceCollisions(x, y);

                    long currentTime = System.currentTimeMillis();
                    Iterator<BladePoint> iterator = bladePoints.iterator();
                    while (iterator.hasNext()) {
                        if (currentTime - iterator.next().time > 100) {
                            iterator.remove();
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                slicing = false;
                bladePoints.clear();
                break;
        }
    }

    private void checkSliceCollisions(float x, float y) {
        // Use a snapshot iterator to avoid modification issues
        for (GameObject obj : gameObjects) {
            if (obj != null && obj.active && !obj.sliced) {
                float objLeft = obj.x - obj.image.getWidth() / 2f;
                float objRight = obj.x + obj.image.getWidth() / 2f;
                float objTop = obj.y - obj.image.getHeight() / 2f;
                float objBottom = obj.y + obj.image.getHeight() / 2f;

                if (x >= objLeft && x <= objRight && y >= objTop && y <= objBottom) {
                    obj.slice();
                    break;
                }
            }
        }
    }

    private void handleBombExplosion() {
        lives--;
        updateLivesDisplay();
        combo = 0;
        comboText.setText("COMBO: 0");

        View flash = new View(this);
        flash.setBackgroundColor(0x80FF0000);
        flash.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        ));
        gameArea.addView(flash);

        flash.animate()
                .alpha(0)
                .setDuration(300)
                .withEndAction(() -> {
                    try {
                        gameArea.removeView(flash);
                    } catch (Exception e) {
                        // Ignore
                    }
                })
                .start();

        Toast.makeText(this, "BOOM! -1 Life", Toast.LENGTH_SHORT).show();

        if (lives <= 0) {
            gameOver();
        }
    }

    private void showSliceEffect(float x, float y, String text, boolean special) {
        LinearLayout effectLayout = new LinearLayout(this);
        effectLayout.setOrientation(LinearLayout.VERTICAL);
        effectLayout.setGravity(android.view.Gravity.CENTER);

        TextView sliceText = new TextView(this);
        sliceText.setText(text);
        sliceText.setTextSize(special ? 32 : 26);
        sliceText.setTextColor(special ? 0xFFFFD700 : 0xFFFFFF00);
        sliceText.setShadowLayer(5, 2, 2, 0xFF000000);
        sliceText.setTypeface(null, Typeface.BOLD);

        if (combo > 1) {
            TextView comboIndicator = new TextView(this);
            comboIndicator.setText("x" + combo + " COMBO!");
            comboIndicator.setTextSize(18);
            comboIndicator.setTextColor(0xFFFFA500);
            comboIndicator.setShadowLayer(3, 1, 1, 0xFF000000);
            effectLayout.addView(comboIndicator);
        }

        effectLayout.addView(sliceText);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = (int) x - 50;
        params.topMargin = (int) y - 80;
        effectLayout.setLayoutParams(params);

        gameArea.addView(effectLayout);

        effectLayout.setScaleX(0.2f);
        effectLayout.setScaleY(0.2f);
        effectLayout.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .translationY(-150)
                .alpha(0)
                .setDuration(800)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    try {
                        gameArea.removeView(effectLayout);
                    } catch (Exception e) {
                        // Ignore
                    }
                })
                .start();
    }

    private void showMissEffect() {
        TextView missText = new TextView(this);
        missText.setText("MISS!");
        missText.setTextSize(24);
        missText.setTextColor(0xFFFF4444);
        missText.setShadowLayer(3, 0, 0, 0xFF000000);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        missText.setLayoutParams(params);

        gameArea.addView(missText);

        missText.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .alpha(0)
                .setDuration(500)
                .withEndAction(() -> {
                    try {
                        gameArea.removeView(missText);
                    } catch (Exception e) {
                        // Ignore
                    }
                })
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
                    gameHandler.postDelayed(this, UPDATE_INTERVAL);
                }
            }
        };
        gameHandler.post(gameRunnable);
    }

    private void updateGame() {
        long currentTime = System.currentTimeMillis();

        // Update game time for speed increase
        gameTime += UPDATE_INTERVAL;

        if (currentTime - lastSpawnTime > spawnRate) {
            spawnGameObject();
            lastSpawnTime = currentTime;
        }

        // Update objects safely using iterator
        for (GameObject obj : gameObjects) {
            if (obj.active) {
                obj.update();
            }
        }

        // Remove inactive objects
        ArrayList<GameObject> toRemove = new ArrayList<>();
        for (GameObject obj : gameObjects) {
            if (!obj.active) {
                toRemove.add(obj);
            }
        }

        for (GameObject obj : toRemove) {
            try {
                gameArea.removeView(obj.image);
                gameObjects.remove(obj);
            } catch (Exception e) {
                // Ignore
            }
        }

        // Combo decay
        if (combo > 0 && currentTime % 1000 < 16) {
            combo = Math.max(0, combo - 1);
        }

        updateUI();
    }

    private void spawnGameObject() {
        int randomValue = random.nextInt(100);
        GameObject obj = null;

        try {
            if (randomValue < bombChance) {
                obj = new GameObject(2, bombDrawable, -1, bombJuiceColor);
            } else if (randomValue < bombChance + specialChance) {
                int fruitIndex = random.nextInt(specialFruitDrawables.length);
                obj = new GameObject(1, specialFruitDrawables[fruitIndex], fruitIndex,
                        specialFruitJuiceColors[fruitIndex]);
            } else {
                int fruitIndex = random.nextInt(fruitDrawables.length);
                obj = new GameObject(0, fruitDrawables[fruitIndex], fruitIndex,
                        fruitJuiceColors[fruitIndex]);
            }

            if (obj != null) {
                gameObjects.add(obj);
                gameArea.addView(obj.image);
            }
        } catch (Exception e) {
            // Ignore spawn errors
        }
    }

    private void clearGameObjects() {
        for (GameObject obj : gameObjects) {
            try {
                gameArea.removeView(obj.image);
            } catch (Exception e) {
                // Ignore
            }
        }
        gameObjects.clear();
    }

    private void updateUI() {
        scoreText.setText("Score: " + score);
        highScoreText.setText("High: " + highScore);
        comboText.setText("COMBO x" + combo);
        updateLivesDisplay();
    }

    private void updateLivesDisplay() {
        StringBuilder livesString = new StringBuilder();
        int displayLives = Math.max(lives, 0);
        displayLives = Math.min(displayLives, 3);

        for (int i = 0; i < displayLives; i++) {
            livesString.append("❤️");
        }
        for (int i = displayLives; i < 3; i++) {
            livesString.append("🤍");
        }
        livesText.setText(livesString.toString());
    }

    private void showNewHighScoreAnimation() {
        TextView newHighScore = new TextView(this);
        newHighScore.setText("🏆 NEW HIGH SCORE! 🏆");
        newHighScore.setTextSize(20);
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
                        .withEndAction(() -> new Handler().postDelayed(() -> {
                            newHighScore.animate()
                                    .alpha(0f)
                                    .setDuration(500)
                                    .withEndAction(() -> {
                                        try {
                                            gameArea.removeView(newHighScore);
                                        } catch (Exception e) {
                                            // Ignore
                                        }
                                    })
                                    .start();
                        }, 2000))
                        .start())
                .start();
    }

    private void restartGame() {
        btnPause.setEnabled(true);
        btnPause.setAlpha(1f);
        clearGameObjects();
        startGame();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameRunning && !gameOver && !isPaused) {
            togglePause();
        }
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
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(Color.WHITE);
        dialogLayout.setPadding(50, 50, 50, 35);

        TextView title = new TextView(this);
        title.setText("Leave Game");
        title.setTypeface(ResourcesCompat.getFont(this, R.font.caudex), Typeface.BOLD);
        title.setTextSize(22);
        title.setPadding(0, 0, 10, 20);
        title.setTextColor(Color.BLACK);

        TextView message = new TextView(this);
        message.setText("Do you want to leave the game?");
        message.setTypeface(ResourcesCompat.getFont(this, R.font.caudex));
        message.setTextSize(16);
        message.setPadding(0, 10, 0, 0);
        message.setTextColor(Color.BLACK);

        dialogLayout.addView(title);
        dialogLayout.addView(message);

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
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
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