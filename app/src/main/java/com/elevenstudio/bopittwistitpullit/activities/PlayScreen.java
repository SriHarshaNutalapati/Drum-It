package com.elevenstudio.bopittwistitpullit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.elevenstudio.bopittwistitpullit.R;
import com.elevenstudio.bopittwistitpullit.gamemodes.GameModeFactory;
import com.elevenstudio.bopittwistitpullit.gamemodes.SurvivalMode;
import com.elevenstudio.bopittwistitpullit.utility.EndGameDialog;
import com.elevenstudio.bopittwistitpullit.utility.GameSettings;
import com.elevenstudio.bopittwistitpullit.utility.GameStartDialog;
import com.elevenstudio.bopittwistitpullit.utility.PauseGameDialog;
import com.elevenstudio.bopittwistitpullit.utility.SettingsDialog;
import com.elevenstudio.bopittwistitpullit.utility.sound_manager;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

public class PlayScreen extends AppCompatActivity {

    private int score_recorder = 0;
    private int total_time_played = 0; // in ms
    private Boolean game_paused = false;
    private Boolean btn_tapped_in_cycle = false;
    private int eng_selected_view_change_timer;
    private TextView eng_selected_view;
    private Boolean game_started = false;
    private Boolean game_ended = false;
    private Boolean pause_dialog_open = false;
    private Boolean start_game_dialog_open = true;
    private Boolean high_score_animation_displayed = false;
    private final String[] eng_selected_view_options = {"kick it", "snare it", "crash it"};
    private String eng_selected_text = "";
    private String btn_tap_status = "ga";
    private Boolean tutorial_on = false;
    private int tutorial_seq = 1;
    /*
        "ga": (go ahead) user tapped correct button => resume game
        "ed": (end game) user tapped wrong button (or) user tapped two buttons => end game
        "nr": (no response) user didn't tap any button (no response from user) => end game
    */

    Thread update_eng_selected_view_thread;

    // Animation
    Animation anim_slide_up, anim_button_shake;

    // sounds
    private sound_manager mSoundManager;

    // game settings
    private GameSettings game_settings;
    private String selected_mode;
    private Boolean show_timer;
    private Boolean sound_setting;
    private Boolean show_tutorial;

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

        // create user data object
        game_settings = new GameSettings(PlayScreen.this);
        open_start_game_dialog();
        sound_setting = game_settings.getSound();
        show_timer = game_settings.getShow_timer();
        show_tutorial = game_settings.getShowTutorial();

        // setup text-to-speech
        setup_tts();

        // Animation for the text
        anim_slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);

        anim_button_shake = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.shake_anim);

        // Loading sounds
        setup_sounds();

    }

    private void open_start_game_dialog(){
        final GameStartDialog start_game_popup = new GameStartDialog(PlayScreen.this, game_settings);
        start_game_popup.show_dialog();
        start_game_popup.start_play_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                start_game_popup.dismiss_dialog();
                setup_selected_mode();
                if(show_tutorial){
                    tutorial_on = true;
                    show_tutorial(eng_selected_view_options[0], R.id.kick_it);
                }else{
                    start_game();
                }
            }
        });
    }

    private void show_tutorial(String eng_selected_view_option_tutorial, int btn_id_tutorial){
        set_eng_selected_view(eng_selected_view_option_tutorial);
        set_speech(eng_selected_view_option_tutorial);
        Button btn_to_be_animated = findViewById(btn_id_tutorial);
        btn_to_be_animated.setAnimation(anim_button_shake);
    }

    private void setup_selected_mode(){
        selected_mode = game_settings.getSelected_mode();
        if(selected_mode.equals(getResources().getString(R.string.classic_mode))){
            setContentView(R.layout.classic_play_screen);
        }else if(selected_mode.equals(getResources().getString(R.string.survival_mode))){
            setContentView(R.layout.survival_play_screen);
        }
        // setting views
        eng_selected_view = findViewById(R.id.eng_selected_view); // Engine selected option
        // Create Game Mode Object
        selected_mode_obj = new GameModeFactory(PlayScreen.this, selected_mode, game_settings);
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
        super.onPause();
        if(!pause_dialog_open && !game_ended && game_started) pause_game();
    }

    public void onResume(){
        super.onResume();
        if(!pause_dialog_open && !start_game_dialog_open) resume_game();
        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION   |
                    SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void onStop(){
        super.onStop();
    }

    private void resume_game(){
        start_game();
        if(game_paused && score_recorder != 0) selected_mode_obj.getGame_mode().resumeTimer();
        game_paused = false;
    }

    private void pause_game(){
        game_paused = true;
        btn_tap_status = "ga";
        update_eng_selected_view_thread.interrupt();
        selected_mode_obj.getGame_mode().stopTimer();
        final PauseGameDialog pause_popup = new PauseGameDialog(PlayScreen.this);
        pause_popup.show_dialog();
        pause_dialog_open = true;
        pause_popup.resume_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resume_game();
                pause_popup.dismiss_dialog();
                pause_dialog_open = false;
            }
        });
        pause_popup.exit_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pause_popup.dismiss_dialog();
                end_play_screen();
                pause_dialog_open = false;
            }
        });
    }

    private void start_game(){
        eng_selected_view_change_timer = selected_mode_obj.getGame_mode().get_delay_time(score_recorder);
        selected_mode_obj.getGame_mode().reset_timer();
        selected_mode_obj.getGame_mode().setup_views_before_start();
        update_eng_selected_view_thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
//                        long log_start_time = System.nanoTime();
//                        Log.i("showLogs", "TASK took : " +  ((System.nanoTime()-log_start_time)/1000000)+ "ms");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!end_game("Too slow")){
                                    update_eng_selected_view();
                                    if(score_recorder == 0) selected_mode_obj.getGame_mode().startTimer();
                                    game_started = true;
                                }
                            }
                        });
                        Thread.sleep(eng_selected_view_change_timer);
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        update_eng_selected_view_thread.start();
    }

    private Boolean end_game(String msg){
        if(btn_tap_status.equals("ed") || btn_tap_status.equals("nr")) {
            set_eng_selected_view("game over");
            update_eng_selected_view_thread.interrupt();
            selected_mode_obj.getGame_mode().stopTimer();
            selected_mode_obj.getGame_mode().update_stats_in_prefs(score_recorder);
            selected_mode_obj.getGame_mode().displayEndGameDialog(msg, score_recorder);
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
            btn_tapped_in_cycle = false;
            eng_selected_view_change_timer = selected_mode_obj.getGame_mode().get_delay_time(score_recorder);
        }
    }

    public void kick_it_tapped(View view){
        if(tutorial_on){
            if(tutorial_seq == 1){
                view.clearAnimation();
                tutorial_seq = 2;
                show_tutorial(eng_selected_view_options[1], R.id.snare_it);
            }
            return;
        }
        if(!btn_tapped_in_cycle) {
            btn_tap_status = eng_selected_text.equals("kick it") ? "ga" : "ed";
            if (!end_game("Wrong drum")) {
                mSoundManager.playSound(1, sound_setting);
                set_score();
                btn_tapped_in_cycle = true;
            }
        }
    }

    public void snare_it_tapped(View view){
        if(tutorial_on){
            if(tutorial_seq == 2){
                view.clearAnimation();
                tutorial_seq = 3;
                show_tutorial(eng_selected_view_options[2], R.id.crash_it);
            }
            return;
        }
        if(!btn_tapped_in_cycle){
            btn_tap_status = eng_selected_text.equals("snare it")? "ga" : "ed";
            if (!end_game("Wrong drum")) {
                mSoundManager.playSound(2, sound_setting);
                set_score();
                btn_tapped_in_cycle = true;
            }
        }
    }

    public void crash_it_tapped(View view){
        if(tutorial_on){
            if(tutorial_seq == 3){
                view.clearAnimation();
                tutorial_seq = 4;
                tutorial_on = false;
                start_game();
            }
            return;
        }
        if(!btn_tapped_in_cycle) {
            btn_tap_status = eng_selected_text.equals("crash it") ? "ga" : "ed";
            if (!end_game("Wrong drum")) {
                mSoundManager.playSound(3, sound_setting);
                set_score();
                btn_tapped_in_cycle = true;
            }
        }
    }

    public void pause_tapped(View view){
        pause_game();
    }


    // Utility Methods (no interference with actual logic)
    @SuppressLint("SetTextI18n")
    private void set_score(){
        score_recorder = score_recorder + 1;
        selected_mode_obj.getGame_mode().setScoreView(score_recorder);
//        if(score_recorder > selected_mode_obj.getGame_mode().get_high_score() && !high_score_animation_displayed) {
//            Toast.makeText(PlayScreen.this, "New High Score!!!!!", Toast.LENGTH_LONG).show();  // Remove This
//            high_score_animation_displayed = true;
//        }
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

    @Override
    public void onBackPressed() {
        end_play_screen();
    }

    private void end_play_screen(){
        btn_tap_status = "ed";
        end_game("");
        Intent main_menu_screen = new Intent(PlayScreen.this, MainMenu.class);
        startActivity(main_menu_screen);
        this.finish();
    }
}