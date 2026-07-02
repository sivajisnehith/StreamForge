package com.Opsfusionn.StreamForge.dto.ffprobe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents individual stream data (video/audio) returned by ffprobe.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FFprobeStream {
    @JsonProperty("codec_name")
    private String codecName;

    @JsonProperty("codec_type")
    private String codecType;

    private Integer width;
    private Integer height;

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public String getCodecType() {
        return codecType;
    }

    public void setCodecType(String codecType) {
        this.codecType = codecType;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
}
