package com.coui.appcompat.lunarutil;

import android.text.TextUtils;
import android.util.Log;
import com.coui.appcompat.picker.COUILunarDatePicker;
import com.coui.appcompat.vibrateutil.VibrateUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class COUILunarUtil {
    public static final int DECREATE_A_LUANR_YEAR = -1;
    private static final int DEFAULT_END_YEAR = 2100;
    private static final int DEFAULT_START_YEAR = 1900;
    private static final int HIGH_BIT_VALUE = 32768;
    public static final int INCREASE_A_LUANR_YEAR = 1;
    private static final int LAUN_LEAP_MONTH_DAYS = 29;
    private static final int LAUN_MONTH_DAYS = 30;
    private static final int LEAPMONTH_BIT_FLAG = 15;
    private static final int LEAPMONTH_BIT_MASK = 65536;
    public static final int LEAP_MONTH = 0;
    private static final int LOW_BIT_VALUE = 8;
    private static final int MAX_YEAR = 2100;
    private static final int MIN_YEAR = 1900;
    public static final int NORMAL_MONTH = 1;
    private static final int ONE = 1;
    private static final String START_DATE = "19000130";
    private static final String TAG = "COUILunar";
    private static final int THIRTY = 30;
    private static final int TWELVE = 12;
    private static final int YEAR_OF_MONTH = 12;
    private static final String[] CHINESE_NUMBER = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"};
    private static final String[] ALL_SC_SOLAR_TERM_NAMES = {"小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨", "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"};
    private static final String[] ALL_TC_SOLAR_TERM_NAMES = {"小寒", "大寒", "立春", "雨水", "驚蟄", "春分", "清明", "穀雨", "立夏", "小滿", "芒種", "夏至", "小暑", "大暑", "立秋", "處暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"};
    private static final long[] LUNAR_INFO = {19416, 19168, 42352, 21717, 53856, 55632, 91476, 22176, 39632, 21970, 19168, 42422, 42192, 53840, 119381, 46400, 54944, 44450, 38320, 84343, 18800, 42160, 46261, 27216, 27968, 109396, 11104, 38256, 21234, 18800, 25958, 54432, 59984, 92821, 23248, 11104, 100067, 37600, 116951, 51536, 54432, 120998, 46416, 22176, 107956, 9680, 37584, 53938, 43344, 46423, 27808, 46416, 86869, 19872, 42416, 83315, 21168, 43432, 59728, 27296, 44710, 43856, 19296, 43748, 42352, 21088, 62051, 55632, 23383, 22176, 38608, 19925, 19152, 42192, 54484, 53840, 54616, 46400, 46752, 103846, 38320, 18864, 43380, 42160, 45690, 27216, 27968, 44870, 43872, 38256, 19189, 18800, 25776, 29859, 59984, 27480, 23232, 43872, 38613, 37600, 51552, 55636, 54432, 55888, 30034, 22176, 43959, 9680, 37584, 51893, 43344, 46240, 47780, 44368, 21977, 19360, 42416, 86390, 21168, 43312, 31060, 27296, 44368, 23378, 19296, 42726, 42208, 53856, 60005, 54576, 23200, 30371, 38608, 19195, 19152, 42192, 118966, 53840, 54560, 56645, 46496, 22224, 21938, 18864, 42359, 42160, 43600, 111189, 27936, 44448, 84835, 37744, 18936, 18800, 25776, 92326, 59984, 27424, 108228, 43744, 37600, 53987, 51552, 54615, 54432, 55888, 23893, 22176, 42704, 21972, 21200, 43448, 43344, 46240, 46758, 44368, 21920, 43940, 42416, 21168, 45683, 26928, 29495, 27296, 44368, 84821, 19296, 42352, 21732, 53600, 59752, 54560, 55968, 92838, 22224, 19168, 43476, 41680, 53584, 62034, 54560};
    private static final int[][] SOLAR_TERM_DAYS = {new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22}, new int[]{6, 21, 4, 19, 6, 21, 5, 21, 6, 22, 6, 22, 8, 23, 8, 24, 8, 24, 9, 24, 8, 23, 8, 22}, new int[]{6, 21, 5, 19, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 21, 6, 22, 6, 22, 8, 23, 8, 24, 8, 24, 9, 24, 8, 23, 8, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 5, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 23, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 21, 6, 22, 6, 22, 8, 23, 8, 24, 8, 23, 9, 24, 8, 23, 8, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 19, 5, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 8, 23, 8, 24, 8, 23, 9, 24, 8, 23, 8, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 19, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 8, 23, 8, 24, 8, 23, 9, 24, 8, 23, 7, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 22, 7, 22}, new int[]{6, 20, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 23, 7, 22}, new int[]{6, 21, 4, 19, 5, 20, 4, 20, 5, 20, 5, 21, 7, 22, 7, 23, 7, 22, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 5, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22}, new int[]{6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 7, 22, 7, 23, 7, 22, 8, 23, 7, 22, 6, 21}, new int[]{5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22}, new int[]{6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 7, 22, 7, 23, 7, 22, 8, 23, 7, 22, 6, 21}, new int[]{5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 6, 22, 7, 23, 7, 22, 8, 23, 7, 22, 6, 21}, new int[]{5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 6, 22, 7, 22, 7, 22, 8, 23, 7, 22, 6, 21}, new int[]{5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 6, 22, 7, 22, 7, 22, 8, 23, 7, 22, 6, 21}, new int[]{5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21}, new int[]{5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22}, new int[]{5, 20, 4, 19, 6, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22}, new int[]{6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 6, 22, 7, 22, 7, 22, 8, 23, 7, 22, 6, 21}};
    private static SimpleDateFormat sChineseDateFormat = new SimpleDateFormat("yyyy年MM月dd日");

    public static int[] calculateLunarByGregorian(int year, int month, int day) {
        Date date;
        int[] iArr = {VibrateUtils.STRENGTH_MAX_STEP, 1, 1, 1};
        if (year == Integer.MIN_VALUE) {
            iArr[0] = year;
            int index = month - 1;
            iArr[1] = (index % 12) + 1;
            iArr[2] = day;
            iArr[3] = index / 12 > 0 ? 0 : 1;
            return iArr;
        }
        Date date2 = null;
        try {
            date = sChineseDateFormat.parse("1900年1月31日");
        } catch (ParseException e) {
            Log.e(TAG, "calculateLunarByGregorian(),parse baseDate error.");
            e.printStackTrace();
            date = null;
        }
        if (date == null) {
            Log.e(TAG, "baseDate is null,return lunar date:2000.1.1");
            return iArr;
        }
        try {
            date2 = sChineseDateFormat.parse(year + "年" + month + "月" + day + "日");
        } catch (ParseException e) {
            Log.e(TAG, "calculateLunarByGregorian(),parse currentDate error.");
            e.printStackTrace();
        }
        if (date2 == null) {
            Log.e(TAG, "currentDate is null,return lunar date:2000.1.1");
            return iArr;
        }
        int iRound = Math.round((date2.getTime() - date.getTime()) / 8.64E7f);
        int index_2 = 1900;
        int iDaysOfLunarYear = 0;
        while (index_2 < 10000 && iRound > 0) {
            iDaysOfLunarYear = daysOfLunarYear(index_2);
            iRound -= iDaysOfLunarYear;
            index_2++;
        }
        if (iRound < 0) {
            iRound += iDaysOfLunarYear;
            index_2--;
        }
        int iLeapMonth = leapMonth(index_2);
        int index_3 = 1;
        int index_4 = 0;
        int iDaysOfALunarMonth = 0;
        while (index_3 < 13 && iRound > 0) {
            if (iLeapMonth > 0 && index_3 == iLeapMonth + 1 && index_4 == 0) {
                index_3--;
                iDaysOfALunarMonth = daysOfLeapMonthInLunarYear(index_2);
                index_4 = 1;
            } else {
                iDaysOfALunarMonth = daysOfALunarMonth(index_2, index_3);
            }
            iRound -= iDaysOfALunarMonth;
            if (index_4 != 0 && index_3 == iLeapMonth + 1) {
                index_4 = 0;
            }
            index_3++;
        }
        if (iRound == 0 && iLeapMonth > 0 && index_3 == iLeapMonth + 1) {
            if (index_4 != 0) {
                index_4 = 0;
            } else {
                index_3--;
                index_4 = 1;
            }
        }
        if (iRound < 0) {
            iRound += iDaysOfALunarMonth;
            index_3--;
        }
        iArr[0] = index_2;
        iArr[1] = index_3;
        iArr[2] = iRound + 1;
        iArr[3] = index_4 ^ 1;
        return iArr;
    }

    public static COUILunarDatePicker.IncompleteDate changeALunarYear(int index, int index_2, int index_3, int index_4) {
        int[] iArrClampMonth = clampMonth(index, index_2, index_4);
        Date dateLunarToSolar = lunarToSolar(index, iArrClampMonth[0], clampDay(index, iArrClampMonth[0], index_3, iArrClampMonth[1] == 0), iArrClampMonth[1] == 0);
        COUILunarDatePicker.IncompleteDate incompleteDate = new COUILunarDatePicker.IncompleteDate();
        if (dateLunarToSolar != null) {
            incompleteDate.setTimeInMillis(dateLunarToSolar.getTime());
        }
        return incompleteDate;
    }

    @Deprecated
    public static Calendar changeALunarYearByOne(Calendar calendar, int index, int index_2, int index_3, int index_4, int index_5) {
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(calendar.getTimeInMillis());
        int days = getDays(index_4, index, index_2, index_3);
        int days2 = getDays(index_5, index, index_2, index_3);
        if (index_4 > index_5) {
            calendar2.add(5, -((daysOfLunarYear(index_5) - days2) + days));
        } else if (index_4 < index_5) {
            calendar2.add(5, (daysOfLunarYear(index_4) + days2) - days);
        }
        return calendar2;
    }

    public static String chineseStringOfALunarDay(int index) {
        String[] strArr = {"初", "十", "廿", "卅"};
        int index_2 = index % 10;
        int index_3 = index_2 == 0 ? 9 : index_2 - 1;
        if (index > 30) {
            return "";
        }
        if (index == 10) {
            return "初十";
        }
        if (index == 20) {
            return "二十";
        }
        if (index == 30) {
            return "三十";
        }
        return strArr[index / 10] + CHINESE_NUMBER[index_3];
    }

    public static int clampDay(int index, int index_2, int index_3, boolean flag) {
        int iDaysOfALunarMonth = !flag ? daysOfALunarMonth(index, index_2) : daysOfLeapMonthInLunarYear(index);
        return index_3 > iDaysOfALunarMonth ? iDaysOfALunarMonth : index_3;
    }

    private static int[] clampMonth(int index, int index_2, int index_3) {
        return new int[]{index_2, ((index_3 == 0 && leapMonth(index) == index_2) ? 1 : 0) ^ 1};
    }

    public static int daysOfALunarMonth(int index, int index_2) {
        if (index != Integer.MIN_VALUE && index >= 1900) {
            if ((((long) (LEAPMONTH_BIT_MASK >> index_2)) & LUNAR_INFO[index - 1900]) == 0) {
                return LAUN_LEAP_MONTH_DAYS;
            }
        }
        return 30;
    }

    public static int daysOfLeapMonthInLunarYear(int index) {
        if (leapMonth(index) != 0) {
            return (LUNAR_INFO[index + (-1900)] & 65536) != 0 ? LAUN_MONTH_DAYS : LAUN_LEAP_MONTH_DAYS;
        }
        return 0;
    }

    public static int daysOfLunarYear(int index) {
        if (index == Integer.MIN_VALUE) {
            return 0;
        }
        int index_3 = 348;
        for (int index_2 = HIGH_BIT_VALUE; index_2 > LOW_BIT_VALUE; index_2 >>= 1) {
            int index_4 = index - 1900;
            if (index_4 >= 0) {
                long[] jArr = LUNAR_INFO;
                if (index_4 < jArr.length && (jArr[index_4] & ((long) index_2)) != 0) {
                    index_3++;
                }
            }
        }
        return index_3 + daysOfLeapMonthInLunarYear(index);
    }

    private static int[] getAMonthSolarTermDays(int index, int index_2) {
        int index_3 = (index_2 - 1) * 2;
        int[] iArr = {0, 0};
        if (index > 1969 && index < 2037) {
            int[] iArr2 = SOLAR_TERM_DAYS[index - 1970];
            int index_4 = iArr2[index_3];
            int index_5 = iArr2[index_3 + 1];
            iArr[0] = index_4;
            iArr[1] = index_5;
        }
        return iArr;
    }

    private static String[] getAMonthSolarTermNames(int index) {
        if (index >= 1 && index <= 12) {
            int index_2 = (index - 1) * 2;
            String[] strArr = ALL_TC_SOLAR_TERM_NAMES;
            return new String[]{strArr[index_2], strArr[index_2 + 1]};
        }
        Log.e(TAG, "getAMonthSolarTermNames(),param gregorianMonth:" + index + " is error");
        return new String[]{"", ""};
    }

    public static int getDays(int index, int index_2, int index_3, int index_4) {
        int iDaysOfALunarMonth;
        if (index == Integer.MIN_VALUE) {
            return 0;
        }
        for (int index_5 = 1; index_5 < index_2; index_5++) {
            index_3 += daysOfALunarMonth(index, index_5);
        }
        if (leapMonth(index) < index_2) {
            iDaysOfALunarMonth = daysOfLeapMonthInLunarYear(index);
        } else {
            if (leapMonth(index) != index_2 || index_4 != 0) {
                return index_3;
            }
            iDaysOfALunarMonth = daysOfALunarMonth(index, index_2);
        }
        return index_3 + iDaysOfALunarMonth;
    }

    private static String getGregFestival(int index, int index_2) {
        if (index == 1 && index_2 == 1) {
            return "";
        }
        if (index == 5 && index_2 == 1) {
            return "";
        }
        if (index == 10 && index_2 == 1) {
            return "";
        }
        return null;
    }

    public static String getLunarDateString(Calendar calendar) {
        int[] iArrCalculateLunarByGregorian = calculateLunarByGregorian(calendar.get(1), calendar.get(2) + 1, calendar.get(5));
        return getLunarDateString(iArrCalculateLunarByGregorian[0], iArrCalculateLunarByGregorian[1], iArrCalculateLunarByGregorian[2], iArrCalculateLunarByGregorian[3]);
    }

    private static String getLunarFestival(int index, int index_2) {
        if (index == 1 && index_2 == 1) {
            return "春節";
        }
        if (index == 5 && index_2 == 5) {
            return "端午";
        }
        if (index == 8 && index_2 == 15) {
            return "中秋";
        }
        return null;
    }

    public static String getLunarFestivalChineseString(int index, int index_2, int index_3) {
        String gregFestival = getGregFestival(index_2, index_3);
        if (!TextUtils.isEmpty(gregFestival)) {
            return gregFestival;
        }
        int[] iArrCalculateLunarByGregorian = calculateLunarByGregorian(index, index_2, index_3);
        String lunarFestival = getLunarFestival(iArrCalculateLunarByGregorian[1], iArrCalculateLunarByGregorian[2]);
        if (!TextUtils.isEmpty(lunarFestival)) {
            return lunarFestival;
        }
        String solarTerm = getSolarTerm(index, index_2, index_3);
        if (TextUtils.isEmpty(solarTerm)) {
            return getLunarNumber(iArrCalculateLunarByGregorian[1], iArrCalculateLunarByGregorian[2], iArrCalculateLunarByGregorian[3] == 0);
        }
        return solarTerm;
    }

    private static String getLunarNumber(int index, int index_2, boolean flag) {
        if (index_2 != 1) {
            return chineseStringOfALunarDay(index_2);
        }
        if (flag) {
            return "闰" + CHINESE_NUMBER[index - 1];
        }
        return CHINESE_NUMBER[index - 1] + "月";
    }

    public static String getSolarTerm(int index, int index_2, int index_3) {
        int[] aMonthSolarTermDays = getAMonthSolarTermDays(index, index_2);
        if (index_3 != aMonthSolarTermDays[0] && index_3 != aMonthSolarTermDays[1]) {
            return null;
        }
        String[] aMonthSolarTermNames = getAMonthSolarTermNames(index_2);
        if (index_3 == aMonthSolarTermDays[0]) {
            return aMonthSolarTermNames[0];
        }
        if (index_3 == aMonthSolarTermDays[1]) {
            return aMonthSolarTermNames[1];
        }
        return null;
    }

    private static int getYearDays(int index) {
        int index_3 = 348;
        for (int index_2 = HIGH_BIT_VALUE; index_2 >= LOW_BIT_VALUE; index_2 >>= 1) {
            if ((LUNAR_INFO[index - 1900] & 65520 & ((long) index_2)) != 0) {
                index_3++;
            }
        }
        return index_3 + daysOfLeapMonthInLunarYear(index);
    }

    private static boolean isLunarDate(int index, int index_2, int index_3, boolean flag) {
        if (index < 1900 || index > 2100 || index_2 < 1 || index_2 > 12 || index_3 < 1 || index_3 > 30) {
            return false;
        }
        return !flag || index_2 == leapMonth(index);
    }

    public static int leapMonth(int index) {
        if (index >= 1900 && index <= 2100) {
            return (int) (LUNAR_INFO[index - 1900] & LEAPMONTH_BIT_FLAG);
        }
        Log.e(TAG, "get leapMonth:" + index + "is out of range.return 0.");
        return 0;
    }

    public static Date lunarToSolar(int index, int index_2, int index_3, boolean flag) {
        if (!isLunarDate(index, index_2, index_3, flag)) {
            return null;
        }
        int iDaysOfALunarMonth = 0;
        for (int index_4 = 1900; index_4 < index; index_4++) {
            iDaysOfALunarMonth += getYearDays(index_4);
        }
        int iLeapMonth = leapMonth(index);
        if (flag && iLeapMonth != index_2) {
            return null;
        }
        int index_5 = 1;
        if (iLeapMonth == 0 || index_2 < iLeapMonth || (index_2 == iLeapMonth && !flag)) {
            while (index_5 < index_2) {
                iDaysOfALunarMonth += daysOfALunarMonth(index, index_5);
                index_5++;
            }
            if (index_3 > daysOfALunarMonth(index, index_2)) {
                return null;
            }
        } else {
            while (index_5 < index_2) {
                iDaysOfALunarMonth += daysOfALunarMonth(index, index_5);
                index_5++;
            }
            if (index_2 > iLeapMonth) {
                iDaysOfALunarMonth += daysOfLeapMonthInLunarYear(index);
                if (index_3 > daysOfALunarMonth(index, index_2)) {
                    return null;
                }
            } else {
                iDaysOfALunarMonth += daysOfALunarMonth(index, index_2);
                if (index_3 > daysOfLeapMonthInLunarYear(index)) {
                    return null;
                }
            }
        }
        int index_6 = iDaysOfALunarMonth + index_3;
        try {
            Date date = new SimpleDateFormat("yyyyMMdd").parse(START_DATE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(5, index_6);
            return calendar.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getLunarDateString(int index, int index_2, int index_3) {
        int[] iArrCalculateLunarByGregorian = calculateLunarByGregorian(index, index_2, index_3);
        return getLunarDateString(iArrCalculateLunarByGregorian[0], iArrCalculateLunarByGregorian[1], iArrCalculateLunarByGregorian[2], iArrCalculateLunarByGregorian[3]);
    }

    private static String getLunarDateString(int index, int index_2, int index_3, int index_4) {
        if (index != Integer.MIN_VALUE) {
            StringBuilder sb = new StringBuilder();
            sb.append(index);
            sb.append("年");
            sb.append(index_4 == 0 ? "闰" : "");
            sb.append(CHINESE_NUMBER[index_2 - 1]);
            sb.append("月");
            sb.append(chineseStringOfALunarDay(index_3));
            return sb.toString();
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append(index_4 == 0 ? "闰" : "");
        sb2.append(CHINESE_NUMBER[index_2 - 1]);
        sb2.append("月");
        sb2.append(chineseStringOfALunarDay(index_3));
        return sb2.toString();
    }
}
