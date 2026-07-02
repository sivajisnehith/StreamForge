package com.Opsfusionn.StreamForge.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Opsfusionn.StreamForge.config.VideoRenditions;
import com.Opsfusionn.StreamForge.model.VideoRendition;

/**
 * Service responsible for generating HLS master playlists.
 */
@Service
public class MasterPlaylistService {

    private static final Logger logger = LoggerFactory.getLogger(MasterPlaylistService.class);

    /**
     * Generates a master.m3u8 playlist file listing all configured video renditions.
     *
     * @param outputDirectory The directory where master.m3u8 should be created.
     * @throws IOException If writing the playlist file fails.
     */
    public void generateMasterPlaylist(Path outputDirectory) throws IOException {
        logger.info("Generating master.m3u8 in directory: {}", outputDirectory);

        List<VideoRendition> renditions = VideoRenditions.getSupportedRenditions();
        StringBuilder playlistBuilder = new StringBuilder();
        playlistBuilder.append("#EXTM3U\n\n");

        for (VideoRendition rendition : renditions) {
            long bandwidth = parseBitrateToBandwidth(rendition.getVideoBitrate());
            playlistBuilder.append("#EXT-X-STREAM-INF:BANDWIDTH=")
                    .append(bandwidth)
                    .append(",RESOLUTION=")
                    .append(rendition.getWidth())
                    .append("x")
                    .append(rendition.getHeight())
                    .append("\n")
                    .append(rendition.getName())
                    .append("/playlist.m3u8\n\n");
        }

        Path masterPlaylistPath = outputDirectory.resolve("master.m3u8");
        Files.writeString(masterPlaylistPath, playlistBuilder.toString().trim() + "\n");
        logger.info("Successfully wrote master playlist to {}", masterPlaylistPath);
    }

    /**
     * Parses the video bitrate string (e.g. "5000k" or "2.8m") into standard numeric bandwidth bps.
     *
     * @param videoBitrate The bitrate representation.
     * @return The bps numeric value.
     */
    private long parseBitrateToBandwidth(String videoBitrate) {
        if (videoBitrate == null || videoBitrate.isBlank()) {
            return 0;
        }
        String clean = videoBitrate.trim().toLowerCase();
        if (clean.endsWith("k")) {
            double value = Double.parseDouble(clean.substring(0, clean.length() - 1));
            return (long) (value * 1000);
        } else if (clean.endsWith("m")) {
            double value = Double.parseDouble(clean.substring(0, clean.length() - 1));
            return (long) (value * 1000000);
        } else {
            return (long) Double.parseDouble(clean);
        }
    }
}
