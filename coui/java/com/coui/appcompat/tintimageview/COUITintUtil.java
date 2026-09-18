package com.coui.appcompat.tintimageview;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import androidx.core.graphics.drawable.DrawableCompat;


public class COUITintUtil {
    public static Bitmap tintBitmap(Bitmap bitmap, int color) {
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        bitmapCreateBitmap.setDensity(bitmap.getDensity());
        new Canvas(bitmapCreateBitmap).drawBitmap(bitmap, 0.0f, 0.0f, paint);
        return bitmapCreateBitmap;
    }

    public static Drawable tintDrawable(Drawable drawable, ColorStateList colorStateList) {
        Drawable drawableR = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(drawableR.mutate(), colorStateList);
        return drawableR;
    }

    public static Drawable tintDrawable(Drawable drawable, int color) {
        Drawable drawableR = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawableR.mutate(), color);
        return drawableR;
    }
}
