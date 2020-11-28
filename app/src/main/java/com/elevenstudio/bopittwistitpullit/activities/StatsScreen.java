package com.elevenstudio.bopittwistitpullit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.elevenstudio.bopittwistitpullit.R;
import com.elevenstudio.bopittwistitpullit.gamemodes.ClassicMode;
import com.elevenstudio.bopittwistitpullit.gamemodes.HiLoMode;
import com.elevenstudio.bopittwistitpullit.gamemodes.SurvivalMode;

import java.util.ArrayList;
import java.util.HashMap;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

public class StatsScreen extends AppCompatActivity {
    Button classic_mode, survival_mode, hi_lo_mode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_screen);
        classic_mode = findViewById(R.id.classic_select);
        survival_mode = findViewById(R.id.survival_select);
        hi_lo_mode = findViewById(R.id.hi_lo_select);
        setup_views();
//        setup_buttons();
        classic_mode_click(classic_mode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION   |
                SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public void classic_mode_click(View view){
        setup_views();
        add_classic_mode_stats();
        classic_mode.setBackgroundResource(R.drawable.stats_btn_on);
        findViewById(R.id.classic_mode_layout).setVisibility(View.VISIBLE);
    }

    public void survival_mode_click(View view){
        setup_views();
        add_survival_mode_stats();
        survival_mode.setBackgroundResource(R.drawable.stats_btn_on);
        findViewById(R.id.survival_mode_layout).setVisibility(View.VISIBLE);
    }

    public void hi_lo_mode_click(View view){
        setup_views();
        add_hi_lo_mode_stats();
        hi_lo_mode.setBackgroundResource(R.drawable.stats_btn_on);
        findViewById(R.id.hi_lo_mode_layout).setVisibility(View.VISIBLE);
    }

    private void setup_views() {
        findViewById(R.id.classic_mode_layout).setVisibility(View.GONE);
        findViewById(R.id.survival_mode_layout).setVisibility(View.GONE);
        findViewById(R.id.hi_lo_mode_layout).setVisibility(View.GONE);

        classic_mode.setBackgroundResource(R.drawable.stats_btn_off);
        survival_mode.setBackgroundResource(R.drawable.stats_btn_off);
        hi_lo_mode.setBackgroundResource(R.drawable.stats_btn_off);
    }

    private String convert_to_time_format(int millisecs){
        String second = Integer.toString((millisecs /1000)%60);
        String minute = Integer.toString((millisecs /(1000*60))%60);
        if(second.length() == 1) second = "0" + second;
        return String.format("0%s:%s", minute, second);
    }

    private void add_classic_mode_stats() {
        SharedPreferences shrd_prefs = ClassicMode.get_class_mode_prefs(this);
        TextView games_played_view = findViewById(R.id.classic_games_played_value);
        games_played_view.setText(String.valueOf(shrd_prefs.getInt(getResources().getString(R.string.classic_games_played), 0)));

        TextView high_score_view = findViewById(R.id.classic_high_score_value);
        high_score_view.setText(String.valueOf(shrd_prefs.getInt(getResources().getString(R.string.classic_high_score), 0)));

        TextView best_time_view = findViewById(R.id.classic_best_time_value);
        best_time_view.setText(convert_to_time_format(shrd_prefs.getInt(getResources().getString(R.string.classic_best_time), 0)));

        TextView avg_time_view = findViewById(R.id.classic_avg_time_value);
        avg_time_view.setText(convert_to_time_format(shrd_prefs.getInt(getResources().getString(R.string.classic_avg_time), 0)));

        TextView avg_score_view = findViewById(R.id.classic_avg_score_value);
        avg_score_view.setText(String.valueOf(shrd_prefs.getInt(getResources().getString(R.string.classic_avg_score), 0)));
    }

    private void add_survival_mode_stats(){
        SharedPreferences shrd_prefs = SurvivalMode.get_survival_mode_prefs(this);
        TextView games_played_view = findViewById(R.id.survival_games_played_value);
        games_played_view.setText(String.valueOf(shrd_prefs.getInt(getResources().getString(R.string.survival_games_played), 0)));

        TextView best_time_view = findViewById(R.id.survival_best_time_value);
        best_time_view.setText(convert_to_time_format(shrd_prefs.getInt(getResources().getString(R.string.survival_best_time), 0)));

        TextView avg_time_view = findViewById(R.id.survival_avg_time_value);
        avg_time_view.setText(convert_to_time_format(shrd_prefs.getInt(getResources().getString(R.string.survival_avg_time), 0)));
    }

    private void add_hi_lo_mode_stats(){
        SharedPreferences shrd_prefs = HiLoMode.get_hi_lo_mode_prefs(this);
        TextView games_played_view = findViewById(R.id.hi_lo_games_played_value);
        games_played_view.setText(String.valueOf(shrd_prefs.getInt(getResources().getString(R.string.hi_lo_games_played), 0)));

        TextView high_score_view = findViewById(R.id.hi_lo_high_score_value);
        high_score_view.setText(String.valueOf(shrd_prefs.getInt(getResources().getString(R.string.hi_lo_high_score), 0)));

        TextView best_time_view = findViewById(R.id.hi_lo_best_time_value);
        best_time_view.setText(convert_to_time_format(shrd_prefs.getInt(getResources().getString(R.string.hi_lo_best_time), 0)));

        TextView avg_time_view = findViewById(R.id.hi_lo_avg_time_value);
        avg_time_view.setText(convert_to_time_format(shrd_prefs.getInt(getResources().getString(R.string.hi_lo_avg_time), 0)));

        TextView avg_score_view = findViewById(R.id.hi_lo_avg_score_value);
        avg_score_view.setText(String.valueOf(shrd_prefs.getInt(getResources().getString(R.string.hi_lo_avg_score), 0)));
    }

    @Override
    public void onBackPressed() {
        Intent main_menu_screen = new Intent(StatsScreen.this, MainMenu.class);
        startActivity(main_menu_screen);
        this.finish();
    }
}