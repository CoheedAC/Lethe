package com.cs48.lethe.ui.miscellaneous;

/**
 * Created by maxkohne on 2/24/15.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * ScrollViewの中のGridViewでも高さを可変にする<br>
 * http://stackoverflow.com/questions/8481844/gridview-height-gets-cut
 */

public class PullToRefreshGridView extends GridView {

    boolean expanded = false;

    public PullToRefreshGridView(Context context) {
        super(context);
    }

    public PullToRefreshGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshGridView(Context context, AttributeSet attrs,
                                 int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Measure the view and its content to determine the measured width and
     * the measured height.
     *
     * @param widthMeasureSpec horizontal space requirements as imposed by the parent.
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // HACK! TAKE THAT ANDROID!
        if (isExpanded()) {
            // Calculate entire height by providing a very large height hint.
            // View.MEASURED_SIZE_MASK represents the largest height possible.
            int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,
                    MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);

            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = getMeasuredHeight();
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}