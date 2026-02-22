package com.example.gamezone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;

public class SnakeGameView extends View {

    // Game constants
    public static final int DIRECTION_UP = 1;
    public static final int DIRECTION_DOWN = 2;
    public static final int DIRECTION_LEFT = 3;
    public static final int DIRECTION_RIGHT = 4;

    private static final int GRID_SIZE = 20;
    private static final int INITIAL_SNAKE_LENGTH = 5;

    // Game variables
    private ArrayList<Point> snake;
    private Point food;
    private int currentDirection;
    private int nextDirection;
    private Random random;
    private boolean foodEaten = false;

    // Paint objects
    private Paint snakePaint;
    private Paint snakeHeadPaint;
    private Paint snakeEyePaint;
    private Paint foodPaint;
    private Paint gridPaint;
    private Paint borderPaint;
    private Paint shadowPaint;

    // Theme variables
    // Theme variables
    private int themeIndex = 0;
    private int[][] themes = {
            // Theme 0: Classic Green Snake, Red Apple
            {Color.rgb(76, 175, 80), Color.rgb(56, 142, 60), Color.rgb(244, 67, 54), Color.YELLOW, Color.BLACK},

            // Theme 1: Yellow Snake, Purple Food
            {Color.rgb(255, 235, 59), Color.rgb(255, 193, 7), Color.rgb(156, 39, 176), Color.CYAN, Color.DKGRAY},

            // Theme 2: Pink Snake, Blue Food
            {Color.rgb(233, 30, 99), Color.rgb(194, 24, 91), Color.rgb(33, 150, 243), Color.WHITE, Color.DKGRAY},

            // Theme 3: Orange Snake, Green Food
            {Color.rgb(255, 152, 0), Color.rgb(245, 124, 0), Color.rgb(76, 175, 80), Color.YELLOW, Color.rgb(30, 30, 30)},

            // Theme 4: Red Snake, Yellow Food
            {Color.rgb(244, 67, 54), Color.rgb(211, 47, 47), Color.rgb(255, 235, 59), Color.WHITE, Color.rgb(40, 40, 40)},

            // Theme 5: Blue Snake, Orange Food
            {Color.rgb(33, 150, 243), Color.rgb(25, 118, 210), Color.rgb(255, 152, 0), Color.CYAN, Color.DKGRAY},

            // Theme 6: Purple Snake, Pink Food
            {Color.rgb(156, 39, 176), Color.rgb(123, 31, 162), Color.rgb(233, 30, 99), Color.GREEN, Color.DKGRAY},

            // Theme 7: Rainbow Theme (Multi-color snake)
            {Color.rgb(76, 175, 80), Color.rgb(156, 39, 176), Color.rgb(255, 87, 34), Color.WHITE, Color.rgb(30, 30, 30)}
    };

    // Segment radius for rounded corners
    private float segmentRadius;

    public SnakeGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        random = new Random();

        // Initialize paints
        snakePaint = new Paint();
        snakeHeadPaint = new Paint();
        snakeEyePaint = new Paint();
        foodPaint = new Paint();
        gridPaint = new Paint();
        borderPaint = new Paint();
        shadowPaint = new Paint();

        // Set paint styles
        snakePaint.setStyle(Paint.Style.FILL);
        snakePaint.setAntiAlias(true);

        snakeHeadPaint.setStyle(Paint.Style.FILL);
        snakeHeadPaint.setAntiAlias(true);

        snakeEyePaint.setStyle(Paint.Style.FILL);
        snakeEyePaint.setColor(Color.WHITE);
        snakeEyePaint.setAntiAlias(true);

        foodPaint.setStyle(Paint.Style.FILL);
        foodPaint.setAntiAlias(true);

        gridPaint.setColor(Color.rgb(50, 50, 50));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1);
        gridPaint.setAntiAlias(true);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        borderPaint.setAntiAlias(true);

        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.argb(100, 0, 0, 0));

        resetGame();
    }

    public void resetGame() {
        // Initialize snake
        snake = new ArrayList<>();
        for (int i = 0; i < INITIAL_SNAKE_LENGTH; i++) {
            snake.add(new Point(10 - i, 10));
        }

        // Initial direction
        currentDirection = DIRECTION_RIGHT;
        nextDirection = DIRECTION_RIGHT;

        // Generate first food
        generateFood();

        foodEaten = false;
        applyTheme();
    }

    // Apply current theme colors
    // Apply current theme colors
    private void applyTheme() {
        int[] theme = themes[themeIndex];

        // For rainbow theme (theme 7), snake body will change color per segment
        if (themeIndex != 7) {
            snakePaint.setColor(theme[0]);        // Snake body color
        }

        snakeHeadPaint.setColor(theme[1]);    // Snake head color
        foodPaint.setColor(theme[2]);         // Food color
        borderPaint.setColor(theme[3]);       // Border color
        gridPaint.setColor(theme[4]);         // Grid color
    }

    // Change to next theme
    public void changeTheme() {
        themeIndex = (themeIndex + 1) % themes.length;
        applyTheme();
        invalidate(); // Redraw with new colors
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int gridWidth = width / GRID_SIZE;
        int gridHeight = height / GRID_SIZE;
        segmentRadius = Math.min(gridWidth, gridHeight) * 0.4f;

        // Draw border
        canvas.drawRect(0, 0, width, height, borderPaint);

        // Draw snake segments with smooth connections
        for (int i = snake.size() - 1; i >= 0; i--) {
            Point point = snake.get(i);

            float left = point.x * gridWidth;
            float top = point.y * gridHeight;
            float right = left + gridWidth;
            float bottom = top + gridHeight;

            // Center of the segment
            float centerX = (left + right) / 2f;
            float centerY = (top + bottom) / 2f;

            // For rainbow theme, set different colors for each segment
            if (themeIndex == 7 && i > 0) {
                // Rainbow effect: cycle through colors based on segment position
                int[] rainbowColors = {
                        Color.rgb(255, 0, 0),     // Red
                        Color.rgb(255, 127, 0),   // Orange
                        Color.rgb(255, 255, 0),   // Yellow
                        Color.rgb(0, 255, 0),     // Green
                        Color.rgb(0, 0, 255),     // Blue
                        Color.rgb(75, 0, 130),    // Indigo
                        Color.rgb(148, 0, 211)    // Violet
                };
                snakePaint.setColor(rainbowColors[i % rainbowColors.length]);
            }

            if (i == 0) {
                // Draw snake head with square shape
                drawSnakeHead(canvas, centerX, centerY, gridWidth, gridHeight);
            } else {
                // Draw snake body segment as perfect square (matching head)
                float bodySize = Math.min(gridWidth, gridHeight) * 0.85f;
                float bodyLeft = centerX - bodySize / 2;
                float bodyTop = centerY - bodySize / 2;
                float bodyRight = bodyLeft + bodySize;
                float bodyBottom = bodyTop + bodySize;

                RectF rect = new RectF(bodyLeft, bodyTop, bodyRight, bodyBottom);
                canvas.drawRect(rect, snakePaint); // Changed to drawRect for square

                // Add shadow effect (square)
                RectF shadowRect = new RectF(bodyLeft + 2, bodyTop + 2, bodyRight + 2, bodyBottom + 2);
                canvas.drawRect(shadowRect, shadowPaint); // Square shadow

                // Draw border around segment (square)
                canvas.drawRect(rect, gridPaint);
            }
        }

        // Draw food with shine effect
        if (food != null) {
            float left = food.x * gridWidth;
            float top = food.y * gridHeight;
            float right = left + gridWidth;
            float bottom = top + gridHeight;

            float centerX = (left + right) / 2f;
            float centerY = (top + bottom) / 2f;
            float radius = Math.min(gridWidth, gridHeight) * 0.35f;

            // Draw food shadow
            canvas.drawCircle(centerX + 2, centerY + 2, radius, shadowPaint);

            // Draw main food circle
            canvas.drawCircle(centerX, centerY, radius, foodPaint);

            // Draw shine effect on food
            foodPaint.setAlpha(128);
            canvas.drawCircle(centerX - radius/3, centerY - radius/3, radius/4, snakeEyePaint);
            foodPaint.setAlpha(255);
        }
    }

    private void drawSnakeHead(Canvas canvas, float centerX, float centerY, float gridWidth, float gridHeight) {
        float headSize = Math.min(gridWidth, gridHeight) * 0.85f; // 85% of grid size

        // Calculate position for square head
        float left = centerX - headSize/2;
        float top = centerY - headSize/2;
        float right = left + headSize;
        float bottom = top + headSize;

        // Draw head shadow (slightly offset)
        RectF shadowRect = new RectF(left + 3, top + 3, right + 3, bottom + 3);
        canvas.drawRect(shadowRect, shadowPaint); // Square shadow

        // Draw perfect square head
        RectF headRect = new RectF(left, top, right, bottom);
        canvas.drawRect(headRect, snakeHeadPaint); // Perfect square

        // Draw border around head (square)
        canvas.drawRect(headRect, gridPaint);

        // Draw eyes based on direction
        float eyeSize = headSize * 0.15f; // Eye size
        float eyeOffsetX = 0;
        float eyeOffsetY = 0;

        switch (currentDirection) {
            case DIRECTION_RIGHT:
                eyeOffsetX = headSize * 0.2f;
                eyeOffsetY = -headSize * 0.2f;
                // Right eye (facing right)
                canvas.drawCircle(centerX + eyeOffsetX, centerY + eyeOffsetY, eyeSize, snakeEyePaint);
                canvas.drawCircle(centerX + eyeOffsetX, centerY - eyeOffsetY, eyeSize, snakeEyePaint);
                break;
            case DIRECTION_LEFT:
                eyeOffsetX = -headSize * 0.2f;
                eyeOffsetY = -headSize * 0.2f;
                // Left eye (facing left)
                canvas.drawCircle(centerX + eyeOffsetX, centerY + eyeOffsetY, eyeSize, snakeEyePaint);
                canvas.drawCircle(centerX + eyeOffsetX, centerY - eyeOffsetY, eyeSize, snakeEyePaint);
                break;
            case DIRECTION_UP:
                eyeOffsetX = -headSize * 0.2f;
                eyeOffsetY = -headSize * 0.2f;
                // Up eyes
                canvas.drawCircle(centerX + eyeOffsetX, centerY + eyeOffsetY, eyeSize, snakeEyePaint);
                canvas.drawCircle(centerX - eyeOffsetX, centerY + eyeOffsetY, eyeSize, snakeEyePaint);
                break;
            case DIRECTION_DOWN:
                eyeOffsetX = -headSize * 0.2f;
                eyeOffsetY = headSize * 0.2f;
                // Down eyes
                canvas.drawCircle(centerX + eyeOffsetX, centerY + eyeOffsetY, eyeSize, snakeEyePaint);
                canvas.drawCircle(centerX - eyeOffsetX, centerY + eyeOffsetY, eyeSize, snakeEyePaint);
                break;
        }

        // Draw eye pupils
        snakeEyePaint.setColor(Color.BLACK);
        for (int i = 0; i < 2; i++) {
            float pupilX = 0;
            float pupilY = 0;

            switch (currentDirection) {
                case DIRECTION_RIGHT:
                    pupilX = centerX + eyeOffsetX + eyeSize/3;
                    pupilY = (i == 0) ? centerY + eyeOffsetY : centerY - eyeOffsetY;
                    break;
                case DIRECTION_LEFT:
                    pupilX = centerX + eyeOffsetX - eyeSize/3;
                    pupilY = (i == 0) ? centerY + eyeOffsetY : centerY - eyeOffsetY;
                    break;
                case DIRECTION_UP:
                    pupilX = (i == 0) ? centerX + eyeOffsetX : centerX - eyeOffsetX;
                    pupilY = centerY + eyeOffsetY - eyeSize/3;
                    break;
                case DIRECTION_DOWN:
                    pupilX = (i == 0) ? centerX + eyeOffsetX : centerX - eyeOffsetX;
                    pupilY = centerY + eyeOffsetY + eyeSize/3;
                    break;
            }
            canvas.drawCircle(pupilX, pupilY, eyeSize/2, snakeEyePaint);
        }
        snakeEyePaint.setColor(Color.WHITE);

        // Draw mouth (simple line or curve)
        Paint mouthPaint = new Paint();
        mouthPaint.setColor(Color.BLACK);
        mouthPaint.setStrokeWidth(3);
        mouthPaint.setStyle(Paint.Style.STROKE);

        float mouthY = centerY + headSize * 0.1f;
        if (currentDirection == DIRECTION_RIGHT || currentDirection == DIRECTION_LEFT) {
            // Horizontal mouth for left/right
            canvas.drawLine(centerX - headSize * 0.15f, mouthY,
                    centerX + headSize * 0.15f, mouthY, mouthPaint);
        } else {
            // Smile mouth for up/down
            RectF mouthRect = new RectF(
                    centerX - headSize * 0.15f,
                    mouthY - headSize * 0.05f,
                    centerX + headSize * 0.15f,
                    mouthY + headSize * 0.05f
            );
            canvas.drawArc(mouthRect, 0, 180, false, mouthPaint);
        }
    }


    public boolean update() {
        // Update direction
        currentDirection = nextDirection;

        // Get head position
        Point head = snake.get(0);
        Point newHead = new Point(head.x, head.y);

        // Move head based on direction
        switch (currentDirection) {
            case DIRECTION_UP:
                newHead.y--;
                break;
            case DIRECTION_DOWN:
                newHead.y++;
                break;
            case DIRECTION_LEFT:
                newHead.x--;
                break;
            case DIRECTION_RIGHT:
                newHead.x++;
                break;
        }

        // Check collision with walls
        if (newHead.x < 0 || newHead.x >= GRID_SIZE ||
                newHead.y < 0 || newHead.y >= GRID_SIZE) {
            return false; // Game over
        }

        // Check collision with self
        for (int i = 1; i < snake.size(); i++) {
            Point segment = snake.get(i);
            if (segment.equals(newHead)) {
                return false; // Game over
            }
        }

        // Move snake
        snake.add(0, newHead);

        // Check if food is eaten
        if (newHead.equals(food)) {
            foodEaten = true;
            generateFood();
        } else {
            // Remove tail if no food eaten
            snake.remove(snake.size() - 1);
            foodEaten = false;
        }

        return true;
    }

    private void generateFood() {
        boolean validPosition;
        do {
            validPosition = true;
            food = new Point(
                    random.nextInt(GRID_SIZE),
                    random.nextInt(GRID_SIZE)
            );

            // Check that food doesn't appear on snake
            for (Point segment : snake) {
                if (segment.equals(food)) {
                    validPosition = false;
                    break;
                }
            }
        } while (!validPosition);
    }

    public boolean isSnakeAtInitialPosition() {
        if (snake == null || snake.size() != INITIAL_SNAKE_LENGTH) {
            return false;
        }
        // Check if snake is at starting position (10,10) etc.
        Point head = snake.get(0);
        return head.x == 10 && head.y == 10;
    }

    public void changeDirection(int newDirection) {
        // Prevent 180-degree turns
        if ((currentDirection == DIRECTION_UP && newDirection != DIRECTION_DOWN) ||
                (currentDirection == DIRECTION_DOWN && newDirection != DIRECTION_UP) ||
                (currentDirection == DIRECTION_LEFT && newDirection != DIRECTION_RIGHT) ||
                (currentDirection == DIRECTION_RIGHT && newDirection != DIRECTION_LEFT)) {
            nextDirection = newDirection;
        }
    }

    public boolean isFoodEaten() {
        return foodEaten;
    }

    // Point class for snake segments and food
    private class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        boolean equals(Point other) {
            return this.x == other.x && this.y == other.y;
        }
    }
}