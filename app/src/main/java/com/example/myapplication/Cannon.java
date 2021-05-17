package com.example.myapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

public class Cannon {
    private int baseRadius;
    private int barrelLength;
    private Point barrelEnd = new Point(); // end point of Cannons barrel
    private double barrelAngle;
    private CannonBall cannonBall; // the Cannon cannon's ball
    private Paint paint = new Paint();
    private CannonView cannonView;// view containing the Cannon

    public Cannon(CannonView cannonView,  int baseRadius, int barrelLength, int barrelWidth) {
        this.baseRadius = baseRadius;
        this.barrelLength = barrelLength;
        this.cannonView = cannonView;
        paint.setStrokeWidth(barrelWidth); // set width of barrel
        paint.setColor(Color.BLACK);
        align(Math.PI/2);
    }// aligns the cannon's barrel to the given angle


    protected void align(double barrelAngle) {
        this.barrelAngle = barrelAngle;

        barrelEnd.x = (int) ( barrelLength * Math.sin(barrelAngle));
        barrelEnd.y = (int) (- barrelLength * Math.cos(barrelAngle)) + cannonView.getScreenHeight()/2;


    }

    public void fireCannonball(){

        // calculates cannon ball velocity's x component

        int velocityX = (int) (CannonView.CANNON_BALL_SPEED_PERCENT * cannonView.getScreenWidth()*Math.sin(barrelAngle));

        int velocityY = (int) ( CannonView.CANNON_BALL_SPEED_PERCENT * cannonView.getScreenWidth()* - Math.cos(barrelAngle));

        int radius = (int)  (cannonView.getScreenHeight() * CannonView.CANNON_BALL_RADIUS_PERCENT);

        // CONSTRUCT CANNON BALL AND POSITION IT IN THE CANNON

        cannonBall = new CannonBall(cannonView, Color.BLACK, cannonView.CANNON_SOUND_ID, -radius,
                        cannonView.getScreenHeight()/2 - radius, radius, velocityX, velocityY);
        cannonBall.playSound();
    }
    public void draw(Canvas canvas){
        //draw the cannon barrel
        canvas.drawLine(0, cannonView.getScreenHeight()/2, barrelEnd.x,barrelEnd.y,paint);
        // draw the cannon base
        canvas.drawCircle(0,(int) cannonView.getScreenHeight()/2, (int) baseRadius, paint);
    }

    public CannonBall getCannonBall(){
        return cannonBall;

    }
    public void removeCannonBall(){
        cannonBall = null;
    }



}
