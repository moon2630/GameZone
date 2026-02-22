package com.example.gamezone;



import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class matching_card_game extends AppCompatActivity {

    private static final int GAME_DURATION_MS = 120_000; // 2 minutes
    private static final int FLIP_ANIMATION_DURATION = 300;
    private static final int[] ALL_COLORS = {
            R.drawable.game_vector1, R.drawable.game_vector2, R.drawable.game_vector3,
            R.drawable.game_vector4, R.drawable.game_vector6, R.drawable.game_vector5,
            R.drawable.game_vector7, R.drawable.game_vector8, R.drawable.game_vector9, R.drawable.game_vector10,
            R.drawable.game_vector11, R.drawable.game_vector12, R.drawable.game_vector13, R.drawable.game_vector14,
            R.drawable.game_vector15, R.drawable.game_vector16, R.drawable.game_vector17, R.drawable.game_vector18,
            R.drawable.game_vector19, R.drawable.game_vector20, R.drawable.game_vector21, R.drawable.game_vector22,
            R.drawable.game_vector23, R.drawable.game_vector24, R.drawable.game_vector25, R.drawable.game_vector26
    };
    private int[] COLORS; // Dynamic array for current game
    private List<Integer> usedColorIndices = new ArrayList<>(); // Track used drawable indices
    private GridLayout gridCards;
    private TextView tvTimer, tvMoves, tvScore, tvBestScore;
    private AppCompatButton btnRestart;
    private ImageButton fabHint;
    private List<Card> cards;
    private Card firstCard, secondCard;
    private int moves, score, bestScore;
    private CountDownTimer timer;
    private boolean isGameActive, usedHint;
    private int gridRows, gridCols, pairsNeeded;
    private SharedPreferences prefs;
    private MediaPlayer flipSound, mismatchSound, matchSound, shuffleSound;
    private long timeRemainingMs;
    private Card dummyCard; // Track the dummy card

    private AlertDialog levelDialog, gameOverDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching_card_game);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.blue_photo));
        }


        // Initialize sound effects
        try {
            flipSound = MediaPlayer.create(this, R.raw.flip);
            mismatchSound = MediaPlayer.create(this, R.raw.mismatch);
            matchSound = MediaPlayer.create(this, R.raw.match);
            shuffleSound = MediaPlayer.create(this, R.raw.shuffle);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading sounds", Toast.LENGTH_SHORT).show();
        }

        prefs = getSharedPreferences("CardGamePrefs", MODE_PRIVATE);

        // Show level selection dialog
        showLevelDialog();
    }


    private void showLevelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.game_start, null);
        builder.setView(dialogView);
        levelDialog = builder.create();

        // Make dialog non-cancelable with back press closing activity
        levelDialog.setCancelable(true);
        levelDialog.setOnCancelListener(dialogInterface -> finish());

        // Setup buttons
        AppCompatButton btnEasy = dialogView.findViewById(R.id.btn_easy);
        AppCompatButton btnMedium = dialogView.findViewById(R.id.btn_medium);
        AppCompatButton btnHard = dialogView.findViewById(R.id.btn_hard);
        AppCompatButton btnExit = dialogView.findViewById(R.id.btn_exit);

        btnEasy.setOnClickListener(v -> startGameWithLevel(4, 4, 8));
        btnMedium.setOnClickListener(v -> startGameWithLevel(5, 5, 12));
        btnHard.setOnClickListener(v -> startGameWithLevel(6, 6, 18));
        btnExit.setOnClickListener(v -> finish());

        levelDialog.show();
    }

    private void startGameWithLevel(int rows, int cols, int pairs) {
        gridRows = rows;
        gridCols = cols;
        pairsNeeded = pairs;
        bestScore = prefs.getInt("best_score_" + gridRows + "x" + gridCols, 0);

        // Select COLORS for the game
        int colorsNeeded = pairsNeeded;
        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < ALL_COLORS.length; i++) {
            if (!usedColorIndices.contains(i)) {
                availableIndices.add(i);
            }
        }

        // If not enough unused drawables, reset used indices
        if (availableIndices.size() < colorsNeeded) {
            usedColorIndices.clear();
            for (int i = 0; i < ALL_COLORS.length; i++) {
                availableIndices.add(i);
            }
        }

        // Randomly select required number of drawables
        Collections.shuffle(availableIndices);
        COLORS = new int[colorsNeeded];
        for (int i = 0; i < colorsNeeded && i < availableIndices.size(); i++) {
            COLORS[i] = ALL_COLORS[availableIndices.get(i)];
            usedColorIndices.add(availableIndices.get(i));
        }

        // Switch to game layout

        // Initialize game UI
        gridCards = findViewById(R.id.grid_cards);
        tvTimer = findViewById(R.id.tv_timer);
        tvMoves = findViewById(R.id.tv_moves);
        tvScore = findViewById(R.id.tv_score);
        tvBestScore = findViewById(R.id.tv_best_score);
        btnRestart = findViewById(R.id.btn_restart);
        fabHint = findViewById(R.id.fab_hint);
        tvBestScore.setText("Best: " + bestScore);


        if (levelDialog != null) {
            levelDialog.dismiss();
            levelDialog = null;
        }

        startGame();
    }


    private void startGame() {
        moves = 0;
        score = 0;
        usedHint = false;
        isGameActive = false; // Disable interaction until animation completes
        timeRemainingMs = GAME_DURATION_MS;
        firstCard = null;
        secondCard = null;
        dummyCard = null;
        tvMoves.setText("Moves: 0");
        tvScore.setText("Score: 0");
        fabHint.setEnabled(true);
        updateTimerDisplay(timeRemainingMs);

        fabHint.setEnabled(true);
        fabHint.setAlpha(1.0f);
        fabHint.setOnClickListener(v -> {
            if (!isGameActive) {
                return;
            }
            if (usedHint) {
                Toast.makeText(this, "You can use only one hint", Toast.LENGTH_SHORT).show();
            } else {
                usedHint = true;
                fabHint.setAlpha(0.5f);
                moves += 2;
                tvMoves.setText("Moves: " + moves);
                showHint();
            }
        });

        // Initialize cards
        cards = new ArrayList<>();
        int colorsNeeded = pairsNeeded;
        for (int i = 0; i < colorsNeeded; i++) {
            int color = COLORS[i % COLORS.length];
            cards.add(new Card(color));
            cards.add(new Card(color));
        }
        if (gridRows == 5 && gridCols == 5) { // Medium level (5x5)
            cards.add(new Card(R.drawable.card_back_flip)); // Dummy card
        }
        Collections.shuffle(cards);
        shuffleSound.start();

        // Setup grid
        gridCards.setRowCount(gridRows);
        gridCards.setColumnCount(gridCols);
        gridCards.removeAllViews();
        for (int i = 0; i < cards.size(); i++) {
            ImageView cardView = new ImageView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.rowSpec = GridLayout.spec(i / gridCols, 1f);
            params.columnSpec = GridLayout.spec(i % gridCols, 1f);
            params.setMargins(4, 4, 4, 4);
            cardView.setLayoutParams(params);
            cardView.setBackgroundResource(R.drawable.card_back);
            cardView.setTag(i);
            cardView.setOnClickListener(v -> onCardClick((int) v.getTag()));
            if (gridRows == 6 && gridCols == 6) { // Hard level
                cardView.setScaleType(ImageView.ScaleType.FIT_CENTER); // Scale to fit bounds
                cardView.setPadding(8, 8, 8, 8); // Add padding to center vector
                int cellSize = (int) (60 / (float) gridCols * getResources().getDisplayMetrics().density); // Scale size for 6x6
                cardView.getLayoutParams().width = cellSize;
                cardView.getLayoutParams().height = cellSize;
            } else {
                cardView.setScaleType(ImageView.ScaleType.CENTER); // Keep original for easy/medium
            }
            cardView.setVisibility(View.INVISIBLE); // Initially invisible
            gridCards.addView(cardView);
        }

        // Animate cards appearing one by one
        long delay = 0;
        final long delayIncrement = 100; // 100ms delay between each card
        for (int i = 0; i < gridCards.getChildCount(); i++) {
            ImageView cardView = (ImageView) gridCards.getChildAt(i);
            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator translateX = ObjectAnimator.ofFloat(cardView, "translationX", -1000f, 0f);
            ObjectAnimator translateY = ObjectAnimator.ofFloat(cardView, "translationY", -1000f, 0f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(cardView, "alpha", 0f, 1f);
            ObjectAnimator rotation = ObjectAnimator.ofFloat(cardView, "rotation", -90f, 0f);
            translateX.setDuration(500);
            translateY.setDuration(500);
            alpha.setDuration(500);
            rotation.setDuration(500);
            animatorSet.playTogether(translateX, translateY, alpha, rotation);
            animatorSet.setStartDelay(delay);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    cardView.setVisibility(View.VISIBLE); // Make visible when animation starts
                }
            });
            delay += delayIncrement;
            if (i == gridCards.getChildCount() - 1) {
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isGameActive = true; // Enable interaction after last card
                        shuffleSound.stop(); // Stop shuffle music
                        shuffleSound.prepareAsync(); // Prepare for next use
                        // Start timer immediately
                        startTimer(timeRemainingMs);
                    }
                });
            }
            animatorSet.start();
        }

        // Restart button
        btnRestart.setOnClickListener(v -> {
            // Create the dialog layout
            LinearLayout dialogLayout = new LinearLayout(this);
            dialogLayout.setOrientation(LinearLayout.VERTICAL);
            dialogLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
            dialogLayout.setPadding(50, 50, 50, 35);

            // Title
            TextView title = new TextView(this);
            title.setText("Restart Game");
            title.setTypeface(ResourcesCompat.getFont(this, R.font.caudex), Typeface.BOLD);
            title.setTextSize(22);
            title.setPadding(0, 0, 10, 20);
            title.setTextColor(getResources().getColor(android.R.color.black));

            // Message
            TextView message = new TextView(this);
            message.setText("Are you sure you want to restart?");
            message.setTypeface(ResourcesCompat.getFont(this, R.font.caudex));
            message.setTextSize(16);
            message.setPadding(0, 10, 0, 0);
            message.setTextColor(getResources().getColor(android.R.color.black));

            // Add views to the dialog layout
            dialogLayout.addView(title);
            dialogLayout.addView(message);

            // Create and show the dialog
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogLayout)
                    .setPositiveButton("Yes", (dialogInterface, which) -> {
                        if (timer != null) timer.cancel();
                        startGame();
                    })
                    .setNegativeButton("No", null)
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
            }
        });
    }

    // Updated showUITapTargets method
    private void startTimer(long durationMs) {
        timer = new CountDownTimer(durationMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemainingMs = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                isGameActive = false;
                timeRemainingMs = 0;
                updateTimerDisplay(0);
                showGameOverScreen(false);
            }
        }.start();
    }

    private void updateTimerDisplay(long millis) {
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        tvTimer.setText(String.format(Locale.getDefault(), "Time: %02d:%02d", minutes, seconds));
    }

    private void onCardClick(int position) {
        if (!isGameActive || cards.get(position).isFlipped() || cards.get(position).isMatched() ||
                (firstCard != null && secondCard != null)) {
            return;
        }

        Card card = cards.get(position);
        if (card == firstCard) {
            return; // Prevent selecting the same card twice
        }

        ImageView cardView = (ImageView) gridCards.getChildAt(position);
        flipCard(card, cardView, true);

        if (firstCard == null) {
            firstCard = card;
            if (card.getColorResId() == R.drawable.card_back_flip) {
                dummyCard = card; // Track the dummy card
            }
        } else {
            secondCard = card;
            moves++;
            tvMoves.setText("Moves: " + moves);
            checkMatch();
        }
    }

    private void checkMatch() {
        ImageView firstView = (ImageView) gridCards.getChildAt(cards.indexOf(firstCard));
        ImageView secondView = (ImageView) gridCards.getChildAt(cards.indexOf(secondCard));
        if (firstCard.getColorResId() == secondCard.getColorResId()) {
            firstCard.setMatched(true);
            secondCard.setMatched(true);
            score += 100;
            tvScore.setText("Score: " + score);
            new android.os.Handler().postDelayed(() -> {
                firstView.setImageResource(firstCard.getColorResId());
                secondView.setImageResource(secondCard.getColorResId());
                firstView.setBackground(ContextCompat.getDrawable(this, R.drawable.card_matched_border));
                secondView.setBackground(ContextCompat.getDrawable(this, R.drawable.card_matched_border));
                if (matchSound != null) {
                    matchSound.start();
                }
                firstCard = null;
                secondCard = null;
                checkGameWon();
            }, FLIP_ANIMATION_DURATION);
        } else {
            if (mismatchSound != null) {
                mismatchSound.start();
            }
            new android.os.Handler().postDelayed(() -> {
                flipCard(firstCard, firstView, false);
                flipCard(secondCard, secondView, false);
                firstCard = null;
                secondCard = null;
                dummyCard = null; // Clear dummy card reference
            }, 1000);
        }
    }

    private void flipCard(Card card, ImageView cardView, boolean toFront) {
        if (flipSound != null) {
            flipSound.start();
        }
        float start = toFront ? 0f : 180f;
        float end = toFront ? 180f : 0f;
        ObjectAnimator flip = ObjectAnimator.ofFloat(cardView, "rotationY", start, end);
        flip.setDuration(FLIP_ANIMATION_DURATION);
        flip.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (toFront) {
                    cardView.setImageResource(card.getColorResId());
                    if (card.isMatched()) {
                        cardView.setBackground(ContextCompat.getDrawable(matching_card_game.this, R.drawable.card_matched_border));
                    } else {
                        cardView.setBackgroundResource(R.drawable.card_back_flip);
                    }
                    card.setFlipped(true);
                } else {
                    cardView.setImageResource(0);
                    cardView.setBackgroundResource(R.drawable.card_back);
                    card.setFlipped(false);
                    if (card == dummyCard) {
                        dummyCard = null; // Clear dummy card reference when unflipped
                    }
                }
            }
        });
        flip.start();
    }

    private void showHint() {
        for (int i = 0; i < cards.size() - 1; i++) {
            if (!cards.get(i).isMatched() && !cards.get(i).isFlipped()) {
                for (int j = i + 1; j < cards.size(); j++) {
                    if (!cards.get(j).isMatched() && !cards.get(j).isFlipped() &&
                            cards.get(i).getColorResId() == cards.get(j).getColorResId()) {
                        ImageView card1 = (ImageView) gridCards.getChildAt(i);
                        ImageView card2 = (ImageView) gridCards.getChildAt(j);
                        flipCard(cards.get(i), card1, true);
                        flipCard(cards.get(j), card2, true);
                        int finalI = i;
                        int finalJ = j;
                        new android.os.Handler().postDelayed(() -> {
                            flipCard(cards.get(finalI), card1, false);
                            flipCard(cards.get(finalJ), card2, false);
                        }, 1000);
                        return;
                    }
                }
            }
        }
    }

    private void checkGameWon() {
        int matchedPairs = 0;
        for (Card card : cards) {
            if (card.isMatched()) matchedPairs++;
        }
        if (matchedPairs / 2 >= pairsNeeded) {
            isGameActive = false;
            if (timer != null) timer.cancel();
            score += (int) (timeRemainingMs / 1000) * 10; // Bonus for time remaining
            if (score > bestScore) {
                bestScore = score;
                prefs.edit().putInt("best_score_" + gridRows + "x" + gridCols, bestScore).apply();
            }

            // Winning animation
            long delay = 0;
            final long delayIncrement = 50; // 50ms delay between each card
            for (int i = 0; i < gridCards.getChildCount(); i++) {
                ImageView cardView = (ImageView) gridCards.getChildAt(i);
                if (cards.get(i).isMatched()) { // Only animate matched cards
                    AnimatorSet animatorSet = new AnimatorSet();
                    ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 1.2f);
                    ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 1.2f);
                    ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(cardView, "scaleX", 1.2f, 1f);
                    ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(cardView, "scaleY", 1.2f, 1f);
                    ObjectAnimator rotation = ObjectAnimator.ofFloat(cardView, "rotation", 0f, 10f, -10f, 0f);
                    ObjectAnimator alpha = ObjectAnimator.ofFloat(cardView, "alpha", 1f, 0f);
                    scaleXUp.setDuration(200);
                    scaleYUp.setDuration(200);
                    scaleXDown.setDuration(200);
                    scaleYDown.setDuration(200);
                    rotation.setDuration(400);
                    alpha.setDuration(300);
                    AnimatorSet scaleSet = new AnimatorSet();
                    scaleSet.play(scaleXUp).with(scaleYUp);
                    scaleSet.play(scaleXDown).with(scaleYDown).after(scaleXUp);
                    animatorSet.play(scaleSet).with(rotation);
                    animatorSet.play(alpha).after(scaleSet);
                    animatorSet.setStartDelay(delay);
                    delay += delayIncrement;
                    if (i == gridCards.getChildCount() - 1) {
                        animatorSet.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (matchSound != null) matchSound.start();
                                showGameOverScreen(true);
                            }
                        });
                    }
                    animatorSet.start();
                }
            }
            // If no cards were animated (unlikely), proceed directly
            if (delay == 0) {
                if (matchSound != null) matchSound.start();
                showGameOverScreen(true);
            }
        }
    }

    private void showGameOverScreen(boolean won) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        isGameActive = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.game_over, null);
        builder.setView(dialogView);
        gameOverDialog = builder.create();


        // Make dialog non-cancelable with back press closing activity
        gameOverDialog.setCancelable(true);
        gameOverDialog.setOnCancelListener(dialogInterface -> finish());

        // Setup UI elements
        TextView tvTitle = dialogView.findViewById(R.id.tv_game_over_title);
        TextView tvFinalScore = dialogView.findViewById(R.id.tv_final_score);
        TextView tvFinalMoves = dialogView.findViewById(R.id.tv_final_moves);
        TextView tvBestScore = dialogView.findViewById(R.id.tv_best_score);
        AppCompatButton btnPlayAgain = dialogView.findViewById(R.id.btn_play_again);
        AppCompatButton btnExit = dialogView.findViewById(R.id.btn_exit);

        tvTitle.setText(won ? "Congratulations!" : "Time is Over");
        tvFinalScore.setText("Score: " + score);
        tvFinalMoves.setText("Moves: " + moves);
        tvBestScore.setText("Best Score: " + bestScore);

        btnPlayAgain.setOnClickListener(v -> {
            firstCard = null;
            secondCard = null;
            dummyCard = null;
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (gameOverDialog != null) {
                gameOverDialog.dismiss();
                gameOverDialog = null;
            }
            startGameWithLevel(gridRows, gridCols, pairsNeeded);
        });
        btnExit.setOnClickListener(v -> finish());

        gameOverDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGameActive && timeRemainingMs > 0) {
            startTimer(timeRemainingMs);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (flipSound != null) {
            flipSound.release();
            flipSound = null;
        }
        if (mismatchSound != null) {
            mismatchSound.release();
            mismatchSound = null;
        }
        if (matchSound != null) {
            matchSound.release();
            matchSound = null;
        }
        if (shuffleSound != null) {
            shuffleSound.release();
            shuffleSound = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (isGameActive) {
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
}
