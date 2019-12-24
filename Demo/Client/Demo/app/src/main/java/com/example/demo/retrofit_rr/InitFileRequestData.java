package com.example.demo.retrofit_rr;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InitFileRequestData {
    @SerializedName("random_seed")
    @Expose
    private int randomSeed;

    @SerializedName("file_number")
    @Expose
    private int fileNumber;

    public InitFileRequestData(int randomSeed, int fileNumber) {
        this.randomSeed = randomSeed;
        this.fileNumber = fileNumber;
    }

    public InitFileRequestData() {
    }

    public int getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(int randomSeed) {
        this.randomSeed = randomSeed;
    }

    public int getFileNumber() {
        return fileNumber;
    }

    public void setFileNumber(int fileNumber) {
        this.fileNumber = fileNumber;
    }
}
