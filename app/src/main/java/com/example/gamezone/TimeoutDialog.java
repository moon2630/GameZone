package com.example.gamezone;


import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

public class TimeoutDialog extends Dialog {

    public TimeoutDialog(@NonNull Context context, String playerName) {
        super(context);
        setContentView(R.layout.dialog_timeout);

        TextView messageText = findViewById(R.id.messageText);
        messageText.setText(playerName + ", you lose this round because your time limit is over.");

        AppCompatButton okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(v -> dismiss());
    }
}