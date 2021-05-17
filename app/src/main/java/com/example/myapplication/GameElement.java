package com.example.myapplication;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class GameElement {

    protected CannonView view;
    protected Paint paint = new Paint();
    protected Rect shape;
    protected  float velocityY;
    private int soundId;

    public GameElement(CannonView view, int color, int soundId, int x, int y, int width, int length, float velocityY) {
        this.view = view;
        paint.setColor(color);
        this.soundId = soundId;
        shape = new Rect(x,y,x+width, y+length);
        this.velocityY = velocityY;


    }

    // update game element position and check for wall collision;
    public void update(double interval){
        // updates vertical vertical position of shape based on the vertical velocity v;
        shape.offset(0,(int) (velocityY*interval));
        // ****** used get Height instead of get Screen Height**************************************
        if(shape.top < 0 && velocityY < 0 || shape.bottom > view.getScreenHeight() && velocityY > 0){
    velocityY *= -1;
        }
    }

    public void draw(Canvas canvas){
        canvas.drawRect(shape,paint);
    }

    public void playSound(){
        view.playSound(soundId);
    }
}
