package com.example.gamezone;

public class Card {
    private final int colorResId;
    private boolean isFlipped;
    private boolean isMatched;

    public Card(int colorResId) {
        this.colorResId = colorResId;
        this.isFlipped = false;
        this.isMatched = false;
    }

    public int getColorResId() {
        return colorResId;
    }

    public boolean isFlipped() {
        return isFlipped;
    }

    public void setFlipped(boolean flipped) {
        isFlipped = flipped;
    }

    public boolean isMatched() {
        return isMatched;
    }

    public void setMatched(boolean matched) {
        isMatched = matched;
    }
}