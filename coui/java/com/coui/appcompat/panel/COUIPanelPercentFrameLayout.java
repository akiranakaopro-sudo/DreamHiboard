package com.coui.appcompat.panel;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowInsets;

import androidx.annotation.NonNull;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.grid.COUIPercentWidthFrameLayout;
import com.coui.appcompat.grid.COUIResponsiveUtils;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.roundRect.COUIShapePath;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.uiutil.UIUtil;
import com.coui.appcompat.version.COUIVersionUtil;
import com.oplus.graphics.OplusOutline;
import com.oplus.graphics.OplusOutlineAdapter;


public class COUIPanelPercentFrameLayout extends COUIPercentWidthFrameLayout {
    public static final float MEDIUM_AND_LARGE_SCREEN = 2.0f;
    private static final float OS_16_1_WEIGHT = 3.0f;
    public static final float SMALL_SCREEN = 1.0f;
    private static final String TAG = "COUIPanelPercentFrameLayout";
    private static final int UNSET_WIDTH = -1;
    private Bitmap mBitmap;
    private int mBottomDiff;
    private final Paint mClipPaint;
    private boolean mHasAnchor;
    private boolean mIsHandlePanel;
    private boolean mIsSupportSmoothRoundCorner;
    private int mMaxHeight;
    private int mMaxHeightOfAttr;
    private int mMaxWidth;
    private final Rect mMeasureRect;
    private final Rect mOplusOutLineRect;
    private OplusOutlineAdapter mOplusOutline;
    private final ViewOutlineProvider mOutlineProvider;
    private final Path mPath;
    private int mPreferWidth;
    private float mRadius;
    private float mRatio;
    private final RectF mRectF;
    private boolean mUseNormalSmoothCorner;
    private float mWeight;

//
    public COUIPanelPercentFrameLayout(Context context) {
        this(context, null);
    }

    private Bitmap createClipSmoothRoundBitmap() {
        if (this.mRectF.width() <= 0.0f || this.mRectF.height() <= 0.0f) {
            COUILog.i(TAG, "createClipSmoothRoundBitmap return for width and height must be > 0");
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap((int) this.mRectF.width(), (int) this.mRectF.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(1);
        paint.setColor(-1);
        canvas.drawPath(this.mPath, paint);
        return bitmap;
    }

    public void enforceChangeScreenWidth() {
        if (this.mPreferWidth == UNSET_WIDTH) {
            return;
        }
        try {
            Resources resources = getContext().getResources();
            Configuration configuration = resources.getConfiguration();
            int screenWidthDp = configuration.screenWidthDp;
            int preferWidth = this.mPreferWidth;
            if (screenWidthDp == preferWidth) {
                return;
            }
            configuration.screenWidthDp = preferWidth;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            Log.d(TAG, "enforceChangeScreenWidth : PreferWidth:" + this.mPreferWidth);
        } catch (Exception unused) {
            Log.d(TAG, "enforceChangeScreenWidth : failed to updateConfiguration");
        }
    }

    private void initAttr(AttributeSet attributeSet) {
        if (getContext() != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.COUIPanelPercentFrameLayout);
            this.mMaxHeight = typedArray.getDimensionPixelOffset(R.styleable.COUIPanelPercentFrameLayout_maxPanelHeight, 0);
            typedArray.recycle();
        }
        this.mMaxHeightOfAttr = this.mMaxHeight;
        this.mRatio = COUIPanelMultiWindowUtils.isSmallScreen(getContext(), null) ? SMALL_SCREEN : MEDIUM_AND_LARGE_SCREEN;
        initRadiusAndWeight();
    }

    private void initRadiusAndWeight() {
        boolean supportSmoothRoundCorner = RoundCornerUtil.isPathSupportSingleCorner() && RoundCornerUtil.isSmoothRoundRectOn() && !this.mUseNormalSmoothCorner;
        this.mIsSupportSmoothRoundCorner = supportSmoothRoundCorner;
        if (!supportSmoothRoundCorner) {
            this.mRadius = COUIContextUtil.getAttrDimens(getContext(), R.attr.couiRoundCornerXL);
            this.mWeight = 0.0f;
            updateClipToOutline(true);
        } else if (RoundCornerUtil.getSmoothStyleType() == 0) {
            this.mRadius = COUIContextUtil.getAttrDimens(getContext(), R.attr.couiRoundCornerXLRadius);
            this.mWeight = COUIContextUtil.getAttrFloat(getContext(), R.attr.couiRoundCornerXLWeight);
        } else if (RoundCornerUtil.getSmoothStyleType() == 1) {
            updateClipToOutline(false);
            if (COUIVersionUtil.getOSVersionCode() > 37) {
                this.mRadius = getResources().getDimension(R.dimen.coui_panel_os_16_1_radius_28_dp);
                this.mWeight = OS_16_1_WEIGHT;
            } else {
                this.mRadius = COUIContextUtil.getAttrDimens(getContext(), R.attr.couiRoundCornerXL);
                this.mWeight = 0.0f;
            }
        }
    }

    public int updateBottomCornerRadius() {
        if (this.mIsHandlePanel) {
            return getContext().getResources().getDimensionPixelOffset(R.dimen.coui_bottom_sheet_bg_top_corner_radius);
        }
        int dimensionPixelOffset = getContext().getResources().getDimensionPixelOffset(R.dimen.coui_bottom_sheet_bg_bottom_corner_radius);
        Activity activity = UIUtil.contextToActivity(getContext());
        if (activity != null) {
            int requestedOrientation = activity.getRequestedOrientation();
            if (requestedOrientation == 1 && (activity.getResources().getConfiguration().screenLayout & 48) == 32) {
                return getContext().getResources().getDimensionPixelOffset(R.dimen.coui_bottom_sheet_bg_top_corner_radius);
            }
            if (requestedOrientation == 0) {
                return 0;
            }
        }
        return dimensionPixelOffset;
    }

    private void updateClipToOutline(boolean setOutlineProvider) {
        COUILog.i(TAG, "updateClipToOutline setOutlineProvider=" + setOutlineProvider);
        if (setOutlineProvider) {
            super.setOutlineProvider(this.mOutlineProvider);
            setClipToOutline(true);
        } else {
            super.setOutlineProvider(null);
            setClipToOutline(false);
        }
    }

    private void updatePath() {
        this.mPath.reset();
        if (updateBottomCornerRadius() == 0) {
            COUIShapePath.getSmoothRoundRectPath(this.mPath, this.mRectF, this.mRadius, this.mWeight);
        } else {
            COUIShapePath.getSmoothRoundRectPath(this.mPath, this.mRectF, this.mRadius, this.mWeight, true, true, false, false);
        }
    }

    public void delPreferWidth() {
        this.mPreferWidth = UNSET_WIDTH;
        Log.d(TAG, "delPreferWidth");
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (getClipToOutline()) {
            super.draw(canvas);
            return;
        }
        if (this.mBitmap == null || COUIVersionUtil.getOSVersionCode() >= 37) {
            canvas.save();
            canvas.clipPath(this.mPath);
            super.draw(canvas);
            canvas.restore();
            return;
        }
        int saveLayerCount = canvas.saveLayer(null, null);
        super.draw(canvas);
        canvas.drawBitmap(this.mBitmap, 0.0f, 0.0f, this.mClipPaint);
        canvas.restoreToCount(saveLayerCount);
    }

    public int getGridNumber() {
        return this.mGridNumber;
    }

    public boolean getHasAnchor() {
        return this.mHasAnchor;
    }

    public int getPaddingSize() {
        return this.mPaddingSize;
    }

    public int getPaddingType() {
        return this.mPaddingType;
    }

    public float getRatio() {
        if (this.mIsHandlePanel) {
            return 1.0f;
        }
        return this.mRatio;
    }

    public boolean isIsHandlePanel() {
        return this.mIsHandlePanel;
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        int paddingBottom = getPaddingBottom();
        WindowInsets windowInsetsOnApplyWindowInsets = super.onApplyWindowInsets(windowInsets);
        if (paddingBottom != 0 && getPaddingBottom() == 0) {
            setPaddingRelative(getPaddingStart(), getPaddingTop(), getPaddingEnd(), paddingBottom);
        }
        return windowInsetsOnApplyWindowInsets;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mRatio = COUIPanelMultiWindowUtils.isSmallScreen(getContext(), null) ? SMALL_SCREEN : MEDIUM_AND_LARGE_SCREEN;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        getWindowVisibleDisplayFrame(this.mMeasureRect);
        int visibleHeight = this.mMeasureRect.height();
        int maxHeight = this.mMaxHeight;
        if (visibleHeight > maxHeight && maxHeight > 0 && maxHeight < View.MeasureSpec.getSize(heightMeasureSpec)) {
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mMaxHeight, View.MeasureSpec.getMode(heightMeasureSpec));
        }
        setPercentIndentEnabled((COUIPanelMultiWindowUtils.isSmallScreen(getContext(), null) || View.MeasureSpec.getSize(widthMeasureSpec) >= this.mMeasureRect.width()) && !COUIResponsiveUtils.isSmallScreen(getContext(), this.mMeasureRect.width()) && this.mMaxWidth == 0);
        int maxWidth = this.mMaxWidth;
        if (maxWidth != 0) {
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.getMode(widthMeasureSpec));
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        enforceChangeScreenWidth();
        if (getClipToOutline()) {
            return;
        }
        this.mRectF.set(0.0f, 0.0f, width, height);
        updatePath();
        if (this.mIsSupportSmoothRoundCorner) {
            if (!(this.mBitmap != null && width == oldWidth && height == oldHeight) && COUIVersionUtil.getOSVersionCode() < 37) {
                this.mBitmap = createClipSmoothRoundBitmap();
            }
        }
    }

    public void prepareForOutlineProvider() {
        super.setOutlineProvider(this.mOutlineProvider);
        setClipToOutline(true);
    }

    public void restoreDefaultMaxSize() {
        if (this.mMaxWidth == 0) {
            return;
        }
        this.mMaxWidth = 0;
        this.mMaxHeight = this.mMaxHeightOfAttr;
        requestLayout();
    }

    public void setHasAnchor(boolean hasAnchor) {
        if (this.mHasAnchor != hasAnchor) {
            this.mHasAnchor = hasAnchor;
            updateClipToOutline(hasAnchor);
        }
    }

    public void setIsHandlePanel(boolean isHandlePanel) {
        this.mIsHandlePanel = isHandlePanel;
    }

    public void setMaxHeight(int maxHeight) {
        if (this.mMaxHeight != maxHeight) {
            this.mMaxHeight = maxHeight;
            requestLayout();
        }
    }

    public void setMaxSize(int maxWidth, int maxHeight) {
        if (maxHeight == this.mMaxHeight && maxWidth == this.mMaxWidth) {
            return;
        }
        this.mMaxWidth = maxWidth;
        this.mMaxHeight = maxHeight;
        requestLayout();
    }

    public void setOutlineBottomOffset(int bottomOffset) {
        this.mBottomDiff = bottomOffset;
    }

    @Override
    public void setOutlineProvider(ViewOutlineProvider viewOutlineProvider) {
    }

    public void setPreferWidth(int preferWidth) {
        this.mPreferWidth = preferWidth;
        Log.d(TAG, "setPreferWidth =：" + this.mPreferWidth);
    }

    public void setUseNormalSmoothCorner(boolean useNormalSmoothCorner) {
        if (this.mUseNormalSmoothCorner != useNormalSmoothCorner) {
            this.mUseNormalSmoothCorner = useNormalSmoothCorner;
            initRadiusAndWeight();
        }
    }

    public void updateLayoutWhileConfigChange(Configuration configuration) {
        this.mRatio = COUIPanelMultiWindowUtils.isSmallScreen(getContext(), configuration) ? SMALL_SCREEN : MEDIUM_AND_LARGE_SCREEN;
    }

    public COUIPanelPercentFrameLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public COUIPanelPercentFrameLayout(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.mPath = new Path();
        this.mRectF = new RectF();
        Paint paint = new Paint(1);
        this.mClipPaint = paint;
        this.mRatio = 1.0f;
        this.mHasAnchor = false;
        this.mPreferWidth = UNSET_WIDTH;
        this.mIsSupportSmoothRoundCorner = false;
        this.mBitmap = null;
        this.mOplusOutLineRect = new Rect();
        this.mOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                COUIPanelPercentFrameLayout.this.enforceChangeScreenWidth();
                int bottomCornerRadius = COUIPanelPercentFrameLayout.this.updateBottomCornerRadius();
                int height = view.getHeight() + COUIPanelPercentFrameLayout.this.mBottomDiff;
                if (!COUIPanelPercentFrameLayout.this.mIsSupportSmoothRoundCorner) {
                    outline.setRoundRect(0, 0, view.getWidth(), height + bottomCornerRadius, COUIPanelPercentFrameLayout.this.mRadius);
                    return;
                }
                if (RoundCornerUtil.getSmoothStyleType() == 0) {
                    new OplusOutline(outline).setSmoothRoundRect(0, 0, view.getWidth(), height + bottomCornerRadius, COUIPanelPercentFrameLayout.this.mRadius, COUIPanelPercentFrameLayout.this.mWeight);
                    return;
                }
                if (RoundCornerUtil.getSmoothStyleType() == 1) {
                    COUIPanelPercentFrameLayout.this.mOplusOutline = new OplusOutlineAdapter(outline, RoundCornerUtil.getSmoothStyleType());
                    COUIPanelPercentFrameLayout.this.mOplusOutLineRect.left = 0;
                    COUIPanelPercentFrameLayout.this.mOplusOutLineRect.top = 0;
                    COUIPanelPercentFrameLayout.this.mOplusOutLineRect.right = view.getWidth();
                    COUIPanelPercentFrameLayout.this.mOplusOutLineRect.bottom = height + bottomCornerRadius;
                    if (COUIVersionUtil.getOSVersionCode() <= 37 || !COUIPanelPercentFrameLayout.this.mIsSupportSmoothRoundCorner) {
                        COUIPanelPercentFrameLayout.this.mOplusOutline.setSmoothRoundRect(COUIPanelPercentFrameLayout.this.mOplusOutLineRect, COUIPanelPercentFrameLayout.this.mRadius);
                    } else {
                        COUIPanelPercentFrameLayout.this.mOplusOutline.setSmoothRoundRect(COUIPanelPercentFrameLayout.this.mOplusOutLineRect, COUIPanelPercentFrameLayout.this.mRadius, COUIPanelPercentFrameLayout.this.mWeight);
                    }
                }
            }
        };
        initAttr(attributeSet);
        this.mMeasureRect = new Rect();
        if (COUIVersionUtil.getOSVersionCode() < 37) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        }
    }
}
