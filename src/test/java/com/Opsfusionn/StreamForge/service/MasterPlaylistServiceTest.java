package com.Opsfusionn.StreamForge.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests verifying dynamic generation of HLS master playlist by MasterPlaylistService.
 */
public class MasterPlaylistServiceTest {

    private final MasterPlaylistService masterPlaylistService = new MasterPlaylistService();

    @Test
    public void testGenerateMasterPlaylist(@TempDir Path tempDir) throws IOException {
        masterPlaylistService.generateMasterPlaylist(tempDir);

        Path masterPlaylistFile = tempDir.resolve("master.m3u8");
        assertTrue(Files.exists(masterPlaylistFile));

        String content = Files.readString(masterPlaylistFile);
        System.out.println("--- GENERATED PLAYLIST ---");
        System.out.println(content);
        System.out.println("--------------------------");

        // Verify base m3u8 format marker
        assertTrue(content.contains("#EXTM3U"));

        // Verify 1080p rendition info and file mapping
        assertTrue(content.contains("#EXT-X-STREAM-INF:BANDWIDTH=5000000,RESOLUTION=1920x1080"));
        assertTrue(content.contains("1080/playlist.m3u8"));

        // Verify 720p rendition info and file mapping
        assertTrue(content.contains("#EXT-X-STREAM-INF:BANDWIDTH=2800000,RESOLUTION=1280x720"));
        assertTrue(content.contains("720/playlist.m3u8"));

        // Verify 480p rendition info and file mapping
        assertTrue(content.contains("#EXT-X-STREAM-INF:BANDWIDTH=1400000,RESOLUTION=854x480"));
        assertTrue(content.contains("480/playlist.m3u8"));
    }
}
