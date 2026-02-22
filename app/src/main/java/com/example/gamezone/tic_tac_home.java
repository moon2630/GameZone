package com.example.gamezone;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;

public class tic_tac_home extends AppCompatActivity {

    private EditText etPlayerOne, etPlayerTwo;
    private ImageView ivPlayerOneSymbol, ivPlayerTwoSymbol;
    private int currentSymbolSet = 0;

    // Symbol pairs for Player 1 and Player 2
    private final int[][] symbolPairs = {
            {R.drawable.tc_heart_red, R.drawable.tc_heart_blue},
            {R.drawable.tc_pizza, R.drawable.tc_burger},
            {R.drawable.tc_chocolate, R.drawable.tc_ice_cream_cone},
            {R.drawable.tc_cat, R.drawable.tc_dog},
            {R.drawable.tc_apple, R.drawable.tc_banana},
            {R.drawable.tc_ghost, R.drawable.tc_pumpkin},
            {R.drawable.tc_black_cat, R.drawable.tc_white_cat},
            {R.drawable.tc_cookie, R.drawable.tc_candy},
            {R.drawable.tc_beer3, R.drawable.tc_beer_bottle}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tic_tac_home);

        setupFullScreen();
        initializeViews();
        setListeners();
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

    private void initializeViews() {
        etPlayerOne = findViewById(R.id.playerOne);
        etPlayerTwo = findViewById(R.id.playerTwo);
        ivPlayerOneSymbol = findViewById(R.id.change_player1_symbol);
        ivPlayerTwoSymbol = findViewById(R.id.change_player2_symbol);
    }

    private void setListeners() {
        findViewById(R.id.startGameButton).setOnClickListener(v -> startTwoPlayerGame());
        findViewById(R.id.computerButton).setOnClickListener(v -> showDifficultyDialog());
        findViewById(R.id.change_symbol).setOnClickListener(v -> changeSymbols());
    }

    private void showDifficultyDialog() {
        // Create the dialog layout
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setBackgroundColor(Color.WHITE);
        dialogLayout.setPadding(50, 50, 50, 35);

        // Title with BLUE color (your requested change)
        TextView title = new TextView(this);
        title.setText("SELECT DIFFICULTY");
        title.setTypeface(ResourcesCompat.getFont(this, R.font.caudex), Typeface.BOLD);
        title.setTextSize(22);
        title.setPadding(0, 0, 0, 20);
        title.setTextColor(Color.BLUE); // Changed to BLUE
        title.setShadowLayer(15, 0, 0, Color.parseColor("#3366CC")); // Light blue shadow
        title.setGravity(Gravity.CENTER);
        title.setLetterSpacing(0.1f);

        // Subtitle
        TextView subtitle = new TextView(this);
        subtitle.setText("Choose your challenge level");
        subtitle.setTypeface(ResourcesCompat.getFont(this, R.font.caudex));
        subtitle.setTextSize(14);
        subtitle.setPadding(0, 0, 0, 30);
        subtitle.setTextColor(Color.GRAY);
        subtitle.setGravity(Gravity.CENTER);

        // Buttons container
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.VERTICAL);
        buttonsLayout.setPadding(20, 10, 20, 0);

        // Difficulty levels - REMOVED emojis and colors (your requested change)
        String[] difficultyOptions = {"Easy", "Medium", "Hard"};

        AlertDialog[] dialogHolder = new AlertDialog[1];

        for (int i = 0; i < difficultyOptions.length; i++) {
            String difficulty = difficultyOptions[i];

            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            buttonLayout.setPadding(40, 25, 20, 25);
            buttonLayout.setBackgroundResource(R.drawable.neon_button_bg);
            buttonLayout.setElevation(5);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 0, 0, 15);
            buttonLayout.setLayoutParams(layoutParams);

            // Difficulty name TextView
            TextView difficultyText = new TextView(this);
            difficultyText.setText(difficulty);
            difficultyText.setTypeface(ResourcesCompat.getFont(this, R.font.caudex), Typeface.BOLD);
            difficultyText.setTextSize(18);

            // Set different colors for each difficulty
            switch (difficulty) {
                case "Easy":
                    difficultyText.setTextColor(Color.parseColor("#00FF00"));
                    difficultyText.setShadowLayer(10, 0, 0, Color.parseColor("#00FF00"));
                    break;
                case "Medium":
                    difficultyText.setTextColor(Color.parseColor("#FFFF00"));
                    difficultyText.setShadowLayer(10, 0, 0, Color.parseColor("#FFFF00"));
                    break;
                case "Hard":
                    difficultyText.setTextColor(Color.parseColor("#FF00FF"));
                    difficultyText.setShadowLayer(10, 0, 0, Color.parseColor("#FF00FF"));
                    break;
            }

            // Description TextView
            TextView descText = new TextView(this);
            descText.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            descText.setGravity(Gravity.END);
            descText.setTypeface(ResourcesCompat.getFont(this, R.font.caudex));
            descText.setTextSize(12);

            switch (difficulty) {
                case "Easy":
                    descText.setText("vs Beginner");
                    descText.setTextColor(Color.parseColor("#00FF00"));
                    break;
                case "Medium":
                    descText.setText("vs Intermediate");
                    descText.setTextColor(Color.parseColor("#FFFF00"));
                    break;
                case "Hard":
                    descText.setText("vs Expert");
                    descText.setTextColor(Color.parseColor("#FF00FF"));
                    break;
            }

            buttonLayout.addView(difficultyText);
            buttonLayout.addView(descText);

            int index = i;
            buttonLayout.setOnClickListener(v -> {
                String selectedDifficulty = difficultyOptions[index];
                Intent intent = new Intent(tic_tac_home.this, tic_tac_game.class);
                intent.putExtra("playerOne", "You");
                intent.putExtra("playerTwo", "Computer");
                intent.putExtra("isComputerMode", true);
                intent.putExtra("difficultyLevel", selectedDifficulty);
                intent.putExtra("playerOneSymbol", symbolPairs[currentSymbolSet][0]);
                intent.putExtra("playerTwoSymbol", symbolPairs[currentSymbolSet][1]);

                startActivity(intent);
                if (dialogHolder[0] != null) dialogHolder[0].dismiss();
            });

            // Hover effect
            buttonLayout.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setAlpha(0.7f);
                        v.setScaleX(0.98f);
                        v.setScaleY(0.98f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.setAlpha(1f);
                        v.setScaleX(1f);
                        v.setScaleY(1f);
                        break;
                }
                return false;
            });

            buttonsLayout.addView(buttonLayout);

            // REMOVED the divider line completely (your requested change)
            // No divider line added after Hard button
        }

        // Cancel button with custom style
        LinearLayout cancelLayout = new LinearLayout(this);
        cancelLayout.setOrientation(LinearLayout.HORIZONTAL);
        cancelLayout.setGravity(Gravity.CENTER);
        cancelLayout.setPadding(0, 25, 0, 0); // Increased top padding

        TextView cancelText = new TextView(this);
        cancelText.setText("CANCEL");
        cancelText.setTypeface(ResourcesCompat.getFont(this, R.font.caudex), Typeface.BOLD);
        cancelText.setTextSize(16);
        cancelText.setTextColor(Color.parseColor("#FF4444"));
        cancelText.setShadowLayer(8, 0, 0, Color.parseColor("#FFFF4444"));
        cancelText.setPadding(30, 15, 30, 15);
        cancelText.setGravity(Gravity.CENTER);

        cancelLayout.setOnClickListener(v -> {
            if (dialogHolder[0] != null) dialogHolder[0].dismiss();
        });

        cancelLayout.addView(cancelText);
        buttonsLayout.addView(cancelLayout);

        // Add all views to dialog layout
        dialogLayout.addView(title);
        dialogLayout.addView(subtitle);
        dialogLayout.addView(buttonsLayout);

        // Create and show dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .create();

        dialogHolder[0] = dialog;
        dialog.show();

        // Customize dialog window
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;

            // Set background color with rounded corners
            dialogLayout.setBackgroundColor(Color.WHITE);
            dialogLayout.setElevation(20);

            // Add rounded corners
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dialogLayout.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 30);
                    }
                });
                dialogLayout.setClipToOutline(true);
            }
        }
    }
    private void startTwoPlayerGame() {
        String playerOneName = etPlayerOne.getText().toString().trim();
        String playerTwoName = etPlayerTwo.getText().toString().trim();

        // No validation required - empty names will default to "Player 1" and "Player 2" in game activity
        Intent intent = new Intent(tic_tac_home.this, tic_tac_game.class);
        intent.putExtra("playerOne", playerOneName);
        intent.putExtra("playerTwo", playerTwoName);
        intent.putExtra("isComputerMode", false);
        intent.putExtra("playerOneSymbol", symbolPairs[currentSymbolSet][0]);
        intent.putExtra("playerTwoSymbol", symbolPairs[currentSymbolSet][1]);
        startActivity(intent);
    }



    private void changeSymbols() {
        currentSymbolSet = (currentSymbolSet + 1) % symbolPairs.length;

        // Update symbols in the home screen
        ivPlayerOneSymbol.setImageResource(symbolPairs[currentSymbolSet][0]);
        ivPlayerTwoSymbol.setImageResource(symbolPairs[currentSymbolSet][1]);

        // Show which symbol set is selected
        String[] symbolNames = {
                "❤️ Red Heart vs 💛 Yellow Heart",
                "🍕 Pizza vs 🍔 Burger",
                "🍫 Chocolate vs 🍧 Ice-cream",
                "😺 Cat vs 🐶 Dog",
                "🍎 Apple vs 🍌 Banana",
                "👻 Ghost vs 🎃 Pumpkin",
                "🐈‍⬛ Black Cat vs 🐈 White Cat",
                "🍪 Cookie vs 🍬 Candy",
                "🍺 Beer vs 🍾 Bottle"
        };

        Toast.makeText(this, symbolNames[currentSymbolSet], Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}