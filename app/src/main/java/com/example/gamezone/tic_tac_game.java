package com.example.gamezone;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.gamezone.databinding.ActivityTicTacGameBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class tic_tac_game extends AppCompatActivity {

    private static final long TIME_LIMIT = 20000;
    private final List<int[]> combinationList = new ArrayList<>();
    private ActivityTicTacGameBinding binding;
    private int[] boxPositions = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    private int playerTurn = 1;
    private int playerOneScore = 0;
    private int playerTwoScore = 0;
    private boolean isComputerMode = false;
    private String difficultyLevel = "Easy";
    private CountDownTimer countDownTimer;
    private boolean isResultDialogOpen = false;

    private int playerOneSymbol = R.drawable.tc_heart_red;
    private int playerTwoSymbol = R.drawable.tc_heart_blue;

    private MediaPlayer winSound;
    private MediaPlayer clickSound;

    private Handler winAnimationHandler = new Handler();
    private Runnable showWinnerDialogRunnable;

    // Track winning animation state
    private boolean isWinningAnimationPlaying = false;
    private List<ImageView> animatingBoxes = new ArrayList<>();
    private List<AnimatorSet> activeAnimations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicTacGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupFullScreen();
        initializeSound();
        initializeGame();
        winAnimationHandler = new Handler();
    }

    private void setupFullScreen() {
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
    }

    private void initializeSound() {
        try {
//            winSound = MediaPlayer.create(this, R.raw.win_sound);
            clickSound = MediaPlayer.create(this, R.raw.click_sound);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeGame() {
        setupWinningCombinations();
        loadPlayerData();
        resetGameState();
        setClickListeners();
    }

    private void setupWinningCombinations() {
        combinationList.clear();
        combinationList.add(new int[]{0, 1, 2});
        combinationList.add(new int[]{3, 4, 5});
        combinationList.add(new int[]{6, 7, 8});
        combinationList.add(new int[]{0, 3, 6});
        combinationList.add(new int[]{1, 4, 7});
        combinationList.add(new int[]{2, 5, 8});
        combinationList.add(new int[]{2, 4, 6});
        combinationList.add(new int[]{0, 4, 8});
    }

    private void loadPlayerData() {
        Intent intent = getIntent();
        if (intent != null) {
            String playerOneName = intent.getStringExtra("playerOne");
            String playerTwoName = intent.getStringExtra("playerTwo");
            isComputerMode = intent.getBooleanExtra("isComputerMode", false);
            difficultyLevel = intent.getStringExtra("difficultyLevel");

            playerOneSymbol = intent.getIntExtra("playerOneSymbol", R.drawable.tc_heart_red);
            playerTwoSymbol = intent.getIntExtra("playerTwoSymbol", R.drawable.tc_heart_blue);

            String displayPlayerOneName = (playerOneName != null && !playerOneName.trim().isEmpty())
                    ? playerOneName : "Player 1";
            String displayPlayerTwoName;

            if (isComputerMode) {
                displayPlayerTwoName = "Computer";
            } else {
                displayPlayerTwoName = (playerTwoName != null && !playerTwoName.trim().isEmpty())
                        ? playerTwoName : "Player 2";
            }

            binding.playerOneName.setText(displayPlayerOneName);
            binding.playerTwoName.setText(displayPlayerTwoName);

            binding.changePlayer1GameSymbol.setImageResource(playerOneSymbol);
            binding.changePlayer2GameSymbol.setImageResource(playerTwoSymbol);
        }
    }

    private void resetGameState() {
        boxPositions = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        playerTurn = 1;
        isResultDialogOpen = false;

        // CRITICAL: Stop all animations before resetting
        stopAllAnimations();

        resetBoardUI();
        changePlayerTurn(1);
        startCountdownTimer();

        if (showWinnerDialogRunnable != null) {
            winAnimationHandler.removeCallbacks(showWinnerDialogRunnable);
            showWinnerDialogRunnable = null;
        }
    }

    // CRITICAL: Completely stop and remove all animations
    private void stopAllAnimations() {
        // Stop all active AnimatorSets
        for (AnimatorSet animator : activeAnimations) {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
        }
        activeAnimations.clear();

        // Clear the list of animating boxes
        for (ImageView box : animatingBoxes) {
            if (box != null) {
                // Cancel all animations on this view
                box.animate().cancel();
                box.clearAnimation();

                // Reset all visual properties
                box.setScaleX(1f);
                box.setScaleY(1f);
                box.clearColorFilter();
                box.setBackgroundResource(R.drawable.neon_button_purple);
                box.setAlpha(1f);
                box.setVisibility(View.VISIBLE);
            }
        }
        animatingBoxes.clear();

        // Reset flag
        isWinningAnimationPlaying = false;
    }

    private void resetBoardUI() {
        int[] imageViewIds = {R.id.image1, R.id.image2, R.id.image3, R.id.image4, R.id.image5,
                R.id.image6, R.id.image7, R.id.image8, R.id.image9};
        for (int id : imageViewIds) {
            ImageView imageView = binding.getRoot().findViewById(id);
            if (imageView != null) {
                // Stop any ongoing animations
                imageView.animate().cancel();
                imageView.clearAnimation();

                // Reset all properties
                imageView.setImageDrawable(null);
                imageView.setImageResource(0);
                imageView.setBackgroundResource(R.drawable.neon_button_purple);
                imageView.setPadding(25, 25, 25, 25);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.clearColorFilter();
                imageView.setAlpha(1f);
                imageView.setScaleX(1f);
                imageView.setScaleY(1f);
                imageView.setVisibility(View.VISIBLE);
                imageView.setTranslationX(0);
                imageView.setTranslationY(0);
                imageView.setRotation(0);
            }
        }
    }

    private void startCountdownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        binding.timer.setText(String.valueOf(TIME_LIMIT / 1000));

        countDownTimer = new CountDownTimer(TIME_LIMIT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.timer.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                if (!isResultDialogOpen && !isFinishing()) {
                    handleTimeout();
                }
            }
        }.start();
    }


    private void setClickListeners() {
        if (isResultDialogOpen) return;

        binding.image1.setOnClickListener(view -> performAction(binding.image1, 0));
        binding.image2.setOnClickListener(view -> performAction(binding.image2, 1));
        binding.image3.setOnClickListener(view -> performAction(binding.image3, 2));
        binding.image4.setOnClickListener(view -> performAction(binding.image4, 3));
        binding.image5.setOnClickListener(view -> performAction(binding.image5, 4));
        binding.image6.setOnClickListener(view -> performAction(binding.image6, 5));
        binding.image7.setOnClickListener(view -> performAction(binding.image7, 6));
        binding.image8.setOnClickListener(view -> performAction(binding.image8, 7));
        binding.image9.setOnClickListener(view -> performAction(binding.image9, 8));
    }

    private void performAction(ImageView imageView, int selectedBoxPosition) {
        // Don't allow moves during winning animation
        if (isResultDialogOpen || boxPositions[selectedBoxPosition] != 0 || isWinningAnimationPlaying) {
            return;
        }

        playClickSound();
        animateClick(imageView);

        boxPositions[selectedBoxPosition] = playerTurn;

        if (playerTurn == 1) {
            imageView.setImageResource(playerOneSymbol);
        } else {
            imageView.setImageResource(playerTwoSymbol);
        }

        imageView.setPadding(25, 25, 25, 25);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setVisibility(View.VISIBLE);
        animateSymbolPlacement(imageView);

        if (checkResults()) {
            handleWin();
        } else if (isBoardFull()) {
            handleDraw();
        } else {
            switchPlayer();
        }
    }

    private void animateClick(ImageView imageView) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.8f, 1f, 0.8f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(150);
        scaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        imageView.startAnimation(scaleAnimation);
    }

    private void animateSymbolPlacement(ImageView imageView) {
        imageView.animate().cancel();
        imageView.setVisibility(View.VISIBLE);
        imageView.setScaleX(0.7f);
        imageView.setScaleY(0.7f);

        imageView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    // CRITICAL: Stop winning animations completely
    private void stopWinningAnimations() {
        // Stop all active AnimatorSets
        for (AnimatorSet animator : activeAnimations) {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
                animator.removeAllListeners();
            }
        }
        activeAnimations.clear();

        // Reset all animating boxes
        for (ImageView box : animatingBoxes) {
            if (box != null) {
                // Cancel all animations
                box.animate().cancel();
                box.clearAnimation();

                // Reset to original state
                box.setScaleX(1f);
                box.setScaleY(1f);
                box.clearColorFilter();
                box.setBackgroundResource(R.drawable.neon_button_purple);
                box.setAlpha(1f);

                // Force immediate layout update
                box.requestLayout();
                box.invalidate();
            }
        }
        animatingBoxes.clear();

        // Reset flag
        isWinningAnimationPlaying = false;
    }

    // CRITICAL: Only animate winning boxes and track them
    private void animateWinningBoxes(ImageView... boxes) {
        // First stop any existing winning animations completely
        stopWinningAnimations();

        // Set flag that winning animation is playing
        isWinningAnimationPlaying = true;

        for (ImageView box : boxes) {
            if (box == null || box.getDrawable() == null) continue;

            // Add to list of animating boxes
            animatingBoxes.add(box);

            // Cancel any existing animations
            box.animate().cancel();
            box.clearAnimation();
            box.clearColorFilter();
            box.setVisibility(View.VISIBLE);
            box.setAlpha(1f);

            // Create pulse animation
            AnimatorSet pulseSet = new AnimatorSet();

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(box, "scaleX", 1f, 1.05f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(box, "scaleY", 1f, 1.05f, 1f);

            scaleX.setDuration(600);
            scaleY.setDuration(600);
            scaleX.setRepeatCount(ValueAnimator.INFINITE);
            scaleY.setRepeatCount(ValueAnimator.INFINITE);
            scaleX.setRepeatMode(ValueAnimator.REVERSE);
            scaleY.setRepeatMode(ValueAnimator.REVERSE);
            scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

            pulseSet.playTogether(scaleX, scaleY);
            pulseSet.start();

            // Add to active animations list
            activeAnimations.add(pulseSet);

            int glowColor = playerTurn == 1 ?
                    ContextCompat.getColor(this, R.color.neon_blue) :
                    ContextCompat.getColor(this, R.color.neon_pink);

            box.setColorFilter(glowColor, PorterDuff.Mode.SRC_ATOP);
            box.setBackgroundResource(playerTurn == 1 ?
                    R.drawable.neon_button_blue_glow :
                    R.drawable.neon_button_pink_glow);
        }
    }

    private void playClickSound() {
        try {
            if (clickSound != null) {
                if (clickSound.isPlaying()) {
                    clickSound.seekTo(0);
                }
                clickSound.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleWin() {
        playWinSound();

        int[] winningCombo = getWinningCombination();
        if (winningCombo != null) {
            ImageView[] winningBoxes = new ImageView[3];
            for (int i = 0; i < 3; i++) {
                winningBoxes[i] = getImageViewByPosition(winningCombo[i]);
            }
            animateWinningBoxes(winningBoxes);
        }

        if (playerTurn == 1) {
            playerOneScore++;
        } else {
            playerTwoScore++;
        }
        updateScores();

        if (showWinnerDialogRunnable != null) {
            winAnimationHandler.removeCallbacks(showWinnerDialogRunnable);
        }

        showWinnerDialogRunnable = () -> {
            if (!isFinishing() && !isResultDialogOpen) {
                // Stop animations before showing dialog
                stopWinningAnimations();
                showResultDialog(getWinnerMessage());
            }
        };
        winAnimationHandler.postDelayed(showWinnerDialogRunnable, 2000);
    }

    private int[] getWinningCombination() {
        for (int[] combination : combinationList) {
            if (boxPositions[combination[0]] == playerTurn &&
                    boxPositions[combination[1]] == playerTurn &&
                    boxPositions[combination[2]] == playerTurn) {
                return combination;
            }
        }
        return null;
    }

    private void handleDraw() {
        showResultDialog("Match Draw");
    }

    private void playWinSound() {
        try {
            if (winSound != null) {
                if (winSound.isPlaying()) {
                    winSound.seekTo(0);
                }
                winSound.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getWinnerMessage() {
        return (playerTurn == 1 ?
                binding.playerOneName.getText() :
                binding.playerTwoName.getText()) + " is a Winner!";
    }

    private void showResultDialog(String message) {
        if (isResultDialogOpen) return;

        isResultDialogOpen = true;

        // CRITICAL: Stop all animations before showing dialog
        stopWinningAnimations();

        if (showWinnerDialogRunnable != null) {
            winAnimationHandler.removeCallbacks(showWinnerDialogRunnable);
            showWinnerDialogRunnable = null;
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
            binding.timer.setText("0");
        }

        ResultDialog resultDialog = new ResultDialog(tic_tac_game.this, message, tic_tac_game.this);
        resultDialog.setCancelable(false);
        resultDialog.show();
    }

    private void handleTimeout() {
        if (isResultDialogOpen || isFinishing()) return;

        isResultDialogOpen = true;

        // CRITICAL: Stop all animations on timeout
        stopWinningAnimations();

        if (countDownTimer != null) {
            countDownTimer.cancel();
            binding.timer.setText("0");
        }

        String playerName = playerTurn == 1 ?
                binding.playerOneName.getText().toString() :
                binding.playerTwoName.getText().toString();

        TimeoutDialog timeoutDialog = new TimeoutDialog(tic_tac_game.this, playerName);
        timeoutDialog.setCancelable(false);
        timeoutDialog.show();

        timeoutDialog.findViewById(R.id.okButton).setOnClickListener(v -> {
            if (playerTurn == 1) {
                playerTwoScore++;
            } else {
                playerOneScore++;
            }
            updateScores();
            timeoutDialog.dismiss();
            restartMatch();
        });
    }

    public void restartMatch() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // CRITICAL: Stop all animations before restarting
        stopAllAnimations();

        if (showWinnerDialogRunnable != null) {
            winAnimationHandler.removeCallbacks(showWinnerDialogRunnable);
            showWinnerDialogRunnable = null;
        }

        // Reset game state
        resetGameState();
    }

    private void switchPlayer() {
        playerTurn = playerTurn == 1 ? 2 : 1;
        changePlayerTurn(playerTurn);

        if (isComputerMode && playerTurn == 2 && !isWinningAnimationPlaying) {
            binding.getRoot().postDelayed(this::computerMove, 500);
        }
    }

    private void computerMove() {
        if (isResultDialogOpen || isFinishing() || isWinningAnimationPlaying) return;

        int bestMove = -1;
        switch (difficultyLevel) {
            case "Medium":
                bestMove = getMediumMove();
                break;
            case "Hard":
                bestMove = findBestMove();
                break;
            default:
                bestMove = getRandomMove();
        }

        if (bestMove != -1) {
            ImageView imageView = getImageViewByPosition(bestMove);
            if (imageView != null) {
                performAction(imageView, bestMove);
            }
        }
    }

    private int getRandomMove() {
        List<Integer> availableBoxes = new ArrayList<>();
        for (int i = 0; i < boxPositions.length; i++) {
            if (boxPositions[i] == 0) {
                availableBoxes.add(i);
            }
        }
        return availableBoxes.isEmpty() ? -1 :
                availableBoxes.get(new Random().nextInt(availableBoxes.size()));
    }

    private int getMediumMove() {
        int winningMove = findWinningMove(2);
        if (winningMove != -1) return winningMove;

        int blockingMove = findWinningMove(1);
        if (blockingMove != -1) return blockingMove;

        return getRandomMove();
    }

    private int findWinningMove(int player) {
        for (int i = 0; i < boxPositions.length; i++) {
            if (boxPositions[i] == 0) {
                boxPositions[i] = player;
                boolean isWin = checkResultsForPlayer(player);
                boxPositions[i] = 0;
                if (isWin) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int bestMove = -1;

        for (int i = 0; i < boxPositions.length; i++) {
            if (boxPositions[i] == 0) {
                boxPositions[i] = 2;
                int score = minimax(boxPositions, 0, false);
                boxPositions[i] = 0;

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = i;
                }
            }
        }
        return bestMove;
    }

    private int minimax(int[] board, int depth, boolean isMaximizing) {
        if (checkResultsForPlayer(2)) return 10 - depth;
        if (checkResultsForPlayer(1)) return depth - 10;
        if (isBoardFull()) return 0;

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < board.length; i++) {
                if (board[i] == 0) {
                    board[i] = 2;
                    int score = minimax(board, depth + 1, false);
                    board[i] = 0;
                    bestScore = Math.max(score, bestScore);
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < board.length; i++) {
                if (board[i] == 0) {
                    board[i] = 1;
                    int score = minimax(board, depth + 1, true);
                    board[i] = 0;
                    bestScore = Math.min(score, bestScore);
                }
            }
            return bestScore;
        }
    }

    private boolean checkResultsForPlayer(int player) {
        for (int[] combination : combinationList) {
            if (boxPositions[combination[0]] == player &&
                    boxPositions[combination[1]] == player &&
                    boxPositions[combination[2]] == player) {
                return true;
            }
        }
        return false;
    }

    private boolean isBoardFull() {
        for (int position : boxPositions) {
            if (position == 0) return false;
        }
        return true;
    }

    private ImageView getImageViewByPosition(int position) {
        switch (position) {
            case 0: return binding.image1;
            case 1: return binding.image2;
            case 2: return binding.image3;
            case 3: return binding.image4;
            case 4: return binding.image5;
            case 5: return binding.image6;
            case 6: return binding.image7;
            case 7: return binding.image8;
            case 8: return binding.image9;
            default: return null;
        }
    }

    private void changePlayerTurn(int currentPlayerTurn) {
        playerTurn = currentPlayerTurn;
        if (playerTurn == 1) {
            binding.playerOneLayout.setBackgroundResource(R.drawable.neon_button_bg);
            binding.playerTwoLayout.setBackgroundResource(R.drawable.player_turn_active);
        } else {
            binding.playerTwoLayout.setBackgroundResource(R.drawable.neon_button_bg);
            binding.playerOneLayout.setBackgroundResource(R.drawable.player_turn_active);
        }
    }

    private boolean checkResults() {
        return checkResultsForPlayer(playerTurn);
    }

    private void updateScores() {
        binding.playerOnePoint.setText(String.valueOf(playerOneScore));
        binding.playerTwoPoint.setText(String.valueOf(playerTwoScore));

        if ((playerOneScore >= 4 || playerTwoScore >= 4) && !isResultDialogOpen) {
            isResultDialogOpen = true;

            // CRITICAL: Stop all animations before showing match over dialog
            stopAllAnimations();

            String winner = playerOneScore >= 4 ?
                    binding.playerOneName.getText().toString() :
                    binding.playerTwoName.getText().toString();

            playWinSound();

            MatchOverDialog matchOverDialog = new MatchOverDialog(tic_tac_game.this, winner, winSound);
            matchOverDialog.setCancelable(false);
            matchOverDialog.show();
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
                    Intent intent = new Intent(this, tic_tac_home.class);
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
    @Override
    protected void onDestroy() {
        releaseMediaPlayers();
        cancelTimer();

        // CRITICAL: Stop all animations on destroy
        stopAllAnimations();

        if (winAnimationHandler != null) {
            winAnimationHandler.removeCallbacksAndMessages(null);
        }

        super.onDestroy();
    }

    private void releaseMediaPlayers() {
        if (winSound != null) {
            winSound.release();
            winSound = null;
        }
        if (clickSound != null) {
            clickSound.release();
            clickSound = null;
        }
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}