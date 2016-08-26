package com.mapbox.mapboxsdk.gesture;


import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.mapbox.mapboxsdk.maps.MapView;

import java.util.HashMap;

public class GesturesCatcher {
    private final MapView mapView;
    HashMap<Integer, MotionEvent> motionEvents = new HashMap<Integer, MotionEvent>();
    Context context;
    boolean isCatched;
//    EventBus bus;
    int touchThreshold;

    public GesturesCatcher(Context context, MapView mapView) {
        this.context = context;
        this.touchThreshold = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mapView = mapView;
    }

    public boolean isEventCatched(MotionEvent event) {
        int action = event.getActionMasked();
        int id = event.getPointerId(event.getActionIndex());
        isCatched = false;
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            motionEvents.put(id, MotionEvent.obtain(event));
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            motionEvents.remove(id);
        } else if (action == MotionEvent.ACTION_MOVE) {
            isCatched = isDetectingMoveEvent(event);
        }

        return isCatched;
    }

    private boolean isDetectingMoveEvent(MotionEvent event) {
        for (int index = 0; index < event.getPointerCount(); index++) {
            int id = event.getPointerId(index);
            if (compareEvents(motionEvents.get(id), event, index, touchThreshold)) {
                return true;
            }
        }
        return false;
    }

    private boolean compareEvents(MotionEvent down, MotionEvent move, int motionIndex, int threshold) {
        int offsetX = (int) Math.abs(move.getX(motionIndex) - down.getX(motionIndex));
        int offsetY = (int) Math.abs(move.getY(motionIndex) - down.getY(motionIndex));
        if (offsetX > threshold || offsetY > threshold) {
            return true;
        }
        return false;
    }


    public void dispatchMotionEvents() {
        if (isCatched) {
            for (MotionEvent event : motionEvents.values()) {
                mapView.onDispatchTouchEvent(event);
            }
            isCatched = false;
            motionEvents.clear();
        }
    }
}
