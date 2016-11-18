package com.greenkee.pokeADot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.games.Games;
import com.greenkee.basegameutils.BaseGameActivity;

/**
 * Created by Gateway Oct 2010 on 8/16/2014.
 */
public class CanvasActivity extends BaseGameActivity implements AudioManager.OnAudioFocusChangeListener  {
    public static String gameMode;
    public static String CLASSIC_MODE = "CLASSIC";
    public static String MEMORY_MODE = "MEMORY";

    //background music
    public static MediaPlayer mBackgroundMusic = null;

    //tells if user is going to next activity or exiting app
    public static boolean nextActivity = false;

    protected CanvasView mView;
    protected CanvasView.CanvasThread mThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //classes must setContentView

        getGameHelper().setMaxAutoSignInAttempts(0);

        if (savedInstanceState == null) {
        } else {
            super.onRestoreInstanceState(savedInstanceState);
        }

        PreferenceManager.setDefaultValues(this, com.greenkee.pokeADot.R.xml.preferences, false);


        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        /*
        mView = (CanvasView) findViewById(com.greenkee.pokeADot.R.id.uiScreen); //should have a view named UI screen in all layouts
        mThread = mView.getThread();
        mView.setActivity(this);

        if (savedInstanceState == null) {
            mThread.doStart();
            System.out.println("STARTED");
        } else {
            super.onRestoreInstanceState(savedInstanceState);
            mThread.restoreState(savedInstanceState);

        }
        */
    }


    @Override
    public void onSignInFailed() {
        System.out.println("SIGN IN FAILED");
        displayShortToast(this, "You will be able to save high scores and achievements once you have connection.");

    }

    @Override
    public void onSignInSucceeded() {
        System.out.println("SIGN IN SUCCESSFUL");
    }

    @Override
    protected void onPause(){
        mView.getThread().pause();
        super.onPause();
        pauseMusic();
        mThread.onPause();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) { //allows one to set a buffer on the bottom, takes screen size
        if (!mView.piecesCreated) {
            mThread.screenSizeObtained(0);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkGooglePlayServices();
        //checkHighScore();
        //checkAchievements();
        startMusic(this);
        mThread.resumeScreen();

    }

    public static void startMusic(Context c) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        if(sharedPrefs.getBoolean(SettingsActivity.PreferenceKeys.PREF_MUSIC_ENABLED, false)){
            if (mBackgroundMusic == null) {
                mBackgroundMusic = MediaPlayer.create(c, com.greenkee.pokeADot.R.raw.background_music);
                mBackgroundMusic.setLooping(true);

            }
            if (!mBackgroundMusic.isPlaying()) mBackgroundMusic.start();
        }
    }

    public static void pauseMusic(){
        if(mBackgroundMusic != null){
            mBackgroundMusic.pause();
        }
    }

    public static void stopMusic() {
        if (mBackgroundMusic != null) {
            mBackgroundMusic.release();
            mBackgroundMusic = null;
        }
    }


    public static void checkToStopMusic() {
        if (!nextActivity) {
            stopMusic();
        } else {
            nextActivity = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkToStopMusic();
        System.out.println("ACTIVITY STOPPED");
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // just have the View's thread save its state into our Bundle
        super.onSaveInstanceState(outState);
        mThread.saveState(outState);
    }

    protected void checkGooglePlayServices() {

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status == ConnectionResult.SUCCESS) {
            System.out.println("SERVICES SUCCESS");
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 3);

        }
    }

    public void postHighScore( SharedPreferences prefs, int score) {
        if (isSignedIn()) {
            Games.Leaderboards.submitScore(getApiClient(), getString(com.greenkee.pokeADot.R.string.leaderboard_high_scores), score);
            storeString(prefs, SettingsActivity.HighScores.DATA_HIGH_SCORE_TO_POST, "0");
            storeString(prefs, SettingsActivity.HighScores.DATA_POST_SCORE, "false");
            displayShortToast(this, "Your High Score has been posted");
        }else{
            String currentHigh = prefs.getString(SettingsActivity.HighScores.DATA_HIGH_SCORE_TO_POST, "0");
            if(score > Integer.parseInt(currentHigh)){
                TitleScreen.storeString(prefs, SettingsActivity.HighScores.DATA_HIGH_SCORE_TO_POST, Integer.toString(score));
                TitleScreen.storeString(prefs, SettingsActivity.HighScores.DATA_POST_SCORE, "true");
            }
        }

    }

    public static void storeString(SharedPreferences prefs, String key, String data) {
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString(key, data);
        prefEditor.commit();
    }

    public static MediaPlayer getMusic(){
        return mBackgroundMusic;
    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                startMusic(this);
                mBackgroundMusic.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mBackgroundMusic.isPlaying()) mBackgroundMusic.stop();
                stopMusic();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mBackgroundMusic.isPlaying()) mBackgroundMusic.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mBackgroundMusic.isPlaying()) mBackgroundMusic.setVolume(0.1f, 0.1f);
                break;
        }
    }

    public static void displayLongToast(Context c, String message) {
        CharSequence text = message;
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(c, text, duration);
        toast.show();
    }

    public static void displayShortToast(Context c, String message) {
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(c, text, duration);
        toast.show();
    }


    @Override
    public void onBackPressed() {
        if(this instanceof TitleScreen){
            nextActivity = false;
            System.exit(0);
        }else{
            nextActivity = true;

            NavUtils.navigateUpFromSameTask(this);
        }

        //finish();
        //super.onBackPressed();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        System.out.println("ACTIVITY DESTROYED");
    }

}
