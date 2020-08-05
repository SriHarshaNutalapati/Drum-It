package com.elevenstudio.bopittwistitpullit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class stats_screen extends AppCompatActivity {

    private TextView high_score_view, best_time_view, avg_score_view, avg_time_view, games_played_view;

    // for preferences
    final String PREFS_NAME = "com.elevenstudios.btp";
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_screen);

        high_score_view = findViewById(R.id.high_score_view);
        best_time_view = findViewById(R.id.best_time_view);
        avg_score_view = findViewById(R.id.avg_score_view);
        avg_time_view = findViewById(R.id.avg_time_view);
        games_played_view = findViewById(R.id.games_played_view);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        high_score_view.setText(String.valueOf(prefs.getInt("high_score", 0)));
        avg_score_view.setText(String.valueOf(prefs.getInt("score_avg", 0)));
        best_time_view.setText(String.valueOf(prefs.getInt("best_time", 0)));
        avg_time_view.setText(String.valueOf(prefs.getInt("time_avg", 0)));
        games_played_view.setText(String.valueOf(prefs.getInt("games_played", 0)));
    }
}