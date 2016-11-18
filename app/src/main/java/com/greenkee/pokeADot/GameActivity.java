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


public class GameActivity extends CanvasActivity {

    private static final int MENU_BUTTON_1 = 1;
    private static final int MENU_BUTTON_2 = 2;
    private static final int MENU_BUTTON_3 = 3;
    private static final int MENU_BUTTON_4 = 4;
    private static final int MENU_BUTTON_5 = 5;

    public static int numTouches = 0;
    public static final int MAX_TOUCHES = 2;

    float p1X = -1, p1Y = -1, p2X = -1, p2Y = -1;
    int p1ID = -1, p2ID = -1;

    public static SoundPool audio = null;
    public static int bounceSound = -1, collectSound = -1, deathSound = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // tell system to use the layout defined in our XML file
        setContentView(com.greenkee.pokeADot.R.layout.activity_game_screen);

        mView = (GameView) findViewById(com.greenkee.pokeADot.R.id.gameScreen);
        mThread = mView.getThread();
        mView.setActivity(this);
        ((GameView)mView).setTextViews((TextView) findViewById(com.greenkee.pokeADot.R.id.status_display),
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
                                                 //  System.out.println("ACTION_DOWN");
                                                 if (((GameView.GameThread)mThread).getCurrentState() == GameView.GameThread.STATE_RUNNING) {
                                                     int index = MotionEventCompat.getActionIndex(event);
                                                     p1ID = MotionEventCompat.getPointerId(event, index);
                                                     if (p1ID != -1) {
                                                         int p1Index = MotionEventCompat.findPointerIndex(event, p1ID);
                                                         ((GameView.GameThread)mThread).checkTouch(MotionEventCompat.getX(event, p1Index),
                                                                 MotionEventCompat.getY(event, p1Index));
                                                         //DO SOMETHING HERE

                                                     }
                                                 } else if (!(mView.dialogOpen) && (!(((GameView.GameThread)mThread).gameOver)) &&
                                                         (((GameView.GameThread)mThread).getCurrentState() == GameView.GameThread.STATE_READY)
                                                         || (((GameView.GameThread)mThread).getCurrentState() == GameView.GameThread.STATE_LOSE)) {
                                                     ((GameView.GameThread)mThread).startGame();

                                                     // System.out.println("START GAME");

                                                 }else if(((GameView.GameThread)mThread).getCurrentState() == GameView.GameThread.STATE_PAUSE){
                                                     mThread.unpause();
                                                 }

                                                 return true;
                                             }
                                             case (MotionEvent.ACTION_MOVE): {
                                                 //  System.out.println("ACTION_MOVE");
                                                 if (((GameView.GameThread)mThread).getCurrentState() == GameView.GameThread.STATE_RUNNING) {
                                                     if (p1ID != -1) {
                                                         int p1Index = MotionEventCompat.findPointerIndex(event, p1ID);
                                                         //DO SOMETHING HERE

                                                     }
                                                 }


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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_BUTTON_1, 0, getString(com.greenkee.pokeADot.R.string.reset_title));
        menu.add(0, MENU_BUTTON_3, 0, getString(com.greenkee.pokeADot.R.string.pause_title));
        menu.add(0, MENU_BUTTON_2, 0, getString(com.greenkee.pokeADot.R.string.settings_title));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_BUTTON_1:
                ((GameView.GameThread)mThread).resetGame();
                return true;
            case MENU_BUTTON_2:
                goToSettings(mView);
                return true;
            case MENU_BUTTON_3:
                if(((GameView.GameThread)mThread).getCurrentState() == GameView.GameThread.STATE_RUNNING){
                    mThread.pause();
                }else if(((GameView.GameThread)mThread).getCurrentState() == GameView.GameThread.STATE_PAUSE){
                    mThread.unpause();
                }

                return true;
        }

        return false;
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
        if(((GameView.GameThread)mThread).getCurrentState() == GameView.GameThread.STATE_RUNNING){
            mThread.pause();
        }else if(((GameView.GameThread)mThread).getCurrentState() == GameView.GameThread.STATE_PAUSE){
            mThread.unpause();
        }
    }

    @Override
    protected void onResume() {
        if (audio == null){
            loadAudio();
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        ((GameView.GameThread)mThread).checkSettings(sharedPrefs);
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

    public void unlockAchievement(int achievementId) {
        if (isSignedIn()) {
            Games.Achievements.unlock(getApiClient(), getString(achievementId));
        }
        System.out.println("ACHIEVEMENT UNLOCKED:" + achievementId);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        switch(achievementId){
            case (com.greenkee.pokeADot.R.string.achievement_on_a_roll):
                TitleScreen.storeString(prefs, SettingsActivity.Achievements.ACHIEVE_ON_A_ROLL, "true");
                break;
            case (com.greenkee.pokeADot.R.string.achievement_not_your_day):
                TitleScreen.storeString(prefs, SettingsActivity.Achievements.ACHIEVE_NOT_YOUR_DAY, "true");
                break;

        }

    }

    public void achievementToast(String achievement) {
        // Only show toast if not signed in. If signed in, the standard Google Play
        // toasts will appear, so we don't need to show our own.
        if (!isSignedIn()) {
            Toast.makeText(this, getString(com.greenkee.pokeADot.R.string.achievement) + ": " + achievement,
                    Toast.LENGTH_LONG).show();
        }
    }

    public void incrementAchievement(int achievementId,  int increment) {
        if (isSignedIn()) {
            Games.Achievements.increment(getApiClient(), getString(achievementId), increment);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        switch(achievementId){
            case (com.greenkee.pokeADot.R.string.achievement_getting_warmed_up):
                int steps = Integer.parseInt(prefs.getString(SettingsActivity.Achievements.ACHIEVE_GETTING_WARMED_UP, "0"));
                steps++;
                TitleScreen.storeString(prefs, SettingsActivity.Achievements.ACHIEVE_GETTING_WARMED_UP, Integer.toString(steps));

                System.out.println("WARMED UP:"+prefs.getString(SettingsActivity.Achievements.ACHIEVE_GETTING_WARMED_UP, "not a number"));
                break;
            case(com.greenkee.pokeADot.R.string.achievement_dedicated):
                break;
        }
    }

    public boolean getSignedIn(){
        return isSignedIn();
    }

    public void postHighScoreGameActivity(SharedPreferences prefs, int score){
        if(isSignedIn()){
            Games.Leaderboards.submitScore(getApiClient(), getString(com.greenkee.pokeADot.R.string.leaderboard_high_scores), score);
        }else{
            String currentHigh = prefs.getString(SettingsActivity.HighScores.DATA_HIGH_SCORE_TO_POST, "0");
            if(score > Integer.parseInt(currentHigh)){
                TitleScreen.storeString(prefs, SettingsActivity.HighScores.DATA_HIGH_SCORE_TO_POST, Integer.toString(score));
                TitleScreen.storeString(prefs, SettingsActivity.HighScores.DATA_POST_SCORE, "true");
            }
        }
    }

    public void goToSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        TitleScreen.nextActivity = true;
        startActivity(intent);
    }


    public void goToMenu(View view) {
        Intent i = new Intent(this, PlayActivity.class);
        TitleScreen.nextActivity = true;
        startActivity(i);
    }

    public void resetGame(View view) {
        ((GameView.GameThread)mThread).resetGame();
    }
}
