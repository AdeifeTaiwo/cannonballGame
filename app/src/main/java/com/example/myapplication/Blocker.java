package com.example.myapplication;

public class Blocker extends GameElement {
    private int missPenalty; // the miss penalty for this blocker


    public Blocker(CannonView view, int color, int missPenalty,  int x, int y, int width, int length,  float velocityY) {
        super(view, color, CannonView.BLOCKER_SOUND_ID, x, y, width, length, velocityY);
        this.missPenalty = missPenalty;


    }
    public int getMissPenalty(){
        return  missPenalty;
    }

}
