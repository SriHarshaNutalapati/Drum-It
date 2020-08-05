package com.elevenstudio.bopittwistitpullit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class opening_screen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT_COMPANY = 2950;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent main_menu = new Intent(opening_screen.this, main_menu.class);
                startActivity(main_menu);
                finish();
            }
        }, SPLASH_TIME_OUT_COMPANY);
    }
}