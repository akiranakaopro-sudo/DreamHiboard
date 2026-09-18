package com.coui.appcompat.dateutils;

import android.content.Context;
import android.text.format.DateUtils;

import com.coui.appcompat.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class COUIDateUtils {
    public static String getYMDWDate(Context context, Date date) {
        if (!isSimplifiedChinese(context)) {
            return DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault()).format(date);
        }
        return new SimpleDateFormat(
                context.getResources().getString(R.string.ymdw), Locale.getDefault()).format(date);
    }

    public static String getYMDWHMDate(Context context, Date date) {
        if (!isSimplifiedChinese(context)) {
            return DateFormat.getDateTimeInstance(
                    DateFormat.FULL, DateFormat.SHORT, Locale.getDefault()).format(date);
        }
        return new SimpleDateFormat(
                context.getResources().getString(R.string.ymdwhm), Locale.getDefault()).format(date);
    }

    public static String getYMDWsHMDate(Context context, Date date) {
        if (!isSimplifiedChinese(context)) {
            return DateUtils.formatDateTime(context, date.getTime(), 32791);
        }
        return new SimpleDateFormat(
                context.getResources().getString(R.string.ymdwshm), Locale.getDefault()).format(date);
    }

    private static boolean isSimplifiedChinese(Context context) {
        String locale = context.getResources().getConfiguration().locale.toString();
        return locale != null && locale.equalsIgnoreCase("zh_CN");
    }
}
