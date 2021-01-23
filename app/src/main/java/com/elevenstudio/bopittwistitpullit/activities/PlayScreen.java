package com.elevenstudio.bopittwistitpullit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.captaindroid.tvg.Tvg;
import com.elevenstudio.bopittwistitpullit.R;
import com.elevenstudio.bopittwistitpullit.gamemodes.GameModeFactory;
import com.elevenstudio.bopittwistitpullit.handlers.UserWaitingHandlerThread;
import com.elevenstudio.bopittwistitpullit.utility.GameSettings;
import com.elevenstudio.bopittwistitpullit.utility.GameStartDialog;
import com.elevenstudio.bopittwistitpullit.utility.PauseGameDialog;
import com.elevenstudio.bopittwistitpullit.utility.ProbabilityList;
import com.elevenstudio.bopittwistitpullit.utility.sound_manager;
import com.muddzdev.styleabletoast.StyleableToast;

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
    private final String[] eng_selected_view_options = {"kick it", "snare it", "crash it", "ride it", "hat it", "tom it"};
    private String eng_selected_text = "";
    private String btn_tap_status = "ga";
    private Boolean tutorial_on = false;
    private int tutorial_seq = 1;
    private int lifes_remaining;
    private String end_game_status_text = "Too slow!";
    /*
        "ga": (go ahead) user tapped correct button => resume game
        "ed": (end game) user tapped wrong button (or) user tapped two buttons => end game
        "nr": (no response) user didn't tap any button (no response from user) => end game
    */

    UserWaitingHandlerThread update_eng_selected_view_thread, powerup_handler_thread;

    // Animation
    Animation anim_slide_up, anim_button_shake;

    // sounds
    private sound_manager mSoundManager;

    private TextToSpeech eng_selected_text_speech;

    // game settings
    private GameSettings game_settings;
    private String selected_mode;
    private Boolean show_timer;
    private Boolean sound_setting;

    // mode selected
    private GameModeFactory selected_mode_obj;

    // colors for animated text
    int kick_it_start, kick_it_end, snare_it_start, snare_it_end, crash_it_start, crash_it_end;

    public PlayScreen() {
        update_eng_selected_view_thread = null;
        powerup_handler_thread = null;
    }

    // power_ups
    private ProgressBar powerUpProgressBar;
    private ImageView powerUpImageView;
    private int progressStatus = 0;
    private int multiplier_power_up = 1;
    private Boolean freeze_power_up_activated = false;
    private final ProbabilityList<Object> power_ups = new ProbabilityList<>().add(45, "multiplier").add(45, "freeze").add(10, "slowdown");
    private final ProbabilityList<Object> powerUpImages = new ProbabilityList<>().add(33, R.drawable.slowdown_powerup_activated)
            .add(34, R.drawable.multiplier_powerup)
            .add(33, R.drawable.freeze_powerup_activated);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_screen);

        // create user data object
        game_settings = new GameSettings(PlayScreen.this);
        open_start_game_dialog();
        sound_setting = game_settings.getSound();
        show_timer = game_settings.getShow_timer();

        // Animation for the text
        anim_slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);

        anim_button_shake = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.shake_anim);

        // Loading sounds
        setup_sounds();

        setup_tts();

        kick_it_start = getResources().getColor(R.color.bass_gradient_start_color);
        kick_it_end = getResources().getColor(R.color.bass_gradient_end_color);
        snare_it_start = getResources().getColor(R.color.snare_gradient_start_color);
        snare_it_end = getResources().getColor(R.color.snare_gradient_end_color);
        crash_it_start = getResources().getColor(R.color.cymbal_gradient_start_color);
        crash_it_end = getResources().getColor(R.color.cymbal_gradient_end_color);

        update_eng_selected_view_thread = new UserWaitingHandlerThread(PlayScreen.this);
        update_eng_selected_view_thread.start();
        powerup_handler_thread = new UserWaitingHandlerThread(PlayScreen.this);
        powerup_handler_thread.start();

    }

    private void open_start_game_dialog() {
        final GameStartDialog start_game_popup = new GameStartDialog(PlayScreen.this, game_settings);
        start_game_popup.show_dialog();
        start_game_popup.start_play_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                start_game_popup.dismiss_dialog();
                setup_selected_mode();
                if (game_settings.getShowTutorial()) {
                    tutorial_on = true;
                    show_tutorial(eng_selected_view_options[0], R.id.kick_it);
                } else {
                    start_game();
                }
            }
        });
    }

    private void show_tutorial(String eng_selected_view_option_tutorial, int btn_id_tutorial) {
//        set_eng_selected_view(eng_selected_view_option_tutorial);
//        set_speech(eng_selected_view_option_tutorial);
        change_text_color(eng_selected_view_option_tutorial);
        Button btn_to_be_animated = findViewById(btn_id_tutorial);
        btn_to_be_animated.setAnimation(anim_button_shake);
    }

    private void setup_selected_mode() {
        selected_mode = game_settings.getSelected_mode();
        if (selected_mode.equals(getResources().getString(R.string.classic_mode))) {
            setContentView(R.layout.classic_play_screen);
        } else if (selected_mode.equals(getResources().getString(R.string.survival_mode))) {
            setContentView(R.layout.survival_play_screen);
        }
        // setting views
        eng_selected_view = findViewById(R.id.eng_selected_view); // Engine selected option
        powerUpProgressBar = findViewById(R.id.power_up_progress_bar);
        powerUpImageView = findViewById(R.id.powerUpImage);
        // Create Game Mode Object
        selected_mode_obj = new GameModeFactory(PlayScreen.this, selected_mode, game_settings);

        lifes_remaining = selected_mode_obj.getGame_mode().get_lifes_remaining();
    }


    public void onPause() {
        super.onPause();
        if (!pause_dialog_open && !game_ended && game_started) pause_game();
    }

    public void onResume() {
        super.onResume();
        if (!pause_dialog_open && !start_game_dialog_open) resume_game();
        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void onStop() {
        super.onStop();
    }

    private void resume_game() {
        start_game();
        if (game_paused && score_recorder != 0) selected_mode_obj.getGame_mode().resumeTimer();
        game_paused = false;
    }

    private void pause_game() {
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

    private void start_game() {
        selected_mode_obj.getGame_mode().reset_timer();
        selected_mode_obj.getGame_mode().setup_views_before_start();
        selected_mode_obj.getGame_mode().startTimer();
        game_started = true;
        update_eng_selected_view_thread.getWaitingHandler().post(new UserOptionRunnable());
        powerup_handler_thread.getWaitingHandler().postAtFrontOfQueue(new powerUpRunnable());
    }

    private void change_text_color(String text) {
        switch (text) {
            case "kick it":
                Tvg.change(eng_selected_view, kick_it_start, kick_it_end);
                break;
            case "snare it":
                Tvg.change(eng_selected_view, snare_it_start, snare_it_end);
                break;
            case "crash it":
                Tvg.change(eng_selected_view, crash_it_start, crash_it_end);
                break;
        }
    }

    public void kick_it_tapped(View view) {
        if (tutorial_on) {
            if (tutorial_seq == 1) {
                view.clearAnimation();
                tutorial_seq = 2;
                show_tutorial(eng_selected_view_options[1], R.id.snare_it);
            }
            return;
        }
        if (eng_selected_text.equals("kick it")) {
            right_drum_clicked(0);
        } else if (!btn_tapped_in_cycle) {
            wrong_drum_clicked();
        }
    }

    public void snare_it_tapped(View view) {
        if (tutorial_on) {
            if (tutorial_seq == 2) {
                view.clearAnimation();
                tutorial_seq = 3;
                show_tutorial(eng_selected_view_options[2], R.id.crash_it);
            }
            return;
        }
        if (eng_selected_text.equals("snare it")) {
            right_drum_clicked(1);
        } else if (!btn_tapped_in_cycle) {
            wrong_drum_clicked();
        }
    }

    public void crash_it_tapped(View view) {
        if (tutorial_on) {
            if (tutorial_seq == 3) {
                view.clearAnimation();
                tutorial_seq = 4;
                show_tutorial(eng_selected_view_options[3], R.id.ride_it);
            }
            return;
        }
        if (eng_selected_text.equals("crash it")) {
            right_drum_clicked(2);
        } else if (!btn_tapped_in_cycle) {
            wrong_drum_clicked();
        }
    }

    public void ride_it_tapped(View view) {
        if (tutorial_on) {
            if (tutorial_seq == 4) {
                view.clearAnimation();
                tutorial_seq = 5;
                show_tutorial(eng_selected_view_options[4], R.id.tom_it);
            }
            return;
        }
        if (eng_selected_text.equals("ride it")) {
            right_drum_clicked(3);
        } else if (!btn_tapped_in_cycle) {
            wrong_drum_clicked();
        }
    }

    public void hat_it_tapped(View view) {
        if (tutorial_on) {
            if (tutorial_seq == 5) {
                view.clearAnimation();
                tutorial_seq = 6;
                show_tutorial(eng_selected_view_options[5], R.id.tom_it);
            }
            return;
        }
        if (eng_selected_text.equals("hat it")) {
            right_drum_clicked(4);
        } else if (!btn_tapped_in_cycle) {
            wrong_drum_clicked();
        }
    }

    public void tom_it_tapped(View view) {
        if (tutorial_on) {
            if (tutorial_seq == 6) {
                view.clearAnimation();
                tutorial_seq = 7;
                tutorial_on = false;
                start_game();
            }
            return;
        }
        if (eng_selected_text.equals("tom it")) {
            right_drum_clicked(5);
        } else if (!btn_tapped_in_cycle) {
            wrong_drum_clicked();
        }
    }

    private void wrong_drum_clicked() {
        btn_tap_status = "nr";
        btn_tapped_in_cycle = true;
        end_game_status_text = "Wrong drum!";
    }

    private void right_drum_clicked(int index) {
        mSoundManager.playSound(index, sound_setting);
        if (!btn_tapped_in_cycle) set_score();
        btn_tapped_in_cycle = true;
        btn_tap_status = "ga";
    }

    public void pause_tapped(View view) {
        pause_game();
    }


    // Utility Methods (no interference with actual logic)
    @SuppressLint("SetTextI18n")
    private void set_score() {
        score_recorder = score_recorder + multiplier_power_up;
        selected_mode_obj.getGame_mode().setScoreView(score_recorder);
        selected_mode_obj.getGame_mode().setTime_interval_gap_score_count();
    }

    private void setup_sounds() {
        mSoundManager = new sound_manager();
        mSoundManager.initSounds(getBaseContext());
        mSoundManager.addSound(0, R.raw.kick_drum_sound);
        mSoundManager.addSound(1, R.raw.snare_drum_sound);
        mSoundManager.addSound(2, R.raw.crash_it_sound);
        mSoundManager.addSound(3, R.raw.ride_it_sound);
        mSoundManager.addSound(4, R.raw.hat_it_sound);
        mSoundManager.addSound(5, R.raw.tom_drum_sound);
    }

    @Override
    public void onBackPressed() {
        end_play_screen();
    }

    private void end_play_screen() {
        btn_tap_status = "ed";
        end_game("");
        Intent main_menu_screen = new Intent(PlayScreen.this, MainMenu.class);
        startActivity(main_menu_screen);
        this.finish();
    }

    private void set_eng_selected_view(String text) {
        eng_selected_view.setText(text);
        eng_selected_view.startAnimation(anim_slide_up);
    }

    private class UserOptionRunnable implements Runnable {

        private String option_text;

        private void run_bg_process() {
            if (btn_tap_status.equals("nr") && !freeze_power_up_activated) {
                lifes_remaining = lifes_remaining - 1;
                update_ui_no_response();
            }
            option_text = get_random_option();
            speakWord(option_text);
            eng_selected_text = option_text;
            update_ui_correct_response();
            btn_tap_status = "nr";
            btn_tapped_in_cycle = false;
            end_game_status_text = "Too slow!";
        }

        private String get_random_option() {
            int rnd = new Random().nextInt(eng_selected_view_options.length);
            return eng_selected_view_options[rnd];
        }

        private void update_ui_correct_response() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!game_ended) {
                        set_eng_selected_view(option_text);
                        change_text_color(option_text);
                    }
                }
            });
        }

        private void update_ui_no_response() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    selected_mode_obj.getGame_mode().set_life_view(lifes_remaining);
                    if (lifes_remaining == 0) {
                        end_game(end_game_status_text);
                    } else {
                        update_eng_selected_view_thread.getWaitingHandler().postAtFrontOfQueue(new UserWaitingRunnable());
                    }
                }
            });
        }

        @Override
        public void run() {
            run_bg_process();
        }
    }

    private void next_option_work() {
        update_eng_selected_view_thread.getWaitingHandler().postAtFrontOfQueue(new UserOptionRunnable());
    }

    private void end_game(String msg) {
        set_eng_selected_view("game over");
        selected_mode_obj.getGame_mode().stopTimer();
        selected_mode_obj.getGame_mode().update_stats_in_prefs(score_recorder);
        selected_mode_obj.getGame_mode().displayEndGameDialog(msg, score_recorder);
        game_ended = true;
        update_eng_selected_view_thread.quitSafely();
    }

    private void setup_tts() {
        eng_selected_text_speech = new TextToSpeech(PlayScreen.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int lang_res = eng_selected_text_speech.setLanguage(Locale.US);
                    if (lang_res == TextToSpeech.LANG_MISSING_DATA || lang_res == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.i("TextToSpeech", "Language Not Supported");
                    }
                    eng_selected_text_speech.setSpeechRate((float) 1.5);
                    eng_selected_text_speech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            if (utteranceId.equals("mostRecentUtteranceID")) {
                                update_eng_selected_view_thread.getWaitingHandler().postAtFrontOfQueue(new UserWaitingRunnable());
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {
                        }
                    });
                }
            }
        });
    }

    public void speakWord(String string) {
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "mostRecentUtteranceID");
        eng_selected_text_speech.speak(string, TextToSpeech.QUEUE_FLUSH, params);
    }

    private class UserWaitingRunnable implements Runnable {

        @Override
        public void run() {
            SystemClock.sleep(selected_mode_obj.getGame_mode().get_delay_time(score_recorder));
            next_option_work();
        }
    }

    private class powerUpRunnable implements Runnable {
        @Override
        public void run() {
            while (progressStatus < 100) {
                SystemClock.sleep(200);
                progressStatus += 1;
                powerUpProgressBar.setProgress(progressStatus);
            }
            activatePowerUp();
            powerup_handler_thread.getWaitingHandler().postAtFrontOfQueue(new powerDownRunnable());
        }

        private void activatePowerUp(){
            String selectedPowerUp = (String) power_ups.next();
            if (selectedPowerUp.equals("slowdown")){
                selected_mode_obj.getGame_mode().slow_down_timer();
                powerUpImageView.setImageResource(R.drawable.slowdown_powerup_activated);
            }else if (selectedPowerUp.equals("multiplier")){
                multiplier_power_up *= 2;
                powerUpImageView.setImageResource(R.drawable.multiplier_powerup);
            }else {
                freeze_power_up_activated = true;
                powerUpImageView.setImageResource(R.drawable.freeze_powerup_activated);
            }
        }
    }

    private class powerDownRunnable implements Runnable {
        @Override
        public void run() {
            while (progressStatus != 0) {
                SystemClock.sleep(200);
                progressStatus -= 1;
                powerUpProgressBar.setProgress(progressStatus);
            }
            deactivatePowerUp();
            powerup_handler_thread.getWaitingHandler().postAtFrontOfQueue(new powerUpRunnable());
        }

        private void deactivatePowerUp() {
            freeze_power_up_activated = false;
            multiplier_power_up = 1;
            powerUpImageView.setImageResource(R.drawable.question_mark);
        }
    }

}