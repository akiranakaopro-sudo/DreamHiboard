package com.oplus.graphics;

import android.graphics.Rect;

interface IOplusOutline {
    void setSmoothRoundRect(int left, int top, int right, int bottom, float radius, float weight);

    void setSmoothRoundRect(int left, int top, int right, int bottom, float radius);

    void setSmoothRoundRect(Rect rect, float radius, float weight);

    void setSmoothRoundRect(Rect rect, float radius);
}
