package com.Opsfusionn.StreamForge.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.Opsfusionn.StreamForge.dto.VideoMetadata;
import com.Opsfusionn.StreamForge.dto.ffprobe.FFprobeFormat;
import com.Opsfusionn.StreamForge.dto.ffprobe.FFprobeResponse;
import com.Opsfusionn.StreamForge.dto.ffprobe.FFprobeStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

/**
 * Unit tests verifying Jackson deserialization and mapping of FFprobe data to VideoMetadata DTO.
 */
public class FFprobeMapperTest {

    private final FFprobeMapper mapper = new FFprobeMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testMappingFromRawResponse() {
        FFprobeResponse response = new FFprobeResponse();
        
        FFprobeFormat format = new FFprobeFormat();
        format.setDuration("120.50");
        format.setBitRate("1500000");
        response.setFormat(format);

        FFprobeStream videoStream = new FFprobeStream();
        videoStream.setCodecType("video");
        videoStream.setCodecName("h264");
        videoStream.setWidth(1920);
        videoStream.setHeight(1080);

        FFprobeStream audioStream = new FFprobeStream();
        audioStream.setCodecType("audio");
        audioStream.setCodecName("aac");

        response.setStreams(List.of(videoStream, audioStream));

        VideoMetadata metadata = mapper.toVideoMetadata(response);

        assertEquals(120.50, metadata.getDuration());
        assertEquals(1500000L, metadata.getBitRate());
        assertEquals("h264", metadata.getVideoCodec());
        assertEquals("aac", metadata.getAudioCodec());
        assertEquals(1920, metadata.getWidth());
        assertEquals(1080, metadata.getHeight());
    }

    @Test
    public void testDeserializationAndMapping() throws Exception {
        String json = "{\n" +
                "  \"streams\": [\n" +
                "    {\n" +
                "      \"index\": 0,\n" +
                "      \"codec_name\": \"h264\",\n" +
                "      \"codec_long_name\": \"H.264 / AVC / MPEG-4 AVC / MPEG-4 part 10\",\n" +
                "      \"codec_type\": \"video\",\n" +
                "      \"width\": 1280,\n" +
                "      \"height\": 720\n" +
                "    },\n" +
                "    {\n" +
                "      \"index\": 1,\n" +
                "      \"codec_name\": \"aac\",\n" +
                "      \"codec_type\": \"audio\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"format\": {\n" +
                "    \"filename\": \"test.mp4\",\n" +
                "    \"duration\": \"60.000000\",\n" +
                "    \"bit_rate\": \"800000\"\n" +
                "  }\n" +
                "}";

        FFprobeResponse response = objectMapper.readValue(json, FFprobeResponse.class);
        VideoMetadata metadata = mapper.toVideoMetadata(response);

        assertEquals(60.0, metadata.getDuration());
        assertEquals(800000L, metadata.getBitRate());
        assertEquals("h264", metadata.getVideoCodec());
        assertEquals("aac", metadata.getAudioCodec());
        assertEquals(1280, metadata.getWidth());
        assertEquals(720, metadata.getHeight());
    }
}
