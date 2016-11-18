package com.greenkee.pokeADot;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.games.Games;
import com.greenkee.basegameutils.BaseGameActivity;

/**
 * Created by Gateway Oct 2010 on 8/31/2014.
 */
public class SherlockCanvasActivity extends SherlockActivity implements AudioManager.OnAudioFocusChangeListener  {

    protected CanvasView mView;
    protected CanvasView.CanvasThread mThread;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //classes must setContentView

        if (savedInstanceState == null) {
        } else {
            super.onRestoreInstanceState(savedInstanceState);
        }

        PreferenceManager.setDefaultValues(this, com.greenkee.pokeADot.R.xml.preferences, false);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        //checkHighScore();
        //checkAchievements();
        startMusic(this);
        mThread.resumeScreen();

    }

    public static void startMusic(Context c) {
        CanvasActivity.startMusic(c);
    }

    public static void pauseMusic(){
        CanvasActivity.pauseMusic();
    }

    public static void stopMusic() {
        CanvasActivity.stopMusic();
    }


    public static void checkToStopMusic() {
        CanvasActivity.checkToStopMusic();
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


    public static void storeString(SharedPreferences prefs, String key, String data) {
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString(key, data);
        prefEditor.commit();
    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                startMusic(this);
                CanvasActivity.getMusic().setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (CanvasActivity.getMusic().isPlaying()) CanvasActivity.getMusic().stop();
                stopMusic();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (CanvasActivity.getMusic().isPlaying()) CanvasActivity.getMusic().pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (CanvasActivity.getMusic().isPlaying()) CanvasActivity.getMusic().setVolume(0.1f, 0.1f);
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
            CanvasActivity.nextActivity = true;

            NavUtils.navigateUpFromSameTask(this);


        //finish();
        //super.onBackPressed();
    }

}