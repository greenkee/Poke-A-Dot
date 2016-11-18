package com.greenkee.pokeADot;


import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.games.Games;

public class PlayActivity extends SherlockCanvasActivity implements View.OnClickListener, AudioManager.OnAudioFocusChangeListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);
        initialize();

        mView = (PlayView) findViewById(R.id.uiScreen); //should have a view named UI screen in all layouts
        mThread = mView.getThread();
        ((PlayView)mView).setSherlockActivity(this);

        if (savedInstanceState == null) {
            mThread.doStart();
        } else {
            super.onRestoreInstanceState(savedInstanceState);
            mThread.restoreState(savedInstanceState);

        }
        checkFirstTimePlaying(); //checks to see if game is being played for first time
    }

    private void checkFirstTimePlaying() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPrefs.getString(SettingsActivity.PreferenceKeys.FIRST_TIME_PLAYING, "false").equals("true")){
            storeString(sharedPrefs, SettingsActivity.PreferenceKeys.FIRST_TIME_PLAYING, "false");
            ((PlayView)mView).startTutorialThread();

        }
    }


    private void initialize() {
        findViewById(R.id.bClassic).setOnClickListener(this);
        findViewById(R.id.bTutorial).setOnClickListener(this);
        findViewById(R.id.bMemory).setOnClickListener(this);
    }


    public void startClassicGame() {
        Intent intent = new Intent(this, GameActivity.class);
        CanvasActivity.nextActivity = true;
        CanvasActivity.gameMode = CanvasActivity.CLASSIC_MODE;
        startActivity(intent);
    }

    public void startMemoryGame() {
        Intent intent = new Intent(this, GameActivity.class);
        CanvasActivity.nextActivity = true;
        CanvasActivity.gameMode = CanvasActivity.MEMORY_MODE;
        startActivity(intent);
    }

    protected void startTutorial() {
        Intent intent = new Intent(this, HelpActivity.class);
        CanvasActivity.nextActivity = true;
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.bClassic):
                startClassicGame();
                break;
            case (R.id.bMemory):
                startMemoryGame();
                break;
            case (R.id.bTutorial):
                startTutorial();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mThread.unpause();
    }

}
