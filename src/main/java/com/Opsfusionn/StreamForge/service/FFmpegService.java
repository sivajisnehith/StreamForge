package com.Opsfusionn.StreamForge.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FFmpegService {
    private static final Logger logger =
        LoggerFactory.getLogger(FFmpegService.class);



        private ProcessBuilder buildProcessBuilder(Path inputFile,Path outputDirectory) {
            Path playlistFile = outputDirectory.resolve("playlist.m3u8");
            return new ProcessBuilder(
                "ffmpeg",
                "-y",   //overwrite existing files
                "-i",   //input video 
                inputFile.toString(),   
                "-c:v",
                "libx264",  //Encode video
                "-c:a",
                "aac",   //Encode audio
                "-hls_time",
                "6",  //6seconds per frame
                "-hls_playlist_type",   //video on demand
                "vod",
                "-hls_segment_filename",  //name segment files
                outputDirectory.resolve("segment%03d.ts").toString(),
                playlistFile.toString()  //generate a playlist now 
            );
        }

        private void executeProcess(ProcessBuilder processBuilder) throws IOException{
            processBuilder.inheritIO();
            //logger.info("Starting FFmpeg for {}", inputFile);

            Process process = processBuilder.start();

            logger.info("FFmpeg completed successfully.");
            int exitCode;

            try {
                exitCode = process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("FFmpeg execution was interrupted.", e);
            }

            if (exitCode != 0) {
                throw new IOException("FFmpeg failed with exit code: " + exitCode);
            }
        }
        public void processVideo(Path inputFile, Path outputDirectory) throws IOException {

            if (!Files.exists(inputFile)) {
                throw new IOException("Input video not found: " + inputFile);
            }
            ProcessBuilder processBuilder = buildProcessBuilder(inputFile, outputDirectory);
            executeProcess(processBuilder);
        }
}
