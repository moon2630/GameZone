package com.example.gamezone;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class snake_game extends AppCompatActivity implements SensorEventListener, View.OnTouchListener {

    private SnakeGameView snakeGameView;
    private TextView tvScore, tvHighScore, tvGameStatus;
    // Add this with other View declarations
    private RelativeLayout swipeControlArea;
    private TextView btnStart, btnUp, btnDown, btnLeft, btnRight, btnTheme, btnControlToggle;
    private Handler handler = new Handler();
    private static final long UPDATE_DELAY = 150;
    private int currentScore = 0;
    private int highScore = 0;


    private boolean gameRunning = false;
    private boolean useTiltControls = false;
    private SharedPreferences preferences;

    // Tilt control variables
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float lastX, lastY;
    private static final float TILT_THRESHOLD = 1.5f;
    private static final int TILT_DEBOUNCE = 5;
    private int tiltDebounceCounter = 0;

    // Touch control variables
    private float touchStartX, touchStartY;
    private static final int MIN_SWIPE_DISTANCE = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake_game);



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


        preferences = getSharedPreferences("SnakeGamePrefs", MODE_PRIVATE);
        highScore = preferences.getInt("highScore", 0);

        // Initialize sensor manager for tilt controls
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        initializeViews();
        setupButtonListeners();
        setupTouchControls();
        updateHighScoreDisplay();
    }


    private void initializeViews() {
        snakeGameView = findViewById(R.id.snakeView);
        tvScore = findViewById(R.id.tvScore);
        tvHighScore = findViewById(R.id.tvHighScore);
        tvGameStatus = findViewById(R.id.tvGameStatus);
        btnStart = findViewById(R.id.btnStart);
        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnTheme = findViewById(R.id.btnTheme);
        swipeControlArea = findViewById(R.id.swipeControlArea); // Add this line

        btnControlToggle = findViewById(R.id.btnControlToggle);
    }

    private void setupTouchControls() {
        // Set touch listener on the swipe control area for swipe controls
        if (swipeControlArea != null) {
            swipeControlArea.setOnTouchListener(this);
        }
    }
    private void setupButtonListeners() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameRunning) {
                    // Check if game is over (by checking button text)
                    if (btnStart.getText().equals("Start Game")) {
                        // New game - reset everything
                        snakeGameView.resetGame();
                        currentScore = 0;
                        updateScoreDisplay();
                    }
                    startGame();
                } else {
                    pauseGame();
                }
            }
        });

        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameRunning && !useTiltControls) {
                    snakeGameView.changeDirection(SnakeGameView.DIRECTION_UP);
                }
            }
        });

        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameRunning && !useTiltControls) {
                    snakeGameView.changeDirection(SnakeGameView.DIRECTION_DOWN);
                }
            }
        });

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameRunning && !useTiltControls) {
                    snakeGameView.changeDirection(SnakeGameView.DIRECTION_LEFT);
                }
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameRunning && !useTiltControls) {
                    snakeGameView.changeDirection(SnakeGameView.DIRECTION_RIGHT);
                }
            }
        });

        btnTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snakeGameView.changeTheme();
                Toast.makeText(snake_game.this, "Theme Changed!", Toast.LENGTH_SHORT).show();
            }
        });

        btnControlToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTiltControls();
            }
        });
    }

    private void toggleTiltControls() {
        useTiltControls = !useTiltControls;

        if (useTiltControls) {
            btnControlToggle.setText("Tilt: ON");

            // Hide direction buttons when tilt is on
            if (btnUp != null) btnUp.setVisibility(View.GONE);
            if (btnDown != null) btnDown.setVisibility(View.GONE);
            if (btnLeft != null) btnLeft.setVisibility(View.GONE);
            if (btnRight != null) btnRight.setVisibility(View.GONE);

            // Show swipe control area
            if (swipeControlArea != null) {
                swipeControlArea.setVisibility(View.VISIBLE);
            }

            // Start tilt sensor
            if (sensorManager != null && accelerometer != null) {
                sensorManager.registerListener(this, accelerometer,
                        SensorManager.SENSOR_DELAY_GAME);
            }

            Toast.makeText(this, "Tilt Controls Enabled\nSwipe bottom area for movement",
                    Toast.LENGTH_LONG).show();
        } else {
            btnControlToggle.setText("Tilt: OFF");

            // Show direction buttons when tilt is off
            if (btnUp != null) btnUp.setVisibility(View.VISIBLE);
            if (btnDown != null) btnDown.setVisibility(View.VISIBLE);
            if (btnLeft != null) btnLeft.setVisibility(View.VISIBLE);
            if (btnRight != null) btnRight.setVisibility(View.VISIBLE);

            // Hide swipe control area
            if (swipeControlArea != null) {
                swipeControlArea.setVisibility(View.GONE);
            }

            // Stop tilt sensor
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }

            Toast.makeText(this, "Button Controls Enabled",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private void startGame() {
        gameRunning = true;
        btnStart.setText("Pause");
        tvGameStatus.setVisibility(View.INVISIBLE);

        // Start game loop
        handler.postDelayed(gameRunnable, UPDATE_DELAY);

        // Register tilt sensor if enabled
        if (useTiltControls && sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    private void pauseGame() {
        gameRunning = false;
        btnStart.setText("Resume");
        tvGameStatus.setText("Game Paused");
        tvGameStatus.setVisibility(View.VISIBLE);
        handler.removeCallbacks(gameRunnable);

        // Store the current state - no need to modify anything else
        // The snakeGameView retains its complete state

        // Unregister sensor if tilt is on
        if (useTiltControls && sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }



    private void gameOver() {
        gameRunning = false;
        btnStart.setText("Start Game");  // This indicates it's a new game
        tvGameStatus.setText("Game Over! Score: " + currentScore);
        tvGameStatus.setVisibility(View.VISIBLE);
        handler.removeCallbacks(gameRunnable);

        // Unregister sensor if tilt is on
        if (useTiltControls && sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        // Save high score if current score is higher
        if (currentScore > highScore) {
            highScore = currentScore;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("highScore", highScore);
            editor.apply();
            updateHighScoreDisplay();
        }
    }

    private Runnable gameRunnable = new Runnable() {
        @Override
        public void run() {
            if (gameRunning) {
                // Update game state
                boolean gameContinues = snakeGameView.update();

                // Check if snake ate food
                if (snakeGameView.isFoodEaten()) {
                    currentScore += 10;
                    updateScoreDisplay();
                }

                // Check game over
                if (!gameContinues) {
                    gameOver();
                    return;
                }

                // Redraw the view
                snakeGameView.invalidate();

                // Schedule next update
                handler.postDelayed(this, UPDATE_DELAY);
            }
        }
    };

    // Touch controls for swiping
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Check if game is running AND we're touching the swipe control area
        if (!gameRunning || v.getId() != R.id.swipeControlArea) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                return true;

            case MotionEvent.ACTION_UP:
                float touchEndX = event.getX();
                float touchEndY = event.getY();

                float deltaX = touchEndX - touchStartX;
                float deltaY = touchEndY - touchStartY;

                // Calculate swipe distance
                float distanceX = Math.abs(deltaX);
                float distanceY = Math.abs(deltaY);

                // Check if it's a valid swipe
                if (Math.max(distanceX, distanceY) < MIN_SWIPE_DISTANCE) {
                    return false; // Too short, not a swipe
                }

                // Determine swipe direction
                if (distanceX > distanceY) {
                    // Horizontal swipe
                    if (deltaX > 0) {
                        // Swipe RIGHT
                        snakeGameView.changeDirection(SnakeGameView.DIRECTION_RIGHT);
                    } else {
                        // Swipe LEFT
                        snakeGameView.changeDirection(SnakeGameView.DIRECTION_LEFT);
                    }
                } else {
                    // Vertical swipe
                    if (deltaY > 0) {
                        // Swipe DOWN
                        snakeGameView.changeDirection(SnakeGameView.DIRECTION_DOWN);
                    } else {
                        // Swipe UP
                        snakeGameView.changeDirection(SnakeGameView.DIRECTION_UP);
                    }
                }
                return true;
        }
        return false;
    }

    // SensorEventListener methods for tilt controls
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!gameRunning || !useTiltControls || snakeGameView == null) return;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];

            // Debounce to prevent too frequent direction changes
            if (tiltDebounceCounter > 0) {
                tiltDebounceCounter--;
                lastX = x;
                lastY = y;
                return;
            }

            // Calculate tilt intensity
            float deltaX = Math.abs(x - lastX);
            float deltaY = Math.abs(y - lastY);

            // Check if tilt is significant enough
            if (deltaX > TILT_THRESHOLD || deltaY > TILT_THRESHOLD) {
                // Determine which axis has more movement
                if (Math.abs(x) > Math.abs(y)) {
                    // Horizontal tilt
                    if (x > 1.0f) {
                        snakeGameView.changeDirection(SnakeGameView.DIRECTION_LEFT);
                        tiltDebounceCounter = TILT_DEBOUNCE;
                    } else if (x < -1.0f) {
                        snakeGameView.changeDirection(SnakeGameView.DIRECTION_RIGHT);
                        tiltDebounceCounter = TILT_DEBOUNCE;
                    }
                } else {
                    // Vertical tilt
                    if (y > 1.0f) {
                        snakeGameView.changeDirection(SnakeGameView.DIRECTION_UP);
                        tiltDebounceCounter = TILT_DEBOUNCE;
                    } else if (y < -1.0f) {
                        snakeGameView.changeDirection(SnakeGameView.DIRECTION_DOWN);
                        tiltDebounceCounter = TILT_DEBOUNCE;
                    }
                }
            }

            lastX = x;
            lastY = y;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    private void updateScoreDisplay() {
        tvScore.setText("Score: " + currentScore);
    }

    private void updateHighScoreDisplay() {
        tvHighScore.setText("High Score: " + highScore);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameRunning) {
            pauseGame();
        }
        // Always unregister sensor when activity pauses
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-register sensor if tilt is enabled and game is running
        if (useTiltControls && gameRunning && sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
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