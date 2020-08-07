package com.elevenstudio.bopittwistitpullit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import static android.content.Context.MODE_PRIVATE;

public class SettingsDialog extends AlertDialog {
    private AlertDialog.Builder alert_builder;
    private View layout_view;
    private AlertDialog dialog;

    // Views
    private Switch show_timer_switch, music_switch, sound_switch;
    protected Button close_btn;

    protected SettingsDialog(Context context, final SharedPreferences prefs) {
        super(context);
        alert_builder = new AlertDialog.Builder(context);
        layout_view = getLayoutInflater().inflate(R.layout.settings_dialog, null);

        show_timer_switch = layout_view.findViewById(R.id.show_timer_switch);
        music_switch = layout_view.findViewById(R.id.music_switch);
        sound_switch = layout_view.findViewById(R.id.sound_switch);
        close_btn = layout_view.findViewById(R.id.close_btn);

        // switch listeners
        show_timer_switch.setChecked(prefs.getBoolean("show_timer", true));
        show_timer_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //commit prefs on change
                prefs.edit().putBoolean("show_timer", isChecked).apply();
                prefs.edit().commit();
            }
        });

        music_switch.setChecked(prefs.getBoolean("music", true));
        music_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //commit prefs on change
                prefs.edit().putBoolean("music", isChecked).apply();
                prefs.edit().commit();
            }
        });

        sound_switch.setChecked(prefs.getBoolean("sound", true));
        sound_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //commit prefs on change
                prefs.edit().putBoolean("sound", isChecked).apply();
                prefs.edit().commit();
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
