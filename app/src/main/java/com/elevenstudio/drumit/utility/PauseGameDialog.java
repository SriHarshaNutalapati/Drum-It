package com.elevenstudio.drumit.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.elevenstudio.drumit.R;

public class PauseGameDialog extends AlertDialog {
    private AlertDialog.Builder alert_builder;
    private View layout_view;
    private AlertDialog dialog;

    // Views
    public Button resume_btn;
    public Button exit_btn;

    public PauseGameDialog(Context context) {
        super(context);
        alert_builder = new AlertDialog.Builder(context);
        layout_view = getLayoutInflater().inflate(R.layout.pause_game_dialog, null);
        resume_btn = layout_view.findViewById(R.id.resume_btn);
        exit_btn = layout_view.findViewById(R.id.exit_btn);
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
