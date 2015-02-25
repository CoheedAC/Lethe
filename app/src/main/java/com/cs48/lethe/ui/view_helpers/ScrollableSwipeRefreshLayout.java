package com.cs48.lethe.ui.view_helpers;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import com.cs48.lethe.R;

/**
 * Created by maxkohne on 2/24/15.
 */
public class ScrollableSwipeRefreshLayout extends SwipeRefreshLayout {

    public ScrollableSwipeRefreshLayout(Context context) {
        super(context);
    }

    public ScrollableSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canChildScrollUp() {
        // Condition to check scrollview reached at top while scrolling
        return findViewById(R.id.feedGridView).canScrollVertically(-1);
    }
}
