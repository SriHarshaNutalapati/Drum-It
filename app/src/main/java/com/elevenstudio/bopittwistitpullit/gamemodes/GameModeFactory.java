package com.elevenstudio.bopittwistitpullit.gamemodes;

import android.app.Activity;
import android.content.Context;
import android.widget.Chronometer;
import android.widget.TextView;

import com.elevenstudio.bopittwistitpullit.R;
import com.elevenstudio.bopittwistitpullit.utility.GameSettings;

public class GameModeFactory {
    public GameMode getGame_mode() {
        return game_mode;
    }

    private GameMode game_mode;

    public GameModeFactory(Activity context, String selected_mode, GameSettings game_settings) {
        if(selected_mode.equals(context.getResources().getString(R.string.classic_mode))){
            game_mode = new ClassicMode(context, game_settings);
        }else if(selected_mode.equals(context.getResources().getString(R.string.survival_mode))){
            game_mode = new SurvivalMode(context);
        }else if(selected_mode.equals(context.getResources().getString(R.string.hi_lo_mode))){
            game_mode = new HiLoMode(context, game_settings);
        }
    }


}
