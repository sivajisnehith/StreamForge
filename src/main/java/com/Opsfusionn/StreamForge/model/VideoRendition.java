package com.Opsfusionn.StreamForge.model;

public class VideoRendition {
    private String name;

    private int width;

    private int height;

    private String videoBitrate;

    public VideoRendition(String name, int width, int height, String videoBitrate) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.videoBitrate = videoBitrate;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getVideoBitrate() {
        return videoBitrate;
    }


}
