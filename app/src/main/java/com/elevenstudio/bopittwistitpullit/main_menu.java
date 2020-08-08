package com.elevenstudio.bopittwistitpullit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class main_menu extends AppCompatActivity {

    // for preferences
    final String PREFS_NAME = "com.elevenstudios.btp";
    final String PREF_VERSION_CODE_KEY = "version_code";
    final int DOESNT_EXIST = -1;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        checkFirstRun();
    }

    public void startPlay(View view){
        Intent play_screen = new Intent(main_menu.this, play_screen.class);
        startActivity(play_screen);
    }

    public void startStats(View view){
        Intent stats_screen = new Intent(main_menu.this, stats_screen.class);
        startActivity(stats_screen);
    }

    private void checkFirstRun() {
        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (savedVersionCode == DOESNT_EXIST) {
            // This is a new install (or the user cleared the shared preferences)
            Toast.makeText(main_menu.this, "This is first time", Toast.LENGTH_LONG).show();  // Remove This
            prefs.edit().putInt("high_score", 0).apply(); // set high score to 0
            prefs.edit().putInt("best_time", 0).apply();
            prefs.edit().putInt("score_avg",0).apply();
            prefs.edit().putInt("time_avg", 0).apply();
            prefs.edit().putInt("games_played", 0).apply();
            prefs.edit().putBoolean("music", true).apply();
            prefs.edit().putBoolean("sound", true).apply();
            prefs.edit().putBoolean("show_timer", true).apply();
            prefs.edit().apply();
        } else if (currentVersionCode >= savedVersionCode) {
            // This is just a normal run
            // No effect even if there is an upgrade as of now
            return;
        }
        /*
            else if (currentVersionCode > savedVersionCode) {
                //  Runs when user upgrades app.
                //  Not required as of now
            }

        */

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }
}