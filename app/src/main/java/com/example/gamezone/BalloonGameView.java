package com.example.gamezone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;

public class BalloonGameView extends View {

    // Add TAG for logging
    private static final String TAG = "BalloonGameView";

    private ArrayList<Balloon> balloons;
    private ArrayList<PopEffect> popEffects;
    private Random random;
    private Paint paint;
    private Bitmap[] balloonImages;
    private long lastSpawnTime;

    // Difficulty settings
    private int difficulty = 1;
    private int spawnDelay;
    private int gameTime;
    private float baseSpeed;
    private int maxBalloons;

    // Balloon colors
    private int[] balloonColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
            Color.MAGENTA, Color.CYAN, Color.rgb(255, 165, 0)
    };


    // Balloon spawning control
    private int spawnMultiplier = 1; // Start with 1x
    private long lastQuantityIncreaseTime;
    private final int QUANTITY_INCREASE_INTERVAL = 10000; // 10 seconds in milliseconds

    // Difficulty configurations
    private static final int[] SPAWN_DELAYS = {1500, 800, 400};
    private static final int[] GAME_TIMES = {90, 60, 45};
    private static final float[] BASE_SPEEDS = {2.8f, 3.5f, 4.0f};
    private static final int[] MAX_BALLOONS = {15, 25, 40};

    // Missed balloon tracking
    private int missedBalloons = 0;
    private int maxMissedBeforeRemoval = 3;

    // Add listener for balloon events
    public interface BalloonEventListener {
        void onBalloonMissed();
        void onBalloonRemoved();
    }

    private BalloonEventListener balloonEventListener;

    public void setBalloonEventListener(BalloonEventListener listener) {
        this.balloonEventListener = listener;
    }

    public BalloonGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        balloons = new ArrayList<>();
        popEffects = new ArrayList<>();
        random = new Random();
        paint = new Paint();
        paint.setAntiAlias(true);

        createBalloonImages();
        lastSpawnTime = System.currentTimeMillis();
        lastQuantityIncreaseTime = System.currentTimeMillis(); // Add this
        spawnMultiplier = 1; // Add this
    }
    public void setDifficulty(int level) {
        if (level >= 1 && level <= 3) {
            this.difficulty = level;
            int index = level - 1;

            this.spawnDelay = SPAWN_DELAYS[index];
            this.gameTime = GAME_TIMES[index];
            this.baseSpeed = BASE_SPEEDS[index];
            this.maxBalloons = MAX_BALLOONS[index];

            balloons.clear();
            popEffects.clear();
            missedBalloons = 0;
        }
    }

    public int getGameTime() {
        return gameTime;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public float getSpeedMultiplier() {
        switch (difficulty) {
            case 1: return 1.0f;
            case 2: return 1.5f;
            case 3: return 2.0f;
            default: return 1.0f;
        }
    }

    public int getMissedBalloons() {
        return missedBalloons;
    }

    public void resetMissedCount() {
        missedBalloons = 0;
    }

    private void createBalloonImages() {
        balloonImages = new Bitmap[balloonColors.length];
        int size = 100;

        for (int i = 0; i < balloonColors.length; i++) {
            balloonImages[i] = createBalloonBitmap(balloonColors[i], size);
        }
    }

    private Bitmap createBalloonBitmap(int color, int size) {
        Bitmap bitmap = Bitmap.createBitmap(size, (int)(size * 1.2f), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();

        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        canvas.drawOval(10, 10, size - 10, size - 10, p);

        p.setColor(darkenColor(color));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);
        canvas.drawOval(10, 10, size - 10, size - 10, p);

        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);
        canvas.drawRect(size/2 - 5, size - 15, size/2 + 5, size - 5, p);

        p.setColor(Color.rgb(101, 67, 33));
        p.setStrokeWidth(2);
        canvas.drawLine(size/2, size - 5, size/2, size * 1.2f, p);

        p.setColor(Color.argb(150, 255, 255, 255));
        canvas.drawCircle(size/3, size/3, size/8, p);

        return bitmap;
    }

    private int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }


    private int getMaxSpawnMultiplier() {
        switch(difficulty) {
            case 1: return 5;  // Easy: max 4 balloons
            case 2: return 7;  // Medium: max 6 balloons
            case 3: return 9;  // Hard: max 8 balloons
            default: return 6;
        }
    }


    public String getSpawnInfo() {
        int max = getMaxSpawnMultiplier();
        return "Spawning: " + spawnMultiplier + "/" + max +
                " balloons (Difficulty: " + difficulty + ")";
    }
    public void update() {
        long currentTime = System.currentTimeMillis();

        // CHECK: Increase spawn quantity every 10 seconds
        if (currentTime - lastQuantityIncreaseTime > QUANTITY_INCREASE_INTERVAL) {
            int maxMultiplier = getMaxSpawnMultiplier(); // Get max based on difficulty

            if (spawnMultiplier < maxMultiplier) { // Only increase if below max
                spawnMultiplier++;
                lastQuantityIncreaseTime = currentTime;
                Log.d(TAG, "Increased spawn multiplier to: " + spawnMultiplier +
                        "/" + maxMultiplier + " (Difficulty: " + difficulty + ")");
            }
        }

        // Spawn new balloons with multiplier
        if (currentTime - lastSpawnTime > spawnDelay && balloons.size() < maxBalloons) {
            // Spawn multiple balloons based on multiplier
            for (int i = 0; i < spawnMultiplier; i++) {
                if (balloons.size() < maxBalloons) {
                    spawnBalloon();
                }
            }
            lastSpawnTime = currentTime;
        }


        // Update balloon positions
        for (int i = balloons.size() - 1; i >= 0; i--) {
            Balloon balloon = balloons.get(i);

            float adjustedSpeed = balloon.baseSpeed * getSpeedMultiplier();
            balloon.y -= adjustedSpeed;

            // Check if balloon has reached the top of the screen
            float topEdge = balloon.y + balloon.height;

            // LOGGING: Track balloon position
            if (balloon.y < 100 && balloon.y > 80 && !balloon.missedMarked) {
                Log.d(TAG, "Balloon approaching top: y=" + balloon.y + ", topEdge=" + topEdge);
            }

            // Balloon is considered missed ONLY when it reaches the top
            if (topEdge <= 5 && !balloon.missedMarked) {
                Log.d(TAG, "Balloon reached top - marking as missed: ID=" + System.identityHashCode(balloon) +
                        ", Position: y=" + balloon.y);
                balloon.missedMarked = true;
                missedBalloons++;

                // Notify listener
                if (balloonEventListener != null) {
                    balloonEventListener.onBalloonMissed();
                }

                // If too many balloons missed, remove the oldest missed one
                if (missedBalloons >= maxMissedBeforeRemoval) {
                    removeOldestMissedBalloon();
                }
            }

            // Remove balloons ONLY when they are completely off-screen at the top
            // Changed condition: Remove when top edge is less than -50 (completely gone)
            if (topEdge < -50) {
                Log.d(TAG, "Removing balloon from top - ID=" + System.identityHashCode(balloon) +
                        ", topEdge=" + topEdge);
                balloons.remove(i);

                // Create fade effect only if it was missed
                if (balloon.missedMarked) {
                    createFadeEffect(balloon.x + balloon.width/2, 5, // Position at top
                            balloonColors[balloon.colorIndex], false);
                }
            }
        }

        // Update pop effects
        for (int i = popEffects.size() - 1; i >= 0; i--) {
            PopEffect effect = popEffects.get(i);
            effect.update();

            if (effect.isFinished()) {
                popEffects.remove(i);
            }
        }
    }

    private void removeOldestMissedBalloon() {
        for (int i = 0; i < balloons.size(); i++) {
            if (balloons.get(i).missedMarked) {
                Balloon balloon = balloons.remove(i);
                Log.d(TAG, "Force removing missed balloon - ID=" + System.identityHashCode(balloon));

                // Create fade effect at current position
                createFadeEffect(balloon.x + balloon.width/2, balloon.y + balloon.height/2,
                        balloonColors[balloon.colorIndex], true);

                missedBalloons = Math.max(0, missedBalloons - 1);

                // Notify listener
                if (balloonEventListener != null) {
                    balloonEventListener.onBalloonRemoved();
                }

                break;
            }
        }
    }

    public int popBalloon(float touchX, float touchY) {
        int points = 0;

        for (int i = balloons.size() - 1; i >= 0; i--) {
            Balloon balloon = balloons.get(i);

            // Check if touch is within balloon bounds
            if (touchX >= balloon.x && touchX <= balloon.x + balloon.width &&
                    touchY >= balloon.y && touchY <= balloon.y + balloon.height) {

                Log.d(TAG, "Balloon popped at position: (" + touchX + ", " + touchY +
                        "), Balloon bounds: x=" + balloon.x + ", y=" + balloon.y +
                        ", width=" + balloon.width + ", height=" + balloon.height);

                // Calculate points
                points = (int)(15 / balloon.scale);
                float speedRatio = balloon.baseSpeed / baseSpeed;
                if (speedRatio > 1.2f) {
                    points += (int)(points * 0.5f);
                }
                points += (difficulty - 1) * 5;

                // Create pop effect
                createPopEffect(balloon.x + balloon.width/2, balloon.y + balloon.height/2,
                        balloonColors[balloon.colorIndex]);

                balloons.remove(i);

                // Reset missed counter on successful pop
                if (missedBalloons > 0) {
                    missedBalloons = Math.max(0, missedBalloons - 1);
                }

                break;
            }
        }

        return points;
    }

    private void createPopEffect(float x, float y, int color) {
        popEffects.add(new PopEffect(x, y, color, true));
    }

    private void createFadeEffect(float x, float y, int color, boolean forcedRemoval) {
        popEffects.add(new PopEffect(x, y, color, forcedRemoval));
    }

    public void reset() {
        balloons.clear();
        popEffects.clear();
        missedBalloons = 0;
        lastSpawnTime = System.currentTimeMillis();
        lastQuantityIncreaseTime = System.currentTimeMillis(); // Add this
        spawnMultiplier = 1; // Reset to 1x
    }

    public int getBalloonCount() {
        return balloons.size();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw all balloons
        for (Balloon balloon : balloons) {
            if (balloonImages != null && balloon.colorIndex < balloonImages.length) {
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                        balloonImages[balloon.colorIndex],
                        (int)(balloonImages[balloon.colorIndex].getWidth() * balloon.scale),
                        (int)(balloonImages[balloon.colorIndex].getHeight() * balloon.scale),
                        true
                );

                // Apply transparency if marked as missed
                if (balloon.missedMarked) {
                    paint.setAlpha(150);
                } else {
                    paint.setAlpha(255);
                }

                canvas.drawBitmap(scaledBitmap, balloon.x, balloon.y, paint);
                paint.setAlpha(255);

                // Draw missed indicator
                if (balloon.missedMarked) {
                    drawMissedIndicator(canvas, balloon);
                }
            }
        }

        // Draw pop effects
        for (PopEffect effect : popEffects) {
            effect.draw(canvas);
        }

        // REMOVED: drawMissedCounter(canvas);
        // REMOVED: drawDifficultyIndicator(canvas);
    }

    private void drawMissedIndicator(Canvas canvas, Balloon balloon) {
        Paint indicatorPaint = new Paint();
        indicatorPaint.setColor(Color.RED);
        indicatorPaint.setStyle(Paint.Style.STROKE);
        indicatorPaint.setStrokeWidth(3);
        indicatorPaint.setAlpha(200);

        canvas.drawCircle(balloon.x + balloon.width/2,
                balloon.y + balloon.height/2,
                balloon.width/2 + 5, indicatorPaint);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(20);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAlpha(200);

        canvas.drawText("MISSED", balloon.x + balloon.width/2,
                balloon.y - 10, textPaint);
    }



    private void spawnBalloon() {
        int colorIndex = random.nextInt(balloonColors.length);
        int x = random.nextInt(Math.max(1, getWidth() - 100));
        int y = getHeight() + 50;

        float minSpeed = baseSpeed;
        float maxSpeed = baseSpeed + (difficulty * 0.8f);
        float speed = minSpeed + random.nextFloat() * (maxSpeed - minSpeed);

        float minSize, maxSize;
        switch (difficulty) {
            case 1:
                minSize = 0.7f;
                maxSize = 1.3f;
                break;
            case 2:
                minSize = 0.6f;
                maxSize = 1.1f;
                break;
            case 3:
                minSize = 0.5f;
                maxSize = 0.9f;
                break;
            default:
                minSize = 0.7f;
                maxSize = 1.2f;
        }
        float scale = minSize + random.nextFloat() * (maxSize - minSize);

        balloons.add(new Balloon(x, y, colorIndex, speed, scale));
    }

    // Balloon class - REMOVED lifeTime field
    private class Balloon {
        float x, y;
        int colorIndex;
        float baseSpeed;
        float scale;
        int width, height;
        boolean missedMarked = false;

        Balloon(float x, float y, int colorIndex, float speed, float scale) {
            this.x = x;
            this.y = y;
            this.colorIndex = colorIndex;
            this.baseSpeed = speed;
            this.scale = scale;

            if (balloonImages != null && colorIndex < balloonImages.length) {
                this.width = (int)(balloonImages[colorIndex].getWidth() * scale);
                this.height = (int)(balloonImages[colorIndex].getHeight() * scale);
            }
        }
    }

    // Pop Effect class (same as before)
    private class PopEffect {
        float x, y;
        int color;
        float radius;
        float maxRadius;
        int alpha;
        int duration;
        int currentFrame;
        boolean isPop;
        String text;

        PopEffect(float x, float y, int color, boolean isPop) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.isPop = isPop;
            this.radius = 5;
            this.maxRadius = isPop ? 40 : 30;
            this.alpha = 255;
            this.duration = isPop ? 20 : 30;
            this.currentFrame = 0;
            this.text = isPop ? "POP!" : "MISSED";
        }

        void update() {
            currentFrame++;
            if (currentFrame < duration) {
                float progress = (float) currentFrame / duration;
                radius = progress * maxRadius;
                alpha = (int)(255 * (1 - progress));
            }
        }

        boolean isFinished() {
            return currentFrame >= duration;
        }

        void draw(Canvas canvas) {
            if (isFinished()) return;

            Paint effectPaint = new Paint();

            if (isPop) {
                effectPaint.setColor(color);
                effectPaint.setAlpha(alpha / 2);
                effectPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(x, y, radius, effectPaint);

                effectPaint.setColor(Color.WHITE);
                effectPaint.setAlpha(alpha);
                effectPaint.setStyle(Paint.Style.STROKE);
                effectPaint.setStrokeWidth(3);
                canvas.drawCircle(x, y, radius, effectPaint);

                effectPaint.setStyle(Paint.Style.FILL);
                effectPaint.setColor(Color.argb(alpha, 255, 255, 255));
                canvas.drawCircle(x, y, radius / 2, effectPaint);

                effectPaint.setTextSize(30);
                effectPaint.setTextAlign(Paint.Align.CENTER);
                effectPaint.setColor(Color.WHITE);
                effectPaint.setAlpha(alpha);
                canvas.drawText(text, x, y - radius - 10, effectPaint);

                drawParticles(canvas, effectPaint);
            } else {
                effectPaint.setColor(Color.RED);
                effectPaint.setAlpha(alpha);
                effectPaint.setStyle(Paint.Style.STROKE);
                effectPaint.setStrokeWidth(4);
                canvas.drawCircle(x, y, radius, effectPaint);

                if (text.equals("MISSED")) {
                    effectPaint.setTextSize(25);
                    effectPaint.setTextAlign(Paint.Align.CENTER);
                    effectPaint.setStyle(Paint.Style.FILL);
                    canvas.drawText(text, x, y - radius - 5, effectPaint);
                }
            }
        }

        private void drawParticles(Canvas canvas, Paint paint) {
            int particleCount = 8;
            float particleRadius = 5;

            for (int i = 0; i < particleCount; i++) {
                float angle = (float) (2 * Math.PI * i / particleCount);
                float px = x + (float) (radius * Math.cos(angle));
                float py = y + (float) (radius * Math.sin(angle));

                paint.setColor(color);
                paint.setAlpha(alpha);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(px, py, particleRadius, paint);
            }
        }
    }
}