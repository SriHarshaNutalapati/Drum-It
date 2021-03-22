package com.elevenstudio.drumit.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.elevenstudio.drumit.R;

import java.util.HashMap;

public class ModeSelectorDialog extends AlertDialog {
    private AlertDialog.Builder alert_builder;
    private View layout_view;
    private AlertDialog dialog;
    HashMap<String, Integer> mode_view_map = new HashMap<>();
    HashMap<String, String> mode_view_name_map = new HashMap<>();

    // Views
    private RadioButton classic_radio, survival_radio, hi_lo_radio;
    private RadioGroup mode_radio_group;
    public Button close_btn;

    // Game settings
    private GameSettings game_settings;

    public ModeSelectorDialog(Context context, GameSettings game_settings_obj) {
        super(context);
        alert_builder = new AlertDialog.Builder(context);
        layout_view = getLayoutInflater().inflate(R.layout.mode_selection_dialog, null);

        mode_view_map.put(context.getResources().getString(R.string.classic_mode), R.id.classic_mode_radio);
        mode_view_map.put(context.getResources().getString(R.string.survival_mode), R.id.survival_mode_radio);
        mode_view_map.put(context.getResources().getString(R.string.hi_lo_mode), R.id.hi_lo_radio);

        mode_view_name_map.put(context.getResources().getString(R.string.classic_mode_ui), context.getResources().getString(R.string.classic_mode));
        mode_view_name_map.put(context.getResources().getString(R.string.survival_mode_ui), context.getResources().getString(R.string.survival_mode));
        mode_view_name_map.put(context.getResources().getString(R.string.hi_lo_mode_ui), context.getResources().getString(R.string.hi_lo_mode));

        game_settings = game_settings_obj;

        classic_radio = layout_view.findViewById(R.id.classic_mode_radio);
        survival_radio = layout_view.findViewById(R.id.survival_mode_radio);
        hi_lo_radio = layout_view.findViewById(R.id.hi_lo_radio);
        close_btn = layout_view.findViewById(R.id.mode_dialog_close_btn);
        mode_radio_group = layout_view.findViewById(R.id.game_mode_radio_group);

        mode_radio_group.check(mode_view_map.get(game_settings.getSelected_mode()));
        mode_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton mode_selected= layout_view.findViewById(checkedId);
                game_settings.updateSelectedMode(mode_view_name_map.get((String) mode_selected.getText()));
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
