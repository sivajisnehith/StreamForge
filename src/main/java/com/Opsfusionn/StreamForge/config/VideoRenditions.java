package com.Opsfusionn.StreamForge.config;

import java.util.List;

import com.Opsfusionn.StreamForge.model.VideoRendition;

public final class VideoRenditions {

    private VideoRenditions() {
        // Prevent instantiation
    }

    private static final List<VideoRendition> RENDITIONS = List.of(

            new VideoRendition(
                    "1080",
                    1920,
                    1080,
                    "5000k"
            ),

            new VideoRendition(
                    "720",
                    1280,
                    720,
                    "2800k"
            ),

            new VideoRendition(
                    "480",
                    854,
                    480,
                    "1400k"
            )
    );

    public static List<VideoRendition> getSupportedRenditions() {
        return RENDITIONS;
    }
}