package com.example.gamezone;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.Random;

public class rps_game extends AppCompatActivity {

    // Views
    private RelativeLayout vsComputerLayout, twoPlayerLayout;
    private CardView modeSelectionLayout,difficultyLayout,gameOverLayout;

    // Text Views
    private TextView tvScoreVsComputer, tvDifficultyDisplay, tvResultVsComputer;
    private TextView tvScoreTwoPlayer, tvResultTwoPlayer;
    private TextView tvPlayerChoiceText, tvComputerChoiceText;
    private TextView tvPlayer1ChoiceText, tvPlayer2ChoiceText;
    private TextView tvGameOverTitle, tvGameOverMessage, tvFinalScore;

    // Image Views
    private TextView ivPlayerChoice, ivComputerChoice;
    private TextView ivPlayer1Choice, ivPlayer2Choice;

    // Buttons
    private TextView btnRock, btnPaper, btnScissors;
    private TextView btnPlayer1Rock, btnPlayer1Paper, btnPlayer1Scissors;
    private TextView btnPlayer2Rock, btnPlayer2Paper, btnPlayer2Scissors;
    private TextView btnRevealTwoPlayer;

    // Game State
    private String gameMode = ""; // "computer" or "twoPlayer"
    private String difficulty = "easy"; // "easy", "medium", "hard"
    private int playerScore = 0;
    private int computerScore = 0;
    private int player1Score = 0;
    private int player2Score = 0;
    private int maxRounds = 3;
    private int roundCount = 0;

    // Player choices
    private String playerChoice = "";
    private String computerChoice = "";
    private String player1Choice = "";
    private String player2Choice = "";

    // Game Stats
    private int gamesPlayed = 0;
    private int gamesWon = 0;
    private int gamesLost = 0;
    private int draws = 0;

    // Preferences
    private SharedPreferences preferences;
    private Random random = new Random();

    // Animation
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rps_game);


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
        preferences = getSharedPreferences("RPSGamePrefs", MODE_PRIVATE);
        loadGameStats();

        // Initialize views
        initViews();

        // Setup button listeners
        setupButtonListeners();
    }

    private void initViews() {
        // Layouts
        modeSelectionLayout = findViewById(R.id.modeSelectionLayout);
        difficultyLayout = findViewById(R.id.difficultyLayout);
        vsComputerLayout = findViewById(R.id.vsComputerLayout);
        twoPlayerLayout = findViewById(R.id.twoPlayerLayout);
        gameOverLayout = findViewById(R.id.gameOverLayout);

        // VS Computer views
        tvScoreVsComputer = findViewById(R.id.tvScoreVsComputer);
        tvDifficultyDisplay = findViewById(R.id.tvDifficultyDisplay);
        tvResultVsComputer = findViewById(R.id.tvResultVsComputer);
        tvPlayerChoiceText = findViewById(R.id.tvPlayerChoiceText);
        tvComputerChoiceText = findViewById(R.id.tvComputerChoiceText);

        ivPlayerChoice = findViewById(R.id.ivPlayerChoice);
        ivComputerChoice = findViewById(R.id.ivComputerChoice);

        btnRock = findViewById(R.id.btnRock);
        btnPaper = findViewById(R.id.btnPaper);
        btnScissors = findViewById(R.id.btnScissors);

        // Two Player views
        tvScoreTwoPlayer = findViewById(R.id.tvScoreTwoPlayer);
        tvResultTwoPlayer = findViewById(R.id.tvResultTwoPlayer);
        tvPlayer1ChoiceText = findViewById(R.id.tvPlayer1ChoiceText);
        tvPlayer2ChoiceText = findViewById(R.id.tvPlayer2ChoiceText);

        ivPlayer1Choice = findViewById(R.id.ivPlayer1Choice);
        ivPlayer2Choice = findViewById(R.id.ivPlayer2Choice);

        btnPlayer1Rock = findViewById(R.id.btnPlayer1Rock);
        btnPlayer1Paper = findViewById(R.id.btnPlayer1Paper);
        btnPlayer1Scissors = findViewById(R.id.btnPlayer1Scissors);

        btnPlayer2Rock = findViewById(R.id.btnPlayer2Rock);
        btnPlayer2Paper = findViewById(R.id.btnPlayer2Paper);
        btnPlayer2Scissors = findViewById(R.id.btnPlayer2Scissors);

        btnRevealTwoPlayer = findViewById(R.id.btnRevealTwoPlayer);

        // Game Over views
        tvGameOverTitle = findViewById(R.id.tvGameOverTitle);
        tvGameOverMessage = findViewById(R.id.tvGameOverMessage);
        tvFinalScore = findViewById(R.id.tvFinalScore);
    }

    private void setupButtonListeners() {
        // Mode selection
        findViewById(R.id.btnVsComputer).setOnClickListener(v -> selectGameMode("computer"));
        findViewById(R.id.btnTwoPlayer).setOnClickListener(v -> selectGameMode("twoPlayer"));

        // Difficulty selection
        findViewById(R.id.btnEasy).setOnClickListener(v -> startVsComputerGame("easy"));
        findViewById(R.id.btnMedium).setOnClickListener(v -> startVsComputerGame("medium"));
        findViewById(R.id.btnHard).setOnClickListener(v -> startVsComputerGame("hard"));
        findViewById(R.id.btnBackFromDifficulty).setOnClickListener(v -> showMainMenu());

        // VS Computer game buttons
        btnRock.setOnClickListener(v -> makePlayerMove("rock"));
        btnPaper.setOnClickListener(v -> makePlayerMove("paper"));
        btnScissors.setOnClickListener(v -> makePlayerMove("scissors"));

        findViewById(R.id.btnResetVsComputer).setOnClickListener(v -> resetVsComputerGame());
        findViewById(R.id.btnBackVsComputer).setOnClickListener(v -> showMainMenu());

        // Two Player game buttons
        btnPlayer1Rock.setOnClickListener(v -> makePlayer1Move("rock"));
        btnPlayer1Paper.setOnClickListener(v -> makePlayer1Move("paper"));
        btnPlayer1Scissors.setOnClickListener(v -> makePlayer1Move("scissors"));

        btnPlayer2Rock.setOnClickListener(v -> makePlayer2Move("rock"));
        btnPlayer2Paper.setOnClickListener(v -> makePlayer2Move("paper"));
        btnPlayer2Scissors.setOnClickListener(v -> makePlayer2Move("scissors"));

        btnRevealTwoPlayer.setOnClickListener(v -> revealTwoPlayerResults());

        findViewById(R.id.btnResetTwoPlayer).setOnClickListener(v -> resetTwoPlayerGame());
        findViewById(R.id.btnBackTwoPlayer).setOnClickListener(v -> showMainMenu());

        // Game Over buttons
        findViewById(R.id.btnPlayAgain).setOnClickListener(v -> playAgain());
        findViewById(R.id.btnMainMenuFromGameOver).setOnClickListener(v -> showMainMenu());
    }

    private void selectGameMode(String mode) {
        gameMode = mode;

        if (mode.equals("computer")) {
            // Show difficulty selection
            modeSelectionLayout.setVisibility(View.GONE);
            difficultyLayout.setVisibility(View.VISIBLE);
        } else if (mode.equals("twoPlayer")) {
            // Start two player game directly
            startTwoPlayerGame();
        }
    }

    private void startVsComputerGame(String diff) {
        difficulty = diff;

        // Hide difficulty, show game screen
        difficultyLayout.setVisibility(View.GONE);
        vsComputerLayout.setVisibility(View.VISIBLE);

        // Set difficulty display
        String diffText = difficulty.toUpperCase();
        tvDifficultyDisplay.setText("Difficulty: " + diffText);

        // Initialize TextViews with background
        ivPlayerChoice.setBackgroundResource(R.drawable.button_background_restart);
        ivComputerChoice.setBackgroundResource(R.drawable.button_background_restart);
        ivPlayerChoice.setText("");
        ivComputerChoice.setText("");

        // Reset game state
        resetVsComputerGame();

        Toast.makeText(this, "First to " + maxRounds + " wins!", Toast.LENGTH_SHORT).show();
    }

    private void startTwoPlayerGame() {
        modeSelectionLayout.setVisibility(View.GONE);
        twoPlayerLayout.setVisibility(View.VISIBLE);

        // Initialize TextViews with background
        ivPlayer1Choice.setBackgroundResource(R.drawable.button_background_restart);
        ivPlayer2Choice.setBackgroundResource(R.drawable.button_background_restart);
        ivPlayer1Choice.setText("");
        ivPlayer2Choice.setText("");

        // Reset game state
        resetTwoPlayerGame();

        Toast.makeText(this, "Player 1 vs Player 2 - First to " + maxRounds + " wins!", Toast.LENGTH_LONG).show();
    }

    private void makePlayerMove(String move) {
        if (playerChoice.isEmpty()) {
            playerChoice = move;
            updatePlayerDisplay();

            // Disable player buttons
            setPlayerButtonsEnabled(false);

            // Animate player choice
            animateChoice(ivPlayerChoice);

            // Computer makes move after delay
            handler.postDelayed(() -> {
                makeComputerMove();
                determineWinner();
            }, 1000);
        }
    }

    private void updatePlayerDisplay() {
        // Remove background and show emoji
        ivPlayerChoice.setBackgroundResource(android.R.color.transparent);

        switch (playerChoice) {
            case "rock":
                ivPlayerChoice.setText("👊🏻");
                break;
            case "paper":
                ivPlayerChoice.setText("✋🏻");
                break;
            case "scissors":
                ivPlayerChoice.setText("✌🏻");
                break;
        }
    }
    private void makeComputerMove() {
        // Generate computer choice based on difficulty
        computerChoice = generateComputerChoice();

        // Remove background and show emoji
        ivComputerChoice.setBackgroundResource(android.R.color.transparent);

        // Update computer display with emojis
        switch (computerChoice) {
            case "rock":
                ivComputerChoice.setText("👊🏻");
                break;
            case "paper":
                ivComputerChoice.setText("✋🏻");
                break;
            case "scissors":
                ivComputerChoice.setText("✌🏻");
                break;
        }

        // Animate computer choice
        animateChoice(ivComputerChoice);
    }


    private String generateComputerChoice() {
        int choice = random.nextInt(100);

        switch (difficulty) {
            case "easy":
                // Easy: Random choice (33% each)
                if (choice < 33) return "rock";
                else if (choice < 66) return "paper";
                else return "scissors";

            case "medium":
                // Medium: Slightly smarter - counters player's common moves
                if (playerChoice.equals("rock")) {
                    // More likely to choose paper (counter rock)
                    if (choice < 40) return "paper";
                    else if (choice < 70) return "scissors";
                    else return "rock";
                } else if (playerChoice.equals("paper")) {
                    // More likely to choose scissors (counter paper)
                    if (choice < 40) return "scissors";
                    else if (choice < 70) return "rock";
                    else return "paper";
                } else {
                    // More likely to choose rock (counter scissors)
                    if (choice < 40) return "rock";
                    else if (choice < 70) return "paper";
                    else return "scissors";
                }

            case "hard":
                // Hard: AI that learns and counters patterns
                if (roundCount == 0) {
                    // First round: random
                    if (choice < 33) return "rock";
                    else if (choice < 66) return "paper";
                    else return "scissors";
                } else {
                    // Analyze player's pattern and counter
                    if (playerChoice.equals("rock")) {
                        // If player chose rock twice in a row, counter with paper
                        return "paper";
                    } else if (playerChoice.equals("paper")) {
                        return "scissors";
                    } else {
                        return "rock";
                    }
                }
        }

        return "rock"; // Default fallback
    }

    private void determineWinner() {
        String result = "";
        gamesPlayed++;

        if (playerChoice.equals(computerChoice)) {
            result = "🤝 IT'S A DRAW! 🤝";
            draws++;
            tvResultVsComputer.setTextColor(Color.YELLOW);
        } else if ((playerChoice.equals("rock") && computerChoice.equals("scissors")) ||
                (playerChoice.equals("paper") && computerChoice.equals("rock")) ||
                (playerChoice.equals("scissors") && computerChoice.equals("paper"))) {
            // Player wins
            result = "🎉 YOU WIN THIS ROUND! 🎉";
            playerScore++;
            gamesWon++;
            tvResultVsComputer.setTextColor(Color.GREEN);

            // Animate victory
            animateVictory(ivPlayerChoice);
        } else {
            // Computer wins
            result = "💻 COMPUTER WINS THIS ROUND! 💻";
            computerScore++;
            gamesLost++;
            tvResultVsComputer.setTextColor(Color.RED);

            // Animate victory
            animateVictory(ivComputerChoice);
        }

        roundCount++;
        updateScoreDisplay();

        tvResultVsComputer.setText(result);
        tvResultVsComputer.setVisibility(View.VISIBLE);

        // Check if game is over
        if (playerScore >= maxRounds || computerScore >= maxRounds) {
            endVsComputerGame();
        } else {
            // Enable buttons for next round after delay
            handler.postDelayed(() -> {
                resetRound();
            }, 2000);
        }

        // Save stats
        saveGameStats();
    }

    private void makePlayer1Move(String move) {
        if (player1Choice.isEmpty()) {
            player1Choice = move;
            updatePlayer1Display();
            checkTwoPlayerReady();
        }
    }

    private void makePlayer2Move(String move) {
        if (player2Choice.isEmpty()) {
            player2Choice = move;
            updatePlayer2Display();
            checkTwoPlayerReady();
        }
    }

    private void updatePlayer1Display() {
        // Remove background and show emoji
        ivPlayer1Choice.setBackgroundResource(android.R.color.transparent);

        switch (player1Choice) {
            case "rock":
                ivPlayer1Choice.setText("👊🏻");
                break;
            case "paper":
                ivPlayer1Choice.setText("✋🏻");
                break;
            case "scissors":
                ivPlayer1Choice.setText("✌🏻");
                break;
        }

        tvPlayer1ChoiceText.setText("Ready!");

        // Disable player 1 buttons
        btnPlayer1Rock.setEnabled(false);
        btnPlayer1Paper.setEnabled(false);
        btnPlayer1Scissors.setEnabled(false);
    }
    private void updatePlayer2Display() {
        // Remove background and show emoji
        ivPlayer2Choice.setBackgroundResource(android.R.color.transparent);

        switch (player2Choice) {
            case "rock":
                ivPlayer2Choice.setText("👊🏻");
                break;
            case "paper":
                ivPlayer2Choice.setText("✋🏻");
                break;
            case "scissors":
                ivPlayer2Choice.setText("✌🏻");
                break;
        }

        tvPlayer2ChoiceText.setText("Ready!");

        // Disable player 2 buttons
        btnPlayer2Rock.setEnabled(false);
        btnPlayer2Paper.setEnabled(false);
        btnPlayer2Scissors.setEnabled(false);
    }
    private void revealTwoPlayerResults() {
        // Update TextViews with emojis directly
        switch (player1Choice) {
            case "rock":
                ivPlayer1Choice.setText("👊🏻");
                tvPlayer1ChoiceText.setText("ROCK");
                break;
            case "paper":
                ivPlayer1Choice.setText("✋🏻");
                tvPlayer1ChoiceText.setText("PAPER");
                break;
            case "scissors":
                ivPlayer1Choice.setText("✌🏻");
                tvPlayer1ChoiceText.setText("SCISSORS");
                break;
        }

        switch (player2Choice) {
            case "rock":
                ivPlayer2Choice.setText("👊🏻");
                tvPlayer2ChoiceText.setText("ROCK");
                break;
            case "paper":
                ivPlayer2Choice.setText("✋🏻");
                tvPlayer2ChoiceText.setText("PAPER");
                break;
            case "scissors":
                ivPlayer2Choice.setText("✌🏻");
                tvPlayer2ChoiceText.setText("SCISSORS");
                break;
        }

        // Animate both choices
        animateChoice(ivPlayer1Choice);
        animateChoice(ivPlayer2Choice);

        // Determine winner
        determineTwoPlayerWinner();
    }
    private void checkTwoPlayerReady() {
        if (!player1Choice.isEmpty() && !player2Choice.isEmpty()) {
            // Both players have chosen, show reveal button
            btnRevealTwoPlayer.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Both players ready! Click REVEAL", Toast.LENGTH_SHORT).show();
        }
    }


    private void determineTwoPlayerWinner() {
        String result = "";
        roundCount++;

        if (player1Choice.equals(player2Choice)) {
            result = "🤝 IT'S A DRAW! 🤝";
            tvResultTwoPlayer.setTextColor(Color.YELLOW);
        } else if ((player1Choice.equals("rock") && player2Choice.equals("scissors")) ||
                (player1Choice.equals("paper") && player2Choice.equals("rock")) ||
                (player1Choice.equals("scissors") && player2Choice.equals("paper"))) {
            // Player 1 wins
            result = "🎉 PLAYER 1 WINS THIS ROUND! 🎉";
            player1Score++;
            tvResultTwoPlayer.setTextColor(ContextCompat.getColor(this, R.color.background_tab));

            // Animate victory - Use ivPlayer1Choice (TextView)
            animateVictory(ivPlayer1Choice);
        } else {
            // Player 2 wins
            result = "🎉 PLAYER 2 WINS THIS ROUND! 🎉";
            player2Score++;
            tvResultTwoPlayer.setTextColor(ContextCompat.getColor(this, R.color.background_tab));

            // Animate victory - Use ivPlayer2Choice (TextView)
            animateVictory(ivPlayer2Choice);
        }

        updateTwoPlayerScoreDisplay();

        tvResultTwoPlayer.setText(result);
        tvResultTwoPlayer.setVisibility(View.VISIBLE);
        btnRevealTwoPlayer.setVisibility(View.GONE);

        // Check if game is over
        if (player1Score >= maxRounds || player2Score >= maxRounds) {
            endTwoPlayerGame();
        } else {
            // Reset for next round after delay
            handler.postDelayed(() -> {
                resetTwoPlayerRound();
            }, 2000);
        }
    }
    private void resetRound() {
        playerChoice = "";
        computerChoice = "";

        // Reset TextViews
        ivPlayerChoice.setText("");
        ivComputerChoice.setText("");

        // Restore background
        ivPlayerChoice.setBackgroundResource(R.drawable.button_background_restart);
        ivComputerChoice.setBackgroundResource(R.drawable.button_background_restart);

        // Reset text
        tvPlayerChoiceText.setText("Choose your move!");
        tvComputerChoiceText.setText("Thinking...");
        tvResultVsComputer.setVisibility(View.GONE);

        // Enable player buttons
        setPlayerButtonsEnabled(true);
    }

    private void resetTwoPlayerRound() {
        player1Choice = "";
        player2Choice = "";

        // Reset TextViews
        ivPlayer1Choice.setText("");
        ivPlayer2Choice.setText("");

        // Restore background
        ivPlayer1Choice.setBackgroundResource(R.drawable.button_background_restart);
        ivPlayer2Choice.setBackgroundResource(R.drawable.button_background_restart);

        // Reset text
        tvPlayer1ChoiceText.setText("Tap to choose!");
        tvPlayer2ChoiceText.setText("Tap to choose!");
        tvResultTwoPlayer.setVisibility(View.GONE);
        btnRevealTwoPlayer.setVisibility(View.GONE);

        // Enable buttons
        btnPlayer1Rock.setEnabled(true);
        btnPlayer1Paper.setEnabled(true);
        btnPlayer1Scissors.setEnabled(true);
        btnPlayer2Rock.setEnabled(true);
        btnPlayer2Paper.setEnabled(true);
        btnPlayer2Scissors.setEnabled(true);
    }


    private void resetVsComputerGame() {
        playerScore = 0;
        computerScore = 0;
        roundCount = 0;

        updateScoreDisplay();
        resetRound();

        Toast.makeText(this, "Game reset! New match started.", Toast.LENGTH_SHORT).show();
    }

    private void resetTwoPlayerGame() {
        player1Score = 0;
        player2Score = 0;
        roundCount = 0;

        // Initialize TextViews with background
        ivPlayer1Choice.setBackgroundResource(R.drawable.button_background_restart);
        ivPlayer2Choice.setBackgroundResource(R.drawable.button_background_restart);
        ivPlayer1Choice.setText("");
        ivPlayer2Choice.setText("");

        updateTwoPlayerScoreDisplay();
        resetTwoPlayerRound();

        Toast.makeText(this, "Game reset! New match started.", Toast.LENGTH_SHORT).show();
    }

    private void updateScoreDisplay() {
        tvScoreVsComputer.setText("You: " + playerScore + " | CPU: " + computerScore);
    }

    private void updateTwoPlayerScoreDisplay() {
        tvScoreTwoPlayer.setText("Player 1: " + player1Score + " | Player 2: " + player2Score);
    }

    private void setPlayerButtonsEnabled(boolean enabled) {
        btnRock.setEnabled(enabled);
        btnPaper.setEnabled(enabled);
        btnScissors.setEnabled(enabled);

        float alpha = enabled ? 1.0f : 0.5f;
        btnRock.setAlpha(alpha);
        btnPaper.setAlpha(alpha);
        btnScissors.setAlpha(alpha);
    }

    private void endVsComputerGame() {
        String winner = "";
        String message = "";

        if (playerScore > computerScore) {
            winner = "🎉 YOU WIN THE GAME! 🎉";
            message = "Congratulations! You defeated the computer!";
            tvGameOverTitle.setTextColor(Color.GREEN);
        } else {
            winner = "💻 COMPUTER WINS! 💻";
            message = "Better luck next time!";
            tvGameOverTitle.setTextColor(Color.RED);
        }

        tvGameOverTitle.setText(winner);
        tvGameOverMessage.setText(message);
        tvFinalScore.setText("Final Score: You " + playerScore + " - " + computerScore + " Computer");

        vsComputerLayout.setVisibility(View.GONE);
        gameOverLayout.setVisibility(View.VISIBLE);
    }

    private void endTwoPlayerGame() {
        String winner = "";
        String message = "";

        if (player1Score > player2Score) {
            winner = "🏆 PLAYER 1 WINS! 🏆";
            message = "Player 1 is the champion!";
            tvGameOverTitle.setTextColor(ContextCompat.getColor(this, R.color.gradient_end_green));
        } else {
            winner = "🏆 PLAYER 2 WINS! 🏆";
            message = "Player 2 is the champion!";
            tvGameOverTitle.setTextColor(ContextCompat.getColor(this, R.color.gradient_end_green));
        }

        tvGameOverTitle.setText(winner);
        tvGameOverMessage.setText(message);
        tvFinalScore.setText("Final Score: " + player1Score + " - " + player2Score);

        twoPlayerLayout.setVisibility(View.GONE);
        gameOverLayout.setVisibility(View.VISIBLE);
    }

    private void playAgain() {
        gameOverLayout.setVisibility(View.GONE);

        if (gameMode.equals("computer")) {
            resetVsComputerGame();
            vsComputerLayout.setVisibility(View.VISIBLE);
        } else if (gameMode.equals("twoPlayer")) {
            resetTwoPlayerGame();
            twoPlayerLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showMainMenu() {
        // Hide all game layouts, show mode selection
        difficultyLayout.setVisibility(View.GONE);
        vsComputerLayout.setVisibility(View.GONE);
        twoPlayerLayout.setVisibility(View.GONE);
        gameOverLayout.setVisibility(View.GONE);
        modeSelectionLayout.setVisibility(View.VISIBLE);

        // Reset all game states
        resetVsComputerGame();
        resetTwoPlayerGame();
    }

    private void animateChoice(TextView textView) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(textView, "scaleX", 0.5f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(textView, "scaleY", 0.5f, 1.2f, 1.0f);
        scaleX.setDuration(500);
        scaleY.setDuration(500);
        scaleX.start();
        scaleY.start();
    }

    private void animateVictory(TextView textView) {
        ObjectAnimator rotate = ObjectAnimator.ofFloat(textView, "rotation", 0f, 360f);
        rotate.setDuration(1000);
        rotate.start();
    }

    private void loadGameStats() {
        gamesPlayed = preferences.getInt("gamesPlayed", 0);
        gamesWon = preferences.getInt("gamesWon", 0);
        gamesLost = preferences.getInt("gamesLost", 0);
        draws = preferences.getInt("draws", 0);
    }

    private void saveGameStats() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("gamesPlayed", gamesPlayed);
        editor.putInt("gamesWon", gamesWon);
        editor.putInt("gamesLost", gamesLost);
        editor.putInt("draws", draws);
        editor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveGameStats();
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