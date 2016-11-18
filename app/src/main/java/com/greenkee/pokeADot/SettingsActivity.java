package com.greenkee.pokeADot;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class SettingsActivity extends SherlockPreferenceActivity {
    public static int maxHighScore = 10;
    public class PreferenceKeys {
        public static final String PREF_SOUND_ENABLED = "sound_enabled";
        public static final String PREF_MUSIC_ENABLED = "music_enabled";
        public static final String PREF_START_RADIUS = "start_radius";
        public static final String PREF_PERCENT_SHRINK = "percent_shrink";
        public static final String FIRST_TIME_PLAYING = "first_time_playing";

    }

    public class GameSettings{
        public static final String PREF_GAME_MODE = "game_mode";
    }

    public class HighScores{
        public static final String DATA_LAST_SCORE_SUBMITTED = "DATA_LAST_SCORE_SUBMITTED";
        public static final String DATA_HIGH_SCORE_TO_POST = "DATA_HIGH_SCORE_TO_POST";
        public static final String DATA_POST_SCORE = "DATA_POST_SCORE";
        public static final String DATA_HIGH_SCORE_0 = "DATA_HIGH_SCORE_0";
        public static final String DATA_HIGH_SCORE_1 = "DATA_HIGH_SCORE_1";
        public static final String DATA_HIGH_SCORE_2 = "DATA_HIGH_SCORE_2";
        public static final String DATA_HIGH_SCORE_3 = "DATA_HIGH_SCORE_3";
        public static final String DATA_HIGH_SCORE_4 = "DATA_HIGH_SCORE_4";
        public static final String DATA_HIGH_SCORE_5 = "DATA_HIGH_SCORE_5";
        public static final String DATA_HIGH_SCORE_6 = "DATA_HIGH_SCORE_6";
        public static final String DATA_HIGH_SCORE_7 = "DATA_HIGH_SCORE_7";
        public static final String DATA_HIGH_SCORE_8 = "DATA_HIGH_SCORE_8";
        public static final String DATA_HIGH_SCORE_9 = "DATA_HIGH_SCORE_9";
    }

    public class Achievements{
        public static final String ACHIEVE_ON_A_ROLL = "on_a_roll";
        public static final String ACHIEVE_GETTING_WARMED_UP = "getting_warmed_up";
        public static final String ACHIEVE_NOT_YOUR_DAY = "not_your_day";

    }


	
	SharedPreferences prefs;
	SharedPreferences.OnSharedPreferenceChangeListener changeListener;



	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	addPreferencesFromResource(com.greenkee.pokeADot.R.xml.preferences);
	prefs = getPreferenceScreen().getSharedPreferences();

	
	ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	    	finish();
            TitleScreen.nextActivity = true;
	       // NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}



	@Override
	protected void onResume() {
	    super.onResume();

        TitleScreen.startMusic(this);
	     changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
	    	  public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
                  if(key.equals(PreferenceKeys.PREF_PERCENT_SHRINK)){
                      SharedPreferences.Editor prefEditor = prefs.edit();
                      if (prefs.getString(key, "1").length() < 1  ){
                          throwInvalidNumber(prefEditor, key, 100);
                      }else {
                          try{
                              double percent = Double.parseDouble(prefs.getString(key, "100"));
                              if (percent  <= 0 ){

                                  String message;
                                  prefEditor.putString(key, Double.toString(1));
                                  message = Double.toString(1);

                                  displayToast("Please enter a number greater than 0");

                                  prefEditor.commit();

                                  EditTextPreference prefText = (EditTextPreference) findPreference(key);
                                  prefText.setText(message);

                              }else if (percent > 100){
                                  String message;
                                  prefEditor.putString(key, Double.toString(100));
                                  message = Double.toString(100);

                                  displayToast("Please enter a number less than or equal to 100");

                                  prefEditor.commit();

                                  EditTextPreference prefText = (EditTextPreference) findPreference(key);
                                  prefText.setText(message);
                              }
                          } catch(NumberFormatException e){
                              throwInvalidNumber(prefEditor, key, 100);
                          }
                      }

                  }
                  if(key.equals(PreferenceKeys.PREF_START_RADIUS)){
                      SharedPreferences.Editor prefEditor = prefs.edit();
                      if (prefs.getString(key, "1").length() < 1  ){
                          throwInvalidNumber(prefEditor, key, 10);
                      }else {
                          try {
                              double percent = Double.parseDouble(prefs.getString(key, "10"));
                              if (percent < 1) {

                                  String message;
                                  prefEditor.putString(key, Double.toString(1));
                                  message = Double.toString(1);

                                  displayToast("Please enter a number greater than or equal to 1");

                                  prefEditor.commit();

                                  EditTextPreference prefText = (EditTextPreference) findPreference(key);
                                  prefText.setText(message);
                              }
                          }
                             catch(NumberFormatException e){
                              throwInvalidNumber(prefEditor, key, 10);
                          }
                      }

                  }
                  if(key.equals(PreferenceKeys.PREF_MUSIC_ENABLED)){
                      if(prefs.getBoolean(key, false)){
                          TitleScreen.startMusic(getApplicationContext());
                      }else{
                          TitleScreen.stopMusic();
                      }
                  }
	    	  }
	    	};
	    	prefs.registerOnSharedPreferenceChangeListener(changeListener);
	}

	@Override
	protected void onPause() {
	    super.onPause();
        TitleScreen.pauseMusic();
	    prefs.unregisterOnSharedPreferenceChangeListener(changeListener);
	}



    protected void displayToast(String message){
		Context context = getApplicationContext();
		CharSequence text = message;
		int duration = Toast.LENGTH_SHORT;
		
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	protected void throwInvalidNumber(SharedPreferences.Editor editor, String key, double n){
		editor.putString(key, Double.toString(n));
		
		displayToast("Please enter a valid number");

		editor.commit();
		
		EditTextPreference prefText = (EditTextPreference) findPreference(key);
		prefText.setText(Double.toString(n));
	}


    @Override
    protected void onStop() {
        super.onStop();
        TitleScreen.checkToStopMusic();
    }

    @Override
    public void onBackPressed() {
        TitleScreen.nextActivity = true;
        super.onBackPressed();
    }

}
