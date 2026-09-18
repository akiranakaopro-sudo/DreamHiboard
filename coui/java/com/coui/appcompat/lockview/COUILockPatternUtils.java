package com.coui.appcompat.lockview;

import android.content.Context;
import android.util.Log;
import com.coui.appcompat.lockview.COUILockPatternView;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class COUILockPatternUtils {
    private static final int COLUMNS = 3;
    private static final boolean DEBUG = false;
    private static final int ROWS = 3;
    private static final String TAG = "COUILockPatternUtils";
    private static final String UTF_8 = "UTF-8";
    private final Context mContext;

    public COUILockPatternUtils(Context context) {
        this.mContext = context;
    }

    public static String patternToString(List<COUILockPatternView.Cell> list) {
        if (list == null) {
            return "";
        }
        int size = list.size();
        byte[] bArr = new byte[size];
        for (int index = 0; index < size; index++) {
            COUILockPatternView.Cell cell = list.get(index);
            bArr[index] = (byte) ((cell.getRow() * 3) + cell.getColumn() + 49);
        }
        try {
            return new String(bArr, UTF_8);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "patternToString e:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static List<COUILockPatternView.Cell> stringToPattern(String str) {
        byte[] bytes = null;
        if (str == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        try {
            bytes = str.getBytes(UTF_8);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "stringToPattern e:" + e.getMessage());
            e.printStackTrace();
        }
        for (byte encodedByte : bytes) {
            byte cellIndex = (byte) (encodedByte - 49);
            arrayList.add(COUILockPatternView.Cell.of(cellIndex / 3, cellIndex % 3));
        }
        return arrayList;
    }
}
