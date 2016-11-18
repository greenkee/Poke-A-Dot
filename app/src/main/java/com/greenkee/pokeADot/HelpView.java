package com.greenkee.pokeADot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Gateway Oct 2010 on 8/24/2014.
 */
public class HelpView extends GameView implements SurfaceHolder.Callback {
    protected String[] helpStrings = {"Welcome to the \nPoke-a-Dot Tutorial! \nTap to Begin", "Tap the green ball to score points!",
    "Touch the ball multiple times in a row to score combos!", "But miss the green ball, and your combo is reset.",
            "If you touch the red ball you lose, but you can always play again! Try to lose now!", "The red ball that made you lose turns blue. Now you are ready to play! \nTap to finish the tutorial!"}; //6 strings total!
    protected int currentProgress = -1;


        public class HelpThread extends GameThread implements Runnable {

            public HelpThread(SurfaceHolder surfaceHolder, Context context,
                              Handler handler) {
                super(surfaceHolder, context, handler);
            }

            protected void advanceText(){
                currentProgress++;
                setText(helpStrings[currentProgress]);
            }

            @Override
            protected void createNewGame(){
                super.createNewGame();
                System.out.println("HELP NEW GAME CREATED");
                setState(STATE_RUNNING);
                advanceText();
            }


            public void resetGame() {
                System.out.println("RESET GAME HELP");
                gameOver = false;
                currentCombo = 0;
                consecutiveMisses = 0;

                textArrayList.clear();

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


            public void startGame() {
                System.out.println("GAME STARTED HELP");

                if(mMode == STATE_LOSE){
                    createNewGame();
                }
                setState(STATE_RUNNING);
            }

            @Override
            public void setState(int mode, CharSequence message) {
                synchronized (mSurfaceHolder) {

                    mMode = mode;
                    if (mMode == STATE_RUNNING) {
                        onResume();
                    } else {
                        if (mMode == STATE_PAUSE) {
                            onPause();
                        }  else if (mMode == STATE_RESUMING){
                            onResume();
                        }
                    }
                }
            }

            protected boolean checkAnyBallTouched(float x, float y){
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
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            closestIndex = i;
                        }
                        i++;
                    }

                    b = ballArrayList.get(closestIndex);
                    if (b.isVulnerable() && closestDistance < (b.getRadius() + touchBuffer)) {
                        if ((b.getColor() == Color.GREEN)) {
                            ballTouched = true;

                        }else{
                            ballTouched = false;
                        }
                    }
                }
                if(currentCombo > 0 && !ballTouched){
                    FadingText text = new FadingText(mContext, x, y, Color.MAGENTA, "Combo End");
                    currentCombo = 0;
                    textArrayList.add(text);
                }
                updateCombo();
                return ballTouched;
            }


            protected boolean checkRedBallTouched(float x, float y) {
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
                        if (distance < closestDistance) {
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
                            ballTouched = false;

                        } else if (b.getColor() == Color.RED) {
                            b.setColor(Color.BLUE);
                            ballTouched = true;

                        }
                    }
                }
                if(currentCombo > 0){
                    FadingText text = new FadingText(mContext, x, y, Color.MAGENTA, "Combo End");
                    currentCombo = 0;
                    textArrayList.add(text);
                }
                updateCombo();
                return ballTouched;
            }

            protected boolean checkGreenBallTouched(float x, float y) {
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
                        if (distance < closestDistance) {
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
                            changeBallRadius(Ball.radius * (percentBallShrink / 100));


                            currentPointValue++;
                            currentCombo++;
                            int pointValue = currentPointValue;

                            FadingText text1 = new FadingText(mContext, x, y, Color.MAGENTA, "+" + currentPointValue);
                            textArrayList.add(text1);

                            if (currentCombo > 1) {
                                pointValue += currentCombo;
                                FadingText text2 = new FadingText(mContext, x + (float) (text1.getWidth() * 1.5), y, Color.YELLOW, "(+" + currentCombo + ")");
                                textArrayList.add(text2);

                            }
                            updateScore(pointValue);
                            updateCombo();

                        } else if (b.getColor() == Color.RED) {
                            ballTouched = false;
                        }
                    }
                }
                return ballTouched;
            }

            @Override
            public void checkTouch(float x, float y) {
                switch(currentProgress){
                    case(0):
                        advanceText();
                        return;
                    case(1):
                        if(checkGreenBallTouched(x,y)){
                            advanceText();
                        }
                        return;
                    case(2):
                        if(checkGreenBallTouched(x,y)){
                            advanceText();
                        }
                        return;
                    case(3):
                        if(checkAnyBallTouched(x,y)){
                            advanceText();
                        }
                        return;
                    case(4):
                        if(checkRedBallTouched(x,y)){
                            advanceText();
                            setState(STATE_LOSE);
                        }
                        break;
                    case(5):
                        ((HelpActivity)activity).goToMenu();
                        return;
                }
            }


        }


        public HelpView(Context context, AttributeSet attrs) {
            super(context, attrs);
            uiHandler = new Handler();


            // register our interest in hearing about changes to our surface
            SurfaceHolder holder = getHolder();
            holder.addCallback(this);

            // create thread only; it's started in surfaceCreated()
            thread = new HelpThread(holder, context, new Handler() {
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

        protected void displayToast(String message) {
            CharSequence text = message;
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(mContext, text, duration);
            toast.show();
        }

    }


