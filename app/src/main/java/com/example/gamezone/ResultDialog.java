package com.example.gamezone;


import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class ResultDialog extends Dialog {

    private final AppCompatActivity activity;
    private final String message;

    public ResultDialog(Context context, String message, AppCompatActivity activity) {
        super(context);
        this.activity = activity;
        this.message = message;
        setContentView(R.layout.activity_result_dialog);

        TextView messageText = findViewById(R.id.messageText);
        AppCompatButton restartButton = findViewById(R.id.startAgainButton);

        messageText.setText(message);

        restartButton.setOnClickListener(v -> {
            dismiss(); // Close the dialog first
            if (activity instanceof tic_tac_game) {
                ((tic_tac_game) activity).restartMatch();
            }
        });
    }
}