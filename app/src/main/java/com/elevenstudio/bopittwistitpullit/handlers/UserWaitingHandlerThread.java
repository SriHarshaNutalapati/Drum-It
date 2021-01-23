package com.elevenstudio.bopittwistitpullit.handlers;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import java.lang.ref.WeakReference;

public class UserWaitingHandlerThread extends HandlerThread {
    private Handler waitingHandler;
    private WeakReference<Context> playScreenContext;
    public UserWaitingHandlerThread(Context context) {
        super("UserWaitingHandlerThread", Process.THREAD_PRIORITY_BACKGROUND);
        this.playScreenContext = new WeakReference<>(context);
    }

    @Override
    protected void onLooperPrepared() {
        waitingHandler = new Handler();
    }

    public Handler getWaitingHandler(){
        return waitingHandler;
    }
}
