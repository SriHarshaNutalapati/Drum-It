package com.elevenstudio.bopittwistitpullit.gamemodes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.elevenstudio.bopittwistitpullit.R;

import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

class SurvivalMode extends GameMode{
    // preferences (Classic mode stats)
    private SharedPreferences survival_mode_prefs;

    private int eng_selected_view_change_timer;
    private CountDownTimer count_down_timer;
    private TextView count_down_timer_view;

    private final int MINIMUM_TIME_INTERVAL = 700; // milliseconds

    private int time_interval_gap_score_count = -1;
    private int elapsedMilliSecSinceStart;

    private Context context;

    public SurvivalMode(Context current, TextView count_down_timer_view){
        context = current;
        this.count_down_timer_view = count_down_timer_view;
        this.activate_timer();
        this.count_down_timer_view.setVisibility(View.VISIBLE);
        survival_mode_prefs = context.getSharedPreferences(context.getResources().getString(R.string.survival_mode_stats), MODE_PRIVATE);
    }

    public void startTimer(){
        count_down_timer.start();
    }
    private void activate_timer(){
        final int count_down_time = new Random().nextInt((180000 - 100000) + 1) + 100000; // generates a time between 1 min and 3 min
        eng_selected_view_change_timer = count_down_time/100;
        // Countdown Timer
        count_down_timer = new CountDownTimer(count_down_time,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String second = Integer.toString((int) ((millisUntilFinished /1000)%60));
                String minute = Integer.toString((int) ((millisUntilFinished /(1000*60))%60));
                if(second.length() == 1) second = "0" + second;
                count_down_timer_view.setText(String.format("0%s:%s", minute, second));
                elapsedMilliSecSinceStart = (int) (count_down_time-millisUntilFinished);
            }
            @Override
            public void onFinish() {
                count_down_timer_view.setText("Finished");
            }
        };
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

    public void setTime_interval_gap_score_count() {
        time_interval_gap_score_count += 1;
    }

    public int get_delay_time(int score){
        this.reduce_sleep_timer();
        return eng_selected_view_change_timer;
    }

    public void update_stats_in_prefs(int score){
        int games_played = survival_mode_prefs.getInt(context.getResources().getString(R.string.survival_games_played), 0);
        int time_avg = survival_mode_prefs.getInt(context.getResources().getString(R.string.survival_avg_time), 0);
        int best_time = survival_mode_prefs.getInt(context.getResources().getString(R.string.survival_best_time), 0);

        int avg_time = ((time_avg*games_played) + elapsedMilliSecSinceStart)/(games_played + 1);
        survival_mode_prefs.edit().putInt(context.getResources().getString(R.string.survival_avg_time), avg_time).apply();
        survival_mode_prefs.edit().putInt(context.getResources().getString(R.string.classic_games_played), games_played + 1).apply();
        if(elapsedMilliSecSinceStart > best_time) survival_mode_prefs.edit().putInt(context.getResources().getString(R.string.classic_best_time), elapsedMilliSecSinceStart).apply();
    }
}
