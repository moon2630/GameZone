package com.example.gamezone;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

public class MatchOverDialog extends Dialog {

    public MatchOverDialog(@NonNull Context context, String winnerName, MediaPlayer winSound) {
        super(context);
        setContentView(R.layout.dialog_match_over);

        TextView messageText = findViewById(R.id.messageText);
        messageText.setText(winnerName + " wins the match!");

        AppCompatButton okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(v -> {
            // ✅ Stop sound if it's still playing
            if (winSound != null && winSound.isPlaying()) {
                winSound.stop();
                winSound.release(); // Optional: release resources
            }

            context.startActivity(new Intent(context, tic_tac_home.class));
            dismiss();
        });
    }
}

