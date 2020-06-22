package top.yzzblog.messagehelper.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class ObservableScrollView extends ScrollView {
    private boolean isTouching = false;

    private ScrollViewListener scrollViewListener = null;
    private Runnable cancelMoving = new Runnable() {
        @Override
        public void run() {
            if (isTouching) isTouching = false;
        }
    };

    public ObservableScrollView(Context context) {
        super(context);
    }

    public ObservableScrollView(Context context,
                                AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        isTouching = true;
        getHandler().removeCallbacks(cancelMoving);
        postDelayed(cancelMoving, 1700);
        return super.onTouchEvent(ev);
    }

    public void toBottom() {
        post(new Runnable() {
            @Override
            public void run() {
                fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public boolean isTouching() {
        return isTouching;
    }


    public interface ScrollViewListener {
        void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy);
    }

}