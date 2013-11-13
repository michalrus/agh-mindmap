package edu.agh.mindmap.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

public class HorizontalScrollViewWithPropagation extends HorizontalScrollView {

    public ScrollView inner;

    public HorizontalScrollViewWithPropagation(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public HorizontalScrollViewWithPropagation(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalScrollViewWithPropagation(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event) | inner.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return super.onInterceptTouchEvent(event) | inner.onTouchEvent(event);
    }

}
