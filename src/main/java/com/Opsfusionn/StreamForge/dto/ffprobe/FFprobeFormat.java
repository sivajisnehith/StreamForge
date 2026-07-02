package com.Opsfusionn.StreamForge.dto.ffprobe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents format-level container metadata (duration, bitrate) returned by ffprobe.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FFprobeFormat {
    private String duration;

    @JsonProperty("bit_rate")
    private String bitRate;

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getBitRate() {
        return bitRate;
    }

    public void setBitRate(String bitRate) {
        this.bitRate = bitRate;
    }
}
