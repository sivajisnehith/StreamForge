FROM eclipse-temurin:21-jdk

# Install FFmpeg + FFprobe
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/*.jar app.jar

RUN mkdir -p uploads storage/processed

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]