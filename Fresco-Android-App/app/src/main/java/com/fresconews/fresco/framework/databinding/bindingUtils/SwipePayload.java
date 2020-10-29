package com.fresconews.fresco.framework.databinding.bindingUtils;

/**
 * Created by cumisnic on 10/14/15.
 */
public class SwipePayload {

    public final static int IDLE  = 0;
    public final static int RIGHT = 1;
    public final static int LEFT  = 2;

    public int   travel = IDLE;
    public float offset = 0f;

    public SwipePayload() {}

    public SwipePayload(int travel, float offset) {
        this.travel = travel;
        this.offset = offset;
    }
}
