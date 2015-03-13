package com.cs48.lethe.ui.miscellaneous;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Detects left and right swipes across a view as well as single taps.
 */
public abstract class OnHorizontalSwipeListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    /**
     * Constructor that creates a gesture detector
     *
     * @param context Interface to global information about an application environment
     */
    public OnHorizontalSwipeListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public abstract void onSwipeLeft();
    public abstract void onSwipeRight();
    public abstract void onSingleTap();

    /**
     * Called when a touch event is dispatched to a view. This allows
     * listeners to get a chance to respond before the target view.
     *
     * @param v The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about the event.
     *
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        /**
         * Notified when a tap occurs with the down MotionEvent that triggered it.
         * This will be triggered immediately for every down event. All other events
         * should be preceded by this.
         *
         * @param e The down motion event.
         *
         * @return true
         */
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        /**
         * Notified of a fling event when it occurs with the initial on down
         * MotionEvent and the matching up MotionEvent. The calculated velocity
         * is supplied along the x and y axis in pixels per second.
         *
         * @param e1 The first down motion event that started the fling.
         * @param e2 The move motion event that triggered the current onFling.
         * @param velocityX The velocity of this fling measured in pixels per second along the x axis.
         * @param velocityY The velocity of this fling measured in pixels per second along the y axis.
         *
         * @return true if the event is consumed, else false
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0)
                    onSwipeRight();
                else
                    onSwipeLeft();
                return true;
            }
            return false;
        }

        /**
         * Notified when a tap occurs with the up MotionEvent that triggered it.
         *
         * @param event The up motion event that completed the first tap

         * @return true if the event is consumed, else false
         */
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            onSingleTap();
            return true;
        }
    }
}