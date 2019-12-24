package com.example.demo.retrofit_rr;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CreateFileRequestData {
    @SerializedName("increase")
    @Expose
    private float increase;

    public CreateFileRequestData(float increase, String noise, int filter) {
        this.increase = increase;
        this.noise = noise;
        this.filter = filter;
    }

    @SerializedName("noise")
    @Expose
    private String noise;

    @SerializedName("filter")
    @Expose
    private int filter;

    public CreateFileRequestData() {
    }

    public float getIncrease() {
        return increase;
    }

    public void setIncrease(float increase) {
        this.increase = increase;
    }

    public String getNoise() {
        return noise;
    }

    public void setNoise(String noise) {
        this.noise = noise;
    }

    public int getFilter() {
        return filter;
    }

    public void setFilter(int filter) {
        this.filter = filter;
    }
}
