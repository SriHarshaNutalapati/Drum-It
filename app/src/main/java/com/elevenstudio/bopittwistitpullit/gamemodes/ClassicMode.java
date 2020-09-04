package com.elevenstudio.bopittwistitpullit.gamemodes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;

import com.elevenstudio.bopittwistitpullit.R;
import com.elevenstudio.bopittwistitpullit.utility.GameSettings;

import static android.content.Context.MODE_PRIVATE;

class ClassicMode extends GameMode{
    // game settings
    private GameSettings game_settings;
    // preferences (Classic mode stats)
    private SharedPreferences classic_mode_prefs;

    private Chronometer timer_view;

    private int eng_selected_view_change_timer = 1500;
    private final int MINIMUM_TIME_INTERVAL = 700; // milliseconds
    private int elapsedMilliSecSinceStart;

    private Context context;

    public void setTime_interval_gap_score_count() {
        time_interval_gap_score_count += 1;
    }

    private int time_interval_gap_score_count = -1;

    public ClassicMode(Context current, Chronometer chronometer_timer_view, GameSettings game_settings_obj){
        game_settings = game_settings_obj;
        context = current;
        classic_mode_prefs = context.getSharedPreferences(context.getResources().getString(R.string.classic_mode_stats), MODE_PRIVATE);
        this.timer_view = chronometer_timer_view;
        if(game_settings.getShow_timer()) {
            timer_view.setVisibility(View.VISIBLE);
        } else {
            timer_view.setVisibility(View.GONE);
        }
    }

    public void startTimer(){
        timer_view.start();
    }

    public void stopTimer(){
        timer_view.stop();
        elapsedMilliSecSinceStart = (int) (SystemClock.elapsedRealtime() - timer_view.getBase());
    }

    private void reduce_sleep_timer(){
        Log.i("showLogs", "Entered reduce_sleep_timer()");
        if(eng_selected_view_change_timer >= MINIMUM_TIME_INTERVAL){
            eng_selected_view_change_timer = get_reduction_value();
            if(eng_selected_view_change_timer < MINIMUM_TIME_INTERVAL){
                eng_selected_view_change_timer = MINIMUM_TIME_INTERVAL;
            }
        }
        Log.i("showLogs", "Leaving reduce_sleep_timer()");
    }

    private int get_reduction_value(){
        if(eng_selected_view_change_timer <= 800 && time_interval_gap_score_count == 3){
            time_interval_gap_score_count = 0;
            return eng_selected_view_change_timer - 10;
        }else if(time_interval_gap_score_count >= (int)eng_selected_view_change_timer/100){
            int computed_difference = eng_selected_view_change_timer - (int)eng_selected_view_change_timer/10;
            time_interval_gap_score_count = 0;
            return Math.max(computed_difference, 800);
        }
        return eng_selected_view_change_timer;
    }

    public int get_delay_time(int score){
        this.reduce_sleep_timer();
        return getEng_selected_view_change_timer();
    }

    public int getEng_selected_view_change_timer() {
        return eng_selected_view_change_timer;
    }

    public int get_high_score(){
        return classic_mode_prefs.getInt(context.getResources().getString(R.string.classic_high_score), 0);
    }

    public void update_stats_in_prefs(int score){
        int score_avg = classic_mode_prefs.getInt(context.getResources().getString(R.string.classic_avg_score), 0);
        int games_played = classic_mode_prefs.getInt(context.getResources().getString(R.string.classic_games_played), 0);
        int time_avg = classic_mode_prefs.getInt(context.getResources().getString(R.string.classic_avg_time), 0);
        int best_time = classic_mode_prefs.getInt(context.getResources().getString(R.string.classic_best_time), 0);
        int high_score = classic_mode_prefs.getInt(context.getResources().getString(R.string.classic_high_score), 0);

        int avg_high_score = (score + (score_avg*games_played))/(games_played + 1);
        int avg_time = ((time_avg*games_played) + elapsedMilliSecSinceStart)/(games_played + 1);
        classic_mode_prefs.edit().putInt(context.getResources().getString(R.string.classic_avg_score),avg_high_score).apply();
        classic_mode_prefs.edit().putInt(context.getResources().getString(R.string.classic_avg_time), avg_time).apply();
        classic_mode_prefs.edit().putInt(context.getResources().getString(R.string.classic_games_played), games_played + 1).apply();
        if(score > high_score) classic_mode_prefs.edit().putInt(context.getResources().getString(R.string.classic_high_score), score).apply();
        if(elapsedMilliSecSinceStart > best_time) classic_mode_prefs.edit().putInt(context.getResources().getString(R.string.classic_best_time), elapsedMilliSecSinceStart).apply();
    }
}
