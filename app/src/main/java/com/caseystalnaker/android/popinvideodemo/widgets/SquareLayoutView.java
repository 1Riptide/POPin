package com.caseystalnaker.android.popinvideodemo.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by Casey on 9/9/16.
 */

public class SquareLayoutView extends RelativeLayout {

    public SquareLayoutView(Context context) {
        super(context);
    }

    public SquareLayoutView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    public SquareLayoutView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        final int size = width > height ? height : width;
        setMeasuredDimension(size, size);
    }
}