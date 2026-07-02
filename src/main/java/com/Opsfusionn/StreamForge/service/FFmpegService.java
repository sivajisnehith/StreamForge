package com.Opsfusionn.StreamForge.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Opsfusionn.StreamForge.config.VideoRenditions;
import com.Opsfusionn.StreamForge.model.VideoRendition;

@Service
public class FFmpegService {
    private static final Logger logger =
        LoggerFactory.getLogger(FFmpegService.class);



        private ProcessBuilder buildHlsProcessBuilder(
                Path inputFile,
                Path outputDirectory,
                VideoRendition rendition) throws IOException {
            Path renditionDirectory =
                    outputDirectory.resolve(rendition.getName());

            Files.createDirectories(renditionDirectory);

            Path playlistFile =
                    renditionDirectory.resolve("playlist.m3u8");

            return new ProcessBuilder(
                "ffmpeg",
                "-y",   //overwrite existing files
                "-i",   //input video 
                inputFile.toString(),   
                "-c:v",
                "libx264",  //Encode video
                "-vf",
                "scale=" + rendition.getWidth() + ":" + rendition.getHeight(),
                "-b:v",
                rendition.getVideoBitrate(),
                "-c:a",
                "aac",   //Encode audio
                "-hls_time",
                "6",  //6seconds per frame
                "-hls_playlist_type",   //video on demand
                "vod",
                "-hls_segment_filename",  //name segment files
                renditionDirectory.resolve("segment%03d.ts").toString(),
                playlistFile.toString()  //generate a playlist now 
            );
        }

        public void generateHls(Path inputFile, Path outputDirectory) throws IOException {
            for (VideoRendition rendition : VideoRenditions.getSupportedRenditions()) {
                generateHlsForRendition(inputFile, outputDirectory, rendition);
            }
        }

        private void generateHlsForRendition(
                Path inputFile,
                Path outputDirectory,
                VideoRendition rendition)
                throws IOException {
            ProcessBuilder builder =
                    buildHlsProcessBuilder(
                            inputFile,
                            outputDirectory,
                            rendition);

            executeProcess(builder);
        }
        public void generateThumbnail(Path inputFile,Path outputDirectory) throws IOException{
            ProcessBuilder processBuilder = buildThumbnailProcessBuilder(inputFile, outputDirectory);
            executeProcess(processBuilder);
        }
        private ProcessBuilder buildThumbnailProcessBuilder(Path inputFile,Path outputDirectory){
            Path thumbnailFile = outputDirectory.resolve("thumbnail.jpg");
            return new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i",
                    inputFile.toString(),
                    "-ss",
                    "00:00:01",
                    "-frames:v",
                    "1",
                    "-update",
                    "1",
                    thumbnailFile.toString()
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
            generateHls(inputFile, outputDirectory);
            generateThumbnail(inputFile, outputDirectory);
        }

        
}
