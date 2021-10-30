package com.elevenstudio.drumit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
import com.elevenstudio.drumit.BuildConfig;
import com.elevenstudio.drumit.R;
import com.elevenstudio.drumit.gamemodes.GameModeFactory;
import com.elevenstudio.drumit.handlers.UserWaitingHandlerThread;
import com.elevenstudio.drumit.utility.GameSettings;
import com.elevenstudio.drumit.utility.PauseGameDialog;
import com.elevenstudio.drumit.utility.ProbabilityList;
import com.elevenstudio.drumit.utility.soundManager;

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

    private int mBeatsRecorder = 0;
    private Boolean mGamePaused = false;
    private Boolean mDrumTappedInCycle = true;
    private Boolean mGameStarted = false;
    private Boolean mGameEnded = false;
    private Boolean mPauseDialogOpen = false;
    private final String[] mDrumKitList = {"kick it", "snare it", "crash it", "ride it", "hat it", "tom it"};
    private String mSelectedDrum = "";
    private Boolean mTutorialOn = false;
    private int mTutorialSequence = 1;
    private int mNextDrumTimer = 1000;

    private UserWaitingHandlerThread mDrumSelectorHandler, mPowerUpHandler, mFansProgressHandler;

    // Views
    private TextView mScoreView;
    private TextView mSelectedDrumView;

    // Animation
    Animation mAnimSlideUp, mAnimButtonShake;

    // sounds
    private soundManager mSoundManager;

    // TTS
    private TextToSpeech mSelectedDrumTTS;

    // game settings
    private GameSettings mGameSettings;
    private Boolean mSoundSetting;

    // mode selected
    private GameModeFactory mSelectedModeObj;

    // colors for animated text
    int kick_it_start, kick_it_end, snare_it_start, snare_it_end, crash_it_start, crash_it_end;

    public PlayScreen() {
        mDrumSelectorHandler = null;
        mPowerUpHandler = null;
        mFansProgressHandler = null;
    }

    // game progress bar
    private ProgressBar mFansPercentBar;
    private int mFansPercentage = 100;


    // power_ups
    private ProgressBar mPowerUpProgressBar;
    private ImageView mPowerUpImageView;
    private int mProgressStatus = 0;
    private int mMultiplierPowerUp = 1;
    private Boolean freeze_power_up_activated = false;
    private final ProbabilityList<Object> power_ups = new ProbabilityList<>().add(45, "multiplier").add(10, "powerUpFansPercent").add(45, "slowdown");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classic_play_screen);

        // create user data object
        mGameSettings = new GameSettings(PlayScreen.this);
//        open_start_game_dialog();
        mSoundSetting = mGameSettings.getSound();

        // Animation for the text
        mAnimSlideUp = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);

        mAnimButtonShake = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.shake_anim);

        mSelectedDrumView = findViewById(R.id.eng_selected_view); // Engine selected option

        kick_it_start = getResources().getColor(R.color.bass_gradient_start_color);
        kick_it_end = getResources().getColor(R.color.bass_gradient_end_color);
        snare_it_start = getResources().getColor(R.color.snare_gradient_start_color);
        snare_it_end = getResources().getColor(R.color.snare_gradient_end_color);
        crash_it_start = getResources().getColor(R.color.cymbal_gradient_start_color);
        crash_it_end = getResources().getColor(R.color.cymbal_gradient_end_color);

        checkFirstRun();
    }

    private void show_tutorial(String eng_selected_view_option_tutorial, int btn_id_tutorial) {
        set_eng_selected_view(eng_selected_view_option_tutorial);
        speakWord(eng_selected_view_option_tutorial);
        change_text_color(eng_selected_view_option_tutorial);
        Button btn_to_be_animated = findViewById(btn_id_tutorial);
        btn_to_be_animated.setAnimation(mAnimButtonShake);
    }

    private void setup_selected_mode() {
        String selectedMode = mGameSettings.getSelected_mode();
        // setting views
        mPowerUpProgressBar = findViewById(R.id.power_up_progress_bar);
        mPowerUpImageView = findViewById(R.id.powerUpImage);
        mFansPercentBar = findViewById(R.id.healthBar);
        mScoreView = findViewById(R.id.score_view);
        // Create Game Mode Object
        mSelectedModeObj = new GameModeFactory(PlayScreen.this, selectedMode, mGameSettings);
    }

    public void onPause() {
        super.onPause();
        if (!mPauseDialogOpen && !mGameEnded && mGameStarted) pause_game();
    }

    public void onResume() {
        super.onResume();
//        if (!mPauseDialogOpen) resume_game();
        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void onStop() {
        super.onStop();
    }

    private void resume_game() {
        if (mGamePaused && mBeatsRecorder != 0) mSelectedModeObj.getGame_mode().resumeTimer();
        mGamePaused = false;
    }

    private void pause_game() {
        mGamePaused = true;
        mDrumSelectorHandler.interrupt();
        mSelectedModeObj.getGame_mode().stopTimer();
        final PauseGameDialog pause_popup = new PauseGameDialog(PlayScreen.this);
        pause_popup.show_dialog();
        mPauseDialogOpen = true;
        pause_popup.resume_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resume_game();
                pause_popup.dismiss_dialog();
                mPauseDialogOpen = false;
            }
        });
        pause_popup.exit_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pause_popup.dismiss_dialog();
                end_play_screen();
                mPauseDialogOpen = false;
            }
        });
    }

    private void start_game() {
        mSelectedModeObj.getGame_mode().reset_timer();
        mSelectedModeObj.getGame_mode().setup_views_before_start();
        mSelectedModeObj.getGame_mode().startTimer();
        mGameStarted = true;
//        SystemClock.sleep(2000);
        mDrumSelectorHandler.getWaitingHandler().postDelayed(new UserOptionRunnable(), 2000);
//        mPowerUpHandler.getWaitingHandler().postDelayed(new powerUpRunnable(), 2000);
        mFansProgressHandler.getWaitingHandler().postDelayed(new gameProgressBarRunnable(), 2000);
    }

    private void change_text_color(String text) {
        switch (text) {
            case "kick it":
                Tvg.change(mSelectedDrumView, kick_it_start, kick_it_end);
                break;
            case "snare it":
                Tvg.change(mSelectedDrumView, snare_it_start, snare_it_end);
                break;
            case "crash it":
                Tvg.change(mSelectedDrumView, crash_it_start, crash_it_end);
                break;
        }
    }

    public void kick_it_tapped(View view) {
        if (mTutorialOn) {
            if (mTutorialSequence == 1) {
                view.clearAnimation();
                mTutorialSequence = 2;
                show_tutorial(mDrumKitList[1], R.id.snare_it);
            }
            return;
        }
        if (mSelectedDrum.equals("kick it")) {
            right_drum_clicked(0);
        } else if (!mDrumTappedInCycle) {
            wrong_drum_clicked();
        }
    }

    public void snare_it_tapped(View view) {
        if (mTutorialOn) {
            if (mTutorialSequence == 2) {
                view.clearAnimation();
                mTutorialSequence = 3;
                show_tutorial(mDrumKitList[2], R.id.crash_it);
            }
            return;
        }
        if (mSelectedDrum.equals("snare it")) {
            right_drum_clicked(1);
        } else if (!mDrumTappedInCycle) {
            wrong_drum_clicked();
        }
    }

    public void crash_it_tapped(View view) {
        if (mTutorialOn) {
            if (mTutorialSequence == 3) {
                view.clearAnimation();
                mTutorialSequence = 4;
                show_tutorial(mDrumKitList[3], R.id.ride_it);
            }
            return;
        }
        if (mSelectedDrum.equals("crash it")) {
            right_drum_clicked(2);
        } else if (!mDrumTappedInCycle) {
            wrong_drum_clicked();
        }
    }

    public void ride_it_tapped(View view) {
        if (mTutorialOn) {
            if (mTutorialSequence == 4) {
                view.clearAnimation();
                mTutorialSequence = 5;
                show_tutorial(mDrumKitList[4], R.id.hat_it);
            }
            return;
        }
        if (mSelectedDrum.equals("ride it")) {
            right_drum_clicked(3);
        } else if (!mDrumTappedInCycle) {
            wrong_drum_clicked();
        }
    }

    public void hat_it_tapped(View view) {
        if (mTutorialOn) {
            if (mTutorialSequence == 5) {
                view.clearAnimation();
                mTutorialSequence = 6;
                show_tutorial(mDrumKitList[5], R.id.tom_it);
            }
            return;
        }
        if (mSelectedDrum.equals("hat it")) {
            right_drum_clicked(4);
        } else if (!mDrumTappedInCycle) {
            wrong_drum_clicked();
        }
    }

    public void tom_it_tapped(View view) {
        if (mTutorialOn) {
            if (mTutorialSequence == 6) {
                view.clearAnimation();
                mTutorialSequence = 7;
                mTutorialOn = false;
//                setup_game();
            }
            return;
        }
        if (mSelectedDrum.equals("tom it")) {
            right_drum_clicked(5);
        } else if (!mDrumTappedInCycle) {
            wrong_drum_clicked();
        }
    }

    private void wrong_drum_clicked() {
        if(mFansPercentage >= 0) mFansPercentage = mFansPercentage - 10;
        mDrumTappedInCycle = true;
    }

    private void right_drum_clicked(int index) {
        mSoundManager.playSound(index, mSoundSetting);
        if(mFansPercentage <= 100) mFansPercentage = mFansPercentage + 10; //  && !freeze_power_up_activated
        setBeats();
        mDrumTappedInCycle = true;
    }

    public void pause_tapped(View view) {
        pause_game();
    }

    private void setBeats() {
        mBeatsRecorder = mBeatsRecorder + mMultiplierPowerUp;
        mScoreView.setText(String.format(Locale.US, "%05d", mBeatsRecorder));
    }

    private void setup_sounds() {
        mSoundManager = new soundManager();
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
        end_game("");
        Intent main_menu_screen = new Intent(PlayScreen.this, MainMenu.class);
        startActivity(main_menu_screen);
        this.finish();
    }

    private void set_eng_selected_view(String text) {
        mSelectedDrumView.setText(text);
        mSelectedDrumView.startAnimation(mAnimSlideUp);
    }

    private class UserOptionRunnable implements Runnable {

        private String option_text;

        private void run_bg_process() {
            Log.i("Gameprogress", "entered run_bg_process");
            if(!mDrumTappedInCycle) mFansPercentage = mFansPercentage - 10; // && !freeze_power_up_activated
            option_text = get_random_option();
            speakWord(option_text);
            mSelectedDrum = option_text;
            update_ui_correct_response();
            mDrumTappedInCycle = false;
        }

        private String get_random_option() {
            int rnd = new Random().nextInt(mDrumKitList.length);
            return mDrumKitList[rnd];
        }

        private void update_ui_correct_response() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!mGameEnded) {
                        set_eng_selected_view(option_text);
                        change_text_color(option_text);
                    }
                }
            });
        }

        @Override
        public void run() {
            run_bg_process();
        }

    }

    private void end_game(String msg) {
        set_eng_selected_view("game over");
        mDrumSelectorHandler.quit();
        mPowerUpHandler.quit();
        mFansProgressHandler.quit();
        mSelectedModeObj.getGame_mode().stopTimer();
        mSelectedModeObj.getGame_mode().update_stats_in_prefs(mBeatsRecorder);
        mSelectedModeObj.getGame_mode().displayEndGameDialog(msg, mBeatsRecorder);
        mGameEnded = true;
    }

    private void setup_tts() {
        mSelectedDrumTTS = new TextToSpeech(PlayScreen.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int lang_res = mSelectedDrumTTS.setLanguage(Locale.US);
                    if (lang_res == TextToSpeech.LANG_MISSING_DATA || lang_res == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.i("TextToSpeech", "Language Not Supported");
                    }
                    mSelectedDrumTTS.setSpeechRate((float) 1.5);
                    mSelectedDrumTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            if (utteranceId.equals("mostRecentUtteranceID")) {
                                Log.i("Gameprogress", "entered onDone");
                                mDrumSelectorHandler.getWaitingHandler().postAtFrontOfQueue(new UserWaitingRunnable());
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
        mSelectedDrumTTS.speak(string, TextToSpeech.QUEUE_FLUSH, params);
    }

    private class UserWaitingRunnable implements Runnable {

        @Override
        public void run() {
//            SystemClock.sleep(mSelectedModeObj.getGame_mode().get_delay_time(mBeatsRecorder));
//            next_option_work();
            mDrumSelectorHandler.getWaitingHandler().postDelayed(new UserOptionRunnable(), getTimeIntervalGap());
        }
//        private void next_option_work() {
//            mDrumSelectorHandler.getWaitingHandler().postAtFrontOfQueue(new UserOptionRunnable());
//        }

        private int getTimeIntervalGap(){
            if(mNextDrumTimer > 300) mNextDrumTimer = mNextDrumTimer - 50;
            return mNextDrumTimer;
        }
    }

    private class gameProgressBarRunnable implements Runnable {
        @Override
        public void run() {
            while (mFansPercentage > 0) {
                SystemClock.sleep(200);
                 mFansPercentage = mFansPercentage - 1; // if(!freeze_power_up_activated)
                mFansPercentBar.setProgress(mFansPercentage);
            }
//            while (mFansPercentage > 0) {
//                SystemClock.sleep(500);
//                if (mFansPercentage < 100) mFansPercentage = mFansPercentage + 1;
//                mFansPercentBar.setProgress(mFansPercentage);
//            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    end_game("progress over");
                }
            });
        }
    }

    private void setup_game(){

        mDrumSelectorHandler = new UserWaitingHandlerThread(PlayScreen.this);
        mDrumSelectorHandler.start();
        mPowerUpHandler = new UserWaitingHandlerThread(PlayScreen.this);
        mPowerUpHandler.start();
        mFansProgressHandler = new UserWaitingHandlerThread(PlayScreen.this);
        mFansProgressHandler.start();

        start_game();
    }

    private void checkFirstRun() {
        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        int savedVersionCode = -1; // mGameSettings.getCurrentVersion();

        setup_sounds(); setup_tts(); setup_selected_mode();

        // Check for first run or upgrade
        if (savedVersionCode == -1) {
            // This is a new install (or the user cleared the shared preferences)
            Toast.makeText(PlayScreen.this, "This is first time", Toast.LENGTH_LONG).show();  // Remove This
            mTutorialOn = true;
            show_tutorial(mDrumKitList[0], R.id.kick_it);
        } else if (currentVersionCode >= savedVersionCode) {
//             This is just a normal run
//             No effect even if there is an upgrade as of now
            setup_sounds(); setup_tts(); setup_selected_mode();
            setup_game();
            return;
        }
        /*
            else if (currentVersionCode > savedVersionCode) {
                //  Runs when user upgrades app.
                //  Not required as of now
            }

        */

        // Update the shared preferences with the current version code
        mGameSettings.updateVersionCode(currentVersionCode);
    }
}

//    private void open_start_game_dialog() {
//        final GameStartDialog start_game_popup = new GameStartDialog(PlayScreen.this, mGameSettings);
//        start_game_popup.show_dialog();
//        start_game_popup.start_play_btn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                start_game_popup.dismiss_dialog();
//                setup_selected_mode();
//                if (mGameSettings.getShowTutorial()) {
//                    mTutorialOn = true;
//                    show_tutorial(mDrumKitList[0], R.id.kick_it);
//                } else {
//                    start_game();
//                }
//            }
//        });
//    }


//    private class powerUpRunnable implements Runnable {
//        @Override
//        public void run() {
//            while (mProgressStatus < 100) {
//                SystemClock.sleep(400);
//                mProgressStatus += 1;
//                mPowerUpProgressBar.setProgress(mProgressStatus);
//            }
//            activatePowerUp();
//            mPowerUpHandler.getWaitingHandler().postAtFrontOfQueue(new powerDownRunnable());
//        }
//
//        private void activatePowerUp(){
//            String selectedPowerUp = (String) power_ups.next();
//            if (selectedPowerUp.equals("slowdown")){
//                mNextDrumTimer = mNextDrumTimer + 300;
////                mPowerUpImageView.setImageResource(R.drawable.slowdown_powerup_activated);
//            }else if (selectedPowerUp.equals("multiplier")){
//                mMultiplierPowerUp *= 2;
////                mPowerUpImageView.setImageResource(R.drawable.multiplier_powerup);
//            }else {
//                mFansPercentage = mFansPercentage + 30;
//                if(mFansPercentage > 100) mFansPercentage = 100;
////                mPowerUpImageView.setImageResource(R.drawable.freeze_powerup_activated);
//            }
//        }
//    }

//    private class powerDownRunnable implements Runnable {
//        @Override
//        public void run() {
//            while (mProgressStatus != 0) {
//                SystemClock.sleep(200);
//                mProgressStatus -= 1;
//                mPowerUpProgressBar.setProgress(mProgressStatus);
//            }
//            deactivatePowerUp();
//            mPowerUpHandler.getWaitingHandler().postAtFrontOfQueue(new powerUpRunnable());
//        }
//
//        private void deactivatePowerUp() {
//            freeze_power_up_activated = false;
//            mMultiplierPowerUp = 1;
////            mPowerUpImageView.setImageResource(R.drawable.question_mark);
//        }
//    }
