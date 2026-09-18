package com.coui.appcompat.tagview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;

public final class COUITagView extends COUITagBackgroundView {
    private ImageView imageView;
    private ImageView leftImageView;
    private Context mContext;
    private int style;
    private COUITagBackgroundView tagBackground;
    private TextView tagView;

    public COUITagView(Context context) {
        this(context, null);
        if (context == null) {
            throw new NullPointerException("context");
        }
    }

    public COUITagView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        if (context == null) {
            throw new NullPointerException("context");
        }
    }

    public COUITagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (context == null) {
            throw new NullPointerException("context");
        }
        this.mContext = context;
        this.style = (attrs == null || attrs.getStyleAttribute() == 0) ? defStyleAttr : attrs.getStyleAttribute();
        init();

        TypedArray typedArray = this.mContext.obtainStyledAttributes(attrs, R.styleable.COUITagView, defStyleAttr, 0);
        Drawable leftDrawable = typedArray.getDrawable(R.styleable.COUITagView_couiTagViewLeftDrawable);
        int leftDrawableTint = typedArray.getColor(R.styleable.COUITagView_couiTagViewLeftDrawableTint, 0);
        Drawable imageDrawable = typedArray.getDrawable(R.styleable.COUITagView_couiDrawableTagViewImage);
        int imageDrawableTint = typedArray.getColor(R.styleable.COUITagView_couiDrawableTagViewImageTint, 0);
        String tagText = typedArray.getString(R.styleable.COUITagView_couiTagViewText);
        int textColor = typedArray.getColor(
                R.styleable.COUITagView_couiTagViewTextColor,
                COUIContextUtil.getColor(this.mContext, R.color.coui_color_white)
        );
        int textSize = typedArray.getDimensionPixelSize(
                R.styleable.COUITagView_couiTagViewTextSize,
                context.getResources().getDimensionPixelSize(R.dimen.coui_default_tag_textsize)
        );

        if (leftDrawable != null) {
            if (leftDrawableTint != 0) {
                leftDrawable.setTint(leftDrawableTint);
            }
            this.leftImageView.setImageDrawable(leftDrawable);
        } else {
            this.leftImageView.setVisibility(View.GONE);
        }

        if (imageDrawable != null) {
            if (imageDrawableTint != 0) {
                imageDrawable.setTint(imageDrawableTint);
            }
            this.imageView.setImageDrawable(imageDrawable);
        } else {
            this.imageView.setVisibility(View.GONE);
        }

        if (tagText != null) {
            this.tagView.setText(tagText);
        } else {
            this.tagView.setVisibility(View.GONE);
        }
        this.tagView.setTextColor(textColor);
        this.tagView.setTextSize(0, textSize);
        typedArray.recycle();
    }

    public final void init() {
        View inflated = LayoutInflater.from(getContext()).inflate(R.layout.coui_tag_view_layout, (ViewGroup) this, true);
        this.tagBackground = (COUITagBackgroundView) inflated.findViewById(R.id.tagBackground);
        this.leftImageView = (ImageView) inflated.findViewById(R.id.tagLeftImageView);
        this.imageView = (ImageView) inflated.findViewById(R.id.tagImageView);
        this.tagView = (TextView) inflated.findViewById(R.id.tagTextView);
    }

    public final void setImageBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            throw new NullPointerException("bitmap");
        }
        this.imageView.setImageBitmap(bitmap);
        this.imageView.setVisibility(View.VISIBLE);
    }

    public final void setImageDrawable(Drawable drawable) {
        if (drawable == null) {
            throw new NullPointerException("drawable");
        }
        this.imageView.setImageDrawable(drawable);
        this.imageView.setVisibility(View.VISIBLE);
    }

    public final void setImageResoure(int resId) {
        this.imageView.setImageResource(resId);
        this.imageView.setVisibility(View.VISIBLE);
    }

    public final void setLeftImageBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            throw new NullPointerException("bitmap");
        }
        this.leftImageView.setImageBitmap(bitmap);
        this.leftImageView.setVisibility(View.VISIBLE);
    }

    public final void setLeftImageDrawable(Drawable drawable) {
        if (drawable == null) {
            throw new NullPointerException("drawable");
        }
        this.leftImageView.setImageDrawable(drawable);
        this.leftImageView.setVisibility(View.VISIBLE);
    }

    public final void setLeftImageResoure(int resId) {
        this.leftImageView.setImageResource(resId);
        this.leftImageView.setVisibility(View.VISIBLE);
    }

    public final void setTagText(String tagText) {
        if (tagText == null) {
            throw new NullPointerException("tagText");
        }
        this.tagView.setText(tagText);
        this.tagView.setVisibility(View.VISIBLE);
    }

    public final void setTagTextColor(int color) {
        this.tagView.setTextColor(color);
    }

    public final void setTagTextSize(int size) {
        this.tagView.setTextSize(0, size);
    }
}
