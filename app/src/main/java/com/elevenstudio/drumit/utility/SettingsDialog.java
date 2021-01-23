package com.elevenstudio.drumit.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.elevenstudio.drumit.R;

public class SettingsDialog extends AlertDialog {
    private GameSettings game_settings;
    private AlertDialog.Builder alert_builder;
    private View layout_view;
    private AlertDialog dialog;

    // Views
    private Switch show_timer_switch, music_switch, sound_switch;
    public Button close_btn;

    public SettingsDialog(Context context) {
        super(context);
        alert_builder = new AlertDialog.Builder(context);
        layout_view = getLayoutInflater().inflate(R.layout.settings_dialog, null);

        show_timer_switch = layout_view.findViewById(R.id.show_timer_switch);
        music_switch = layout_view.findViewById(R.id.music_switch);
        sound_switch = layout_view.findViewById(R.id.sound_switch);
        close_btn = layout_view.findViewById(R.id.close_btn);
        game_settings = new GameSettings(context);

        // switch listeners
        show_timer_switch.setChecked(game_settings.getShow_timer());
        show_timer_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //commit prefs on change
                game_settings.updateShowTimerSetting(isChecked);
            }
        });

        music_switch.setChecked(game_settings.getMusic());
        music_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //commit prefs on change
                game_settings.updateMusicSetting(isChecked);
            }
        });

        sound_switch.setChecked(game_settings.getSound());
        sound_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //commit prefs on change
                game_settings.updateSoundSetting(isChecked);
            }
        });
    }

    public void show_dialog() {
        if (layout_view.getParent() != null) {
            ((ViewGroup) layout_view.getParent()).removeView(layout_view);
        }
        alert_builder.setView(layout_view);
        dialog = alert_builder.create();
        dialog.show();
    }

    public void dismiss_dialog() {
        dialog.dismiss();
    }
}
