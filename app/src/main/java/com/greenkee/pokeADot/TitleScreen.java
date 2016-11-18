package com.greenkee.pokeADot;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.games.Games;
import com.greenkee.basegameutils.BaseGameActivity;

public class TitleScreen extends CanvasActivity implements View.OnClickListener, AudioManager.OnAudioFocusChangeListener {
    //Request codes
    private final int REQUEST_ACHIEVEMENTS = 1;
    private final int REQUEST_LEADERBOARDS = 2;
    final int RC_RESOLVE = 5000, RC_UNUSED = 5001;

    Button p1Button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.greenkee.pokeADot.R.layout.activity_title_screen);
        initialize();

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
        checkToInitializeDefaults(); //checks to see if game is being played for first time
    }

    protected void checkToInitializeDefaults(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPrefs.getString(SettingsActivity.HighScores.DATA_HIGH_SCORE_TO_POST, "no value").equals("no value")){
            storeString(sharedPrefs, SettingsActivity.HighScores.DATA_HIGH_SCORE_TO_POST, "0");
            storeString(sharedPrefs, SettingsActivity.HighScores.DATA_POST_SCORE, "false");
            storeString(sharedPrefs, SettingsActivity.PreferenceKeys.FIRST_TIME_PLAYING, "true");
        }
        /*
        if(sharedPrefs.getString(SettingsActivity.Achievements.ACHIEVE_GETTING_WARMED_UP, "no value").equals("no value")){
            storeString(sharedPrefs, SettingsActivity.Achievements.ACHIEVE_GETTING_WARMED_UP, "0");
            storeString(sharedPrefs, SettingsActivity.Achievements.ACHIEVE_NOT_YOUR_DAY, "false");
            storeString(sharedPrefs, SettingsActivity.Achievements.ACHIEVE_ON_A_ROLL, "false");
        }
        if(sharedPrefs.getString(SettingsActivity.HighScores.DATA_LAST_SCORE_SUBMITTED, "no value").equals("no value") ){
            storeString(sharedPrefs, SettingsActivity.HighScores.DATA_LAST_SCORE_SUBMITTED, "0");
            storeString(sharedPrefs, SettingsActivity.HighScores.DATA_HIGH_SCORE_0, "0");
        }*/
    }



    private void initialize() {
        p1Button = (Button) findViewById(R.id.bPlay);
        p1Button.setOnClickListener(this);
        findViewById(com.greenkee.pokeADot.R.id.sign_in_button).setOnClickListener(this);
        findViewById(com.greenkee.pokeADot.R.id.sign_out_button).setOnClickListener(this);
        findViewById(com.greenkee.pokeADot.R.id.bAchievements).setOnClickListener(this);
        findViewById(com.greenkee.pokeADot.R.id.bSettings).setOnClickListener(this);
        findViewById(com.greenkee.pokeADot.R.id.bLeaderboards).setOnClickListener(this);


    }


    public void playGame(View view) {
        Intent intent = new Intent(this, PlayActivity.class);
        nextActivity = true;
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.bPlay):
                playGame(v);
                break;
            case (R.id.sign_in_button):
                // start the asynchronous sign in flow
                beginUserInitiatedSignIn();
                getGameHelper().setMaxAutoSignInAttempts(0);
                System.out.println("SIGNED IN");
                break;
            case (R.id.sign_out_button):
                // sign out.
                signOut();
                System.out.println("SIGNED OUT");
                // show sign-in button, hide the sign-out button
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_out_button).setVisibility(View.GONE);
                break;
            case (R.id.bAchievements):
                showAchievementsRequested();
                break;
            case (R.id.bSettings):
                goToSettings();
                break;
            case (R.id.bLeaderboards):
                showLeaderboardsRequestedDialog();
                break;
        }
    }


    public void goToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        nextActivity = true;
        startActivity(intent);
    }

    protected void checkHighScore(){
        if(isSignedIn()){
            System.out.println("HIGH SCORE CHECKED");
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            String highScore = sharedPrefs.getString(SettingsActivity.HighScores.DATA_HIGH_SCORE_TO_POST, "0");
            if (Integer.parseInt(highScore) > 0  && sharedPrefs.getString(SettingsActivity.HighScores.DATA_POST_SCORE, "false").equals("true")){
                postHighScore(sharedPrefs, Integer.parseInt(highScore));
            }
        }
    }

    protected void showLeaderboardsRequestedDialog(){
        mView.dialogOpen = false;
        ((TitleView)mView).startLeaderboardThread();
    }

    protected void checkAchievements(){
        if(isSignedIn()) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (sharedPrefs.getString(SettingsActivity.Achievements.ACHIEVE_GETTING_WARMED_UP, "0").equals("10")) {
                System.out.println("WARMED UP:"+sharedPrefs.getString(SettingsActivity.Achievements.ACHIEVE_GETTING_WARMED_UP, "not a number"));
                Games.Achievements.unlock(getApiClient(), getString(com.greenkee.pokeADot.R.string.achievement_getting_warmed_up));
            }
            if (sharedPrefs.getString(SettingsActivity.Achievements.ACHIEVE_ON_A_ROLL, "false").equals("true")) {
                Games.Achievements.unlock(getApiClient(), getString(com.greenkee.pokeADot.R.string.achievement_on_a_roll));
            }
            if (sharedPrefs.getString(SettingsActivity.Achievements.ACHIEVE_NOT_YOUR_DAY, "false").equals("true")) {
                Games.Achievements.unlock(getApiClient(), getString(com.greenkee.pokeADot.R.string.achievement_not_your_day));
            }
        }
    }


    @Override
    public void onSignInFailed() {
        // Sign in has failed. So show the user the sign-in button.
        super.onSignInFailed();
        findViewById(com.greenkee.pokeADot.R.id.sign_in_button).setVisibility(View.VISIBLE);
        findViewById(com.greenkee.pokeADot.R.id.sign_out_button).setVisibility(View.GONE);

    }

    @Override
    public void onSignInSucceeded() {
        super.onSignInSucceeded();
        // show sign-out button, hide the sign-in button
        findViewById(com.greenkee.pokeADot.R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(com.greenkee.pokeADot.R.id.sign_out_button).setVisibility(View.VISIBLE);
        checkHighScore();
    }

    public void showAchievementsRequested() {
        if (isSignedIn()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()),
                    REQUEST_ACHIEVEMENTS);
        } else {
            showAlert(getString(com.greenkee.pokeADot.R.string.achievements_not_available));
        }
    }

    public void localLeaderboardsRequested(){
        Intent i = new Intent(this, DisplayHighScores.class);
        TitleScreen.nextActivity = true;
        startActivity(i);
    }

    public void showLeaderboardsRequested() {
        if (isSignedIn()) {
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(),
                    getString(com.greenkee.pokeADot.R.string.leaderboard_high_scores)), REQUEST_LEADERBOARDS);
        } else {
            showAlert(getString(com.greenkee.pokeADot.R.string.leaderboards_not_available));
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mThread.unpause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


}
