package com.example.gamezone;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Locale;

public class word_search extends AppCompatActivity {

    // Game Views
    private WordSearchView wordSearchView;
    private TextView tvTimer, tvFound, tvDifficulty;
    private TextView tvGameResult, tvTimeTaken, tvWordsFound;


    private CardView difficultyLayout,gameOverLayout;
    private RelativeLayout gameLayout;
    private LinearLayout wordsContainer;
    private Button btnHint;

    // Game State
    private boolean gameRunning = false;
    private String difficulty = "easy";
    private CountDownTimer gameTimer;
    private long timeRemaining = 300000; // 5 minutes in milliseconds
    private int totalTime = 300; // 5 minutes in seconds

    // Game Stats
    private int puzzlesSolved = 0;
    private int bestTimeEasy = Integer.MAX_VALUE;
    private int bestTimeMedium = Integer.MAX_VALUE;
    private int bestTimeHard = Integer.MAX_VALUE;
    // Add this with other game state variables
    private boolean hintUsed = false;
    // Preferences
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_search);


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
        preferences = getSharedPreferences("WordSearchPrefs", MODE_PRIVATE);
        loadBestTimes();

        // Initialize views
        initViews();

        // Setup button listeners
        setupButtonListeners();
    }

    private void initViews() {
        wordSearchView = findViewById(R.id.wordSearchView);

        // Text views
        tvTimer = findViewById(R.id.tvTimer);
        tvFound = findViewById(R.id.tvFound);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvGameResult = findViewById(R.id.tvGameResult);
        tvTimeTaken = findViewById(R.id.tvTimeTaken);
        tvWordsFound = findViewById(R.id.tvWordsFound);

        // Layouts
        difficultyLayout = findViewById(R.id.difficultyLayout);
        gameLayout = findViewById(R.id.gameLayout);
        gameOverLayout = findViewById(R.id.gameOverLayout);

        // Words container
        wordsContainer = findViewById(R.id.wordsContainer);

        // Buttons
        btnHint = findViewById(R.id.btnHint);


    }

    private void setupButtonListeners() {
        // Difficulty selection
        findViewById(R.id.btnEasy).setOnClickListener(v -> startGame("easy"));
        findViewById(R.id.btnMedium).setOnClickListener(v -> startGame("medium"));
        findViewById(R.id.btnHard).setOnClickListener(v -> startGame("hard"));

        // Game over buttons
        findViewById(R.id.btnPlayAgain).setOnClickListener(v -> playAgain());
        findViewById(R.id.btnNewPuzzle).setOnClickListener(v -> newPuzzle());
        findViewById(R.id.btnMainMenu).setOnClickListener(v -> showMainMenu());

        // Hint button
        btnHint.setOnClickListener(v -> showHint());
    }

    private void loadBestTimes() {
        bestTimeEasy = preferences.getInt("bestTimeEasy", Integer.MAX_VALUE);
        bestTimeMedium = preferences.getInt("bestTimeMedium", Integer.MAX_VALUE);
        bestTimeHard = preferences.getInt("bestTimeHard", Integer.MAX_VALUE);
        puzzlesSolved = preferences.getInt("puzzlesSolved", 0);
    }

    private void saveBestTimes() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("bestTimeEasy", bestTimeEasy);
        editor.putInt("bestTimeMedium", bestTimeMedium);
        editor.putInt("bestTimeHard", bestTimeHard);
        editor.putInt("puzzlesSolved", puzzlesSolved);
        editor.apply();
    }

// Update these methods in word_search.java

    private void startGame(String diff) {
        difficulty = diff;
        hintUsed = false; // Reset hint usage for new game

        // Set difficulty on game view
        wordSearchView.setDifficulty(difficulty);

        // Show game layout
        difficultyLayout.setVisibility(View.GONE);
        gameLayout.setVisibility(View.VISIBLE);
        gameOverLayout.setVisibility(View.GONE);
        btnHint.setVisibility(View.VISIBLE);

        // Set difficulty text
        String diffText = difficulty.toUpperCase();
        tvDifficulty.setText(diffText);

        // Set timer based on difficulty - UPDATED TIMES
        switch (difficulty) {
            case "easy":
                totalTime = 120; // 2 minutes
                break;
            case "medium":
                totalTime = 210; // 3.5 minutes (3:30)
                break;
            case "hard":
                totalTime = 260; // 4:20 minutes
                break;
        }
        timeRemaining = totalTime * 1000L;

        // Display words to find
        displayWordsToFind();

        // Update found count
        updateFoundCount();

        // Start timer
        startTimer();

        // Start game check loop
        startGameCheckLoop();

        Toast.makeText(this, "Find all " + wordSearchView.getTotalWords() + " words!", Toast.LENGTH_SHORT).show();
    }

    private void displayWordsToFind() {
        wordsContainer.removeAllViews();

        ArrayList<String> words = wordSearchView.getWordsToFind();

        // Set text size based on difficulty
        float textSize;
        switch (difficulty) {
            case "easy":
                textSize = 18f;
                break;
            case "medium":
                textSize = 16f;
                break;
            case "hard":
                textSize = 14f;
                break;
            default:
                textSize = 16f;
                break;
        }

        LinearLayout currentRow = null;

        for (int i = 0; i < words.size(); i++) {
            // Create new row every 3 words
            if (i % 3 == 0) {
                currentRow = new LinearLayout(this);
                currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setGravity(Gravity.CENTER);
                currentRow.setPadding(0, 5, 0, 5);
                wordsContainer.addView(currentRow);
            }

            // Add word to current row
            if (currentRow != null) {
                TextView wordView = createWordTextView(words.get(i), textSize);
                currentRow.addView(wordView);

                // Add spacing between words (except last word in row)
                if (i % 3 < 2 && i < words.size() - 1) {
                    View spacer = new View(this);
                    spacer.setLayoutParams(new LinearLayout.LayoutParams(20, 1));
                    currentRow.addView(spacer);
                }
            }
        }
    }

    private TextView createWordTextView(String word, float textSize) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        textView.setLayoutParams(params);

        textView.setText(word);
        textView.setTextSize(textSize);
        textView.setTextColor(Color.WHITE);
        textView.setPadding(16, 10, 16, 10);
        textView.setBackgroundColor(Color.parseColor("#333333"));
        textView.setId(View.generateViewId());
        textView.setTag(word);

        // Set font to caudex
        try {
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.caudex));
        } catch (Exception e) {
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }

        return textView;
    }
    private void newPuzzle() {
        // Reset hint button
        btnHint.setEnabled(true);
        btnHint.setAlpha(1.0f);
        btnHint.setText("💡");

        // Create new puzzle with same difficulty
        wordSearchView.createNewPuzzle();

        // Reset game
        gameOverLayout.setVisibility(View.GONE);
        gameLayout.setVisibility(View.VISIBLE);
        btnHint.setVisibility(View.VISIBLE);

        // Reset timer - UPDATED TIMES
        switch (difficulty) {
            case "easy":
                totalTime = 120; // 2 minutes
                break;
            case "medium":
                totalTime = 210; // 3.5 minutes
                break;
            case "hard":
                totalTime = 260; // 4:20 minutes
                break;
        }
        timeRemaining = totalTime * 1000L;

        // Display new words
        displayWordsToFind();

        // Start timer and game check
        startTimer();
        startGameCheckLoop();

        Toast.makeText(this, "New puzzle generated!", Toast.LENGTH_SHORT).show();
    }


    private void updateWordDisplay(String foundWord) {
        // Loop through all rows in wordsContainer
        for (int i = 0; i < wordsContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) wordsContainer.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                View child = row.getChildAt(j);
                if (child instanceof TextView) {
                    TextView textView = (TextView) child;
                    if (textView.getTag() != null && textView.getTag().equals(foundWord)) {
                        textView.setTextColor(Color.WHITE);
                        textView.setBackgroundColor(Color.parseColor("#4CAF50"));
                        textView.setPaintFlags(textView.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                        return;
                    }
                }
            }
        }
    }

    private void startTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        gameTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                timeRemaining = 0;
                updateTimerDisplay();
                gameOver(false);
            }
        }.start();
    }

    private void updateTimerDisplay() {
        int minutes = (int) (timeRemaining / 1000) / 60;
        int seconds = (int) (timeRemaining / 1000) % 60;

        String timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimer.setText("" + timeString);


    }

    private void startGameCheckLoop() {
        gameRunning = true;
        final Handler handler = new Handler();

        Runnable gameCheck = new Runnable() {
            @Override
            public void run() {
                if (gameRunning) {
                    // Update found count
                    updateFoundCount();

                    // Check if puzzle is complete
                    if (wordSearchView.isPuzzleComplete()) {
                        gameOver(true);
                    } else {
                        // Schedule next check
                        handler.postDelayed(this, 500);
                    }
                }
            }
        };

        handler.postDelayed(gameCheck, 500);
    }

    private void updateFoundCount() {
        int found = wordSearchView.getFoundCount();
        int total = wordSearchView.getTotalWords();
        tvFound.setText("Found: " + found + "/" + total);

        // Update word displays for found words
        ArrayList<String> foundWords = wordSearchView.getFoundWords();
        for (String word : foundWords) {
            updateWordDisplay(word);
        }
    }

    private void showHint() {
        // Check if hint has already been used
        if (hintUsed) {
            Toast.makeText(this, "You can use only 1 hint per game!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if all words are already found
        if (wordSearchView.isPuzzleComplete()) {
            Toast.makeText(this, "All words already found!", Toast.LENGTH_SHORT).show();
            return;
        }

        wordSearchView.showHint();
        hintUsed = true; // Mark hint as used

        // Disable hint button for the rest of the game
        btnHint.setEnabled(false);
        btnHint.setAlpha(0.5f);
        btnHint.setText("💡");

        Toast.makeText(this, "Hint shown! Word will flash briefly.", Toast.LENGTH_SHORT).show();
    }


    private void gameOver(boolean success) {
        gameRunning = false;

        if (gameTimer != null) {
            gameTimer.cancel();
        }

        // Hide game layout, show game over layout
        gameLayout.setVisibility(View.GONE);
        gameOverLayout.setVisibility(View.VISIBLE);
        btnHint.setVisibility(View.GONE);

        if (success) {
            puzzlesSolved++;
            int timeUsed = totalTime - (int)(timeRemaining / 1000);

            // Update best time if applicable
            boolean newBest = false;
            switch (difficulty) {
                case "easy":
                    if (timeUsed < bestTimeEasy) {
                        bestTimeEasy = timeUsed;
                        newBest = true;
                    }
                    break;
                case "medium":
                    if (timeUsed < bestTimeMedium) {
                        bestTimeMedium = timeUsed;
                        newBest = true;
                    }
                    break;
                case "hard":
                    if (timeUsed < bestTimeHard) {
                        bestTimeHard = timeUsed;
                        newBest = true;
                    }
                    break;
            }

            // Save stats
            saveBestTimes();

            // Display results
            tvGameResult.setText("🎉 PUZZLE SOLVED! 🎉");
            tvGameResult.setTextColor(Color.parseColor("#4CAF50"));

            int minutes = timeUsed / 60;
            int seconds = timeUsed % 60;
            tvTimeTaken.setText(String.format("Time: %02d:%02d", minutes, seconds));

            if (newBest) {
                tvWordsFound.setText("🏆 NEW BEST TIME! 🏆");
                tvWordsFound.setTextColor(Color.YELLOW);
            } else {
                tvWordsFound.setText("Words: " + wordSearchView.getFoundCount() + "/" + wordSearchView.getTotalWords());
                tvWordsFound.setTextColor(Color.parseColor("#FFD700"));
            }

            Toast.makeText(this, "Congratulations! Puzzle solved!", Toast.LENGTH_LONG).show();
        } else {
            // Time's up
            tvGameResult.setText("⏰ TIME'S UP! ⏰");
            tvGameResult.setTextColor(Color.RED);

            int found = wordSearchView.getFoundCount();
            int total = wordSearchView.getTotalWords();
            tvTimeTaken.setText("Found: " + found + "/" + total + " words");
            tvWordsFound.setText("Try again to find all words!");
            tvWordsFound.setTextColor(Color.BLACK);

            Toast.makeText(this, "Time's up! " + found + " out of " + total + " words found.", Toast.LENGTH_LONG).show();
        }
    }

    private void playAgain() {
        // Reset hint button
        btnHint.setEnabled(true);
        btnHint.setAlpha(1.0f);
        btnHint.setText("💡");

        // Start new game with same difficulty
        startGame(difficulty);
    }

    private void showMainMenu() {
        gameOverLayout.setVisibility(View.GONE);
        difficultyLayout.setVisibility(View.VISIBLE);

        // Cancel any running timers
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        gameRunning = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        gameRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameTimer != null) {
            gameTimer.cancel();
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