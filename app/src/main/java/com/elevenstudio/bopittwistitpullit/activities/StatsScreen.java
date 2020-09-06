package com.elevenstudio.bopittwistitpullit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.elevenstudio.bopittwistitpullit.R;
import com.elevenstudio.bopittwistitpullit.gamemodes.ClassicMode;
import com.elevenstudio.bopittwistitpullit.gamemodes.HiLoMode;
import com.elevenstudio.bopittwistitpullit.gamemodes.SurvivalMode;

import java.util.ArrayList;
import java.util.HashMap;

public class StatsScreen extends AppCompatActivity {
    private Spinner mode_spinner;
    private ArrayList<String> gameModes = new ArrayList<>();
    private String selected_mode;

    HashMap<String, View> mode_layout_map = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_screen);
        setup_variables();
        setup_views();
        setup_spinner();
    }

    private void setup_spinner() {
        //Getting the instance of Spinner and applying OnItemSelectedListener on it
        mode_spinner = findViewById(R.id.mode_select_spinner);
        mode_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selected_mode = gameModes.get(position);
                mode_spinner.setSelection(position);
                setup_views();
                mode_layout_map.get(selected_mode).setVisibility(View.VISIBLE);
                if(selected_mode.equals(getResources().getString(R.string.classic_mode_ui))) add_classic_mode_stats();
                if(selected_mode.equals(getResources().getString(R.string.survival_mode_ui))) add_survival_mode_stats();
                if(selected_mode.equals(getResources().getString(R.string.hi_lo_mode_ui))) add_hi_lo_mode_stats();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //Creating the ArrayAdapter instance having the bank name list
        ArrayAdapter mode_selector = new ArrayAdapter(this,android.R.layout.simple_spinner_item,gameModes);
        mode_selector.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        mode_spinner.setAdapter(mode_selector);
        // Preselect classic mode
        mode_spinner.setSelection(0);
    }

    private void setup_views() {
        findViewById(R.id.classic_mode_layout).setVisibility(View.GONE);
        findViewById(R.id.survival_mode_layout).setVisibility(View.GONE);
        findViewById(R.id.hi_lo_mode_layout).setVisibility(View.GONE);
    }

    private void setup_variables() {
        selected_mode = getResources().getString(R.string.classic_mode_ui);
        gameModes.add(getResources().getString(R.string.classic_mode_ui));
        gameModes.add(getResources().getString(R.string.survival_mode_ui));
        gameModes.add(getResources().getString(R.string.hi_lo_mode_ui));

        mode_layout_map.put(getResources().getString(R.string.classic_mode_ui), findViewById(R.id.classic_mode_layout));
        mode_layout_map.put(getResources().getString(R.string.survival_mode_ui), findViewById(R.id.survival_mode_layout));
        mode_layout_map.put(getResources().getString(R.string.hi_lo_mode_ui), findViewById(R.id.hi_lo_mode_layout));
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
}