package com.coui.appcompat.statusbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.view.COUICompatUtil;
import java.lang.ref.WeakReference;

public class COUIStatusBarResponseUtil {
    public static final int DELAY = 1000;
    private static final int MSG_REGISTER = 0;
    private static final int MSG_UNREGISTER = 1;
    private static final int RECEIVER_EXPORTED = 2;
    private static final int RECEIVER_NOT_EXPORTED = 4;
    private BroadcastDelayRunnable mBroadcastDelayRunnable;
    private Context mContext;
    private int mDelay = 0;
    private Handler mH;
    private boolean mIsRegistered;
    private StatusBarClickListener mStatusBarClickListener;
    private BroadcastReceiver myReceiver;
    private final String TAG = "COUIStatusBarResponseUtil";

    public class BroadcastDelayRunnable implements Runnable {
        private WeakReference<Context> mContextRef;
        private int mMsg;

        public BroadcastDelayRunnable(Context context) {
            this.mContextRef = new WeakReference<>(context);
        }

        @Override
        public void run() {
            Context context = this.mContextRef.get();
            if (context == null) {
                COUILog.e(TAG, "lost mContextRef , failed to execute mBroadcastDelayRunnable");
            } else if (this.mMsg == MSG_REGISTER) {
                COUIStatusBarResponseUtil.this.initReceiver(context);
            } else {
                COUIStatusBarResponseUtil.this.unregisterRegister(context);
                this.mContextRef.clear();
            }
        }

        public void setMsg(int msg) {
            this.mMsg = msg;
        }
    }

    public interface StatusBarClickListener {
        void onStatusBarClicked();
    }

    public COUIStatusBarResponseUtil(Context context) {
        this.mContext = context;
    }

    private void initReceiver(Context context) {
        if (this.mIsRegistered) {
            return;
        }
        this.myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                COUILog.i(TAG, "The broadcast receiver was registered successfully and receives the broadcast");
                if (COUIStatusBarResponseUtil.this.mStatusBarClickListener != null) {
                    COUIStatusBarResponseUtil.this.mStatusBarClickListener.onStatusBarClicked();
                    COUILog.i(TAG, "onStatusBarClicked is called at time :" + System.currentTimeMillis());
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.oplus.clicktop");
        intentFilter.addAction(COUICompatUtil.getInstance().getClickTopName());
        this.mIsRegistered = true;
        if (Build.VERSION.SDK_INT > 31) {
            context.registerReceiver(this.myReceiver, intentFilter, RECEIVER_EXPORTED);
        } else {
            context.registerReceiver(this.myReceiver, intentFilter);
        }
    }

    private void unregisterRegister(Context context) {
        if (this.mIsRegistered) {
            this.mIsRegistered = false;
            context.unregisterReceiver(this.myReceiver);
        }
    }

    public void onPause() {
        Handler handler = this.mH;
        if (handler != null) {
            handler.removeCallbacks(this.mBroadcastDelayRunnable);
            this.mBroadcastDelayRunnable.setMsg(MSG_UNREGISTER);
            this.mH.postDelayed(this.mBroadcastDelayRunnable, this.mDelay);
            this.mH = null;
            this.mBroadcastDelayRunnable = null;
        }
    }

    public void onResume() {
        if (this.mH != null || this.mBroadcastDelayRunnable != null) {
            COUILog.e(TAG, "onResume call multiple times");
            return;
        }
        this.mH = new Handler(Looper.myLooper());
        BroadcastDelayRunnable broadcastDelayRunnable = new BroadcastDelayRunnable(this.mContext);
        this.mBroadcastDelayRunnable = broadcastDelayRunnable;
        broadcastDelayRunnable.setMsg(MSG_REGISTER);
        this.mH.postDelayed(this.mBroadcastDelayRunnable, this.mDelay);
    }

    public void setDelay(int delay) {
        this.mDelay = delay;
    }

    public void setStatusBarClickListener(StatusBarClickListener statusBarClickListener) {
        this.mStatusBarClickListener = statusBarClickListener;
    }
}
