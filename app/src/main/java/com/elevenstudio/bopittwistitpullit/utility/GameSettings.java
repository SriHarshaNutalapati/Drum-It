package com.elevenstudio.bopittwistitpullit.utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.elevenstudio.bopittwistitpullit.R;

import static android.content.Context.MODE_PRIVATE;

public class GameSettings {

    private SharedPreferences game_settings_prefs;
    private Context context;

    public GameSettings(Context current) {
        context = current;
        game_settings_prefs = context.getSharedPreferences(context.getResources().getString(R.string.game_settings), MODE_PRIVATE);
    }

    /*Getters*/
    public Boolean getShow_timer() {
        return game_settings_prefs.getBoolean(context.getResources().getString(R.string.show_timer_setting), true);
    }

    public Boolean getMusic() {
        return game_settings_prefs.getBoolean(context.getResources().getString(R.string.music_setting), true);
    }

    public Boolean getSound() {
        return game_settings_prefs.getBoolean(context.getResources().getString(R.string.sound_setting), true);
    }

    public String getSelected_mode() {
        return game_settings_prefs.getString(context.getResources().getString(R.string.selected_mode_setting), context.getResources().getString(R.string.classic_mode));
    }

    public int getCurrentVersion(){
        return game_settings_prefs.getInt(context.getResources().getString(R.string.version_code), -1);
    }

    /*Setters*/
    public void updateShowTimerSetting(Boolean status){
        game_settings_prefs.edit().putBoolean(context.getResources().getString(R.string.show_timer_setting), status).apply();
    }

    public void updateSoundSetting(Boolean status){
        game_settings_prefs.edit().putBoolean(context.getResources().getString(R.string.sound_setting), status).apply();
    }

    public void updateSelectedMode(String selected_mode){
        game_settings_prefs.edit().putString(context.getResources().getString(R.string.selected_mode_setting), selected_mode).apply();
    }

    public void updateMusicSetting(Boolean status){
        game_settings_prefs.edit().putBoolean(context.getResources().getString(R.string.music_setting), status).apply();
    }

    public void updateVersionCode(int version){
        game_settings_prefs.edit().putInt(context.getResources().getString(R.string.version_code), version).apply();
    }


}
