package com.Opsfusionn.StreamForge.dto;

public class VideoMetadata {
    private double duration;

    private int width;

    private int height;

    private String videoCodec;

    private String audioCodec;

    private long bitRate;

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setVideoCodec(String videoCodec) {
        this.videoCodec = videoCodec;
    }

    public void setAudioCodec(String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public void setBitRate(long bitRate) {
        this.bitRate = bitRate;
    }

    public double getDuration() {
        return duration;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public long getBitRate() {
        return bitRate;
    }
}