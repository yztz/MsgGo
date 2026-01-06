/*
 * Copyright (C) 2026 yztz
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package top.yztz.msggo.widgets;

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