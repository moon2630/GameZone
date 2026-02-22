package com.example.gamezone;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout animatedBackground;
    private Handler animationHandler = new Handler();
    private Random random = new Random();
    private boolean animationRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.main_statusBar));
        }

        animatedBackground = findViewById(R.id.animatedBackground);

        // Setup card click listeners
        setupCardClickListeners();

        // Start background animations
        startBackgroundAnimations();
    }

    private void setupCardClickListeners() {
        LinearLayout cardView1 = findViewById(R.id.cardView1);
        cardView1.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, matching_card_game.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(intent);
        });

        LinearLayout cardView2 = findViewById(R.id.cardView2);
        cardView2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, baloon_game.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(intent);
        });

        LinearLayout cardView3 = findViewById(R.id.cardView3);
        cardView3.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, car_racing.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(intent);
        });

        LinearLayout cardView4 = findViewById(R.id.cardView4);
        cardView4.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, flappy_bird.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(intent);
        });

        LinearLayout cardView5 = findViewById(R.id.cardView5);
        cardView5.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, maths_game.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(intent);
        });

        LinearLayout cardView6 = findViewById(R.id.cardView6);
        cardView6.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, rps_game.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(intent);
        });

        LinearLayout cardView7 = findViewById(R.id.cardView7);
        cardView7.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, snake_game.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(intent);
        });

        LinearLayout cardView8 = findViewById(R.id.cardView8);
        cardView8.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, space_shooter.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(intent);
        });
        LinearLayout cardView9 = findViewById(R.id.cardView9);
        cardView9.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, tic_tac_home.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(intent);
        });
        LinearLayout cardView10 = findViewById(R.id.cardView10);
        cardView10.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, word_search.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(intent);
        });
        LinearLayout cardView11 = findViewById(R.id.cardView11);
        cardView11.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, cup_and_ball_game.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(intent);
        });
        LinearLayout cardView12 = findViewById(R.id.cardView12);
        cardView12.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, guess_the_number.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(intent);
        });


        LinearLayout cardView13 = findViewById(R.id.cardView13);
        cardView13.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, chess_setup.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(intent);
        });


        LinearLayout cardView14 = findViewById(R.id.cardView14);
        cardView14.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, fruit_slicer.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(intent);
        });
    }

    private void startBackgroundAnimations() {
        // Create flying dots
        createFlyingDots();

        // Create floating particles
        createFloatingParticles();

        // Add occasional big glowing dots
        createGlowingDots();
    }

    private void createGlowingDots() {
        Runnable glowingDotRunnable = new Runnable() {
            @Override
            public void run() {
                if (animationRunning) {
                    createGlowingDot();
                    animationHandler.postDelayed(this, 3000); // Create every 3 seconds
                }
            }
        };
        animationHandler.post(glowingDotRunnable);
    }

    private void createGlowingDot() {
        View glowingDot = new View(this);

        // Big glowing dot (8-15dp)
        int size = random.nextInt(8) + 8;
        int sizePx = (int) (size * getResources().getDisplayMetrics().density);

        // 10 Bright colors for glowing dots
        int[] glowingColors = {
                Color.parseColor("#00FFFF"), // Cyan
                Color.parseColor("#FF00FF"), // Magenta
                Color.parseColor("#FFFF00"), // Yellow
                Color.parseColor("#00FF00"), // Lime
                Color.parseColor("#FF6600"), // Orange
                Color.parseColor("#FF0066"), // Pink
                Color.parseColor("#33CCFF"), // Sky Blue
                Color.parseColor("#CC00FF"), // Purple
                Color.parseColor("#00FFCC"), // Turquoise
                Color.parseColor("#FFCC00")  // Gold
        };
        int color = glowingColors[random.nextInt(glowingColors.length)];

        glowingDot.setBackground(getResources().getDrawable(R.drawable.circle_dot));
        glowingDot.getBackground().setTint(color);
        glowingDot.setAlpha(0.9f);
        glowingDot.setElevation(15f);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(sizePx, sizePx);

        // Start from random corner
        int startCorner = random.nextInt(4);
        int startX = 0, startY = 0;

        switch (startCorner) {
            case 0: // Top-Left
                startX = -sizePx;
                startY = -sizePx;
                break;
            case 1: // Top-Right
                startX = animatedBackground.getWidth();
                startY = -sizePx;
                break;
            case 2: // Bottom-Left
                startX = -sizePx;
                startY = animatedBackground.getHeight();
                break;
            case 3: // Bottom-Right
                startX = animatedBackground.getWidth();
                startY = animatedBackground.getHeight();
                break;
        }

        params.leftMargin = startX;
        params.topMargin = startY;
        glowingDot.setLayoutParams(params);
        animatedBackground.addView(glowingDot);

        // Move to opposite corner
        int endX = 0, endY = 0;

        switch (startCorner) {
            case 0: // Top-Left to Bottom-Right
                endX = animatedBackground.getWidth() + sizePx;
                endY = animatedBackground.getHeight() + sizePx;
                break;
            case 1: // Top-Right to Bottom-Left
                endX = -sizePx;
                endY = animatedBackground.getHeight() + sizePx;
                break;
            case 2: // Bottom-Left to Top-Right
                endX = animatedBackground.getWidth() + sizePx;
                endY = -sizePx;
                break;
            case 3: // Bottom-Right to Top-Left
                endX = -sizePx;
                endY = -sizePx;
                break;
        }

        int duration = 5000;

        float translateX = endX - startX;
        float translateY = endY - startY;

        glowingDot.animate()
                .translationX(translateX)
                .translationY(translateY)
                .setDuration(duration)
                .setInterpolator(new android.view.animation.LinearInterpolator())
                .withEndAction(() -> animatedBackground.removeView(glowingDot))
                .start();

        // Pulsing glow animation
        startPulseAnimation(glowingDot);
    }

    private void startPulseAnimation(View view) {
        Runnable pulseRunnable = new Runnable() {
            @Override
            public void run() {
                if (view.getParent() != null) {
                    view.animate()
                            .scaleX(1.6f)
                            .scaleY(1.6f)
                            .alpha(0.7f)
                            .setDuration(800)
                            .withEndAction(() -> {
                                if (view.getParent() != null) {
                                    view.animate()
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .alpha(0.9f)
                                            .setDuration(800)
                                            .start();
                                }
                            })
                            .start();

                    if (view.getParent() != null) {
                        animationHandler.postDelayed(this, 1600);
                    }
                }
            }
        };
        animationHandler.post(pulseRunnable);
    }

    private void createFlyingDots() {
        Runnable createDotRunnable = new Runnable() {
            @Override
            public void run() {
                if (animationRunning) {
                    createFlyingDot();
                    // Create multiple dots at once for more movement
                    if (random.nextBoolean()) {
                        createFlyingDot(); // Second dot
                    }
                    animationHandler.postDelayed(this, 500); // Every 500ms
                }
            }
        };
        animationHandler.post(createDotRunnable);
    }

    private void createFloatingParticles() {
        Runnable particleRunnable = new Runnable() {
            @Override
            public void run() {
                if (animationRunning) {
                    for (int i = 0; i < 4; i++) { // Create 4 particles at once
                        createFloatingParticle();
                    }
                    animationHandler.postDelayed(this, 800); // Every 800ms
                }
            }
        };
        animationHandler.post(particleRunnable);
    }

    private void createFlyingDot() {
        View dot = new View(this);

        // Random size (6-12dp)
        int size = random.nextInt(7) + 6;
        int sizePx = (int) (size * getResources().getDisplayMetrics().density);

        // 10 Vibrant neon colors
        int[] neonColors = {
                Color.parseColor("#00FFFF"), // Cyan
                Color.parseColor("#FF00FF"), // Magenta
                Color.parseColor("#FFFF00"), // Yellow
                Color.parseColor("#00FF00"), // Lime Green
                Color.parseColor("#FF6600"), // Orange
                Color.parseColor("#FF0066"), // Hot Pink
                Color.parseColor("#33CCFF"), // Sky Blue
                Color.parseColor("#CC00FF"), // Purple
                Color.parseColor("#00FFCC"), // Turquoise
                Color.parseColor("#FFCC00")  // Gold
        };
        int color = neonColors[random.nextInt(neonColors.length)];

        // Make the dot circular
        dot.setBackground(getResources().getDrawable(R.drawable.circle_dot));
        dot.getBackground().setTint(color);
        dot.setAlpha(0.85f);
        dot.setElevation(10f);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(sizePx, sizePx);

        // Random starting position and direction
        int direction = random.nextInt(4); // 0: left to right, 1: right to left, 2: top to bottom, 3: bottom to top
        int startX, startY;

        switch (direction) {
            case 0: // Left to Right
                startX = -sizePx;
                startY = random.nextInt(animatedBackground.getHeight());
                params.leftMargin = startX;
                params.topMargin = startY;
                break;

            case 1: // Right to Left
                startX = animatedBackground.getWidth();
                startY = random.nextInt(animatedBackground.getHeight());
                params.leftMargin = startX;
                params.topMargin = startY;
                break;

            case 2: // Top to Bottom
                startX = random.nextInt(animatedBackground.getWidth());
                startY = -sizePx;
                params.leftMargin = startX;
                params.topMargin = startY;
                break;

            case 3: // Bottom to Top
                startX = random.nextInt(animatedBackground.getWidth());
                startY = animatedBackground.getHeight();
                params.leftMargin = startX;
                params.topMargin = startY;
                break;

            default:
                startX = -sizePx;
                startY = random.nextInt(animatedBackground.getHeight());
                params.leftMargin = startX;
                params.topMargin = startY;
        }

        dot.setLayoutParams(params);
        animatedBackground.addView(dot);

        // SPEED CONTROL: Different speeds for different directions
        int duration;
        int distance;

        switch (direction) {
            case 0: // Left to Right - Medium speed
                distance = animatedBackground.getWidth() + sizePx;
                duration = 2500 + random.nextInt(1500); // 2.5-4 seconds
                break;

            case 1: // Right to Left - Fast speed
                distance = animatedBackground.getWidth() + sizePx;
                duration = 2000 + random.nextInt(1000); // 2-3 seconds (faster)
                break;

            case 2: // Top to Bottom - Slow speed
                distance = animatedBackground.getHeight() + sizePx;
                duration = 10000 + random.nextInt(3500); // 3.5-5 seconds (slower)
                break;

            case 3: // Bottom to Top - Medium-Fast speed
                distance = animatedBackground.getHeight() + sizePx;
                duration = 2200 + random.nextInt(1300); // 2.2-3.5 seconds
                break;

            default:
                distance = animatedBackground.getWidth() + sizePx;
                duration = 3000;
        }

        // Add random speed variation (±20%)
        float speedMultiplier = 0.8f + random.nextFloat() * 0.4f; // 0.8 to 1.2
        duration = (int) (duration / speedMultiplier);

        // Determine animation based on direction
        switch (direction) {
            case 0: // Left to Right
                dot.animate()
                        .translationX(distance)
                        .translationY(random.nextInt(200) - 100) // Some vertical movement
                        .setDuration(duration)
                        .setInterpolator(new android.view.animation.LinearInterpolator()) // Constant speed
                        .withEndAction(() -> animatedBackground.removeView(dot))
                        .start();
                break;

            case 1: // Right to Left
                dot.animate()
                        .translationX(-distance)
                        .translationY(random.nextInt(200) - 100)
                        .setDuration(duration)
                        .setInterpolator(new android.view.animation.AccelerateInterpolator()) // Start slow, end fast
                        .withEndAction(() -> animatedBackground.removeView(dot))
                        .start();
                break;

            case 2: // Top to Bottom
                dot.animate()
                        .translationY(distance)
                        .translationX(random.nextInt(200) - 100) // Some horizontal movement
                        .setDuration(duration)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator()) // Start fast, end slow
                        .withEndAction(() -> animatedBackground.removeView(dot))
                        .start();
                break;

            case 3: // Bottom to Top
                dot.animate()
                        .translationY(-distance)
                        .translationX(random.nextInt(200) - 100)
                        .setDuration(duration)
                        .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator()) // Slow start/end, fast middle
                        .withEndAction(() -> animatedBackground.removeView(dot))
                        .start();
                break;
        }

        // Pulsing effect
        dot.animate()
                .scaleX(1.4f)
                .scaleY(1.4f)
                .setDuration(1000)
                .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                .withEndAction(() -> dot.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(1000)
                        .start())
                .start();
    }


    private void createFloatingParticle() {
        View particle = new View(this);

        // Size (3-7dp)
        int size = random.nextInt(5) + 3;
        int sizePx = (int) (size * getResources().getDisplayMetrics().density);

        // 10 Bright neon colors for particles too
        int[] neonColors = {
                Color.parseColor("#80DEEA"), // Light Cyan
                Color.parseColor("#B388FF"), // Light Purple
                Color.parseColor("#69F0AE"), // Light Green
                Color.parseColor("#FFD740"), // Light Yellow
                Color.parseColor("#FF80AB"), // Light Pink
                Color.parseColor("#80CBC4"), // Teal
                Color.parseColor("#FFAB91"), // Coral
                Color.parseColor("#9FA8DA"), // Lavender
                Color.parseColor("#CE93D8"), // Orchid
                Color.parseColor("#A5D6A7")  // Mint
        };
        int color = neonColors[random.nextInt(neonColors.length)];

        particle.setBackground(getResources().getDrawable(R.drawable.circle_dot));
        particle.getBackground().setTint(color);
        particle.setAlpha(0.5f);

        // Random starting position (any edge)
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(sizePx, sizePx);

        int startEdge = random.nextInt(4);
        int startX = 0, startY = 0;

        switch (startEdge) {
            case 0: // Top
                startX = random.nextInt(animatedBackground.getWidth());
                startY = -sizePx;
                break;
            case 1: // Bottom
                startX = random.nextInt(animatedBackground.getWidth());
                startY = animatedBackground.getHeight();
                break;
            case 2: // Left
                startX = -sizePx;
                startY = random.nextInt(animatedBackground.getHeight());
                break;
            case 3: // Right
                startX = animatedBackground.getWidth();
                startY = random.nextInt(animatedBackground.getHeight());
                break;
        }

        params.leftMargin = startX;
        params.topMargin = startY;
        particle.setLayoutParams(params);
        animatedBackground.addView(particle);

        // Random target position (opposite or different edge)
        int endX = 0, endY = 0;
        int direction = random.nextInt(8); // 8 different movement patterns

        switch (direction) {
            case 0: // Top to Bottom
                endX = startX + random.nextInt(100) - 50;
                endY = animatedBackground.getHeight() + sizePx;
                break;
            case 1: // Bottom to Top
                endX = startX + random.nextInt(100) - 50;
                endY = -sizePx;
                break;
            case 2: // Left to Right
                endX = animatedBackground.getWidth() + sizePx;
                endY = startY + random.nextInt(100) - 50;
                break;
            case 3: // Right to Left
                endX = -sizePx;
                endY = startY + random.nextInt(100) - 50;
                break;
            case 4: // Diagonal Top-Left to Bottom-Right
                endX = animatedBackground.getWidth() + sizePx;
                endY = animatedBackground.getHeight() + sizePx;
                break;
            case 5: // Diagonal Top-Right to Bottom-Left
                endX = -sizePx;
                endY = animatedBackground.getHeight() + sizePx;
                break;
            case 6: // Diagonal Bottom-Left to Top-Right
                endX = animatedBackground.getWidth() + sizePx;
                endY = -sizePx;
                break;
            case 7: // Diagonal Bottom-Right to Top-Left
                endX = -sizePx;
                endY = -sizePx;
                break;
        }

        int duration = 4000 + random.nextInt(3000); // 4-7 seconds

        // Calculate translation needed
        float translateX = endX - startX;
        float translateY = endY - startY;

        particle.animate()
                .translationX(translateX)
                .translationY(translateY)
                .alpha(0.1f)
                .setDuration(duration)
                .setInterpolator(new android.view.animation.LinearInterpolator())
                .withEndAction(() -> animatedBackground.removeView(particle))
                .start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        animationRunning = false;
        animationHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        animationRunning = true;
        startBackgroundAnimations();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        animationRunning = false;
        animationHandler.removeCallbacksAndMessages(null);
    }
}