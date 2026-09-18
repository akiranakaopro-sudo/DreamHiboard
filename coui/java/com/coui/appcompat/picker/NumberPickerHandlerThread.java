package com.coui.appcompat.picker;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

class NumberPickerHandlerThread extends Thread {
    static final int ONE_WAIT_TIME = 10;
    static final int OUT_WAIT_TIME = 1000;
    static final String TAG = "numberThread";

    Handler handler;
    Looper looper;
    int priority;
    int tid = -1;

    NumberPickerHandlerThread(String name) {
        super(name);
    }

    NumberPickerHandlerThread(String name, int priority) {
        super(name);
        this.priority = priority;
    }

    public Looper getLooper() {
        boolean interrupted = false;
        if (!isAlive()) {
            return null;
        }
        synchronized (this) {
            long start = System.currentTimeMillis();
            while (isAlive() && looper == null) {
                if (System.currentTimeMillis() - start > OUT_WAIT_TIME) {
                    Log.e(TAG, "numberPick Wait for looper timeout");
                    break;
                }
                try {
                    wait(ONE_WAIT_TIME);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return looper;
    }

    public Handler getThreadHandler() {
        if (handler == null && getLooper() != null) {
            handler = new Handler(getLooper());
        }
        return handler;
    }

    public int getThreadId() {
        return tid;
    }

    public void onLooperPrepared() {
    }

    public boolean quit() {
        Looper looper = getLooper();
        if (looper == null) {
            return false;
        }
        looper.quit();
        return true;
    }

    public boolean quitSafely() {
        Looper looper = getLooper();
        if (looper == null) {
            return false;
        }
        looper.quitSafely();
        return true;
    }

    @Override
    public void run() {
        tid = Process.myTid();
        Looper.prepare();
        synchronized (this) {
            looper = Looper.myLooper();
            notifyAll();
        }
        Process.setThreadPriority(priority);
        onLooperPrepared();
        Looper.loop();
        tid = -1;
    }
}
