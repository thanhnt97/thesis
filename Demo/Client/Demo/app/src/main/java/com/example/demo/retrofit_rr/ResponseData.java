package com.example.demo.retrofit_rr;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseData {
    @SerializedName("labels")
    @Expose
    private String labels[];


    @SerializedName("start_times")
    @Expose
    private float startTimes[];


    @SerializedName("end_times")
    @Expose
    private float endTimes[];


    @SerializedName("url")
    @Expose
    private String url;

    private String icrease;
    private String noise;
    private String filter;

    public String getIcrease() {
        return icrease;
    }

    public void setIcrease(String icrease) {
        this.icrease = icrease;
    }

    public String getNoise() {
        return noise;
    }

    public void setNoise(String noise) {
        this.noise = noise;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public ResponseData(String[] labels, float[] startTimes, float[] endTimes, String url) {
        this.labels = labels;
        this.startTimes = startTimes;
        this.endTimes = endTimes;
        this.url = url;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public float[] getStartTimes() {
        return startTimes;
    }

    public void setStartTimes(float[] startTimes) {
        this.startTimes = startTimes;
    }

    public float[] getEndTimes() {
        return endTimes;
    }

    public void setEndTimes(float[] endTimes) {
        this.endTimes = endTimes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public float getDuration() {
        return endTimes[endTimes.length-1] - startTimes[0];
    }

    public ResponseData() {
    }
}
