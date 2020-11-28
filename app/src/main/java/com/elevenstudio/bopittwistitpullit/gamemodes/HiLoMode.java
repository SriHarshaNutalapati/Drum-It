package com.elevenstudio.bopittwistitpullit.gamemodes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;

import com.elevenstudio.bopittwistitpullit.R;
import com.elevenstudio.bopittwistitpullit.activities.MainMenu;
import com.elevenstudio.bopittwistitpullit.activities.PlayScreen;
import com.elevenstudio.bopittwistitpullit.utility.EndGameDialog;
import com.elevenstudio.bopittwistitpullit.utility.GameSettings;

import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class HiLoMode extends GameMode{
    // game settings
    private GameSettings game_settings;
    // preferences (HiLo mode stats)
    private SharedPreferences hi_lo_mode_prefs;

    private Chronometer timer_view;
    private int mTimeWhenStopped = 0;
    private Random rand_obj = new Random();
    int rand_num;
    private int time_interval_gap_teller = rand_obj.nextInt((5 - 1) + 1) + 1;;

    private int eng_selected_view_change_timer = 1500;
    private final int MINIMUM_TIME_INTERVAL = 700; // milliseconds

    private int time_interval_gap_score_count = 0;
    private int elapsedMilliSecSinceStart;

    private Context context;

    public void setTime_interval_gap_score_count() {
        time_interval_gap_score_count += 1;
    }

    public HiLoMode(Activity current, GameSettings game_settings_obj){
        game_settings = game_settings_obj;
        this.timer_view = current.findViewById(R.id.timer_view);;
        context = current;
        hi_lo_mode_prefs = context.getSharedPreferences(context.getResources().getString(R.string.hi_lo_mode_stats), MODE_PRIVATE);
//        if(game_settings.getShow_timer()) {
//            timer_view.setVisibility(View.VISIBLE);
//        } else {
//        }
    }

    public static SharedPreferences get_hi_lo_mode_prefs(Context context){
        return context.getSharedPreferences(context.getResources().getString(R.string.hi_lo_mode_stats), MODE_PRIVATE);
    }

    public int get_delay_time(int score){
        this.reduce_sleep_timer(score);
        return getEng_selected_view_change_timer();
    }

    public int getEng_selected_view_change_timer() {
        return eng_selected_view_change_timer;
    }

    public void startTimer(){
        timer_view.setBase(SystemClock.elapsedRealtime() - mTimeWhenStopped);
        timer_view.start();
    }

    public void resumeTimer(){
        timer_view.setBase(SystemClock.elapsedRealtime() - mTimeWhenStopped);
        timer_view.start();
    }

    public void stopTimer(){
        timer_view.stop();
        mTimeWhenStopped = (int) (SystemClock.elapsedRealtime() - timer_view.getBase());
        elapsedMilliSecSinceStart = (int) (SystemClock.elapsedRealtime() - timer_view.getBase());
    }

    public void displayEndGameDialog(String msg, int score) {
        final EndGameDialog endGameDialog = new EndGameDialog(context);
        endGameDialog.show_dialog(msg);
        String extra_msg = "";
        if(hi_lo_mode_prefs.getInt(context.getResources().getString(R.string.hi_lo_high_score), 0) == score) extra_msg = "(High score)";
        endGameDialog.score_View.setText("Score: " + score + " " + extra_msg);
        endGameDialog.main_menu_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                endGameDialog.dismiss_dialog();
                Intent main_menu_screen = new Intent(context, MainMenu.class);
                context.startActivity(main_menu_screen);
            }
        });

        endGameDialog.play_again_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                endGameDialog.dismiss_dialog();
                Intent play_again_btn = new Intent(context, PlayScreen.class);
                context.startActivity(play_again_btn);
            }
        });
        endGameDialog.setCancelable(false);
        endGameDialog.setCanceledOnTouchOutside(false);
    }

    private void reduce_sleep_timer(int score){
        Log.i("showLogs", "Entered reduce_sleep_timer()");
        if(eng_selected_view_change_timer >= MINIMUM_TIME_INTERVAL){
            eng_selected_view_change_timer = get_reduction_value(score);
            if(eng_selected_view_change_timer < MINIMUM_TIME_INTERVAL){
                eng_selected_view_change_timer = MINIMUM_TIME_INTERVAL;
            }
        }
        Log.i("showLogs", "Leaving reduce_sleep_timer()");
    }

    private int get_reduction_value(int score){
        rand_num = rand_obj.nextInt((100 - 1) + 1) + 1;
        if(score == 0){
            eng_selected_view_change_timer = rand_obj.nextInt((1600 - 1301) + 1) + 1301; // slow
        }else if(score <= 12 && time_interval_gap_score_count >=3){
            if(rand_num <= 50){
                eng_selected_view_change_timer = rand_obj.nextInt((1300 - 1001) + 1) + 1001; // medium
            }else{
                eng_selected_view_change_timer = rand_obj.nextInt((1600 - 1301) + 1) + 1301; // slow
            }

        }else if((score >= 13 && score <= 19) && time_interval_gap_score_count >=3){
            if(rand_num <= 50){
                eng_selected_view_change_timer = rand_obj.nextInt((1000 - 801) + 1) + 801; // fast
            }else{
                eng_selected_view_change_timer = rand_obj.nextInt((1300 - 1001) + 1) + 1001; // medium
            }
        }else if((score >= 20 && score <= 23)){
            eng_selected_view_change_timer = rand_obj.nextInt((800 - 700) + 1) + 700; // very fast
        }else if(score >= 24 && time_interval_gap_score_count >=time_interval_gap_teller){
            if(rand_num <= 30){
                eng_selected_view_change_timer = rand_obj.nextInt((800 - 700) + 1) + 700; // very fast
            }else if(rand_num <= 45){
                eng_selected_view_change_timer = rand_obj.nextInt((1000 - 801) + 1) + 801; // fast
            }else if(rand_num <= 75){
                eng_selected_view_change_timer = rand_obj.nextInt((1300 - 1001) + 1) + 1001; // medium
            }else if(rand_num <= 90){
                eng_selected_view_change_timer = rand_obj.nextInt((1600 - 1301) + 1) + 1301; // slow
            }else if(rand_num <= 100){
                eng_selected_view_change_timer = rand_obj.nextInt((2000 - 1601) + 1) + 1601; // very slow
            }
        }
        time_interval_gap_score_count = 0;
        return eng_selected_view_change_timer;
    }

    public int get_high_score(){
        return hi_lo_mode_prefs.getInt(context.getResources().getString(R.string.hi_lo_high_score), 0);
    }

    public void update_stats_in_prefs(int score){
        int score_avg = hi_lo_mode_prefs.getInt(context.getResources().getString(R.string.hi_lo_avg_score), 0);
        int games_played = hi_lo_mode_prefs.getInt(context.getResources().getString(R.string.hi_lo_games_played), 0);
        int time_avg = hi_lo_mode_prefs.getInt(context.getResources().getString(R.string.hi_lo_avg_time), 0);
        int best_time = hi_lo_mode_prefs.getInt(context.getResources().getString(R.string.hi_lo_best_time), 0);
        int high_score = hi_lo_mode_prefs.getInt(context.getResources().getString(R.string.hi_lo_high_score), 0);

        int avg_high_score = (score + (score_avg*games_played))/(games_played + 1);
        int avg_time = ((time_avg*games_played) + elapsedMilliSecSinceStart)/(games_played + 1);
        hi_lo_mode_prefs.edit().putInt(context.getResources().getString(R.string.hi_lo_avg_score),avg_high_score).apply();
        hi_lo_mode_prefs.edit().putInt(context.getResources().getString(R.string.hi_lo_avg_time), avg_time).apply();
        hi_lo_mode_prefs.edit().putInt(context.getResources().getString(R.string.hi_lo_games_played), games_played + 1).apply();
        if(score > high_score) hi_lo_mode_prefs.edit().putInt(context.getResources().getString(R.string.hi_lo_high_score), score).apply();
        if(elapsedMilliSecSinceStart > best_time) hi_lo_mode_prefs.edit().putInt(context.getResources().getString(R.string.hi_lo_best_time), elapsedMilliSecSinceStart).apply();
    }
}
