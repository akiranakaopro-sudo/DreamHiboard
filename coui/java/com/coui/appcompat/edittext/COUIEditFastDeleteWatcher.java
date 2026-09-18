package com.coui.appcompat.edittext;

import android.os.SystemClock;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

public class COUIEditFastDeleteWatcher implements TextWatcher {
    private static final int MIN_DELETE_TIME = 100;
    private static final String TAG = "COUIEditFastDeleteWatcher";

    private Editable mBeforeText;
    private final EditText mEdittext;
    private boolean mEnable = true;
    private long mFirstDeleteTime;
    private boolean mIsDeleting;
    private long mLastDeleteTime;

    public COUIEditFastDeleteWatcher(COUIEditText editText) {
        mEdittext = editText;
        editText.addTextChangedListener(this);
    }

    private void log(String message) {
        Log.d(TAG, message);
    }

    private void resetAllState() {
        mFirstDeleteTime = SystemClock.elapsedRealtime();
        mIsDeleting = true;
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (!mEnable) {
            return;
        }
        if (editable.length() >= mBeforeText.length()) {
            mIsDeleting = false;
            return;
        }
        long now = SystemClock.elapsedRealtime();
        if (now - mLastDeleteTime > 1000 && mLastDeleteTime > 0) {
            mIsDeleting = false;
            mLastDeleteTime = 0L;
        }
        if (!mIsDeleting) {
            resetAllState();
        }
        if (now - mFirstDeleteTime < 4000) {
            mLastDeleteTime = SystemClock.elapsedRealtime();
            return;
        }
        if (now - mLastDeleteTime < MIN_DELETE_TIME) {
            int beforeLength = mBeforeText.length();
            int deletedLength = beforeLength - editable.length();
            mEnable = false;
            editable.append(mBeforeText.subSequence(beforeLength - deletedLength, beforeLength));
            mEnable = true;
            return;
        }
        int length = editable.length();
        int deleteCount = Math.min(4, length);
        mEnable = false;
        editable.delete(length - deleteCount, length);
        mLastDeleteTime = SystemClock.elapsedRealtime();
        mEnable = true;
        log("afterTextChanged done");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (mEnable) {
            mBeforeText = new SpannableStringBuilder(s.toString());
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
