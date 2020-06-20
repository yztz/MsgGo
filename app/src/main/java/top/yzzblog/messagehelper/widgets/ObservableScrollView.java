package top.yzzblog.messagehelper.widgets;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import androidx.annotation.NonNull;

public class ObservableScrollView extends ScrollView {
    private boolean isMoving = false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

        }
    };

    private ScrollViewListener scrollViewListener = null;

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
    protected void onScrollChanged(int x, final int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
        isMoving = true;

        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (y == getScrollY()) {
                    isMoving = false;
                }
            }
        }, 3000);
    }


    public void toBottom() {
        post(new Runnable() {
            @Override
            public void run() {
                fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public boolean isMoving() {
        return isMoving;
    }


    public interface ScrollViewListener {
        void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy);
    }

}