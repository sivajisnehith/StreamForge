package com.Opsfusionn.StreamForge.service;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Opsfusionn.StreamForge.dto.VideoMetadata;
import com.Opsfusionn.StreamForge.dto.ffprobe.FFprobeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service executing ffprobe and deserializing its output to extract video metadata.
 */
@Service
public class FFProbeService {
    private static final Logger logger = LoggerFactory.getLogger(FFProbeService.class);
    
    private final ObjectMapper objectMapper;
    private final FFprobeMapper ffprobeMapper;

    public FFProbeService(ObjectMapper objectMapper, FFprobeMapper ffprobeMapper) {
        this.objectMapper = objectMapper;
        this.ffprobeMapper = ffprobeMapper;
    }

    /**
     * Executes ffprobe on the input file, parses the JSON result, and maps it to a VideoMetadata.
     */
    public VideoMetadata extractMetadata(Path inputFile) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffprobe",
                "-v",
                "quiet",
                "-print_format",
                "json",
                "-show_format",
                "-show_streams",
                inputFile.toString()
        );

        Process process = processBuilder.start();

        byte[] outputBytes = process.getInputStream().readAllBytes();
        String json = new String(outputBytes);

        logger.debug("FFprobe Raw JSON Output:\n{}", json);

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("ffprobe execution interrupted.", e);
        }

        if (exitCode != 0) {
            throw new IOException("FFprobe failed with exit code: " + exitCode);
        }

        // Deserialize JSON to DTO using Jackson ObjectMapper
        FFprobeResponse ffprobeResponse = objectMapper.readValue(outputBytes, FFprobeResponse.class);

        // Convert the raw response DTO to domain-friendly VideoMetadata
        return ffprobeMapper.toVideoMetadata(ffprobeResponse);
    }
}
