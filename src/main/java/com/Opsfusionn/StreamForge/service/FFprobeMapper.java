package com.Opsfusionn.StreamForge.service;

import com.Opsfusionn.StreamForge.dto.VideoMetadata;
import com.Opsfusionn.StreamForge.dto.ffprobe.FFprobeFormat;
import com.Opsfusionn.StreamForge.dto.ffprobe.FFprobeResponse;
import com.Opsfusionn.StreamForge.dto.ffprobe.FFprobeStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mapper responsible for translating raw FFprobe responses to domain/application DTOs.
 */
@Component
public class FFprobeMapper {
    private static final Logger logger = LoggerFactory.getLogger(FFprobeMapper.class);

    /**
     * Converts an {@link FFprobeResponse} to a unified {@link VideoMetadata} object.
     * Extracts video-specific metadata (resolution, codec) and audio-specific metadata
     * from their respective streams, along with overall format duration and bitrate.
     */
    public VideoMetadata toVideoMetadata(FFprobeResponse response) {
        if (response == null) {
            return new VideoMetadata();
        }

        VideoMetadata metadata = new VideoMetadata();
        
        FFprobeFormat format = response.getFormat();
        if (format != null) {
            if (format.getDuration() != null) {
                try {
                    metadata.setDuration(Double.parseDouble(format.getDuration()));
                } catch (NumberFormatException e) {
                    logger.warn("Failed to parse duration: {}", format.getDuration());
                }
            }
            if (format.getBitRate() != null) {
                try {
                    metadata.setBitRate(Long.parseLong(format.getBitRate()));
                } catch (NumberFormatException e) {
                    logger.warn("Failed to parse bit rate: {}", format.getBitRate());
                }
            }
        }

        if (response.getStreams() != null) {
            for (FFprobeStream stream : response.getStreams()) {
                if ("video".equalsIgnoreCase(stream.getCodecType())) {
                    metadata.setVideoCodec(stream.getCodecName());
                    if (stream.getWidth() != null) {
                        metadata.setWidth(stream.getWidth());
                    }
                    if (stream.getHeight() != null) {
                        metadata.setHeight(stream.getHeight());
                    }
                } else if ("audio".equalsIgnoreCase(stream.getCodecType())) {
                    metadata.setAudioCodec(stream.getCodecName());
                }
            }
        }

        return metadata;
    }
}
