package com.elevenstudio.drumit.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.elevenstudio.drumit.R;

public class EndGameDialog extends AlertDialog {
    private AlertDialog.Builder alert_builder;
    private View layout_view;
    private AlertDialog dialog;

    // Views
    public TextView main_head_view, score_View;
    public Button main_menu_btn, play_again_btn;

    public EndGameDialog(Context context) {
        super(context);
        alert_builder = new AlertDialog.Builder(context);
        layout_view = getLayoutInflater().inflate(R.layout.end_game_dialog, null);

        main_head_view = layout_view.findViewById(R.id.end_game_main_head);
        score_View = layout_view.findViewById(R.id.end_game_score);
        main_menu_btn = layout_view.findViewById(R.id.end_game_main_menu);
        play_again_btn = layout_view.findViewById(R.id.end_game_play_again);


    }

    public void show_dialog(String message) {
        if (layout_view.getParent() != null) {
            ((ViewGroup) layout_view.getParent()).removeView(layout_view);
        }
        alert_builder.setView(layout_view);
        dialog = alert_builder.create();
        main_head_view.setText(message);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void dismiss_dialog() {
        dialog.dismiss();
    }
}
