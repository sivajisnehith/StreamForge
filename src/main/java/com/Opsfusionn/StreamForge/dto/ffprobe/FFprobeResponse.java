package com.Opsfusionn.StreamForge.dto.ffprobe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Root response object mirroring the JSON output from ffprobe -show_format -show_streams.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FFprobeResponse {
    private List<FFprobeStream> streams;
    private FFprobeFormat format;

    public List<FFprobeStream> getStreams() {
        return streams;
    }

    public void setStreams(List<FFprobeStream> streams) {
        this.streams = streams;
    }

    public FFprobeFormat getFormat() {
        return format;
    }

    public void setFormat(FFprobeFormat format) {
        this.format = format;
    }
}
