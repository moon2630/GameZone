package com.example.gamezone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Random;

public class splash_screen extends AppCompatActivity {

    private ImageView logo, logoName;
    private FrameLayout animatedBackground;
    private Handler handler;
    private Random random = new Random();
    private boolean animationRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // FULL SCREEN
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

        logo = findViewById(R.id.logo);
        logoName = findViewById(R.id.logo_name);
        animatedBackground = findViewById(R.id.animatedBackground);
        handler = new Handler();

        startSplashAnimations();
    }

    private void startSplashAnimations() {
        // Initial invisible state
        logo.setAlpha(0f);
        logoName.setAlpha(0f);

        // Start background dot animations
        startBackgroundAnimations();

        // Add a delay before starting logo animations
        handler.postDelayed(() -> {
            // Start logo animation
            animateLogo();
        }, 300);
    }

    private void startBackgroundAnimations() {
        // Create flying dots
        createFlyingDots();

        // Create floating particles
        createFloatingParticles();

        // Add occasional big glowing dots
        createGlowingDots();
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
                    handler.postDelayed(this, 500); // Every 500ms
                }
            }
        };
        handler.post(createDotRunnable);
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

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);

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

        // Determine animation based on direction
        int duration = 3000 + random.nextInt(2000); // 3-5 seconds

        switch (direction) {
            case 0: // Left to Right
                dot.animate()
                        .translationX(animatedBackground.getWidth() + sizePx)
                        .translationY(random.nextInt(200) - 100) // Some vertical movement
                        .setDuration(duration)
                        .withEndAction(() -> animatedBackground.removeView(dot))
                        .start();
                break;

            case 1: // Right to Left
                dot.animate()
                        .translationX(-(animatedBackground.getWidth() + sizePx))
                        .translationY(random.nextInt(200) - 100)
                        .setDuration(duration)
                        .withEndAction(() -> animatedBackground.removeView(dot))
                        .start();
                break;

            case 2: // Top to Bottom
                dot.animate()
                        .translationY(animatedBackground.getHeight() + sizePx)
                        .translationX(random.nextInt(200) - 100) // Some horizontal movement
                        .setDuration(duration)
                        .withEndAction(() -> animatedBackground.removeView(dot))
                        .start();
                break;

            case 3: // Bottom to Top
                dot.animate()
                        .translationY(-(animatedBackground.getHeight() + sizePx))
                        .translationX(random.nextInt(200) - 100)
                        .setDuration(duration)
                        .withEndAction(() -> animatedBackground.removeView(dot))
                        .start();
                break;
        }

        // Pulsing effect
        dot.animate()
                .scaleX(1.4f)
                .scaleY(1.4f)
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> dot.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(1000)
                        .start())
                .start();
    }

    private void createFloatingParticles() {
        Runnable particleRunnable = new Runnable() {
            @Override
            public void run() {
                if (animationRunning) {
                    for (int i = 0; i < 4; i++) { // Create 4 particles at once
                        createFloatingParticle();
                    }
                    handler.postDelayed(this, 800); // Every 800ms
                }
            }
        };
        handler.post(particleRunnable);
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
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);

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
                .setInterpolator(new LinearInterpolator())
                .withEndAction(() -> animatedBackground.removeView(particle))
                .start();
    }

    private void createGlowingDots() {
        Runnable glowingDotRunnable = new Runnable() {
            @Override
            public void run() {
                if (animationRunning) {
                    createGlowingDot();
                    handler.postDelayed(this, 3000); // Create every 3 seconds
                }
            }
        };
        handler.post(glowingDotRunnable);
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

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);

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
                .setInterpolator(new LinearInterpolator())
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
                        handler.postDelayed(this, 1600);
                    }
                }
            }
        };
        handler.post(pulseRunnable);
    }

    // Rest of your existing animation methods remain the same...
    private void animateLogo() {
        // 1. Fade in logo with bounce
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
        fadeIn.setDuration(800);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());

        // 2. Scale animation (pop effect)
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0.3f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0.3f, 1.2f, 1.0f);
        scaleX.setDuration(1200);
        scaleY.setDuration(1200);

        // 3. Bouncing animation
        ObjectAnimator bounceY = ObjectAnimator.ofFloat(logo, "translationY",
                -50f, 30f, -20f, 10f, 0f);
        bounceY.setDuration(1500);
        bounceY.setInterpolator(new AccelerateDecelerateInterpolator());

        // 4. Continuous rotation animation
        RotateAnimation rotate = new RotateAnimation(0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(3000);
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setRepeatCount(Animation.INFINITE);

        // 5. Glow effect (pulse)
        ValueAnimator glowAnimator = ValueAnimator.ofFloat(0.8f, 1.2f);
        glowAnimator.setDuration(2000);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        glowAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            logo.setScaleX(value);
            logo.setScaleY(value);
        });

        // Play animations
        AnimatorSet logoAnimatorSet = new AnimatorSet();
        logoAnimatorSet.playTogether(fadeIn, scaleX, scaleY, bounceY);
        logoAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Start continuous animations
                logo.startAnimation(rotate);
                glowAnimator.start();

                // Start name animation
                animateLogoName();
            }
        });

        logoAnimatorSet.start();
    }

    private void animateLogoName() {
        // 1. Fade in with slight delay
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(logoName, "alpha", 0f, 1f);
        fadeIn.setDuration(1000);
        fadeIn.setStartDelay(200);

        // 2. Slide up from bottom
        ObjectAnimator slideUp = ObjectAnimator.ofFloat(logoName, "translationY", 150f, 0f);
        slideUp.setDuration(1200);
        slideUp.setInterpolator(new AccelerateDecelerateInterpolator());

        // 3. Scale animation (grow)
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoName, "scaleX", 0.5f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoName, "scaleY", 0.5f, 1.0f);
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);

        // 4. Color/text shine effect
        ValueAnimator shineAnimator = ValueAnimator.ofFloat(0f, 1f);
        shineAnimator.setDuration(1800);
        shineAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shineAnimator.setRepeatMode(ValueAnimator.REVERSE);
        shineAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            // Create shine effect using alpha
            float alpha = 0.7f + (value * 0.3f);
            logoName.setAlpha(alpha);

            // Add subtle scale effect
            float scale = 0.98f + (value * 0.04f);
            logoName.setScaleX(scale);
            logoName.setScaleY(scale);
        });

        // Play animations
        AnimatorSet nameAnimatorSet = new AnimatorSet();
        nameAnimatorSet.playTogether(fadeIn, slideUp, scaleX, scaleY);
        nameAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Start shine effect
                shineAnimator.start();

                // After all animations, go to main activity
                goToMainActivity();
            }
        });

        nameAnimatorSet.start();
    }

    private void goToMainActivity() {
        // Wait for a moment to enjoy the animation
        handler.postDelayed(() -> {
            Intent intent = new Intent(splash_screen.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2000); // 2 seconds to show the complete animation
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        animationRunning = false;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}