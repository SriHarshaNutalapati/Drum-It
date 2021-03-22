package com.elevenstudio.drumit.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.elevenstudio.drumit.R;

import java.util.HashMap;

public class GameStartDialog extends AlertDialog {

    public Button start_play_btn;
    private AlertDialog.Builder alert_builder;
    private View layout_view;
    private AlertDialog dialog;
    private TextView second_rule_view;
    private HashMap<String, Integer> mode_view_map = new HashMap<>();
    private HashMap<String, String> mode_view_name_map = new HashMap<>();
    private String selected_mode;

    // Views
    private RadioButton classic_radio, survival_radio, hi_lo_radio;
    private RadioGroup mode_radio_group;
    private CheckBox show_tutorial_cb;

    // Game settings
    private GameSettings game_settings;

    public GameStartDialog(final Context context, GameSettings game_settings_obj) {
        super(context);
        alert_builder = new AlertDialog.Builder(context);
        layout_view = getLayoutInflater().inflate(R.layout.start_game_layout, null);
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
        mode_radio_group = layout_view.findViewById(R.id.game_mode_radio_group);

        start_play_btn = layout_view.findViewById(R.id.start_play_btn);
        second_rule_view = layout_view.findViewById(R.id.second_rule_view);
        show_tutorial_cb = layout_view.findViewById(R.id.tutorial_checkbox);

        mode_radio_group.check(mode_view_map.get(game_settings.getSelected_mode()));
        selected_mode = game_settings.getSelected_mode();
        mode_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton mode_selected= layout_view.findViewById(checkedId);
                game_settings.updateSelectedMode(mode_view_name_map.get(mode_selected.getText()));
                set_selected_mode_text(context, mode_view_name_map.get(mode_selected.getText()));
            }
        });
        set_selected_mode_text(context, selected_mode);

        show_tutorial_cb.setChecked(game_settings.getShowTutorial());
        show_tutorial_cb.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                game_settings.updateShowTutorialSetting(show_tutorial_cb.isChecked());
            }
        });

    }

    private void set_selected_mode_text(Context context, String selected_mode){
        if(selected_mode.equals(context.getResources().getString(R.string.classic_mode))){
            second_rule_view.setText(context.getResources().getString(R.string.classic_mode_second_rule_text));
        }else if(selected_mode.equals(context.getResources().getString(R.string.survival_mode))){
            second_rule_view.setText(context.getResources().getString(R.string.survival_mode_second_rule_text));
        }
    }

    public void show_dialog() {
        if (layout_view.getParent() != null) {
            ((ViewGroup) layout_view.getParent()).removeView(layout_view);
        }
        alert_builder.setView(layout_view);
        dialog = alert_builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void dismiss_dialog() {
        dialog.dismiss();
    }
}
