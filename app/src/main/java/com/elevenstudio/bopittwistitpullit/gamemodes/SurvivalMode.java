package com.elevenstudio.bopittwistitpullit.gamemodes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.elevenstudio.bopittwistitpullit.R;
import com.elevenstudio.bopittwistitpullit.activities.MainMenu;
import com.elevenstudio.bopittwistitpullit.activities.PlayScreen;
import com.elevenstudio.bopittwistitpullit.utility.EndGameDialog;
import com.elevenstudio.bopittwistitpullit.utility.GameSettings;
import com.muddzdev.styleabletoast.StyleableToast;

import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class SurvivalMode extends GameMode{
    private SharedPreferences survival_mode_prefs;
    private Boolean time_up = false;

    private int eng_selected_view_change_timer; // gap between two taps (in milli seconds)
    private CountDownTimer count_down_timer;
    private TextView count_down_timer_view, current_speed_text, future_speed_text_view;

    private final int MINIMUM_TIME_INTERVAL = 700; // milliseconds

    private int time_interval_gap_score_count = -1; // gap size(number of taps)
    private int elapsedMilliSecSinceStart;
    private long time_remaning;
    private int gap_size_assignee = -1; // assigns the gap size (random number between 2 and 4 (both inclusive)
    private String future_speed = "";

    private Activity context;

    public SurvivalMode(Activity current){
        context = current;
        this.setup_timer();
        gap_size_assignee = new Random().nextInt(5 - 2) + 2; // gives a random number between 4 and 2
        survival_mode_prefs = context.getSharedPreferences(context.getResources().getString(R.string.survival_mode_stats), MODE_PRIVATE);
        this.count_down_timer_view = (TextView) current.findViewById(R.id.count_down_timer_view);
        this.current_speed_text = (TextView) current.findViewById(R.id.current_speed_text);
    }

    public static SharedPreferences get_survival_mode_prefs(Context context){
        return context.getSharedPreferences(context.getResources().getString(R.string.survival_mode_stats), MODE_PRIVATE);
    }

    public void setup_views_before_start(){
        context.findViewById(R.id.count_down_timer_view).setVisibility(View.VISIBLE);
    }

    public void startTimer(){
        count_down_timer.start();
    }

    public void stopTimer(){
        count_down_timer.cancel();
    }

    public void resumeTimer(){
        activate_timer(time_remaning);
        count_down_timer.start();
    }

    public void setup_timer(){
        final int count_down_time = new Random().nextInt((40001 - 30000)) + 30000; // generates a time between 40sec and 30 sec (in milli seconds)
        eng_selected_view_change_timer = 1150;
        activate_timer(count_down_time);
    }

    public void displayEndGameDialog(String msg, int score) {
        if(time_up) return;
        final EndGameDialog endGameDialog = new EndGameDialog(context);
        endGameDialog.show_dialog(msg);
        String second = Integer.toString(((elapsedMilliSecSinceStart /1000)%60));
        String minute = Integer.toString(((elapsedMilliSecSinceStart /(1000*60))%60));
        if(second.length() == 1) second = "0" + second;
        endGameDialog.score_View.setText(String.format("%s: 0%s:%s", "Survived", minute, second));
        endGameDialog.main_menu_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                endGameDialog.dismiss_dialog();
                Intent main_menu_screen = new Intent(context, MainMenu.class);
                context.startActivity(main_menu_screen);
                context.finish();
            }
        });

        endGameDialog.play_again_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                endGameDialog.dismiss_dialog();
                Intent play_again_btn = new Intent(context, PlayScreen.class);
                context.startActivity(play_again_btn);
                context.finish();
            }
        });
        endGameDialog.setCancelable(false);
        endGameDialog.setCanceledOnTouchOutside(false);
    }
    private void activate_timer(final long count_down_time){

        // Countdown Timer
        count_down_timer = new CountDownTimer(count_down_time,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String second = Integer.toString((int) ((millisUntilFinished /1000)%60));
                String minute = Integer.toString((int) ((millisUntilFinished /(1000*60))%60));
                if(second.length() == 1) second = "0" + second;
                count_down_timer_view.setText(String.format("0%s:%s", minute, second));
                time_remaning = millisUntilFinished;
                elapsedMilliSecSinceStart = (int) (count_down_time-millisUntilFinished);
            }
            @Override
            public void onFinish() {
                displayEndGameDialog("You made it! Congratulations!", 0);
                time_up = true;
            }
        };
    }

    private void reduce_sleep_timer(){
        if(time_interval_gap_score_count >= gap_size_assignee){
            gap_size_assignee = new Random().nextInt(5 - 2) + 2; // gives a random number between 4 and 2
            time_interval_gap_score_count = 0;
            int function_execution_number = getRandomElement(new int[]{1,2,3});
            if(function_execution_number == 1){
                eng_selected_view_change_timer = get_reduction_value_classic();
            }else{
                eng_selected_view_change_timer = get_reduction_value_hi_lo();
            }
        }
//        Log.i("time_log", "Gap assignee: " + gap_size_assignee);
//        Log.i("time_log", "time_interval_gap_score_count: " + time_interval_gap_score_count);
    }

    private int get_reduction_value_classic(){
        if(time_remaning > 20000){
            // Not even 10 sec have passed since game start. So select a values between medium, slow and very slow ranges.
            eng_selected_view_change_timer = 1150;
        }else if(time_remaning <= 20000 && time_remaning > 10000){
            // More than 10 sec have passed since game start. So select a values between very fast, fast, medium and slow ranges.
            eng_selected_view_change_timer = 900;
        }else if(time_remaning <= 10000){
            // More than 20 sec have passed since game start. So select a values between very fast, fast, medium and slow ranges.
            eng_selected_view_change_timer = 750;
        }
        if(time_remaning < 23000 && time_remaning > 20000){
            StyleableToast.makeText(context, "Superb! Speed up warning!", Toast.LENGTH_SHORT, R.style.achievement_style).show();
        }else if(time_remaning < 13000 && time_remaning > 10000){
            StyleableToast.makeText(context, "Awesome! Speed up warning!", Toast.LENGTH_SHORT, R.style.achievement_style).show();
        }
        return eng_selected_view_change_timer;
    }

    private int get_reduction_value_hi_lo(){
        if(time_remaning > 20000){
            // Not even 10 sec have passed since game start. So select a values between medium, slow and very slow ranges.
            eng_selected_view_change_timer = getRandomElement(new int[]{1000, 1000 ,1150, 1150, 1150, 1150, 1300, 1300, 1300, 1300});

        }else if(time_remaning <= 20000 && time_remaning > 10000){
            // More than 10 sec have passed since game start. So select a values between very fast, fast, medium and slow ranges.
            eng_selected_view_change_timer = getRandomElement(new int[]{650, 650, 850, 850, 850, 1000, 1000, 1000, 1150, 1150});
        }else if(time_remaning <= 10000){
            // More than 20 sec have passed since game start. So select a values between very fast, fast, medium and slow ranges.
            eng_selected_view_change_timer = getRandomElement(new int[]{650, 650, 650, 650, 650, 650, 850, 850, 850, 850});
        }
        if(time_remaning < 23000 && time_remaning > 20000){
            StyleableToast.makeText(context, "Superb! Speed up warning!", Toast.LENGTH_SHORT, R.style.achievement_style).show();
        }else if(time_remaning < 13000 && time_remaning > 10000){
            StyleableToast.makeText(context, "Awesome! Speed up warning!", Toast.LENGTH_SHORT, R.style.achievement_style).show();
        }
//        Log.i("time_log", "Mode: " + String.valueOf(eng_selected_view_change_timer));
//        Log.i("time_log", "Seconds Passed: " + count_down_timer_view.getText());
        return eng_selected_view_change_timer;
    }

    public void setTime_interval_gap_score_count() {
        time_interval_gap_score_count += 1;
    }

    public int get_delay_time(int score){
        this.reduce_sleep_timer();
//        set_speed_text();
        return eng_selected_view_change_timer;
    }

    private void set_speed_text(){
        this.current_speed_text.setText(GameSettings.speed_to_name_map.get(eng_selected_view_change_timer));
    }
    private static int getRandomElement(int[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }

    public void update_stats_in_prefs(int score){
        int games_played = survival_mode_prefs.getInt(context.getResources().getString(R.string.survival_games_played), 0);
        int time_avg = survival_mode_prefs.getInt(context.getResources().getString(R.string.survival_avg_time), 0);
        int best_time = survival_mode_prefs.getInt(context.getResources().getString(R.string.survival_best_time), 0);

        int avg_time = ((time_avg*games_played) + elapsedMilliSecSinceStart)/(games_played + 1);
        survival_mode_prefs.edit().putInt(context.getResources().getString(R.string.survival_avg_time), avg_time).apply();
        survival_mode_prefs.edit().putInt(context.getResources().getString(R.string.survival_games_played), games_played + 1).apply();
        if(elapsedMilliSecSinceStart > best_time) survival_mode_prefs.edit().putInt(context.getResources().getString(R.string.survival_best_time), elapsedMilliSecSinceStart).apply();
    }
}
