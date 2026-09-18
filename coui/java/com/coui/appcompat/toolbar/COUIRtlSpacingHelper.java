package com.coui.appcompat.toolbar;

public class COUIRtlSpacingHelper {
    public static final int UNDEFINED = Integer.MIN_VALUE;
    private int mLeft = 0;
    private int mRight = 0;
    private int mStart = Integer.MIN_VALUE;
    private int mEnd = Integer.MIN_VALUE;
    private int mExplicitLeft = 0;
    private int mExplicitRight = 0;
    private boolean mIsRtl = false;
    private boolean mIsRelative = false;

    public int getEnd() {
        return this.mIsRtl ? this.mLeft : this.mRight;
    }

    public int getLeft() {
        return this.mLeft;
    }

    public int getRight() {
        return this.mRight;
    }

    public int getStart() {
        return this.mIsRtl ? this.mRight : this.mLeft;
    }

    public void setAbsolute(int left, int right) {
        this.mIsRelative = false;
        if (left != Integer.MIN_VALUE) {
            this.mExplicitLeft = left;
            this.mLeft = left;
        }
        if (right != Integer.MIN_VALUE) {
            this.mExplicitRight = right;
            this.mRight = right;
        }
    }

    public void setDirection(boolean isRtl) {
        if (isRtl == this.mIsRtl) {
            return;
        }
        this.mIsRtl = isRtl;
        if (!this.mIsRelative) {
            this.mLeft = this.mExplicitLeft;
            this.mRight = this.mExplicitRight;
            return;
        }
        if (isRtl) {
            int left = this.mEnd;
            if (left == Integer.MIN_VALUE) {
                left = this.mExplicitLeft;
            }
            this.mLeft = left;
            int right = this.mStart;
            if (right == Integer.MIN_VALUE) {
                right = this.mExplicitRight;
            }
            this.mRight = right;
            return;
        }
        int left2 = this.mStart;
        if (left2 == Integer.MIN_VALUE) {
            left2 = this.mExplicitLeft;
        }
        this.mLeft = left2;
        int right2 = this.mEnd;
        if (right2 == Integer.MIN_VALUE) {
            right2 = this.mExplicitRight;
        }
        this.mRight = right2;
    }

    public void setRelative(int start, int end) {
        this.mStart = start;
        this.mEnd = end;
        this.mIsRelative = true;
        if (this.mIsRtl) {
            if (end != Integer.MIN_VALUE) {
                this.mLeft = end;
            }
            if (start != Integer.MIN_VALUE) {
                this.mRight = start;
                return;
            }
            return;
        }
        if (start != Integer.MIN_VALUE) {
            this.mLeft = start;
        }
        if (end != Integer.MIN_VALUE) {
            this.mRight = end;
        }
    }
}
