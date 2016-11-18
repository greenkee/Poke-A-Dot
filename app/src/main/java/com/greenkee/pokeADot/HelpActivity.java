package com.greenkee.pokeADot;

/**
 * Created by Gateway Oct 2010 on 8/24/2014.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MotionEventCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.games.Games;


public class HelpActivity extends CanvasActivity {
    protected Button menuButton, resetButton, pauseButton;

    public static int numTouches = 0;
    public static final int MAX_TOUCHES = 2;

    float p1X = -1, p1Y = -1, p2X = -1, p2Y = -1;
    int p1ID = -1, p2ID = -1;

    public static SoundPool audio = null;
    public static int bounceSound = -1, collectSound = -1, deathSound = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameMode = CLASSIC_MODE;
        // tell system to use the layout defined in our XML file
        setContentView(com.greenkee.pokeADot.R.layout.activity_help_screen);

        mView = (HelpView) findViewById(com.greenkee.pokeADot.R.id.gameScreen);
        mThread = (mView).getThread();
        mView.setActivity(this);
        ((HelpView)mView).setTextViews((TextView) findViewById(com.greenkee.pokeADot.R.id.status_display),
                (TextView) findViewById(com.greenkee.pokeADot.R.id.score_display), (TextView) findViewById(com.greenkee.pokeADot.R.id.combo_display));

        if (savedInstanceState == null) {
            System.out.println("STATE SET");
            mThread.doStart();
        } else {
            super.onRestoreInstanceState(savedInstanceState);
            mThread.restoreState(savedInstanceState);

        }
        mView.setOnTouchListener(new OnTouchListener() {
                                     @Override
                                     public boolean onTouch(View v, MotionEvent event) {
                                         int action = MotionEventCompat.getActionMasked(event);
                                         final int pointerIndex;
                                         final float x;
                                         final float y;
                                         switch (action) {
                                             case (MotionEvent.ACTION_DOWN): {
                                                     int index = MotionEventCompat.getActionIndex(event);
                                                     p1ID = MotionEventCompat.getPointerId(event, index);
                                                     if (p1ID != -1) {
                                                         int p1Index = MotionEventCompat.findPointerIndex(event, p1ID);
                                                         ((HelpView.HelpThread)mThread).checkTouch(MotionEventCompat.getX(event, p1Index),
                                                                 MotionEventCompat.getY(event, p1Index));
                                                         //DO SOMETHING HERE

                                                     }

                                                 return true;
                                             }
                                             case (MotionEvent.ACTION_MOVE): {
                                                 /*
                                                 //  System.out.println("ACTION_MOVE");
                                                 if (((HelpView.HelpThread)mThread).getCurrentState() == HelpView.HelpThread.STATE_RUNNING) {
                                                     if (p1ID != -1) {
                                                         int p1Index = MotionEventCompat.findPointerIndex(event, p1ID);
                                                         //DO SOMETHING HERE

                                                     }
                                                 }



                                                 */
                                                 return true;
                                             }
                                             case (MotionEvent.ACTION_UP): {
                                                 // System.out.println("ACTION_UP");

                                                 reset();
                                                 return true;
                                             }
                                             case (MotionEvent.ACTION_CANCEL): {
                                                 // System.out.println("ACTION_CANCEL");
                                                 reset();
                                                 return true;
                                             }
                                             case (MotionEvent.ACTION_OUTSIDE): {
                                                 return true;

                                             }
                                         }

                                         return false;
                                     }

                                     private void reset() {
                                         numTouches = 0;
                                         p1ID = -1;
                                         p2ID = -1;
                                     }
                                 }


        );
        initialize();
    }

    private void initialize() {
        menuButton =(Button) (findViewById(R.id.bMenu));
        resetButton =(Button) (findViewById(R.id.bReset));
        pauseButton =(Button) (findViewById(R.id.bPause));

        resetButton.setClickable(false);
        pauseButton.setClickable(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(audio != null){
            audio.release();
            audio = null;
        }
    }

    public void pauseGame(View view){
        if(((HelpView.HelpThread)mThread).getCurrentState() == HelpView.HelpThread.STATE_RUNNING){
            mThread.pause();
        }else if(((HelpView.HelpThread)mThread).getCurrentState() == HelpView.HelpThread.STATE_PAUSE){
            mThread.unpause();
        }
    }

    @Override
    protected void onResume() {
        if (audio == null){
            loadAudio();
        }
        super.onResume();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(audio != null){
            audio.release();
            audio = null;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!mView.piecesCreated) {
            Button pauseButton = (Button) findViewById(com.greenkee.pokeADot.R.id.bPause);
            mThread.screenSizeObtained(pauseButton.getBottom());
        }
    }


    private void loadAudio() {
        audio = new SoundPool(7, AudioManager.STREAM_MUSIC, 0);
        bounceSound = audio.load(this, com.greenkee.pokeADot.R.raw.bounce, 1);
        collectSound = audio.load(this, com.greenkee.pokeADot.R.raw.collect, 1);
        deathSound = audio.load(this, com.greenkee.pokeADot.R.raw.death, 1);

    }


    public void goToMenu(View v){
        goToMenu();
    }

    public void goToMenu() {
        Intent i = new Intent(this, PlayActivity.class);
        TitleScreen.nextActivity = true;
        startActivity(i);
    }

    public void resetGame(View view) {
        ((HelpView.HelpThread)mThread).resetGame();
    }
}
