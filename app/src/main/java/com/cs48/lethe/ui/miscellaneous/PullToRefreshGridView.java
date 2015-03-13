package com.cs48.lethe.ui.miscellaneous;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * http://stackoverflow.com/questions/8481844/gridview-height-gets-cut
 */
public class PullToRefreshGridView extends GridView {

    boolean expanded = false;

    /**
     *
     * @param context Interface to global information about an application environment
     */
    public PullToRefreshGridView(Context context) {
        super(context);
    }

    /**
     *
     * @param context Interface to global information about an application environment
     * @param attrs Attributes of the grid view
     */
    public PullToRefreshGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     *
     * @param context Interface to global information about an application environment
     * @param attrs Attributes of the grid view
     * @param defStyle Style of the attributes
     */
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
