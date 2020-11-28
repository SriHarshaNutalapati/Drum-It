package com.elevenstudio.bopittwistitpullit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.elevenstudio.bopittwistitpullit.BuildConfig;
import com.elevenstudio.bopittwistitpullit.R;
import com.elevenstudio.bopittwistitpullit.utility.GameSettings;
import com.elevenstudio.bopittwistitpullit.utility.ModeSelectorDialog;
import com.elevenstudio.bopittwistitpullit.utility.SettingsDialog;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

public class MainMenu extends AppCompatActivity {

    private GameSettings game_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        game_settings = new GameSettings(MainMenu.this);
        checkFirstRun();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION   |
                SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void startPlay(View view){
        Intent play_screen = new Intent(MainMenu.this, PlayScreen.class);
        startActivity(play_screen);
        this.finish();
    }

    public void startStats(View view){
        Intent stats_screen = new Intent(MainMenu.this, StatsScreen.class);
        startActivity(stats_screen);
        this.finish();
    }

    public void show_settings_dialog(View view){
        final SettingsDialog settings_popup = new SettingsDialog(MainMenu.this);
        settings_popup.show_dialog();
        settings_popup.close_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                settings_popup.dismiss_dialog();
            }
        });
    }

    public void show_mode_dialog(View view){
        final ModeSelectorDialog mode_dialog = new ModeSelectorDialog(MainMenu.this, game_settings);
        mode_dialog.show_dialog();
        mode_dialog.close_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mode_dialog.dismiss_dialog();
            }
        });
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    private void checkFirstRun() {
        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        int savedVersionCode = game_settings.getCurrentVersion();

        // Check for first run or upgrade
        if (savedVersionCode == -1) {
            // This is a new install (or the user cleared the shared preferences)
            Toast.makeText(MainMenu.this, "This is first time", Toast.LENGTH_LONG).show();  // Remove This
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
        game_settings.updateVersionCode(currentVersionCode);
    }
}