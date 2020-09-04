package com.elevenstudio.bopittwistitpullit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.elevenstudio.bopittwistitpullit.R;
import com.elevenstudio.bopittwistitpullit.gamemodes.GameModeFactory;
import com.elevenstudio.bopittwistitpullit.utility.GameSettings;
import com.elevenstudio.bopittwistitpullit.utility.sound_manager;

import java.util.Locale;
import java.util.Random;

public class PlayScreen extends AppCompatActivity {

    private int score_recorder = 0;
    private int total_time_played = 0; // in ms
    private int eng_selected_view_change_timer;
    private TextView eng_selected_view, score_view;
    private Boolean game_started = false;
    private Boolean game_ended = false;
    private Boolean high_score_animation_displayed = false;
    private final String[] eng_selected_view_options = {"kick it", "snare it", "crash it"};
    private String eng_selected_text = "";
    private String btn_tap_status = "ga";
    /*
        "ga": (go ahead) user tapped correct button => resume game
        "ed": (end game) user tapped wrong button (or) user tapped two buttons => end game
        "nr": (no response) user didn't tap any button (no response from user) => end game
    */

    Thread update_eng_selected_view_thread;

    // Animation
    Animation anim_slide_up;

    // sounds
    private sound_manager mSoundManager;

    // game settings
    private GameSettings game_settings;
    private String selected_mode;
    private Boolean show_timer;
    private Boolean sound_setting;

    // TTS
    TextToSpeech eng_selected_text_speech;

    // mode selected
    private GameModeFactory selected_mode_obj;

    public PlayScreen() {
        update_eng_selected_view_thread = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_screen);

        eng_selected_view = findViewById(R.id.eng_selected_view); // Engine selected option
        score_view = findViewById(R.id.score_view); // User score view

        // create user data object
        game_settings = new GameSettings(PlayScreen.this);
        selected_mode = game_settings.getSelected_mode();
        sound_setting = game_settings.getSound();
        show_timer = game_settings.getShow_timer();


        // Create Game Mode Object
        selected_mode_obj = new GameModeFactory(PlayScreen.this, selected_mode, (Chronometer) findViewById(R.id.timer_view), (TextView) findViewById(R.id.count_down_timer_view), game_settings);
        eng_selected_view_change_timer = selected_mode_obj.getGame_mode().get_delay_time(score_recorder);

        // setup text-to-speech
        setup_tts();

        // Animation for the text
        anim_slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);

        // Loading sounds
        setup_sounds();

        // Start the game
        start_game();
    }

    private void setup_tts() {
        eng_selected_text_speech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    eng_selected_text_speech.setLanguage(Locale.US);
                }
            }
        });
    }
    
    public void onPause(){
        if(eng_selected_text_speech !=null){
            eng_selected_text_speech.stop();
            eng_selected_text_speech.shutdown();
        }
        super.onPause();
    }

    private void start_game(){
        update_eng_selected_view_thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
//                        long log_start_time = System.nanoTime();
                        Thread.sleep(eng_selected_view_change_timer);
//                        Log.i("showLogs", "TASK took : " +  ((System.nanoTime()-log_start_time)/1000000)+ "ms");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("showLogs", "Entered run()");
                                if(!end_game()){
                                    update_eng_selected_view();
                                    if(score_recorder == 0) selected_mode_obj.getGame_mode().startTimer();
                                    game_started = true;
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        update_eng_selected_view_thread.start();
    }

    private Boolean end_game(){
        if(btn_tap_status.equals("ed") || btn_tap_status.equals("nr")) {
            set_eng_selected_view("game over");
            update_eng_selected_view_thread.interrupt();
            selected_mode_obj.getGame_mode().stopTimer();
            selected_mode_obj.getGame_mode().update_stats_in_prefs(score_recorder);
            game_ended = true;
            return true;
        }
        return false;
    }

    private void update_eng_selected_view(){
        if(btn_tap_status.equals("ga")){
            String option_text = get_random_option();
            set_eng_selected_view(option_text);
            set_speech(option_text);
            eng_selected_text = option_text;
            btn_tap_status = "nr";
            eng_selected_view_change_timer = selected_mode_obj.getGame_mode().get_delay_time(score_recorder);
        }
    }

    public void kick_it_tapped(View view){
        btn_tap_status = eng_selected_text.equals("kick it")? "ga" : "ed";
        if (!game_ended) {
            mSoundManager.playSound(1, sound_setting);
            set_score();
        }
    }

    public void snare_it_tapped(View view){
        btn_tap_status = eng_selected_text.equals("snare it")? "ga" : "ed";
        if (!game_ended) {
            mSoundManager.playSound(2, sound_setting);
            set_score();
        }
    }

    public void crash_it_tapped(View view){
        btn_tap_status = eng_selected_text.equals("crash it")? "ga" : "ed";
        if (!game_ended) {
            mSoundManager.playSound(3, sound_setting);
            set_score();
        }
    }


    // Utility Methods (no interference with actual logic)
    @SuppressLint("SetTextI18n")
    private void set_score(){
        score_recorder = score_recorder + 1;
        score_view.setText("Score: " + score_recorder);
        if(score_recorder > selected_mode_obj.getGame_mode().get_high_score() && !high_score_animation_displayed) {
            Toast.makeText(PlayScreen.this, "New High Score!!!!!", Toast.LENGTH_LONG).show();  // Remove This
            high_score_animation_displayed = true;
        }
        selected_mode_obj.getGame_mode().setTime_interval_gap_score_count();
    }

    private void set_speech(String speech){
        eng_selected_text_speech.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void set_eng_selected_view(String text){
        eng_selected_view.setText(text);
        eng_selected_view.startAnimation(anim_slide_up);
    }

    private String get_random_option() {
        int rnd = new Random().nextInt(eng_selected_view_options.length);
        return eng_selected_view_options[rnd];
    }

    private void setup_sounds(){
        mSoundManager = new sound_manager();
        mSoundManager.initSounds(getBaseContext());
        mSoundManager.addSound(1, R.raw.kick_drum_sound);
        mSoundManager.addSound(2, R.raw.snare_drum_sound);
        mSoundManager.addSound(3, R.raw.crash_it_sound);
    }
}