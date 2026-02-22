package com.example.gamezone;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class chess_setup extends AppCompatActivity {

    private Button btnTwoPlayers, btnComputer, btnStartGame;
    private LinearLayout twoPlayerLayout, computerLayout;
    private EditText player1Name, player2Name, player1Computer;
    private RadioGroup difficultyGroup;
    private RadioButton radioEasy, radioMedium, radioHard;

    private boolean isComputerMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_setup);


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

        initializeViews();
        setupListeners();

        // Default mode: Two Players
        setTwoPlayerMode();
    }

    private void initializeViews() {
        // Buttons
        btnTwoPlayers = findViewById(R.id.btnTwoPlayers);
        btnComputer = findViewById(R.id.btnComputer);
        btnStartGame = findViewById(R.id.btnStartGame);

        // Layouts
        twoPlayerLayout = findViewById(R.id.twoPlayerLayout);
        computerLayout = findViewById(R.id.computerLayout);

        // Two Player mode inputs
        player1Name = findViewById(R.id.player1Name);
        player2Name = findViewById(R.id.player2Name);

        // Computer mode inputs
        player1Computer = findViewById(R.id.player1Computer);
        difficultyGroup = findViewById(R.id.difficultyGroup);
        radioEasy = findViewById(R.id.radioEasy);
        radioMedium = findViewById(R.id.radioMedium);
        radioHard = findViewById(R.id.radioHard);
    }

    private void setupListeners() {
        btnTwoPlayers.setOnClickListener(v -> setTwoPlayerMode());

        btnComputer.setOnClickListener(v -> setComputerMode());

        btnStartGame.setOnClickListener(v -> startChessGame());
    }

    private void setTwoPlayerMode() {
        isComputerMode = false;

//        // Update button states
//        btnTwoPlayers.setBackgroundResource(R.drawable.button_background_medium);
//        btnComputer.setBackgroundResource(R.drawable.button_background_restart);

        // Show two player layout, hide computer layout
        twoPlayerLayout.setVisibility(View.VISIBLE);
        computerLayout.setVisibility(View.GONE);

        // Set default names
        player1Name.setText("Player 1");
        player2Name.setText("Player 2");
    }

    private void setComputerMode() {
        isComputerMode = true;

//        // Update button states
//        btnTwoPlayers.setBackgroundResource(R.drawable.button_background_medium);
//        btnComputer.setBackgroundResource(R.drawable.button_background_restart);

        // Show computer layout, hide two player layout
        twoPlayerLayout.setVisibility(View.GONE);
        computerLayout.setVisibility(View.VISIBLE);

        // Set default name for player
        player1Computer.setText("Player 1");

        // Set default difficulty to Easy
        radioEasy.setChecked(true);
    }

    private void startChessGame() {
        Intent intent = new Intent(this, chess_game.class);

        if (isComputerMode) {
            // Computer mode
            String playerName = player1Computer.getText().toString().trim();
            if (playerName.isEmpty()) {
                playerName = "Player 1";
            }

            String difficulty = "Easy"; // Default
            int selectedId = difficultyGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.radioEasy) {
                difficulty = "Easy";
            } else if (selectedId == R.id.radioMedium) {
                difficulty = "Medium";
            } else if (selectedId == R.id.radioHard) {
                difficulty = "Hard";
            }

            intent.putExtra("player1Name", playerName);
            intent.putExtra("player2Name", "Computer");
            intent.putExtra("isComputerMode", true);
            intent.putExtra("difficultyLevel", difficulty);

        } else {
            // Two player mode
            String p1Name = player1Name.getText().toString().trim();
            if (p1Name.isEmpty()) {
                p1Name = "Player 1";
            }

            String p2Name = player2Name.getText().toString().trim();
            if (p2Name.isEmpty()) {
                p2Name = "Player 2";
            }

            intent.putExtra("player1Name", p1Name);
            intent.putExtra("player2Name", p2Name);
            intent.putExtra("isComputerMode", false);
            intent.putExtra("difficultyLevel", "Medium"); // Not used in two player mode
        }

        // Start the game
        startActivity(intent);
        finish();
    }


    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}