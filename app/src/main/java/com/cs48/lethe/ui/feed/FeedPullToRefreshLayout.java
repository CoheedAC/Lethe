package com.cs48.lethe.ui.feed;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import com.cs48.lethe.R;

/**
 * A Custom SwipeRefreshLayout for the Feed Fragment which
 * allows the grid to be scrolled vertically.
 */
public class FeedPullToRefreshLayout extends SwipeRefreshLayout {

    /**
     * Simple constructor to use when creating a SwipeRefreshLayout from code.
     *
     * @param context Interface to global information about an application environment
     */
    public FeedPullToRefreshLayout(Context context) {
        super(context);
    }

    /**
     * Constructor that is called when inflating SwipeRefreshLayout from XML.
     *
     * @param context Interface to global information about an application environment
     * @param attrs Attributes of the layout
     */
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
