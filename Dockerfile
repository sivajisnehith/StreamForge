FROM eclipse-temurin:21-jre-jammy

# Install FFmpeg and FFprobe for video transcoding
RUN apt-get update && \
    apt-get install -y --no-install-recommends ffmpeg curl ca-certificates && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy compiled executable JAR
COPY target/*.jar app.jar

# Setup media processing directories and permissions
RUN mkdir -p uploads storage/processed && \
    useradd -m streamforge && \
    chown -R streamforge:streamforge /app

USER streamforge

EXPOSE 8080

# Run JVM optimized for container memory constraints
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
