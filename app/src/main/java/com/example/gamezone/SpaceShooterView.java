package com.example.gamezone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;

public class SpaceShooterView extends View {

    // Game state
    private boolean gameRunning = false;
    private boolean gameOver = false;
    private boolean isPaused = false;

    // Player
    private float playerX, playerY;
    private float playerSpeed = 15;
    private int playerWidth = 80;
    private int playerHeight = 100;
    private int lives = 3;
    private int score = 0;
    private int level = 1;

    // Drawables for aliens and spaceship
    private Drawable[] alienDrawables = new Drawable[6];
    private Drawable spaceshipDrawable;

    // Player bullets
    private class Bullet {
        float x, y;
        float speed;
        boolean isPowerful;

        Bullet(float x, float y, boolean powerful) {
            this.x = x;
            this.y = y;
            this.speed = 25;
            this.isPowerful = powerful;
        }

        void update() {
            y -= speed;
        }

        RectF getRect() {
            float size = isPowerful ? 15 : 8;
            return new RectF(x - size, y - size, x + size, y + size);
        }
    }
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private long lastShotTime = 0;
    private long shotDelay = 300; // milliseconds
    private boolean powerShotActive = false;
    private long powerShotEndTime = 0;

    // Enemies (now using aliens)
    private class Enemy {
        float x, y;
        float speed;
        int type; // 0-5 for different alien types
        int health;
        int points;
        Drawable alienDrawable;

        Enemy(float x, float y, int type, Drawable drawable) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.alienDrawable = drawable;
            this.health = 1;

            switch (type) {
                case 0: // Basic alien 1
                    speed = 3 + level * 0.5f;
                    points = 10;
                    break;
                case 1: // Basic alien 2
                    speed = 3.5f + level * 0.5f;
                    points = 15;
                    break;
                case 2: // Fast alien
                    speed = 6 + level * 0.5f;
                    points = 20;
                    break;
                case 3: // Strong alien
                    speed = 2.5f + level * 0.3f;
                    health = 2;
                    points = 30;
                    break;
                case 4: // Stronger alien
                    speed = 2 + level * 0.3f;
                    health = 3;
                    points = 50;
                    break;
                case 5: // Boss alien
                    speed = 1.5f + level * 0.2f;
                    health = 5;
                    points = 100;
                    break;
            }
        }

        void update() {
            if (!isPaused) {
                y += speed;

                // Zigzag movement for fast enemies
                if (type == 2) {
                    x += (float) Math.sin(y * 0.1) * 2;
                }
            }
        }

        RectF getRect() {
            int size = 35; // Base size for all aliens
            if (type >= 3) size = 35; // Stronger aliens are bigger
            if (type == 5) size = 42; // Boss alien is largest
            return new RectF(x - size, y - size, x + size, y + size);
        }

        boolean hit() {
            health--;
            return health <= 0;
        }
    }
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private long lastEnemySpawn = 0;
    private long enemySpawnDelay = 1000; // milliseconds

    // Asteroids
    private class Asteroid {
        float x, y;
        float speed;
        float rotation;
        float rotationSpeed;
        int size;

        Asteroid(float x, float y, int size) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = 2 + random.nextFloat() * 3 + level * 0.3f;
            this.rotation = random.nextFloat() * 360;
            this.rotationSpeed = random.nextFloat() * 5 - 2.5f;
        }

        void update() {
            if (!isPaused) {
                y += speed;
                rotation += rotationSpeed;
            }
        }

        RectF getRect() {
            return new RectF(x - size, y - size, x + size, y + size);
        }
    }
    private ArrayList<Asteroid> asteroids = new ArrayList<>();
    private long lastAsteroidSpawn = 0;
    private long asteroidSpawnDelay = 2000; // milliseconds

    // Power-ups
    private class PowerUp {
        float x, y;
        float speed;
        int type; // 0=extra life, 1=power shot, 2=slow time, 3=shield, 4=score boost
        boolean active = true;

        PowerUp(float x, float y, int type) {
            this.x = x;
            this.y = y;
            this.speed = 3;
            this.type = type;
        }

        void update() {
            if (!isPaused) {
                y += speed;
            }
        }

        RectF getRect() {
            return new RectF(x - 25, y - 25, x + 25, y + 25);
        }
    }
    private ArrayList<PowerUp> powerups = new ArrayList<>();
    private long lastPowerupSpawn = 0;
    private long powerupSpawnDelay = 10000; // 10 seconds
    private boolean shieldActive = false;
    private long shieldEndTime = 0;
    private boolean slowTimeActive = false;
    private long slowTimeEndTime = 0;
    private boolean scoreBoostActive = false;
    private long scoreBoostEndTime = 0;

    // Hearts (Extra lives that fall from top)
    private class Heart {
        float x, y;
        float speed;
        boolean active = true;

        Heart(float x, float y) {
            this.x = x;
            this.y = y;
            this.speed = 4;
        }

        void update() {
            if (!isPaused) {
                y += speed;
            }
        }

        RectF getRect() {
            return new RectF(x - 20, y - 20, x + 20, y + 20);
        }
    }
    private ArrayList<Heart> hearts = new ArrayList<>();
    private long lastHeartSpawn = 0;
    private long heartSpawnDelay = 15000; // 15 seconds

    // Explosions
    private class Explosion {
        float x, y;
        float size;
        float maxSize;
        int duration = 20; // frames
        int currentFrame = 0;

        Explosion(float x, float y, float maxSize) {
            this.x = x;
            this.y = y;
            this.maxSize = maxSize;
            this.size = 5;
        }

        boolean update() {
            if (!isPaused) {
                currentFrame++;
                size = (float) currentFrame / duration * maxSize;
            }
            return currentFrame >= duration;
        }
    }
    private ArrayList<Explosion> explosions = new ArrayList<>();

    // Paints
    private Paint bulletPaint;
    private Paint asteroidPaint;
    private Paint powerupPaint;
    private Paint explosionPaint;
    private Paint textPaint;
    private Paint heartPaint;

    private Random random = new Random();

    // Difficulty
    private String difficulty = "normal";
    private float difficultyMultiplier = 1.0f;

    public SpaceShooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Load alien drawables
        alienDrawables[0] = getResources().getDrawable(R.drawable.vector_alien1);
        alienDrawables[1] = getResources().getDrawable(R.drawable.vector_alien2);
        alienDrawables[2] = getResources().getDrawable(R.drawable.vector_alien3);
        alienDrawables[3] = getResources().getDrawable(R.drawable.vector_alien4);
        alienDrawables[4] = getResources().getDrawable(R.drawable.vector_alien5);
        alienDrawables[5] = getResources().getDrawable(R.drawable.vector_alien6);

        // Load spaceship drawable
        spaceshipDrawable = getResources().getDrawable(R.drawable.vector_spaceship2);

        // Initialize paints
        bulletPaint = new Paint();
        bulletPaint.setColor(Color.YELLOW);
        bulletPaint.setStyle(Paint.Style.FILL);

        asteroidPaint = new Paint();
        asteroidPaint.setColor(Color.GRAY);
        asteroidPaint.setStyle(Paint.Style.FILL);

        powerupPaint = new Paint();
        powerupPaint.setStyle(Paint.Style.FILL);

        explosionPaint = new Paint();
        explosionPaint.setColor(Color.YELLOW);
        explosionPaint.setStyle(Paint.Style.FILL);
        explosionPaint.setAlpha(200);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.CENTER);

        heartPaint = new Paint();
        heartPaint.setColor(Color.RED);
        heartPaint.setStyle(Paint.Style.FILL);
    }

    public void pauseGame() {
        isPaused = true;
    }

    public void resumeGame() {
        isPaused = false;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
        switch (difficulty) {
            case "easy":
                difficultyMultiplier = 0.7f;
                lives = 5;
                break;
            case "normal":
                difficultyMultiplier = 1.0f;
                lives = 3;
                break;
            case "hard":
                difficultyMultiplier = 1.5f;
                lives = 2;
                break;
            case "expert":
                difficultyMultiplier = 2.0f;
                lives = 1;
                break;
        }
    }

    public void startGame() {
        gameRunning = true;
        gameOver = false;
        isPaused = false;
        score = 0;
        level = 1;

        // Reset collections
        bullets.clear();
        enemies.clear();
        asteroids.clear();
        powerups.clear();
        hearts.clear();
        explosions.clear();

        // Set initial player position
        playerX = getWidth() / 2f;
        playerY = getHeight() - 300;

        // Reset timers
        lastShotTime = System.currentTimeMillis();
        lastEnemySpawn = System.currentTimeMillis();
        lastAsteroidSpawn = System.currentTimeMillis();
        lastPowerupSpawn = System.currentTimeMillis();
        lastHeartSpawn = System.currentTimeMillis();

        // Reset power-ups
        powerShotActive = false;
        shieldActive = false;
        slowTimeActive = false;
        scoreBoostActive = false;
    }

    public void movePlayer(float direction) {
        if (!gameRunning || gameOver || isPaused) return;

        playerX += direction * playerSpeed;

        // Keep player on screen
        if (playerX < playerWidth) playerX = playerWidth;
        if (playerX > getWidth() - playerWidth) playerX = getWidth() - playerWidth;
    }

    public void shoot() {
        if (!gameRunning || gameOver || isPaused) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < shotDelay) return;

        // Create bullet
        boolean powerful = powerShotActive;
        bullets.add(new Bullet(playerX, playerY, powerful));

        // Double shot if powerful
        if (powerful) {
            bullets.add(new Bullet(playerX - 20, playerY, true));
            bullets.add(new Bullet(playerX + 20, playerY, true));
        }

        lastShotTime = currentTime;
    }

    public void update() {
        if (!gameRunning || gameOver || isPaused) return;

        long currentTime = System.currentTimeMillis();

        // Update bullets
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update();

            // Remove bullets that are off screen
            if (bullet.y < 0) {
                bullets.remove(i);
            }
        }

        // Spawn enemies
        if (currentTime - lastEnemySpawn > enemySpawnDelay / difficultyMultiplier) {
            spawnEnemy();
            lastEnemySpawn = currentTime;

            // Decrease spawn delay as level increases
            enemySpawnDelay = Math.max(300, 1000 - level * 50);
        }

        // Update enemies
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update();

            // Check collision with bullets
            boolean enemyDestroyed = false;
            for (int j = bullets.size() - 1; j >= 0; j--) {
                Bullet bullet = bullets.get(j);
                if (RectF.intersects(enemy.getRect(), bullet.getRect())) {
                    bullets.remove(j);
                    if (enemy.hit()) {
                        int points = enemy.points;
                        if (scoreBoostActive) points *= 2;
                        score += points;
                        enemyDestroyed = true;
                        createExplosion(enemy.x, enemy.y, 40);
                        break;
                    }
                }
            }

            // Remove enemies that are off screen or destroyed
            if (enemy.y > getHeight() + 100 || enemyDestroyed) {
                enemies.remove(i);
            }

            // Check collision with player
            if (!shieldActive && RectF.intersects(enemy.getRect(), getPlayerRect())) {
                lives--;
                enemies.remove(i);
                createExplosion(enemy.x, enemy.y, 50);

                if (lives <= 0) {
                    gameOver = true;
                    gameRunning = false;
                    createExplosion(playerX, playerY, 100);
                }
            }
        }

        // Spawn asteroids
        if (currentTime - lastAsteroidSpawn > asteroidSpawnDelay / difficultyMultiplier) {
            spawnAsteroid();
            lastAsteroidSpawn = currentTime;
        }

        // Update asteroids
        for (int i = asteroids.size() - 1; i >= 0; i--) {
            Asteroid asteroid = asteroids.get(i);
            asteroid.update();

            // Check collision with bullets
            for (int j = bullets.size() - 1; j >= 0; j--) {
                Bullet bullet = bullets.get(j);
                if (RectF.intersects(asteroid.getRect(), bullet.getRect())) {
                    bullets.remove(j);
                    int points = 5;
                    if (scoreBoostActive) points *= 2;
                    score += points;

                    // Split large asteroids
                    if (asteroid.size > 30) {
                        for (int k = 0; k < 2; k++) {
                            asteroids.add(new Asteroid(asteroid.x, asteroid.y, asteroid.size / 2));
                        }
                    }

                    asteroids.remove(i);
                    createExplosion(asteroid.x, asteroid.y, asteroid.size);
                    break;
                }
            }

            // Remove asteroids that are off screen
            if (asteroid.y > getHeight() + 100) {
                asteroids.remove(i);
            }

            // Check collision with player
            if (!shieldActive && RectF.intersects(asteroid.getRect(), getPlayerRect())) {
                lives--;
                asteroids.remove(i);
                createExplosion(asteroid.x, asteroid.y, asteroid.size * 2);

                if (lives <= 0) {
                    gameOver = true;
                    gameRunning = false;
                    createExplosion(playerX, playerY, 100);
                }
            }
        }

        // Spawn power-ups
        if (currentTime - lastPowerupSpawn > powerupSpawnDelay) {
            spawnPowerup();
            lastPowerupSpawn = currentTime;
        }

        // Update power-ups
        for (int i = powerups.size() - 1; i >= 0; i--) {
            PowerUp powerup = powerups.get(i);
            powerup.update();

            // Check collision with player
            if (RectF.intersects(powerup.getRect(), getPlayerRect())) {
                applyPowerup(powerup.type);
                powerups.remove(i);
                createExplosion(powerup.x, powerup.y, 30);
            }

            // Remove power-ups that are off screen
            if (powerup.y > getHeight() + 100) {
                powerups.remove(i);
            }
        }

        // Spawn hearts (extra lives)
        if (currentTime - lastHeartSpawn > heartSpawnDelay && lives < 5) {
            spawnHeart();
            lastHeartSpawn = currentTime;
        }

        // Update hearts
        for (int i = hearts.size() - 1; i >= 0; i--) {
            Heart heart = hearts.get(i);
            heart.update();

            // Check collision with player
            if (RectF.intersects(heart.getRect(), getPlayerRect())) {
                if (lives < 5) {
                    lives++;
                    createExplosion(heart.x, heart.y, 25);
                }
                hearts.remove(i);
            }

            // Remove hearts that are off screen
            if (heart.y > getHeight() + 100) {
                hearts.remove(i);
            }
        }

        // Update explosions
        for (int i = explosions.size() - 1; i >= 0; i--) {
            if (explosions.get(i).update()) {
                explosions.remove(i);
            }
        }

        // Update power-up timers
        if (powerShotActive && currentTime > powerShotEndTime) {
            powerShotActive = false;
        }
        if (shieldActive && currentTime > shieldEndTime) {
            shieldActive = false;
        }
        if (slowTimeActive && currentTime > slowTimeEndTime) {
            slowTimeActive = false;
            difficultyMultiplier = getDifficultyMultiplier();
        }
        if (scoreBoostActive && currentTime > scoreBoostEndTime) {
            scoreBoostActive = false;
        }

        // Level up
        if (score > level * 1000) {
            level++;
        }

        // Invalidate to trigger redraw
        invalidate();
    }

    private void spawnEnemy() {
        int type = random.nextInt(6);
        float x = random.nextInt(getWidth() - 100) + 50;
        enemies.add(new Enemy(x, -50, type, alienDrawables[type]));
    }

    private void spawnAsteroid() {
        int size = random.nextInt(30) + 20;
        float x = random.nextInt(getWidth() - size * 2) + size;
        asteroids.add(new Asteroid(x, -size, size));
    }

    private void spawnPowerup() {
        int type = random.nextInt(5);
        float x = random.nextInt(getWidth() - 100) + 50;
        powerups.add(new PowerUp(x, -50, type));
    }

    private void spawnHeart() {
        float x = random.nextInt(getWidth() - 100) + 50;
        hearts.add(new Heart(x, -50));
    }

    private void applyPowerup(int type) {
        switch (type) {
            case 0: // Extra life
                if (lives < 5) lives++;
                break;
            case 1: // Power shot
                powerShotActive = true;
                powerShotEndTime = System.currentTimeMillis() + 10000; // 10 seconds
                break;
            case 2: // Slow time
                slowTimeActive = true;
                slowTimeEndTime = System.currentTimeMillis() + 8000; // 8 seconds
                difficultyMultiplier = 0.5f;
                break;
            case 3: // Shield
                shieldActive = true;
                shieldEndTime = System.currentTimeMillis() + 15000; // 15 seconds
                break;
            case 4: // Score boost
                scoreBoostActive = true;
                scoreBoostEndTime = System.currentTimeMillis() + 12000; // 12 seconds
                break;
        }
    }

    private float getDifficultyMultiplier() {
        switch (difficulty) {
            case "easy": return 0.7f;
            case "normal": return 1.0f;
            case "hard": return 1.5f;
            case "expert": return 2.0f;
            default: return 1.0f;
        }
    }

    private void createExplosion(float x, float y, float size) {
        explosions.add(new Explosion(x, y, size));
    }

    private RectF getPlayerRect() {
        return new RectF(playerX - playerWidth/2, playerY - playerHeight/2,
                playerX + playerWidth/2, playerY + playerHeight/2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw stars background
        drawStars(canvas);

        // Draw explosions
        for (Explosion explosion : explosions) {
            explosionPaint.setAlpha(200 - explosion.currentFrame * 10);
            canvas.drawCircle(explosion.x, explosion.y, explosion.size, explosionPaint);
        }

        // Draw player spaceship
        if (!gameOver) {
            // Draw shield if active
            if (shieldActive) {
                Paint shieldPaint = new Paint();
                shieldPaint.setColor(Color.argb(100, 0, 255, 255));
                shieldPaint.setStyle(Paint.Style.STROKE);
                shieldPaint.setStrokeWidth(5);
                canvas.drawCircle(playerX, playerY, playerWidth, shieldPaint);
            }

            // Draw spaceship using drawable
            if (spaceshipDrawable != null) {
                spaceshipDrawable.setBounds(
                        (int)(playerX - playerWidth/2),
                        (int)(playerY - playerHeight/2),
                        (int)(playerX + playerWidth/2),
                        (int)(playerY + playerHeight/2)
                );
                spaceshipDrawable.draw(canvas);
            }
        }

        // Draw bullets
        for (Bullet bullet : bullets) {
            if (bullet.isPowerful) {
                bulletPaint.setColor(Color.RED);
                canvas.drawCircle(bullet.x, bullet.y, 15, bulletPaint);
                bulletPaint.setColor(Color.YELLOW);
                canvas.drawCircle(bullet.x, bullet.y, 10, bulletPaint);
            } else {
                canvas.drawCircle(bullet.x, bullet.y, 8, bulletPaint);
            }
        }

        // Draw aliens
        for (Enemy enemy : enemies) {
            // Draw alien using drawable
            if (enemy.alienDrawable != null) {
                RectF rect = enemy.getRect();
                enemy.alienDrawable.setBounds(
                        (int)rect.left,
                        (int)rect.top,
                        (int)rect.right,
                        (int)rect.bottom
                );
                enemy.alienDrawable.draw(canvas);
            }

            // Draw health for strong aliens
            if (enemy.type >= 3) {
                Paint healthPaint = new Paint();
                healthPaint.setColor(Color.GREEN);
                float healthWidth = enemy.getRect().width() * enemy.health / (enemy.type == 5 ? 5f : 3f);
                canvas.drawRect(enemy.x - healthWidth/2, enemy.y - 50,
                        enemy.x + healthWidth/2, enemy.y - 45, healthPaint);
            }
        }


        for (Asteroid asteroid : asteroids) {
            Drawable asteroidDrawable = getResources().getDrawable(R.drawable.vector_asteroid);
            int drawSize = asteroid.size * 1;
            asteroidDrawable.setBounds(
                    (int)(asteroid.x - drawSize),
                    (int)(asteroid.y - drawSize),
                    (int)(asteroid.x + drawSize),
                    (int)(asteroid.y + drawSize)
            );

            canvas.save();
            canvas.rotate(asteroid.rotation, asteroid.x, asteroid.y);
            asteroidDrawable.draw(canvas);
            canvas.restore();
        }



        // Draw power-ups
        for (PowerUp powerup : powerups) {
            switch (powerup.type) {
                case 0: powerupPaint.setColor(Color.GREEN); break;    // Life
                case 1: powerupPaint.setColor(Color.YELLOW); break;   // Power shot
                case 2: powerupPaint.setColor(Color.BLUE); break;     // Slow time
                case 3: powerupPaint.setColor(Color.CYAN); break;     // Shield
                case 4: powerupPaint.setColor(Color.MAGENTA); break;  // Score boost
            }
            canvas.drawCircle(powerup.x, powerup.y, 25, powerupPaint);

            // Draw symbol
            textPaint.setTextSize(35);
            String symbol = "";
            switch (powerup.type) {
                case 0: symbol = "❤"; break;
                case 1: symbol = "⚡"; break;
                case 2: symbol = "⏱️"; break;
                case 3: symbol = "🛡️"; break;
                case 4: symbol = "💰"; break;
            }
            canvas.drawText(symbol, powerup.x, powerup.y + 10, textPaint);
        }

        // Draw hearts
        for (Heart heart : hearts) {
            // Draw heart shape
            drawHeart(canvas, heart.x, heart.y, 20);

            // Draw glow effect
            Paint glowPaint = new Paint();
            glowPaint.setColor(Color.argb(100, 255, 0, 0));
            canvas.drawCircle(heart.x, heart.y, 25, glowPaint);
        }

        // Draw game over text
        if (gameOver) {
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(80);
            canvas.drawText("GAME OVER", getWidth() / 2f, getHeight() / 2f, textPaint);

            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(40);
            canvas.drawText("Score: " + score, getWidth() / 2f, getHeight() / 2f + 60, textPaint);
        }

        // Draw paused text
        if (isPaused && gameRunning && !gameOver) {
            textPaint.setColor(Color.YELLOW);
            textPaint.setTextSize(60);
            canvas.drawText("PAUSED", getWidth() / 2f, getHeight() / 2f, textPaint);
        }

        // Draw paused text
 if (isPaused && gameRunning && !gameOver) {
     textPaint.setColor(Color.YELLOW);
     textPaint.setTextSize(60);
     canvas.drawText("PAUSED", getWidth() / 2f, getHeight() / 2f, textPaint);
 }
    }

    private void drawHeart(Canvas canvas, float x, float y, float size) {
        Path path = new Path();

        // Starting point
        path.moveTo(x, y + size/3);

        // Left top curve
        path.cubicTo(x - size, y - size/2,
                x - size/2, y - size,
                x, y - size/3);

        // Right top curve
        path.cubicTo(x + size/2, y - size,
                x + size, y - size/2,
                x, y + size/3);

        path.close();

        canvas.drawPath(path, heartPaint);
    }

    private void drawStars(Canvas canvas) {
        Paint starPaint = new Paint();
        starPaint.setColor(Color.WHITE);

        // Draw some random stars
        for (int i = 0; i < 50; i++) {
            float x = random.nextInt(getWidth());
            float y = random.nextInt(getHeight());
            float size = random.nextFloat() * 3;
            canvas.drawCircle(x, y, size, starPaint);
        }
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public int getLevel() {
        return level;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getActivePowerup() {
        if (powerShotActive) return "POWER SHOT";
        if (shieldActive) return "SHIELD";
        if (slowTimeActive) return "SLOW TIME";
        if (scoreBoostActive) return "SCORE BOOST";
        return null;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!gameRunning) {
            playerX = w / 2f;
            // Try different values:
            playerY = h - 300; // Test with 200 pixels from bottom
        }

    }
}