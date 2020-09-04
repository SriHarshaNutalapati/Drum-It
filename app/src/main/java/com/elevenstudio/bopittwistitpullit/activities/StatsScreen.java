package com.elevenstudio.bopittwistitpullit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.elevenstudio.bopittwistitpullit.R;

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
//        setup_spinner();
        setup_variables();
        setup_views();
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
//        mode_spinner.setSelection(0);
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
}