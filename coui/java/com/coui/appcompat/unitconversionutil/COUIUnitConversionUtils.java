package com.coui.appcompat.unitconversionutil;

import android.content.Context;
import android.util.Log;
import com.coui.appcompat.R;
import com.coui.appcompat.log.COUILog;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class COUIUnitConversionUtils {
    private static final boolean COUI_DEBUG;
    private static final double HUNDRED = 100.0d;
    private static final double MILLION = 1000000.0d;
    private static final String NOPOINT = "0";
    private static final String ONEPOINT = "0.0";
    private static final float ONE_MILLION = 1.0E-7f;
    private static final double POINT_NINE_EIGHT = 0.98d;
    private static final String SIXPOINT = "0.00000";
    private static final double SPECIAL = 1024.0d;
    private static final int SQUARE_FIVE = 5;
    private static final int SQUARE_FOUR = 4;
    private static final int SQUARE_THREE = 3;
    private static final String TAG = "COUIUnitConversionUtils";
    private static final double TEN = 10.0d;
    private static final double THOUSAND = 1000.0d;
    private static final String TWOPOINT = "0.00";
    private String mByteShort;
    private String mByteSpeed;
    private Context mContext;
    private String mGigaByteShort;
    private String mGigaByteSpeed;
    private boolean mIfShowNormal;
    private String mKiloByteShort;
    private String mKiloByteSpeed;
    private String mMegaByteShort;
    private String mMegaByteSpeed;
    private String mMoreDownLoad;
    private String mMostDownLoad;
    private String mPetaByteShort;
    private String mPetaByteSpeed;
    private String mSpecialPoint;
    private String mTeraByteShort;
    private String mTeraByteSpeed;

    static {
        COUI_DEBUG = COUILog.LOG_DEBUG || COUILog.isLoggable(TAG, 3);
    }

    public COUIUnitConversionUtils(Context context) {
        this.mMoreDownLoad = null;
        this.mMostDownLoad = null;
        this.mSpecialPoint = "0.98";
        this.mContext = context;
        this.mIfShowNormal = context.getResources().getBoolean(R.bool.show_direction_normal);
        this.mMoreDownLoad = context.getResources().getString(R.string.more_time_download);
        this.mMostDownLoad = context.getResources().getString(R.string.most_time_download);
        this.mSpecialPoint = formatLocaleNumber(POINT_NINE_EIGHT, TWOPOINT);
        this.mByteShort = this.mContext.getResources().getString(R.string.byteShort);
        this.mKiloByteShort = this.mContext.getResources().getString(R.string.kilobyteShort);
        this.mMegaByteShort = this.mContext.getResources().getString(R.string.megabyteShort);
        this.mGigaByteShort = this.mContext.getResources().getString(R.string.gigabyteShort);
        this.mTeraByteShort = this.mContext.getResources().getString(R.string.terabyteShort);
        this.mPetaByteShort = this.mContext.getResources().getString(R.string.petabyteShort);
        this.mByteSpeed = this.mContext.getResources().getString(R.string.byteSpeed);
        this.mKiloByteSpeed = this.mContext.getResources().getString(R.string.kiloByteSpeed);
        this.mMegaByteSpeed = this.mContext.getResources().getString(R.string.megaByteSpeed);
        this.mGigaByteSpeed = this.mContext.getResources().getString(R.string.gigaByteSpeed);
        this.mTeraByteSpeed = this.mContext.getResources().getString(R.string.teraByteSpeed);
        this.mPetaByteSpeed = this.mContext.getResources().getString(R.string.petaByteSpeed);
    }

    private String formatLocaleNumber(double doubleValue, String str) {
        return new DecimalFormat(str, new DecimalFormatSymbols(this.mContext.getResources().getConfiguration().locale)).format(doubleValue);
    }

    private String formatNumber(double doubleValue, String str, boolean flag) {
        DecimalFormat decimalFormat = new DecimalFormat(str, new DecimalFormatSymbols(Locale.CHINA));
        if (flag) {
            decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        } else {
            decimalFormat.setRoundingMode(RoundingMode.FLOOR);
        }
        return decimalFormat.format(doubleValue);
    }

    private String getChineseDownloadValue(long timestamp) {
        if (0 <= timestamp && timestamp < 10000.0d) {
            if (timestamp == 0) {
                timestamp++;
            }
            return timestamp + " ";
        }
        double doubleValue_2 = timestamp;
        if (10000.0d <= doubleValue_2 && doubleValue_2 < 100000.0d) {
            double dDoubleValue = Double.valueOf(formatNumber(doubleValue_2 / 10000.0d, ONEPOINT, true)).doubleValue();
            int index = (int) dDoubleValue;
            if (dDoubleValue == index) {
                return index + this.mMoreDownLoad;
            }
            return dDoubleValue + this.mMoreDownLoad;
        }
        if (100000.0d <= doubleValue_2 && doubleValue_2 < MILLION) {
            double dDoubleValue2 = Double.valueOf(formatNumber(doubleValue_2 / 10000.0d, ONEPOINT, true)).doubleValue();
            int index_2 = (int) dDoubleValue2;
            if (dDoubleValue2 == index_2) {
                return index_2 + this.mMoreDownLoad;
            }
            return dDoubleValue2 + this.mMoreDownLoad;
        }
        if (MILLION <= doubleValue_2 && doubleValue_2 < 1.0E7d) {
            return ((int) Double.valueOf(formatNumber(doubleValue_2 / 10000.0d, TWOPOINT, true)).doubleValue()) + this.mMoreDownLoad;
        }
        if (1.0E7d <= doubleValue_2 && doubleValue_2 < 1.0E8d) {
            return ((int) Double.valueOf(formatNumber(doubleValue_2 / 10000.0d, TWOPOINT, true)).doubleValue()) + this.mMoreDownLoad;
        }
        if (doubleValue_2 < 1.0E8d) {
            throw new IllegalArgumentException("the value of the incoming is wrong");
        }
        return formatNumber(Double.valueOf(formatNumber(doubleValue_2 / 1.0E8d, SIXPOINT, true)).doubleValue(), ONEPOINT, false) + this.mMostDownLoad;
    }

    private String getChineseStripValue(long timestamp) {
        if (0 <= timestamp && timestamp < 10000.0d) {
            return timestamp + " ";
        }
        double doubleValue_2 = timestamp;
        if (10000.0d > doubleValue_2 || doubleValue_2 >= MILLION) {
            if (MILLION > doubleValue_2 || doubleValue_2 >= 1.0E8d) {
                throw new IllegalArgumentException("the value of the incoming is wrong");
            }
            return formatNumber(doubleValue_2 / 10000.0d, NOPOINT, true) + this.mMoreDownLoad;
        }
        double dDoubleValue = Double.valueOf(formatNumber(doubleValue_2 / 10000.0d, ONEPOINT, true)).doubleValue();
        int index = (int) dDoubleValue;
        if (dDoubleValue == index) {
            return index + this.mMoreDownLoad;
        }
        return dDoubleValue + this.mMoreDownLoad;
    }

    private String getEnglishDownloadValue(long timestamp) {
        if (0 <= timestamp && timestamp < 10000.0d) {
            if (timestamp == 0) {
                timestamp++;
            }
            return timestamp + " ";
        }
        double doubleValue_2 = timestamp;
        if (10000.0d <= doubleValue_2 && doubleValue_2 < 100000.0d) {
            return ((int) (Double.valueOf(formatNumber(doubleValue_2 / 10000.0d, ONEPOINT, true)).doubleValue() * TEN)) + this.mMoreDownLoad;
        }
        if (100000.0d <= doubleValue_2 && doubleValue_2 < MILLION) {
            return ((int) (Double.valueOf(formatNumber(doubleValue_2 / 10000.0d, ONEPOINT, true)).doubleValue() * TEN)) + this.mMoreDownLoad;
        }
        if (MILLION <= doubleValue_2 && doubleValue_2 < 1.0E7d) {
            String number = formatNumber(doubleValue_2 / 10000.0d, TWOPOINT, true);
            double dDoubleValue = Double.valueOf(number).doubleValue() / HUNDRED;
            int index = (int) dDoubleValue;
            if (Math.abs(dDoubleValue - ((double) index)) < 1.0000000116860974E-7d) {
                return index + this.mMostDownLoad;
            }
            return Double.valueOf(number) + this.mMostDownLoad;
        }
        if (1.0E7d > doubleValue_2 || doubleValue_2 >= 1.0E8d) {
            if (doubleValue_2 < 1.0E8d) {
                throw new IllegalArgumentException("the value of the incoming is wrong");
            }
            return ((int) (Double.valueOf(formatNumber(Double.valueOf(formatNumber(doubleValue_2 / 1.0E8d, SIXPOINT, true)).doubleValue(), ONEPOINT, false)).doubleValue() * HUNDRED)) + this.mMostDownLoad;
        }
        String number2 = formatNumber(doubleValue_2 / 10000.0d, TWOPOINT, true);
        double dDoubleValue2 = Double.valueOf(number2).doubleValue() / HUNDRED;
        int index_2 = (int) dDoubleValue2;
        if (Math.abs(dDoubleValue2 - ((double) index_2)) < 1.0000000116860974E-7d) {
            return index_2 + this.mMostDownLoad;
        }
        return Double.valueOf(number2) + this.mMostDownLoad;
    }

    private String getEnglishStripValue(long timestamp) {
        if (0 <= timestamp && timestamp < 10000.0d) {
            return timestamp + " ";
        }
        double doubleValue_2 = timestamp;
        if (10000.0d <= doubleValue_2 && doubleValue_2 < MILLION) {
            return ((int) (Double.valueOf(formatNumber(doubleValue_2 / 10000.0d, ONEPOINT, true)).doubleValue() * TEN)) + this.mMoreDownLoad;
        }
        if (MILLION > doubleValue_2 || doubleValue_2 >= 1.0E8d) {
            throw new IllegalArgumentException("the value of the incoming is wrong");
        }
        return ((int) (Double.valueOf(formatNumber(doubleValue_2 / 10000.0d, NOPOINT, true)).doubleValue() * TEN)) + this.mMoreDownLoad;
    }

    private String getStringComposite(String str, String text) {
        if (COUI_DEBUG) {
            Log.d(TAG, "getStringComposite content:" + str + ",unit:" + str + ",mIfShowNormal:" + this.mIfShowNormal);
        }
        if (this.mIfShowNormal) {
            return str + text;
        }
        return text + " " + str;
    }

    private boolean isChinese() {
        String country = this.mContext.getResources().getConfiguration().locale.getCountry();
        if (country != null) {
            return country.equalsIgnoreCase("CN") || country.equalsIgnoreCase("TW") || country.equalsIgnoreCase("HK");
        }
        return false;
    }

    public String getDownLoadValue(long timestamp) {
        return isChinese() ? getChineseDownloadValue(timestamp) : getEnglishDownloadValue(timestamp);
    }

    public String getSpeedValue(long timestamp) {
        if (0 <= timestamp) {
            double doubleValue_2 = timestamp;
            if (doubleValue_2 < THOUSAND) {
                String number = formatNumber(doubleValue_2, NOPOINT, true);
                long timestamp_2 = Long.parseLong(number);
                String localeNumber = formatLocaleNumber(Double.valueOf(number).doubleValue(), NOPOINT);
                double doubleValue_3 = timestamp_2;
                if (THOUSAND <= doubleValue_3 && doubleValue_3 < SPECIAL) {
                    return getUnitValue(timestamp_2);
                }
                return localeNumber + this.mByteSpeed;
            }
        }
        double doubleValue_4 = timestamp;
        if (THOUSAND <= doubleValue_4 && doubleValue_4 < 1024000.0d) {
            String number2 = formatNumber(doubleValue_4 / SPECIAL, NOPOINT, true);
            long timestamp_3 = Long.parseLong(number2) * 1024;
            String localeNumber2 = formatLocaleNumber(Double.valueOf(number2).doubleValue(), NOPOINT);
            double doubleValue_5 = timestamp_3;
            if (1024000.0d <= doubleValue_5 && doubleValue_5 < Math.pow(SPECIAL, 2.0d) * HUNDRED) {
                return getUnitValue(timestamp_3);
            }
            return localeNumber2 + this.mKiloByteSpeed;
        }
        if (1024000.0d <= doubleValue_4 && doubleValue_4 < Math.pow(SPECIAL, 2.0d) * HUNDRED) {
            String number3 = formatNumber(doubleValue_4 / Math.pow(SPECIAL, 2.0d), ONEPOINT, true);
            long jDoubleValue = (long) (Double.valueOf(number3).doubleValue() * Math.pow(SPECIAL, 2.0d));
            String localeNumber3 = formatLocaleNumber(Double.valueOf(number3).doubleValue(), ONEPOINT);
            double doubleValue_6 = jDoubleValue;
            if (Math.pow(SPECIAL, 2.0d) * HUNDRED <= doubleValue_6 && doubleValue_6 < Math.pow(SPECIAL, 2.0d) * THOUSAND) {
                return getUnitValue(jDoubleValue);
            }
            return localeNumber3 + this.mMegaByteSpeed;
        }
        if (Math.pow(SPECIAL, 2.0d) * HUNDRED <= doubleValue_4 && doubleValue_4 < Math.pow(SPECIAL, 2.0d) * THOUSAND) {
            String number4 = formatNumber(doubleValue_4 / Math.pow(SPECIAL, 2.0d), NOPOINT, true);
            long jDoubleValue2 = (long) (Double.valueOf(number4).doubleValue() * Math.pow(SPECIAL, 2.0d));
            String localeNumber4 = formatLocaleNumber(Double.valueOf(number4).doubleValue(), NOPOINT);
            double doubleValue_7 = jDoubleValue2;
            if (Math.pow(SPECIAL, 2.0d) * THOUSAND <= doubleValue_7 && doubleValue_7 < Math.pow(SPECIAL, 3.0d)) {
                return getUnitValue(jDoubleValue2);
            }
            return localeNumber4 + this.mMegaByteSpeed;
        }
        if (Math.pow(SPECIAL, 2.0d) * THOUSAND <= doubleValue_4 && doubleValue_4 < Math.pow(SPECIAL, 3.0d)) {
            if (doubleValue_4 > Math.pow(SPECIAL, 2.0d) * 1023.0d) {
                return getUnitValue((long) Math.pow(SPECIAL, 3.0d));
            }
            return this.mSpecialPoint + this.mGigaByteSpeed;
        }
        if (Math.pow(SPECIAL, 3.0d) <= doubleValue_4 && doubleValue_4 < Math.pow(SPECIAL, 3.0d) * TEN) {
            String number5 = formatNumber(doubleValue_4 / Math.pow(SPECIAL, 3.0d), TWOPOINT, true);
            long jDoubleValue3 = (long) (Double.valueOf(number5).doubleValue() * Math.pow(SPECIAL, 3.0d));
            String localeNumber5 = formatLocaleNumber(Double.valueOf(number5).doubleValue(), TWOPOINT);
            double doubleValue_8 = jDoubleValue3;
            if (Math.pow(SPECIAL, 3.0d) * TEN <= doubleValue_8 && doubleValue_8 < Math.pow(SPECIAL, 3.0d) * HUNDRED) {
                return getUnitValue(jDoubleValue3);
            }
            return localeNumber5 + this.mGigaByteSpeed;
        }
        if (Math.pow(SPECIAL, 3.0d) * TEN <= doubleValue_4 && doubleValue_4 < Math.pow(SPECIAL, 3.0d) * HUNDRED) {
            String number6 = formatNumber(doubleValue_4 / Math.pow(SPECIAL, 3.0d), ONEPOINT, true);
            long jDoubleValue4 = (long) (Double.valueOf(number6).doubleValue() * Math.pow(SPECIAL, 3.0d));
            String localeNumber6 = formatLocaleNumber(Double.valueOf(number6).doubleValue(), ONEPOINT);
            double doubleValue_9 = jDoubleValue4;
            if (Math.pow(SPECIAL, 3.0d) * HUNDRED <= doubleValue_9 && doubleValue_9 < Math.pow(SPECIAL, 3.0d) * THOUSAND) {
                return getUnitValue(jDoubleValue4);
            }
            return localeNumber6 + this.mGigaByteSpeed;
        }
        if (Math.pow(SPECIAL, 3.0d) * HUNDRED <= doubleValue_4 && doubleValue_4 < Math.pow(SPECIAL, 3.0d) * THOUSAND) {
            String number7 = formatNumber(doubleValue_4 / Math.pow(SPECIAL, 3.0d), NOPOINT, true);
            long jDoubleValue5 = (long) (Double.valueOf(number7).doubleValue() * Math.pow(SPECIAL, 3.0d));
            String localeNumber7 = formatLocaleNumber(Double.valueOf(number7).doubleValue(), NOPOINT);
            double doubleValue_10 = jDoubleValue5;
            if (Math.pow(SPECIAL, 3.0d) * THOUSAND <= doubleValue_10 && doubleValue_10 < Math.pow(SPECIAL, 4.0d)) {
                return getUnitValue(jDoubleValue5);
            }
            return localeNumber7 + this.mGigaByteSpeed;
        }
        if (Math.pow(SPECIAL, 3.0d) * THOUSAND <= doubleValue_4 && doubleValue_4 < Math.pow(SPECIAL, 4.0d)) {
            if (doubleValue_4 > Math.pow(SPECIAL, 3.0d) * 1023.0d) {
                return getUnitValue((long) Math.pow(SPECIAL, 4.0d));
            }
            return this.mSpecialPoint + this.mTeraByteSpeed;
        }
        if (Math.pow(SPECIAL, 4.0d) <= doubleValue_4 && doubleValue_4 < Math.pow(SPECIAL, 4.0d) * TEN) {
            String number8 = formatNumber(doubleValue_4 / Math.pow(SPECIAL, 4.0d), TWOPOINT, true);
            long jDoubleValue6 = (long) (Double.valueOf(number8).doubleValue() * Math.pow(SPECIAL, 4.0d));
            String localeNumber8 = formatLocaleNumber(Double.valueOf(number8).doubleValue(), TWOPOINT);
            double doubleValue_11 = jDoubleValue6;
            if (Math.pow(SPECIAL, 4.0d) * TEN <= doubleValue_11 && doubleValue_11 < Math.pow(SPECIAL, 4.0d) * HUNDRED) {
                return getUnitValue(jDoubleValue6);
            }
            return localeNumber8 + this.mTeraByteSpeed;
        }
        if (Math.pow(SPECIAL, 4.0d) * TEN <= doubleValue_4 && doubleValue_4 < Math.pow(SPECIAL, 4.0d) * HUNDRED) {
            String number9 = formatNumber(doubleValue_4 / Math.pow(SPECIAL, 4.0d), ONEPOINT, true);
            long jDoubleValue7 = (long) (Double.valueOf(number9).doubleValue() * Math.pow(SPECIAL, 4.0d));
            String localeNumber9 = formatLocaleNumber(Double.valueOf(number9).doubleValue(), ONEPOINT);
            double doubleValue_12 = jDoubleValue7;
            if (Math.pow(SPECIAL, 4.0d) * HUNDRED <= doubleValue_12 && doubleValue_12 < Math.pow(SPECIAL, 4.0d) * THOUSAND) {
                return getUnitValue(jDoubleValue7);
            }
            return localeNumber9 + this.mTeraByteSpeed;
        }
        if (Math.pow(SPECIAL, 4.0d) * HUNDRED <= doubleValue_4 && doubleValue_4 < Math.pow(SPECIAL, 4.0d) * THOUSAND) {
            String number10 = formatNumber(doubleValue_4 / Math.pow(SPECIAL, 4.0d), NOPOINT, true);
            long jDoubleValue8 = (long) (Double.valueOf(number10).doubleValue() * Math.pow(SPECIAL, 4.0d));
            double doubleValue_13 = jDoubleValue8;
            if (Math.pow(SPECIAL, 4.0d) * THOUSAND <= doubleValue_13 && doubleValue_13 < Math.pow(SPECIAL, 5.0d)) {
                return getUnitValue(jDoubleValue8);
            }
            return number10 + this.mTeraByteSpeed;
        }
        if (Math.pow(SPECIAL, 4.0d) * THOUSAND <= doubleValue_4 && doubleValue_4 < Math.pow(SPECIAL, 5.0d)) {
            if (doubleValue_4 > Math.pow(SPECIAL, 4.0d) * 1023.0d) {
                return getUnitValue((long) Math.pow(SPECIAL, 5.0d));
            }
            return this.mSpecialPoint + this.mPetaByteSpeed;
        }
        if (Math.pow(SPECIAL, 5.0d) <= doubleValue_4 && doubleValue_4 < Math.pow(SPECIAL, 5.0d) * TEN) {
            String number11 = formatNumber(doubleValue_4 / Math.pow(SPECIAL, 5.0d), TWOPOINT, true);
            long jDoubleValue9 = (long) (Double.valueOf(number11).doubleValue() * Math.pow(SPECIAL, 5.0d));
            String localeNumber10 = formatLocaleNumber(Double.valueOf(number11).doubleValue(), TWOPOINT);
            double doubleValue_14 = jDoubleValue9;
            if (Math.pow(SPECIAL, 5.0d) * TEN <= doubleValue_14 && doubleValue_14 < Math.pow(SPECIAL, 5.0d) * HUNDRED) {
                return getUnitValue(jDoubleValue9);
            }
            return localeNumber10 + this.mPetaByteSpeed;
        }
        if (Math.pow(SPECIAL, 5.0d) * TEN > doubleValue_4 || doubleValue_4 >= Math.pow(SPECIAL, 5.0d) * HUNDRED) {
            if (Math.pow(SPECIAL, 5.0d) * HUNDRED > doubleValue_4 || doubleValue_4 >= Math.pow(SPECIAL, 5.0d) * THOUSAND) {
                throw new IllegalArgumentException("the value of the incoming is wrong");
            }
            return formatLocaleNumber(doubleValue_4 / Math.pow(SPECIAL, 5.0d), NOPOINT) + this.mPetaByteSpeed;
        }
        String number12 = formatNumber(doubleValue_4 / Math.pow(SPECIAL, 5.0d), ONEPOINT, true);
        long jDoubleValue10 = (long) (Double.valueOf(number12).doubleValue() * Math.pow(SPECIAL, 5.0d));
        String localeNumber11 = formatLocaleNumber(Double.valueOf(number12).doubleValue(), ONEPOINT);
        double doubleValue_15 = jDoubleValue10;
        if (Math.pow(SPECIAL, 5.0d) * HUNDRED <= doubleValue_15 && doubleValue_15 < Math.pow(SPECIAL, 5.0d) * THOUSAND) {
            return getUnitValue(jDoubleValue10);
        }
        return localeNumber11 + this.mPetaByteSpeed;
    }

    public String getStripValue(long timestamp) {
        return isChinese() ? getChineseStripValue(timestamp) : getEnglishStripValue(timestamp);
    }

    public String getTransformUnitValue(long timestamp, double doubleValue_2) {
        double doubleValue_3 = timestamp;
        if (0 <= timestamp && doubleValue_3 < THOUSAND) {
            String number = formatNumber(doubleValue_3, NOPOINT, true);
            long timestamp_2 = Long.parseLong(number);
            String localeNumber = formatLocaleNumber(Double.valueOf(number).doubleValue(), NOPOINT);
            double doubleValue_4 = timestamp_2;
            if (THOUSAND <= doubleValue_4 && doubleValue_4 < SPECIAL) {
                return getUnitValue(timestamp_2);
            }
            return getStringComposite(localeNumber, this.mByteShort);
        }
        if (THOUSAND <= doubleValue_3 && doubleValue_3 < 1024000.0d) {
            String number2 = formatNumber(doubleValue_3 / doubleValue_2, NOPOINT, true);
            long timestamp_3 = Long.parseLong(number2) * ((long) doubleValue_2);
            String localeNumber2 = formatLocaleNumber(Double.valueOf(number2).doubleValue(), NOPOINT);
            double doubleValue_5 = timestamp_3;
            if (1024000.0d <= doubleValue_5 && doubleValue_5 < Math.pow(SPECIAL, 2.0d) * HUNDRED) {
                return getTransformUnitValue(timestamp_3, doubleValue_2);
            }
            return getStringComposite(localeNumber2, this.mKiloByteShort);
        }
        if (1024000.0d <= doubleValue_3 && doubleValue_3 < Math.pow(SPECIAL, 2.0d) * HUNDRED) {
            String number3 = formatNumber(doubleValue_3 / Math.pow(doubleValue_2, 2.0d), ONEPOINT, true);
            long jDoubleValue = (long) (Double.valueOf(number3).doubleValue() * Math.pow(doubleValue_2, 2.0d));
            String localeNumber3 = formatLocaleNumber(Double.valueOf(number3).doubleValue(), ONEPOINT);
            double doubleValue_6 = jDoubleValue;
            if (Math.pow(SPECIAL, 2.0d) * HUNDRED <= doubleValue_6 && doubleValue_6 < Math.pow(SPECIAL, 2.0d) * THOUSAND) {
                return getTransformUnitValue(jDoubleValue, doubleValue_2);
            }
            return getStringComposite(localeNumber3, this.mMegaByteShort);
        }
        if (Math.pow(SPECIAL, 2.0d) * HUNDRED <= doubleValue_3 && doubleValue_3 < Math.pow(SPECIAL, 2.0d) * THOUSAND) {
            String number4 = formatNumber(doubleValue_3 / Math.pow(doubleValue_2, 2.0d), NOPOINT, true);
            long jDoubleValue2 = (long) (Double.valueOf(number4).doubleValue() * Math.pow(doubleValue_2, 2.0d));
            String localeNumber4 = formatLocaleNumber(Double.valueOf(number4).doubleValue(), NOPOINT);
            double doubleValue_7 = jDoubleValue2;
            if (Math.pow(SPECIAL, 2.0d) * THOUSAND <= doubleValue_7 && doubleValue_7 < Math.pow(SPECIAL, 3.0d)) {
                return getTransformUnitValue(jDoubleValue2, doubleValue_2);
            }
            return getStringComposite(localeNumber4, this.mMegaByteShort);
        }
        if (Math.pow(SPECIAL, 2.0d) * THOUSAND <= doubleValue_3 && doubleValue_3 < Math.pow(SPECIAL, 3.0d)) {
            if (doubleValue_2 == THOUSAND) {
                String number5 = formatNumber(doubleValue_3 / Math.pow(doubleValue_2, 3.0d), TWOPOINT, true);
                return getStringComposite(formatLocaleNumber(Double.valueOf(number5).doubleValue(), TWOPOINT), this.mGigaByteShort);
            }
            if (doubleValue_2 != SPECIAL) {
                return null;
            }
            if (doubleValue_3 > Math.pow(SPECIAL, 2.0d) * 1023.0d) {
                return getUnitValue((long) Math.pow(SPECIAL, 3.0d));
            }
            return getStringComposite(this.mSpecialPoint, this.mGigaByteShort);
        }
        if (Math.pow(SPECIAL, 3.0d) <= doubleValue_3 && doubleValue_3 < Math.pow(SPECIAL, 3.0d) * TEN) {
            String number6 = formatNumber(doubleValue_3 / Math.pow(doubleValue_2, 3.0d), TWOPOINT, true);
            long jDoubleValue3 = (long) (Double.valueOf(number6).doubleValue() * Math.pow(doubleValue_2, 3.0d));
            String localeNumber5 = formatLocaleNumber(Double.valueOf(number6).doubleValue(), TWOPOINT);
            double doubleValue_8 = jDoubleValue3;
            if (Math.pow(SPECIAL, 3.0d) * TEN <= doubleValue_8 && doubleValue_8 < Math.pow(SPECIAL, 3.0d) * HUNDRED) {
                return getTransformUnitValue(jDoubleValue3, doubleValue_2);
            }
            return getStringComposite(localeNumber5, this.mGigaByteShort);
        }
        if (Math.pow(SPECIAL, 3.0d) * TEN <= doubleValue_3 && doubleValue_3 < Math.pow(SPECIAL, 3.0d) * HUNDRED) {
            String number7 = formatNumber(doubleValue_3 / Math.pow(doubleValue_2, 3.0d), ONEPOINT, true);
            long jDoubleValue4 = (long) (Double.valueOf(number7).doubleValue() * Math.pow(doubleValue_2, 3.0d));
            String localeNumber6 = formatLocaleNumber(Double.valueOf(number7).doubleValue(), ONEPOINT);
            double doubleValue_9 = jDoubleValue4;
            if (Math.pow(SPECIAL, 3.0d) * HUNDRED <= doubleValue_9 && doubleValue_9 < Math.pow(SPECIAL, 3.0d) * THOUSAND) {
                return getTransformUnitValue(jDoubleValue4, doubleValue_2);
            }
            return getStringComposite(localeNumber6, this.mGigaByteShort);
        }
        if (Math.pow(SPECIAL, 3.0d) * HUNDRED <= doubleValue_3 && doubleValue_3 < Math.pow(SPECIAL, 3.0d) * THOUSAND) {
            String number8 = formatNumber(doubleValue_3 / Math.pow(doubleValue_2, 3.0d), NOPOINT, true);
            long jDoubleValue5 = (long) (Double.valueOf(number8).doubleValue() * Math.pow(doubleValue_2, 3.0d));
            String localeNumber7 = formatLocaleNumber(Double.valueOf(number8).doubleValue(), NOPOINT);
            double doubleValue_10 = jDoubleValue5;
            if (Math.pow(SPECIAL, 3.0d) * THOUSAND <= doubleValue_10 && doubleValue_10 < Math.pow(SPECIAL, 4.0d)) {
                return getTransformUnitValue(jDoubleValue5, doubleValue_2);
            }
            return getStringComposite(localeNumber7, this.mGigaByteShort);
        }
        if (Math.pow(SPECIAL, 3.0d) * THOUSAND <= doubleValue_3 && doubleValue_3 < Math.pow(SPECIAL, 4.0d)) {
            if (doubleValue_2 == THOUSAND) {
                String number9 = formatNumber(doubleValue_3 / Math.pow(doubleValue_2, 4.0d), TWOPOINT, true);
                return getStringComposite(formatLocaleNumber(Double.valueOf(number9).doubleValue(), TWOPOINT), this.mTeraByteShort);
            }
            if (doubleValue_2 != SPECIAL) {
                return null;
            }
            if (doubleValue_3 > Math.pow(SPECIAL, 3.0d) * 1023.0d) {
                return getUnitValue((long) Math.pow(SPECIAL, 4.0d));
            }
            return getStringComposite(this.mSpecialPoint, this.mTeraByteShort);
        }
        if (Math.pow(SPECIAL, 4.0d) <= doubleValue_3 && doubleValue_3 < Math.pow(SPECIAL, 4.0d) * TEN) {
            String number10 = formatNumber(doubleValue_3 / Math.pow(doubleValue_2, 4.0d), TWOPOINT, true);
            long jDoubleValue6 = (long) (Double.valueOf(number10).doubleValue() * Math.pow(doubleValue_2, 4.0d));
            String localeNumber8 = formatLocaleNumber(Double.valueOf(number10).doubleValue(), TWOPOINT);
            double doubleValue_11 = jDoubleValue6;
            if (Math.pow(SPECIAL, 4.0d) * TEN <= doubleValue_11 && doubleValue_11 < Math.pow(SPECIAL, 4.0d) * HUNDRED) {
                return getTransformUnitValue(jDoubleValue6, doubleValue_2);
            }
            return getStringComposite(localeNumber8, this.mTeraByteShort);
        }
        if (Math.pow(SPECIAL, 4.0d) * TEN <= doubleValue_3 && doubleValue_3 < Math.pow(SPECIAL, 4.0d) * HUNDRED) {
            String number11 = formatNumber(doubleValue_3 / Math.pow(doubleValue_2, 4.0d), ONEPOINT, true);
            long jDoubleValue7 = (long) (Double.valueOf(number11).doubleValue() * Math.pow(doubleValue_2, 4.0d));
            String localeNumber9 = formatLocaleNumber(Double.valueOf(number11).doubleValue(), ONEPOINT);
            double doubleValue_12 = jDoubleValue7;
            if (Math.pow(SPECIAL, 4.0d) * HUNDRED <= doubleValue_12 && doubleValue_12 < Math.pow(SPECIAL, 4.0d) * THOUSAND) {
                return getTransformUnitValue(jDoubleValue7, doubleValue_2);
            }
            return getStringComposite(localeNumber9, this.mTeraByteShort);
        }
        if (Math.pow(SPECIAL, 4.0d) * HUNDRED <= doubleValue_3 && doubleValue_3 < Math.pow(SPECIAL, 4.0d) * THOUSAND) {
            String number12 = formatNumber(doubleValue_3 / Math.pow(doubleValue_2, 4.0d), NOPOINT, true);
            long jDoubleValue8 = (long) (Double.valueOf(number12).doubleValue() * Math.pow(doubleValue_2, 4.0d));
            String localeNumber10 = formatLocaleNumber(Double.valueOf(number12).doubleValue(), NOPOINT);
            double doubleValue_13 = jDoubleValue8;
            if (Math.pow(SPECIAL, 4.0d) * THOUSAND <= doubleValue_13 && doubleValue_13 < Math.pow(SPECIAL, 5.0d)) {
                return getTransformUnitValue(jDoubleValue8, doubleValue_2);
            }
            return getStringComposite(localeNumber10, this.mTeraByteShort);
        }
        if (Math.pow(SPECIAL, 4.0d) * THOUSAND <= doubleValue_3 && doubleValue_3 < Math.pow(SPECIAL, 5.0d)) {
            if (doubleValue_2 == THOUSAND) {
                String number13 = formatNumber(doubleValue_3 / Math.pow(doubleValue_2, 5.0d), TWOPOINT, true);
                return getStringComposite(formatLocaleNumber(Double.valueOf(number13).doubleValue(), TWOPOINT), this.mPetaByteShort);
            }
            if (doubleValue_2 != SPECIAL) {
                return null;
            }
            if (doubleValue_3 > Math.pow(SPECIAL, 4.0d) * 1023.0d) {
                return getUnitValue((long) Math.pow(SPECIAL, 5.0d));
            }
            return getStringComposite(this.mSpecialPoint, this.mPetaByteShort);
        }
        if (Math.pow(SPECIAL, 5.0d) <= doubleValue_3 && doubleValue_3 < Math.pow(SPECIAL, 5.0d) * TEN) {
            String number14 = formatNumber(doubleValue_3 / Math.pow(SPECIAL, 5.0d), TWOPOINT, true);
            long jDoubleValue9 = (long) (Double.valueOf(number14).doubleValue() * Math.pow(SPECIAL, 5.0d));
            String localeNumber11 = formatLocaleNumber(Double.valueOf(number14).doubleValue(), TWOPOINT);
            double doubleValue_14 = jDoubleValue9;
            if (Math.pow(SPECIAL, 5.0d) * TEN <= doubleValue_14 && doubleValue_14 < Math.pow(SPECIAL, 5.0d) * HUNDRED) {
                return getUnitValue(jDoubleValue9);
            }
            return getStringComposite(localeNumber11, this.mPetaByteShort);
        }
        if (Math.pow(SPECIAL, 5.0d) * TEN > doubleValue_3 || doubleValue_3 >= Math.pow(SPECIAL, 5.0d) * HUNDRED) {
            if (Math.pow(SPECIAL, 5.0d) * HUNDRED <= doubleValue_3 && doubleValue_3 < Math.pow(SPECIAL, 5.0d) * THOUSAND) {
                return getStringComposite(formatLocaleNumber(doubleValue_3 / Math.pow(SPECIAL, 5.0d), NOPOINT), this.mPetaByteShort);
            }
            throw new IllegalArgumentException("the value of the incoming is wrong");
        }
        String number15 = formatNumber(doubleValue_3 / Math.pow(SPECIAL, 5.0d), ONEPOINT, true);
        long jDoubleValue10 = (long) (Double.valueOf(number15).doubleValue() * Math.pow(SPECIAL, 5.0d));
        String localeNumber12 = formatLocaleNumber(Double.valueOf(number15).doubleValue(), ONEPOINT);
        double doubleValue_15 = jDoubleValue10;
        if (Math.pow(SPECIAL, 5.0d) * HUNDRED <= doubleValue_15 && doubleValue_15 < Math.pow(SPECIAL, 5.0d) * THOUSAND) {
            return getUnitValue(jDoubleValue10);
        }
        return getStringComposite(localeNumber12, this.mPetaByteShort);
    }

    public String getUnitThousandValue(long timestamp) {
        return getTransformUnitValue(timestamp, THOUSAND);
    }

    public String getUnitValue(long timestamp) {
        return getTransformUnitValue(timestamp, SPECIAL);
    }

    public String getDownLoadValue(long timestamp, Locale locale) {
        String country = locale != null ? locale.getCountry() : null;
        if (country != null && (country.equalsIgnoreCase("CN") || country.equalsIgnoreCase("TW") || country.equalsIgnoreCase("HK"))) {
            return getChineseDownloadValue(timestamp);
        }
        if (country != null) {
            return getEnglishDownloadValue(timestamp);
        }
        return null;
    }

    public String getStripValue(long timestamp, Locale locale) {
        String country = locale != null ? locale.getCountry() : null;
        if (country != null && (country.equalsIgnoreCase("CN") || country.equalsIgnoreCase("TW") || country.equalsIgnoreCase("HK"))) {
            return getChineseStripValue(timestamp);
        }
        if (country != null) {
            return getEnglishStripValue(timestamp);
        }
        return null;
    }
}
