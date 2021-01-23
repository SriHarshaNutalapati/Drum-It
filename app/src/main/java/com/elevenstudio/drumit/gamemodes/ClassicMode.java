package com.elevenstudio.drumit.gamemodes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.elevenstudio.drumit.R;
import com.elevenstudio.drumit.activities.MainMenu;
import com.elevenstudio.drumit.activities.PlayScreen;
import com.elevenstudio.drumit.utility.EndGameDialog;
import com.elevenstudio.drumit.utility.GameSettings;
import com.muddzdev.styleabletoast.StyleableToast;

import static android.content.Context.MODE_PRIVATE;

public class ClassicMode extends GameMode{
    private final int MAX_LIFES = 3;
    // game settings
    private GameSettings game_settings;
    // preferences (Classic mode stats)
    private SharedPreferences classic_mode_prefs;

    public Chronometer timer_view;
    private TextView score_view;
    private int mTimeWhenStopped = 0;

    private int eng_selected_view_change_timer = 2000;
    private final int MINIMUM_TIME_INTERVAL = 700; // milliseconds
    private int elapsedMilliSecSinceStart;

    private Activity context;

    private int no_life_image;
    private ImageView third_image_view, second_image_view, first_image_view;

    public void setTime_interval_gap_score_count() {
        time_interval_gap_score_count += 1;
    }

    private int time_interval_gap_score_count = -1;

    public ClassicMode(Activity current, GameSettings game_settings_obj){
        game_settings = game_settings_obj;
        context = current;
        classic_mode_prefs = context.getSharedPreferences(context.getResources().getString(R.string.classic_mode_stats), MODE_PRIVATE);
        this.timer_view = current.findViewById(R.id.timer_view);
        this.score_view = current.findViewById(R.id.score_view);
        this.third_image_view = current.findViewById(R.id.life_image_3);
        this.second_image_view = current.findViewById(R.id.life_image_2);
        this.first_image_view = current.findViewById(R.id.life_image_1);
        this.no_life_image = R.drawable.no_life_image;
//        if(game_settings.getShow_timer()) {
//            timer_view.setVisibility(View.VISIBLE);
//        } else {
//        }
    }

    public static SharedPreferences get_class_mode_prefs(Context context){
        return context.getSharedPreferences(context.getResources().getString(R.string.classic_mode_stats), MODE_PRIVATE);
    }

    public void setup_views_before_start(){
        context.findViewById(R.id.score_view).setVisibility(View.VISIBLE);
        context.findViewById(R.id.timer_view).setVisibility(View.VISIBLE);
    }

    public void startTimer(){
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
        endGameDialog.score_View.setText("Score: " + score);
        if(classic_mode_prefs.getInt(context.getResources().getString(R.string.classic_high_score), 0) > score){
            StyleableToast.makeText(context, "High Score!", Toast.LENGTH_LONG, R.style.achievement_style).show();
        }
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

    private int reduce_sleep_timer(){
        return get_reduction_value();
    }

    private int get_reduction_value(){
        if(eng_selected_view_change_timer > 1300 && eng_selected_view_change_timer <= 2000){
            eng_selected_view_change_timer = eng_selected_view_change_timer - 75;
        }else if(eng_selected_view_change_timer <= 1300 && eng_selected_view_change_timer > 1100){
            eng_selected_view_change_timer = eng_selected_view_change_timer - 25;
        }else if(eng_selected_view_change_timer <= 1100 && eng_selected_view_change_timer > 1000){
            eng_selected_view_change_timer = eng_selected_view_change_timer - 10;
        }else if(eng_selected_view_change_timer <= 1000 && eng_selected_view_change_timer > 800){
            eng_selected_view_change_timer = eng_selected_view_change_timer - 5;
        }else if(eng_selected_view_change_timer <= 800){
            eng_selected_view_change_timer = 800;
        }
        return eng_selected_view_change_timer;
    }

    public int get_delay_time(int score){
        this.reduce_sleep_timer();
        return eng_selected_view_change_timer;
    }

    public void reset_timer(){
        timer_view.setBase(SystemClock.elapsedRealtime());
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

    public void slow_down_timer() {
        eng_selected_view_change_timer = eng_selected_view_change_timer + 300;
    }

    public void setScoreView(int score){
        this.score_view.setText("Score: " + score);
//        if(score == 10){
//            StyleableToast.makeText(context, "You're Good!", Toast.LENGTH_SHORT, R.style.achievement_style).show();
//        }else if(score == 25){
//            StyleableToast.makeText(context, "Way to go!", Toast.LENGTH_SHORT, R.style.achievement_style).show();
//        }else if(score == 40){
//            StyleableToast.makeText(context, "Superb game play!", Toast.LENGTH_SHORT, R.style.achievement_style).show();
//        }else if(score == 50){
//            StyleableToast.makeText(context, "Awesome!", Toast.LENGTH_SHORT, R.style.achievement_style).show();
//        }else if(score == 57){
//            StyleableToast.makeText(context, "Marvellous!", Toast.LENGTH_SHORT, R.style.achievement_style).show();
//        }else if(score == 75){
//            StyleableToast.makeText(context, "Out of box playing!", Toast.LENGTH_SHORT, R.style.achievement_style).show();
//        }else if(score == 100){
//            StyleableToast.makeText(context, "Sensational!", Toast.LENGTH_SHORT, R.style.achievement_style).show();
//        }else if(score == 125){
//            StyleableToast.makeText(context, "You're Great!!", Toast.LENGTH_SHORT, R.style.achievement_style).show();
//        }else if(score == 200){
//            StyleableToast.makeText(context, "You're No.1", Toast.LENGTH_SHORT, R.style.achievement_style).show();
//        }
    }

    public int get_lifes_remaining() {
        return MAX_LIFES;
    }

    public void set_life_view(int lifes_remaining){
        if(lifes_remaining == 2){
            third_image_view.setImageResource(this.no_life_image);
            eng_selected_view_change_timer = eng_selected_view_change_timer + 50;
        }else if(lifes_remaining == 1){
            second_image_view.setImageResource(this.no_life_image);
            eng_selected_view_change_timer = eng_selected_view_change_timer + 50;
        }else{
            first_image_view.setImageResource(this.no_life_image);
        }
    }
}
