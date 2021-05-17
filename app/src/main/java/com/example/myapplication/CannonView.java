package com.example.myapplication;




import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.DialogFragment;



import java.util.ArrayList;
import java.util.Random;

@SuppressWarnings("ALL")
public class CannonView extends SurfaceView implements SurfaceHolder.Callback {


    // CONSTANTS FOR THE CANNON
    public static final int MISS_PENALTY = 2;
    public static final int HIT_REWARD = 3;

    public static final double CANNON_BASE_RADIUS_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_WIDTH_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_LENGTH_PERCENT = 1.0 / 10;

    // constants for the Cannonball
    public static final double CANNON_BALL_SPEED_PERCENT = 3.0 / 2;
    public static final double CANNON_BALL_RADIUS_PERCENT = 3.0 / 80;

    //constants for the targets
    private static final double TARGET_WIDTH_PERCENT = 1.0 / 40;
    private static final double TARGET_LENGTH_PERCENT = 3.0 / 20;
    private static final double TARGET_FIRST_X_PERCENT = 3.0 / 5;
    private static final double TARGET_SPACING_PERCENT = 1.0 / 60;

    public static final double TARGET_PIECES = 9;
    public static final double TARGET_MIN_SPEED_PERCENT = 3.0 / 4;
    public static final double TARGET_MAX_SPEED_PERCENT = 6.0 / 4;

    //constants for the Blocker
    public static final double BLOCKER_WIDTH_PERCENT = 1.0 / 40;
    public static final double BLOCKER_LENGTH_PERCENT = 1.0 / 4;
    public static final double BLOCKER_X_PERCENT = 1.0 / 2;
    public static final double BLOCKER_SPEED_PERCENT = 1.0;

    // text size 1/18 of screen width
    public static final double TEXT_SIZE_PERCENT = 1.0 / 18;


    private CannonThread cannonThread;
    public boolean dialogIsDisplayed = false;
    private AppCompatActivity activity; // to display Game Over dialog in GUE thread

    // game objects
    private Cannon cannon;
    private Blocker blocker;
    private ArrayList<Target> targets;

    // dimension variables
    private int screenWidth;
    private int screenHeight;

    // variables for the game loop and tracking statistics
    private boolean gameOver; // is the game over?
    private double timeLeft; // time remaining in seconds
    private int shotsFired; // shots the user has fired
    private double totalElapsedTime; // elapsed seconds

    //constants and variables for managing sounds
    public static final int TARGET_SOUND_ID = 0;
    public static final int BLOCKER_SOUND_ID = 1;

    public static final int CANNON_SOUND_ID = 2;
    private static final String TAG = "CannonView";

    private SoundPool soundPool;
    private SparseIntArray soundMap; // maps IDs to SoundPool

    // private variables used when drawing each item on the screen
    private Paint textPaint;
    private Paint backgroundPaint;



    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);

        activity = (AppCompatActivity) context;
        getHolder().addCallback(this);

        // configure audio attributes for game audio
        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        attrBuilder.setUsage(AudioAttributes.USAGE_GAME);

        // initialize SoundPool to play the app's three sound effects
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(1);
        builder.setAudioAttributes(attrBuilder.build());
        soundPool = builder.build();

        // create Map of sounds and pre - load sounds
        soundMap = new SparseIntArray(3);
        soundMap.put(CANNON_SOUND_ID, soundPool.load(context, R.raw.guncannon, 1));
        soundMap.put(BLOCKER_SOUND_ID, soundPool.load(context, R.raw.metal_hit, 1));
        soundMap.put(TARGET_SOUND_ID, soundPool.load(context, R.raw.boom, 1));

        textPaint = new Paint();
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;
        textPaint.setTextSize((int) (TEXT_SIZE_PERCENT * screenHeight));
        textPaint.setAntiAlias(true); // smoothes the text
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public double getScreenWidth() {
        return screenWidth;
    }

    public void playSound(int soundID) {
        soundPool.play(soundMap.get(soundID), 1, 1, 1, 0, 1f);

    }



    public void newGame() {
        // construct a new Cannon
        cannon = new Cannon(this,
                (int) (CANNON_BASE_RADIUS_PERCENT * screenHeight),
                (int) (CANNON_BARREL_LENGTH_PERCENT * screenWidth),
                (int) (CANNON_BARREL_WIDTH_PERCENT * screenHeight));


        // construct a new Cannon
        Random random = new Random();
        targets = new ArrayList<>(); // construct a new target

        // initialize targetX for the first Target from the left
        int targetX = (int) (TARGET_FIRST_X_PERCENT * screenWidth);

        // calculate Y coordinate of Targets
        int targetY = (int) ((0.5 - TARGET_LENGTH_PERCENT / 2) * screenHeight);

        // add TARGET_PIECES Targets to the Target list
        for (int n = 0; n < TARGET_PIECES; n++) {
            // determine a random velocity between min and max values
            //for Target n
            double velocity = screenHeight * (random.nextDouble() * (TARGET_MAX_SPEED_PERCENT - TARGET_MIN_SPEED_PERCENT) + TARGET_MIN_SPEED_PERCENT);

            // Alternate Target colors between dark and light
            int color = (n % 2 == 0) ? getResources().getColor(R.color.dark, getContext().getTheme()) :
                    getResources().getColor(R.color.light, getContext().getTheme());

            velocity *= -1;

            targets.add(new Target(this, color, HIT_REWARD, targetX, targetY,
                    (int) (TARGET_WIDTH_PERCENT * screenWidth), (int) (TARGET_LENGTH_PERCENT * screenHeight), (int) velocity

            ));

            targetX += (TARGET_WIDTH_PERCENT + TARGET_SPACING_PERCENT) * screenWidth;

        }

        blocker = new Blocker(this, Color.BLACK, MISS_PENALTY,

                (int) (BLOCKER_X_PERCENT * screenWidth),
                (int) ((0.5 - BLOCKER_LENGTH_PERCENT / 2) * screenHeight),
                (int) (BLOCKER_WIDTH_PERCENT * screenWidth),
                (int) (BLOCKER_LENGTH_PERCENT * screenHeight),
                (float) (BLOCKER_SPEED_PERCENT * screenHeight));

        timeLeft = 10; // start the countdown at 10 seconds
        shotsFired = 0;
        totalElapsedTime = 0.0; //

        if (gameOver) {
            gameOver = false;
            cannonThread = new CannonThread(getHolder());
            cannonThread.start(); // start the game loop;

        }
        hideSystemBars();


    }

    private void updatePositions(double elapsedTimeMS) {
        double interval = elapsedTimeMS / 1000;

        if (cannon.getCannonBall() != null)
            cannon.getCannonBall().update(interval);

        blocker.update(interval);

        for (GameElement target : targets)
            target.update(interval);

        timeLeft -= interval;

        if (timeLeft <= 0) {
            timeLeft = 0.0;
            gameOver = true;
            cannonThread.setRunning(false);
            showGameOverDialog(R.string.lose);

            if (targets.isEmpty()) {
                cannonThread.setRunning(false);
                showGameOverDialog(R.string.win);
                gameOver = true;
            }
        }


    }

    public void alignAndFireCannonball(MotionEvent event) {
        Point touchPoint = new Point((int) event.getX(), (int) event.getY());
        //compute the touch's distance from center of the screen on the y axis

        double centerMinusY = (screenHeight / 2 - touchPoint.y);
        double angle = 0;

        // calculates the angle the barrel makes with the horizontal
        angle = Math.atan2(touchPoint.x, centerMinusY);
        // point the barrel at the point where


        // point the b arrel at the point where the screen was touched
        cannon.align(angle);

        // fire Cannonball if there is not already a cannonball on screen

        if (cannon.getCannonBall() == null || !cannon.getCannonBall().isOnScreen()) {
            cannon.fireCannonball();
            ++shotsFired;

        }
    }





    private void showGameOverDialog(final int messageId) {

      final MyAlertDialogFragment gameResult = new MyAlertDialogFragment(getShotsFired(), getTotalElapsedTime(), getActivity());



        // in Gui thread use fragment manager to display the Dialog fragment
        activity.runOnUiThread(new Runnable() {
                                    @Override
                                   public void run() {
                                       showSystemBars();

                                       dialogIsDisplayed = true;
                                       gameResult.setCancelable(false);
                                       gameResult.show(activity.getFragmentManager(), "dialog");


                                   }

                               }

        );

    }

    public int getShotsFired(){
        return shotsFired;
    }
    public double getTotalElapsedTime(){
        return totalElapsedTime;
    }
    private AppCompatActivity getActivity(){
        return activity;
    }



    public void drawGameElements(Canvas canvas) {
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
        canvas.drawText(getResources().getString(R.string.time_remaining_format, timeLeft), 50, 100, textPaint);
        cannon.draw(canvas);  // draw the cannon

        /// draw the Game elements
        if (cannon.getCannonBall() != null && cannon.getCannonBall().isOnScreen())
            cannon.getCannonBall().draw(canvas);


        blocker.draw(canvas);

        for (GameElement target : targets)
            target.draw(canvas);
    }

    public void testForCollision() {

        if (cannon.getCannonBall() != null && cannon.getCannonBall().isOnScreen()) {
            for (int n = 0; n < targets.size(); n++) {
                if (cannon.getCannonBall().collidesWith(targets.get(n))) {
                    targets.get(n).playSound();

                    timeLeft += targets.get(n).getHitReward();

                    cannon.removeCannonBall();

                    targets.remove(n);

                    --n; // ensures we dont skip testing new target n
                    break;
                }
            }
        } else { // remove the Cannonball if it should not be on the screen

            cannon.removeCannonBall();
        }
        if (cannon.getCannonBall()  != null && cannon.getCannonBall().collidesWith(blocker)){
            blocker.playSound();

            cannon.getCannonBall().reverseVelocityX();

            timeLeft -= blocker.getMissPenalty();


        }
    }
     public void stopGame(){
        if(cannonThread != null){
            cannonThread.setRunning(false);
        }
     }

     public void releaseResources(){
        soundPool.release();
        soundPool = null;
     }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if(!dialogIsDisplayed){

            newGame();
            cannonThread = new CannonThread(holder);

            cannonThread.setRunning(true);

            cannonThread.start();
        }

    }

    @Override
    public void surfaceChanged (@NonNull SurfaceHolder holder,int format, int width, int height){

    }

    @Override
    public void surfaceDestroyed (@NonNull SurfaceHolder holder) {

            boolean retry = true;
            cannonThread.setRunning(false);

            while (retry) {
                try {
                    cannonThread.join();

                    retry = false;
                } catch (InterruptedException e) {
                    Log.e(TAG, "thread interruption", e);
                }
            }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        int action = event.getAction();
        if(action == MotionEvent. ACTION_DOWN || action == MotionEvent.ACTION_MOVE){
            alignAndFireCannonball(event);
        }
    return true;
    }

    private class CannonThread extends Thread{

        private SurfaceHolder surfaceHolder;

        private boolean threadIsRunning = true;

        public CannonThread(SurfaceHolder holder){
            surfaceHolder = holder;
            setName("CannonThread");

        }

        private void setRunning(boolean running){
            threadIsRunning = running;
        }

        @Override
        public void run() {

            Canvas canvas = null;
            long previousFrametime = System.currentTimeMillis();

            while(threadIsRunning){
                try {
                    canvas = surfaceHolder.lockCanvas();

                    synchronized (surfaceHolder) {
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMs = currentTime - previousFrametime;
                        totalElapsedTime += elapsedTimeMs / 1000.0;

                        updatePositions(elapsedTimeMs);
                        testForCollision();
                        drawGameElements(canvas);
                        previousFrametime = currentTime;
                    }
                }
                finally {
                    if(canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private void hideSystemBars(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
            setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    }

    private void showSystemBars(){

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN

            );
        }
    }

}