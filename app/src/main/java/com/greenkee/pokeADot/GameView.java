/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.greenkee.pokeADot;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class GameView extends CanvasView implements SurfaceHolder.Callback {

    public class GameThread extends CanvasThread implements Runnable {
        protected int playerScore;
        protected int currentCombo = 0;
        protected int consecutiveMisses = 0;
        protected int currentPointValue = 0;

        protected float resumeCountdown = 0;

        protected ArrayList<Ball> ballArrayList;
        protected ArrayList<FadingText>textArrayList;

        public boolean gameOver = false;
        //Paints
        protected Paint scorePaint;

        protected Bundle mBundle;

        //private boolean drawEnabled = false;

        protected String score, combo;

        protected float ballStartX, ballStartY;
        protected float startBallRadius, percentBallShrink;


        public GameThread(SurfaceHolder surfaceHolder, Context context,
                          Handler handler) {
            super(surfaceHolder, context, handler);

            scorePaint = new Paint();
            scorePaint.setARGB(255, 255, 255, 255);

            ballArrayList = new ArrayList<Ball>();
            textArrayList = new ArrayList<FadingText>();

            mBundle = new Bundle();

            playerScore = 0;
            updateScore(0);
            updateCombo();
        }

        @Override
        public void screenSizeObtained(float buffer) {
            super.screenSizeObtained(buffer);

            ballStartX = (screenWidth / 2);
            ballStartY = ((screenBottom) / 2);
            checkSettings(sharedPrefs);
            changeBallRadius(startBallRadius);


        }


        public void resetGame() {
            System.out.println("RESET GAME");
            gameOver = false;
            currentCombo = 0;
            consecutiveMisses = 0;

            textArrayList.clear();

            if(playerScore == 42){
                ((GameActivity)activity).unlockAchievement(com.greenkee.pokeADot.R.string.achievement_the_answer);
            }

            checkHighScore(playerScore);

            if (mMode != STATE_LOSE) {
                createNewGame();
            }

            try {
                c = mSurfaceHolder.lockCanvas();
                synchronized (mSurfaceHolder) {
                    synchronized (mRunLock) {
                        if (mRun) {
                            clear(c);
                            doDraw(c);
                        }
                    }
                }
            } finally {
                if (c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }

        protected void createNewGame(){
            background = new BackgroundScreen(screenWidth, screenHeight);
            setState(GameView.GameThread.STATE_READY);
            ballArrayList.clear();
            changeBallRadius(startBallRadius);
            ballArrayList.add(new Ball(ballStartX, ballStartY, Color.GREEN));
            System.out.println("BALL ADDED");

            dialogOpen = false;

            playerScore = 0;
            currentPointValue = 0;
            updateScore(0);
            updateCombo();
        }

        @Override
        public void doStart(){
            createNewGame();
        }

        public void startGame() {
            System.out.println("GAME STARTED");
            if(((GameActivity)activity).getSignedIn()){
                ((GameActivity)activity).incrementAchievement(com.greenkee.pokeADot.R.string.achievement_getting_warmed_up, 1);
                System.out.println("Getting Warmed Up Incremented");
                ((GameActivity)activity).incrementAchievement(com.greenkee.pokeADot.R.string.achievement_dedicated, 1);
            }

            if(mMode == STATE_LOSE){
                createNewGame();
            }

            setState(STATE_RUNNING);
        }

        @Override
        public void run() {
            c = null;
            try {
                Thread.sleep(500);
            } catch (Exception ignored) {
            }

            while (!mFinished) {
                try {
                    c = mSurfaceHolder.lockCanvas();
                    synchronized (mSurfaceHolder) {

                        if (mMode == STATE_READY || mMode == STATE_RUNNING || mMode == STATE_RESUMING) updateGame();
                        synchronized (mRunLock) {
                            if (mRun) {
                                clear(c);
                                if (piecesCreated) {
                                    doDraw(c);
                                }
                            }
                        }
                    }
                } finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                        if (gameOver) {
                            resetGame();
                        }
                    }
                }
                synchronized (mPauseLock) {
                    while (mPaused) {
                        try {
                            System.out.println("PAUSED");
                            mPauseLock.wait();
                        } catch (InterruptedException e) {

                        }
                    }
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }


        public int getCurrentState() {
            return mMode;
        }

        protected void setText(String s){
            Message msg = mHandler.obtainMessage();
            mBundle.putString("status_text", s.toString());
            if(s.equals("")){
                mBundle.putInt("status_viz", View.INVISIBLE);
            }else{
                mBundle.putInt("status_viz", View.VISIBLE);
            }

            msg.setData(mBundle);
            mHandler.sendMessage(msg);
        }

        @Override
        public void setState(int mode, CharSequence message) {
            synchronized (mSurfaceHolder) {
                super.setState(mode, message);

                if (mMode == STATE_RUNNING) {
                    System.out.println("GAME_RUNNING");
                    setText("");
                } else {
                    Resources res = mContext.getResources();
                    CharSequence strStatus = "";

                    if (mMode == STATE_READY) {
                        System.out.println("GAME READY");
                        //drawEnabled = false;
                        strStatus = res.getText((com.greenkee.pokeADot.R.string.mode_ready_text));
                    } else if (mMode == STATE_PAUSE) {
                        //drawEnabled = false;
                        strStatus = res.getText(com.greenkee.pokeADot.R.string.mode_paused_text);
                    } else if (mMode == STATE_LOSE) {
                        strStatus = "Game Over! \nTap to Play Again\nYou had " + ballArrayList.size()+" balls in play!";
                    } else if (mMode == STATE_RESUMING){
                        System.out.println("RESUMING GAME");
                        resumeCountdown = 3;
                        //strStatus = "Resuming...\n"+ resumeCountdown;
                    }
                    if (message != null) {
                        strStatus = message + "\n" + strStatus;
                    }

                    setText(strStatus.toString());
                }
            }
        }

        @Override
        protected void doDraw(Canvas canvas) {
            checkObjectBoundaries();
            drawBalls(myCanvas);
            drawText(myCanvas);
            canvas.drawBitmap(myCanvasBitmap, identityMatrix, null);
        }


        @Override
        protected void updateComponents(double e){
            super.updateComponents(e);
            updateBalls(e);
            updateText(e);
            background.update(e);

            if(resumeCountdown <= 0 && mMode == STATE_RESUMING){
                setState(STATE_RUNNING);
            }else if(resumeCountdown > 0){
                resumeCountdown -= e;
                int seconds = (int)(resumeCountdown + .5);
                CharSequence strStatus = "Resuming...\n"+ seconds;

                Message msg = mHandler.obtainMessage();
                mBundle.putString("status_text", strStatus.toString());
                mBundle.putInt("status_viz", View.VISIBLE);
                msg.setData(mBundle);
                mHandler.sendMessage(msg);
            }
        }


        protected void checkObjectBoundaries() {
            checkBallBoundaries();
        }

        protected void drawText(Canvas c){
            int i = 0;
            while (i < textArrayList.size()) {
                if (textArrayList.get(i).isDestroyed()) {
                    textArrayList.remove(i);
                } else {
                    textArrayList.get(i).draw(c);
                    i++;
                }
            }
        }

        protected void drawBalls(Canvas c) {
            int i = 0;
            while (i < ballArrayList.size()) {
                if (ballArrayList.get(i).isDestroyed()) {
                    ballArrayList.remove(i);
                } else {
                    ballArrayList.get(i).draw(c);
                    i++;
                }
            }
        }

        protected void updateText(double e){
            int i = 0;
            while (i < textArrayList.size()) {
                if (textArrayList.get(i).isDestroyed()) {
                    textArrayList.remove(i);
                } else {
                    FadingText text = textArrayList.get(i);

                    text.update(e);
                    i++;
                }
            }
        }

        protected void updateBalls(double e) {
            int i = 0;
            while (i < ballArrayList.size()) {
                if (ballArrayList.get(i).isDestroyed()) {
                    ballArrayList.remove(i);
                } else {
                    Ball b1 = ballArrayList.get(i);
                    /*
                    for(int j = 0; j < ballArrayList.size(); j++){
                        Ball b2 = ballArrayList.get(j);
                        if(j!= i && b1.isTouching(b2.getX(), b2.getY())){
                            if( Math.abs(b1.getX() - b2.getX()) > Math.abs(b1.getY() - b2.getY()) ){
                                if(b1.getX() > b2.getX()){
                                    b1.bounceX(b1.getX() + b1.getRadius());
                                    b2.bounceX(b2.getX() - b2.getRadius());
                                }else{
                                    b1.bounceX(b1.getX() - b1.getRadius());
                                    b2.bounceX(b2.getX() + b2.getRadius());
                                }

                            }else{
                                if(b1.getY() > b2.getY()){
                                    b1.bounceY(b1.getY() + b1.getRadius());
                                    b2.bounceY(b2.getY() - b2.getRadius());
                                }else{
                                    b1.bounceY(b1.getY() - b1.getRadius());
                                    b2.bounceY(b2.getY() + b2.getRadius());
                                }
                            }
                        }
                    }*/

                    b1.update(e);
                    i++;
                }
            }
        }

        protected void checkBallBoundaries() {
            int i = 0;
            while (i < ballArrayList.size()) {
                Ball b = ballArrayList.get(i);
                float ballx = b.getX();
                float bally = b.getY();
                float radius = b.getRadius();


                if (((ballx + radius) > (screenWidth + sideBuffer)) || ((ballx - radius) < sideBuffer)) {
                    if ((ballx + radius) > (screenWidth + sideBuffer)) {
                        b.bounceX(screenWidth + sideBuffer - radius);
                    } else {
                        b.bounceX(radius);
                    }
                }
                if (((bally + radius) > (screenHeight - bottomBuffer)) || ((bally - radius) < 0)) {
                    if ((bally + radius) > (screenHeight - bottomBuffer)) {
                        b.bounceY((screenHeight - bottomBuffer - radius));
                    } else {
                        b.bounceY((radius));
                    }

                }

                i++;
            }
        }

/*
        @Override
        protected void updateComponents(double e){
            super.updateComponents(e);
        }*/

        protected void updateScore(double d) {
            synchronized (mSurfaceHolder) {
                playerScore += (d);
                score = mContext.getString(com.greenkee.pokeADot.R.string.score_header) + playerScore;
                Message msg = mHandler.obtainMessage();
                mBundle.putString("score_text", score);
                msg.setData(mBundle);
                mHandler.sendMessage(msg);


            }
        }

        protected void updateCombo() {
            synchronized (mSurfaceHolder) {
                combo = mContext.getString(com.greenkee.pokeADot.R.string.combo_header) + currentCombo;
                Message msg = mHandler.obtainMessage();
                mBundle.putString("combo_text", combo);
                msg.setData(mBundle);
                mHandler.sendMessage(msg);


            }
        }

        public void changeBallRadius(float radius) {
            Ball.radius = radius;
            touchBuffer = Ball.radius / 2;
        }

        protected float calculateDistance(float x1, float y1, float x2, float y2) {
            return (float) (Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2)));
        }

        public void checkTouch(float x, float y) {
            System.out.println("TOUCHED");
            int i = 0;
            boolean ballTouched = false;
            if (ballArrayList.size() > 0) {
                Ball b = ballArrayList.get(i);
                int closestIndex = 0;
                float closestDistance = calculateDistance(x, y, b.getX(), b.getY());
                i++;
                while (i < ballArrayList.size()) {
                    b = ballArrayList.get(i);
                    float distance = calculateDistance(x, y, b.getX(), b.getY());
                    if(distance < closestDistance){
                        closestDistance = distance;
                        closestIndex = i;
                    }
                    i++;
                }

                b = ballArrayList.get(closestIndex);
                if (b.isVulnerable() && closestDistance < (b.getRadius() + touchBuffer)) {
                    System.out.println("BALL TOUCHED");
                    consecutiveMisses = 0;
                    if ((b.getColor() == Color.GREEN)) {


                        ballTouched = true;
                        b.destroy();
                        ballArrayList.add(0, new Ball(b.getX(), b.getY(), Color.RED));
                        ballArrayList.add(0, new Ball(b.getX(), b.getY(), Color.GREEN));
                        changeBallRadius(Ball.radius * (percentBallShrink/100) );


                        currentPointValue++;
                        currentCombo++;
                        int pointValue = currentPointValue;

                        FadingText text1 = new FadingText(mContext, x, y, Color.MAGENTA, "+"+currentPointValue);
                        textArrayList.add(text1);

                        if(currentCombo > 1){
                           pointValue += currentCombo;
                           FadingText text2 = new FadingText(mContext, x+ (float)(text1.getWidth() * 1.5), y, Color.YELLOW, "(+" + currentCombo +")");
                           textArrayList.add(text2);

                        }
                        updateScore(pointValue);

                        if(currentCombo >= 5 && ((GameActivity)activity).getSignedIn()){
                            ((GameActivity)activity).unlockAchievement(com.greenkee.pokeADot.R.string.achievement_on_a_roll);
                            System.out.println("On a roll unlocked");
                        }

                    } else if (b.getColor() == Color.RED) {
                        b.setColor(Color.BLUE);
                        ballTouched = false;
                        gameOver();
                    }
                }
                if (!ballTouched) {
                    if(currentCombo > 0){
                        FadingText text = new FadingText(mContext, x, y, Color.MAGENTA, "Combo End");
                        currentCombo = 0;
                        textArrayList.add(text);
                    }
                    consecutiveMisses++;
                    if(consecutiveMisses >= 5 && ((GameActivity)activity).getSignedIn()){
                        ((GameActivity)activity).unlockAchievement(com.greenkee.pokeADot.R.string.achievement_not_your_day);
                        System.out.println("Not Your Day unlocked");
                    }
                }
                updateCombo();
            }
        }

        protected void gameOver() {
            gameOver = true;
            synchronized (mSurfaceHolder) {
                setState(STATE_LOSE);
            }
        }

        public ArrayList<Integer> getHighScoreValues(){ //greatest to least
            ArrayList<Integer> highScoreArray = new ArrayList<Integer>();
            for(int i = 0; i < SettingsActivity.maxHighScore; i++){
                String highScoreKey = "DATA_HIGH_SCORE_"+i;
                String highScore = getString(sharedPrefs, highScoreKey);
                int scoreIndex = highScore.indexOf(": ");

                if(scoreIndex > 0){
                    String scoreString = highScore.substring(scoreIndex+2);
                    int scoreValue = Integer.parseInt(scoreString);
                    highScoreArray.add(scoreValue);
                }

            }
            return highScoreArray;
        }


        public void checkHighScore( int score){
            if (score > 0){
                ((GameActivity)activity).postHighScoreGameActivity(sharedPrefs, score);
                int i = 0; boolean scored = false;
                ArrayList<Integer> highScoreArray = getHighScoreValues();
                while(i < SettingsActivity.maxHighScore && !scored){
                    if(i < highScoreArray.size()){
                        if(score > highScoreArray.get(i)){
                            startHighScoreThread(score, i);
                            scored = true;
                        }
                    }else{
                        startHighScoreThread(score, i);
                        scored = true;
                    }

                    i++;
                }
            }

        }

        public void changeHighScore(String entry, int index){
            String highScoreKey = "DATA_HIGH_SCORE_"+index;
            System.out.println("HSK" + highScoreKey);
            String lastHighScore = getHighScore(index);
            storeString(sharedPrefs, highScoreKey, entry);
            if(index < SettingsActivity.maxHighScore -1) {
                changeHighScore(lastHighScore, index + 1);
            }
        }

        public String getHighScore(int index){
            String highScoreKey = "DATA_HIGH_SCORE_"+index;
            return getString(sharedPrefs, highScoreKey);
        }

        public void storeString(SharedPreferences prefs, String key, String data){
            SharedPreferences.Editor prefEditor = prefs.edit();
            prefEditor.putString(key, data);
            prefEditor.commit();
        }


        public  String getString(SharedPreferences prefs, String key){

            return prefs.getString(key, "no value");
        }

        public  void storeInt(SharedPreferences prefs, String key, int data){
            SharedPreferences.Editor prefEditor = prefs.edit();
            prefEditor.putInt(key, data);
            prefEditor.commit();
        }

        public  int getInt(SharedPreferences prefs,  String key){
            return prefs.getInt(key, -1);
        }



        public void checkSettings(SharedPreferences prefs) {
            if (piecesCreated) {
                soundEnabled = prefs.getBoolean(SettingsActivity.PreferenceKeys.PREF_SOUND_ENABLED, true);
                startBallRadius = screenBottom / Float.parseFloat(prefs.getString(SettingsActivity.PreferenceKeys.PREF_START_RADIUS, "10"));
                percentBallShrink = Float.parseFloat(prefs.getString(SettingsActivity.PreferenceKeys.PREF_PERCENT_SHRINK, "97.5"));

            }
        }

    }


    /**
     * Pointer to the text view to display "Paused.." etc.
     */
    public TextView mStatusText;
    public TextView mScoreText;
    public TextView mComboText;


    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        uiHandler = new Handler();


        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new GameThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                mStatusText.setVisibility(m.getData().getInt("status_viz"));
                mStatusText.setText(m.getData().getString("status_text"));

                mScoreText.setVisibility(VISIBLE);
                //System.out.println("ST:" + m.getData().getString("score_text"));
                mScoreText.setText(m.getData().getString("score_text"));

                mComboText.setVisibility(VISIBLE);
                mComboText.setText(m.getData().getString("combo_text"));
            }
        });


        setFocusable(true); // make sure we get key events
    }

    public void startHighScoreThread( final int score, final int index){
        System.out.println("DIALOG OPEN:" + dialogOpen);
        // Do something that takes a while
        if(!dialogOpen){
            dialogOpen = true;
            System.out.println("START HIGH SCORE THREAD");
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    System.out.println("Highscore running");
                    uiHandler.post(new Runnable() { // This thread runs in the UI
                        @Override
                        public void run() {

                            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

                            alert.setTitle("High Score!");
                            alert.setMessage("Enter your name.");

                            final EditText input = new EditText(mContext);
                            input.setHint("Name");
                            alert.setView(input);

                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String name = input.getText().toString();
                                    if (!name.equals("")) {
                                        String message = name + ": " + score;
                                        ((GameThread)getThread()).changeHighScore(message, index);
                                        System.out.println("HS:"+message);
                                        dialogOpen = false;
                                    } else {
                                        displayToast("Please enter a name");
                                        dialogOpen = false;
                                        startHighScoreThread(score, index);
                                    }
                                }
                            });
                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialogOpen = false;
                                    //createConfirmationAlert();
                                }
                            });
                            alert.show();
                        }
                    });

                }
            };
            new Thread(runnable).start();
        }

    }

    protected void displayToast(String message) {
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(mContext, text, duration);
        toast.show();
    }

    /**
     * Installs a pointer to the text view used for messages.
     */
    public void setTextViews(TextView statusView, TextView scoreView, TextView comboView) {
        mStatusText = statusView;
        mScoreText = scoreView;
        mComboText = comboView;
    }



}

