package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ryan on 6/21/2016.
 */
public class NetworkSettings {
    @SerializedName("radius")
    private double radius;

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
