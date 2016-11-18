package com.greenkee.pokeADot;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.games.Games;
import com.greenkee.basegameutils.BaseGameActivity;

import java.util.ArrayList;



public class DisplayHighScores extends SherlockCanvasActivity {
    TextView highScoreDisplay;
    String highScores;
    SharedPreferences sharedPrefs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.greenkee.pokeADot.R.layout.activity_statistics);

        initialize();

        mView = (HighScoreView) findViewById(com.greenkee.pokeADot.R.id.uiScreen); //should have a view named UI screen in all layouts
        mThread = mView.getThread();
        ((HighScoreView)mView).setSherlockActivity(this);



        if (savedInstanceState == null) {
            mThread.doStart();
            System.out.println("STARTED");
        } else {
            super.onRestoreInstanceState(savedInstanceState);
            mThread.restoreState(savedInstanceState);

        }


        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        highScoreDisplay.setText(displayHighScoreValues());


    }

    public String displayHighScoreValues(){
        ArrayList<String> highScoreArray = getHighScoreStrings();
        String message = "";
        for(int i = 0; i < highScoreArray.size(); i++){
            message += highScoreArray.get(i);
            message += "\n";

        }
        return message;
    }

    public ArrayList<String> getHighScoreStrings(){
        ArrayList<String> highScoreArray = new ArrayList<String>();
        for(int i = 0; i < SettingsActivity.maxHighScore; i++){
            String highScoreKey = "DATA_HIGH_SCORE_"+i;
            String highScore;
            try{
                highScore = sharedPrefs.getString(highScoreKey, "no value");
                int scoreIndex = highScore.indexOf(": ");

                if(scoreIndex > 0){
                    highScoreArray.add(highScore);
                }
            }catch (NullPointerException e){
                System.out.println("NO HIGH SCORE VALUE STORED");

            }
        }
        return highScoreArray;
    }

    private void initialize(){
        highScoreDisplay = (TextView)findViewById(com.greenkee.pokeADot.R.id.high_score_text);
    }


    @Override
    protected void onStop() {
        super.onStop();
        CanvasActivity.checkToStopMusic();
    }

    @Override
    public void onBackPressed() {
        TitleScreen.nextActivity = true;
        super.onBackPressed();
    }

    @Override
    protected void onPause(){
        super.onPause();
        CanvasActivity.pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mThread.unpause();

    }


}
