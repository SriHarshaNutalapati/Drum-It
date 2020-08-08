package com.elevenstudio.bopittwistitpullit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Random;

public class play_screen extends AppCompatActivity {

    private TextView eng_selected_view, score_view, timer_view;  // It is text view

    private final String[] eng_selected_view_options = {"bass it", "snare it", "crash it"};

    private Boolean game_started = false;

    private Boolean high_score_animation_displayed = false;

    private int eng_selected_view_change_timer = 1500;

    private final int MINIMUM_TIME_INTERVAL = 700; // milliseconds

    private int time_interval_gap_score_count = -1;

    TextToSpeech eng_selected_text_speech;

    private String eng_selected_text = "";

    private String btn_tap_status = "ga";
    /*
        "ga": user tapped correct button => resume game
        "ed": user tapped wrong button (or) user tapped two buttons => end game
        "nr": user didn't tap any button (no response from user) => end game
    */

    private int score_recorder = 0;

    Thread update_eng_selected_view_thread;

    // For timer
    private boolean timer_thread = true;
    private int secs = 0;
    private int mins = 0;
    private String mins_holder = "";
    private String secs_holder = "";
    Thread gameTimer;
    private int total_time_played = 0; // in ms

    // for preferences
    final String PREFS_NAME = "com.elevenstudios.btp";
    private SharedPreferences prefs;

    // statistics variables
    private int high_score, score_avg, best_time, time_avg, games_played;
    private Boolean show_timer, music, sound;

    public play_screen() {
        update_eng_selected_view_thread = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_screen);

        eng_selected_view = findViewById(R.id.eng_selected_view);
        score_view = findViewById(R.id.score_view);
        timer_view = findViewById(R.id.timer_view);

        eng_selected_text_speech =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    eng_selected_text_speech.setLanguage(Locale.US);
                }
            }
        });

        get_stats_and_preferences();

        // Update UI based on preferences
        update_ui_on_user_prefs();

        // Start the game
        start_game();
        start_timer();
    }

    public void onPause(){
        if(eng_selected_text_speech !=null){
            eng_selected_text_speech.stop();
            eng_selected_text_speech.shutdown();
        }
        super.onPause();
    }

    private void get_stats_and_preferences(){
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        high_score = prefs.getInt("high_score", 0);
        score_avg = prefs.getInt("score_avg", 0);
        best_time = prefs.getInt("best_time", 0);
        time_avg = prefs.getInt("time_avg", 0);
        games_played = prefs.getInt("games_played", 0);

        show_timer = prefs.getBoolean("show_timer", true);
        music = prefs.getBoolean("music", true);
        sound = prefs.getBoolean("sound", true);
    }

    private void update_ui_on_user_prefs(){
        if(!show_timer) timer_view.setVisibility(View.GONE);
    }

    private void start_timer(){
        gameTimer = new Thread() {
            @Override
            public void run() {
                while (timer_thread) {
                    try {
                        Thread.sleep(1000); //1000ms = 1 sec
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                total_time_played += 1000;
                                secs++;
                                if (secs >= 60) {
                                    secs = 0;
                                    mins += 1;
                                }

                                if (mins <= 9) {
                                    mins_holder = String.format("%02d", mins);
                                } else {
                                    mins_holder = String.valueOf(mins);
                                }
                                if (secs <= 9) {
                                    secs_holder = String.format("%02d", secs);
                                } else {
                                    secs_holder = String.valueOf(secs);
                                }
                                timer_view.setText(mins_holder + ":" + secs_holder);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private void start_game(){
        update_eng_selected_view_thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(eng_selected_view_change_timer);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                end_game();
                                update_eng_selected_view();
                                if(score_recorder == 0) gameTimer.start();
                                game_started = true;
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        update_eng_selected_view_thread.start();
    }

    private boolean end_game(){
        if(btn_tap_status.equals("ed") || btn_tap_status.equals("nr")) {
            set_eng_selected_view("game over");
            set_speech("chillaroda");
            update_eng_selected_view_thread.interrupt();
            timer_thread = false;
            update_stats_in_prefs();
            return true;
        }
        return false;
    }

    private void update_stats_in_prefs(){
        if(game_started){
            int avg_high_score = (score_recorder + score_avg)/(games_played + 1);
            int avg_time = (time_avg + total_time_played)/(games_played + 1);
            prefs.edit().putInt("score_avg",avg_high_score).apply();
            prefs.edit().putInt("time_avg", avg_time).apply();
            prefs.edit().putInt("games_played", games_played + 1).apply();
            if(score_recorder > high_score) prefs.edit().putInt("high_score", score_recorder).apply();
            if(total_time_played > best_time) prefs.edit().putInt("best_time", total_time_played).apply();
            prefs.edit().apply();
        }
    }

    private void update_eng_selected_view(){
        if(btn_tap_status.equals("ga")){
            String option_text = get_random_option();
            set_eng_selected_view(option_text);
            set_speech(option_text);
            eng_selected_text = option_text;
            btn_tap_status = "nr";
            reduce_sleep_timer();
            Log.d("Score", "Score: " + score_recorder);
            Log.d("Score", "Interval Score: " + time_interval_gap_score_count);
            Log.d("Score", "Interval Time: " + eng_selected_view_change_timer);
            Log.d("Score", "=======================================================");
        }

    }

    private void set_eng_selected_view(String text){
        eng_selected_view.setText(text);
    }

    private String get_random_option() {
        int rnd = new Random().nextInt(eng_selected_view_options.length);
        return eng_selected_view_options[rnd];
    }

    public void bass_it_tapped(View view){
        btn_tap_status = eng_selected_text.equals("bass it")? "ga" : "ed";
        if (!end_game()) {
            set_score();
        }
    }

    public void snare_it_tapped(View view){
        btn_tap_status = eng_selected_text.equals("snare it")? "ga" : "ed";
        if (!end_game()) {
                set_score();
        }
    }

    public void crash_it_tapped(View view){
        btn_tap_status = eng_selected_text.equals("crash it")? "ga" : "ed";
        if (!end_game()) {
            set_score();
        }
    }

    private void set_score(){
        score_recorder = score_recorder + 1;
        score_view.setText("Score: " + score_recorder);
        if(score_recorder > high_score && !high_score_animation_displayed) {
            Toast.makeText(play_screen.this, "New High Score!!!!!", Toast.LENGTH_LONG).show();  // Remove This
            high_score_animation_displayed = true;
        }
        time_interval_gap_score_count  = time_interval_gap_score_count + 1;
    }

    private void set_speech(String speech){
        eng_selected_text_speech.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void reduce_sleep_timer(){
        if(eng_selected_view_change_timer >= MINIMUM_TIME_INTERVAL){
            eng_selected_view_change_timer = get_reduction_value();
            if(eng_selected_view_change_timer < MINIMUM_TIME_INTERVAL){
                eng_selected_view_change_timer = MINIMUM_TIME_INTERVAL;
            }
        }
    }

    private int get_reduction_value(){
        if(eng_selected_view_change_timer <= 800 && time_interval_gap_score_count == 3){
            time_interval_gap_score_count = 0;
            return eng_selected_view_change_timer - 10;
        }else if(time_interval_gap_score_count >= (int)eng_selected_view_change_timer/100){
            int computed_difference = eng_selected_view_change_timer - (int)eng_selected_view_change_timer/10;
            time_interval_gap_score_count = 0;
            return computed_difference < 800?800:computed_difference;
        }
        return eng_selected_view_change_timer;
    }
}