package com.cs48.lethe.ui.feed;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import com.cs48.lethe.R;

/**
 * Created by maxkohne on 2/24/15.
 */
public class FeedPullToRefreshLayout extends SwipeRefreshLayout {

    public FeedPullToRefreshLayout(Context context) {
        super(context);
    }

    public FeedPullToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @return Whether it is possible for the child view of this layout to scroll up.
     */
    @Override
    public boolean canChildScrollUp() {
        // Condition to check scrollview reached at top while scrolling
        return findViewById(R.id.feedGridView).canScrollVertically(-1);
    }
}
