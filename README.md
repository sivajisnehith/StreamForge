# 🎬 StreamForge

### Distributed & Scalable Asynchronous Video Processing Platform
*A production-ready Spring Boot backend for distributed video ingestion, parallel HLS transcoding, metadata extraction, and cloud-native object storage.*

---

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=for-the-badge&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge&logo=postgresql)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-D8F6FF?style=for-the-badge&logo=rabbitmq)
![MinIO](https://img.shields.io/badge/MinIO-Object_Storage-c72c48?style=for-the-badge&logo=minio)
![FFmpeg](https://img.shields.io/badge/FFmpeg-5.x-007808?style=for-the-badge&logo=ffmpeg)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker)

**Distributed • Scalable • Fault-Tolerant • Cloud-Native**

</div>

---

## 📖 Project Overview

**StreamForge** is a scalable, distributed video transcoding and processing platform. Instead of blocking HTTP requests during high-overhead video transcoding operations, StreamForge decouples the upload lifecycle from the processing lifecycle using an asynchronous messaging architecture.

Original uploads are saved to **MinIO** object storage. The upload triggers a message to **RabbitMQ**, which distributes the work to background consumers. The worker nodes pull raw video chunks, process them into multiple adaptive HLS stream renditions (1080p, 720p, 480p) using **FFmpeg**, generate master playlists, capture poster thumbnails, extract file metadata using **FFprobe**, and upload the final artifacts back to MinIO's processed storage bucket.

---

## 🏗 System Architecture & Pipeline Flow

The platform separates the upload API from processing workers, executing operations in sandboxed temporary spaces for strict resource isolation:

```
                      ┌────────────────────────────────────────┐
                      │                 Client                 │
                      └──────────────────┬─────────────────────┘
                                         │ POST /api/files/upload
                                         ▼
                      ┌────────────────────────────────────────┐
                      │       Spring Boot API Controller       │
                      └──────────────────┬─────────────────────┘
                                         │
                         ┌───────────────┴───────────────┐
                         │ Save Entity                   │ Upload Original File
                         ▼ (Status: UPLOADED)            ▼
             ┌───────────────────────┐       ┌───────────────────────┐
             │      PostgreSQL       │       │    MinIO Storage      │
             │       Database        │       │  (originals bucket)   │
             └───────────────────────┘       └───────────────────────┘
                         │                               ▲
                         │ Send Message                  │
                         ▼                               │
             ┌───────────────────────┐                   │
             │       RabbitMQ        │                   │
             │     Message Broker    │                   │
             └───────────┬───────────┘                   │
                         │ Consume Job                   │
                         ▼                               │
             ┌───────────────────────┐                   │
             │ VideoProcessingWorker │                   │
             └───────────┬───────────┘                   │
                         │                               │
                         ├───────────────────────────────┤ (1. Download Original Video)
                         │                               │
                         ▼ (2. Sandbox FFmpeg Output)    │
             ┌───────────────────────┐                   │
             │  Temp Dir (OS Sand)   ├───────────────────┘
             └───────────┬───────────┘
                         │
                         ├─► 3. Generate HLS Renditions (1080p, 720p, 480p)
                         ├─► 4. Generate master.m3u8 index playlist
                         ├─► 5. Generate video thumbnail.jpg
                         │
                         ▼ (6. Upload processed assets)
             ┌───────────────────────┐
             │     MinIO Storage     │
             │  (processed bucket)   │
             └───────────┬───────────┘
                         │
                         ├─► 7. Run FFprobe to extract metadata
                         ├─► 8. Save Duration, Codecs & Resolution to PostgreSQL
                         │
                         ▼ (9. Completion)
             ┌───────────────────────┐
             │      PostgreSQL       │
             │  (Status: COMPLETED)  │
             └───────────────────────┘
```

---

## ✨ Core Features

*   **Asynchronous Processing**: Immediate HTTP returns on video upload, delegating encoding tasks to RabbitMQ background listeners.
*   **MinIO Object Storage Integration**: Decoupled cloud-native storage utilizing two distinct buckets (`originals` and `processed`).
*   **Multi-Resolution HLS Packaging**: Automatically transcodes input videos to HLS-compliant directories (1080p, 720p, and 480p playlists accompanied by `.ts` segments) with a top-level `master.m3u8` index file.
*   **Automatic Poster Thumbnailing**: Grabs high-quality poster frames using FFmpeg to display in frontends.
*   **FFprobe Metadata Extraction**: Extracts advanced stream metadata including frame rates, audio/video codecs (AAC, H.264), durations, and resolutions, persisting them to PostgreSQL.
*   **Crash Resilient Workers**: Uses Spring AMQP's auto-ack behavior to ensure uncompleted jobs are automatically requeued in RabbitMQ and reset to `PENDING` in PostgreSQL if a worker crashes.
*   **Sandboxed OS Workspaces**: Processes video segments within isolated operating system temp directories, ensuring zero interference between concurrent jobs and automatic cleanup in `finally` blocks.

---

## 🛠 Tech Stack

*   **Core Framework**: Java 21, Spring Boot 3.x, Spring Web (REST Controllers), Spring Data JPA.
*   **Database**: PostgreSQL 17 (durable metadata storage).
*   **Messaging**: RabbitMQ (AMQP protocol message broker).
*   **Media Processing**: FFmpeg 8.x (transcoding engine), FFprobe (analyzing streams).
*   **Storage Provider**: MinIO (S3-compatible API).
*   **Testing**: JUnit 5, Mockito.
*   **Deployment**: Docker & Docker Compose (containerized local environment support).

---

## 📂 Project Structure

```
StreamForge
├── .mvn/                              # Maven Wrapper files
├── .vscode/                           # IDE configurations (auto build config)
├── src/
│   ├── main/
│   │   ├── java/com/Opsfusionn/StreamForge/
│   │   │   ├── config/                # Spring configuration (MinIO & RabbitMQ beans)
│   │   │   ├── controller/            # REST API endpoints (uploads & queries)
│   │   │   ├── dto/                   # Data Transfer Objects (HLS mapping & api responses)
│   │   │   ├── exception/             # Centralized exception handling
│   │   │   ├── messaging/             # RabbitMQ consumer, producer & error handler
│   │   │   ├── model/                 # JPA database entities & enums
│   │   │   ├── repository/            # PostgreSQL repositories
│   │   │   └── service/               # Core business services (FFmpeg, MinIO, etc.)
│   │   └── resources/
│   │       └── application.properties # Server, MinIO, and Database configurations
│   └── test/                          # JUnit integrations & unit test suites
├── docker-compose.yml                 # Local cluster runner (Postgres, RabbitMQ, MinIO)
├── pom.xml                            # Maven project definition
└── README.md                          # Project documentation
```

---

## 🔄 Video Lifecycle States

```
 [ UPLOADED ] ──────► [ PENDING ] ──────► [ PROCESSING ] ──────► [ COMPLETED ]
                                               │
                                               └───────────────► [ FAILED ]
```

*   `UPLOADED`: The video metadata is created in PostgreSQL and the original file is stored in MinIO.
*   `PENDING`: The background worker consumes the queue job and acknowledges receipt.
*   `PROCESSING`: FFmpeg is actively transcoding segments, generating HLS manifests, and capturing thumbnails.
*   `COMPLETED`: The transcoded streams are successfully uploaded to the MinIO `processed` bucket, metadata is persisted, and local temp workspaces are cleaned.
*   `FAILED`: An exception occurred during transcoding or networking; status is updated by the global error handler.

---

## 🚀 Getting Started

### Prerequisites
*   Docker & Docker Compose installed.
*   Java Development Kit (JDK) 21 installed.
*   Maven 3.x installed.

### Setup and Running

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/your-username/StreamForge.git
    cd StreamForge
    ```

2.  **Spin up the infrastructure** (PostgreSQL, RabbitMQ, and MinIO):
    ```bash
    docker-compose up -d
    ```

3.  **Run the Spring Boot application**:
    ```bash
    ./mvnw spring-boot:run
    ```

4.  **Upload a Video**:
    Post a video file to the ingestion endpoint:
    ```bash
    curl -X POST -F "file=@/path/to/video.mp4" http://localhost:8080/api/files/upload
    ```

5.  **Verify Processing**:
    You can query the status of the video using the API endpoint:
    ```bash
    curl http://localhost:8080/api/videos/{video_id}
    ```

---

## 🤝 Contributing

Contributions, suggestions, and improvements are welcome. Feel free to fork the repository and submit a pull request.

---

## 📄 License

This project is licensed under the MIT License.

---

<div align="center">

### ⭐ If you like this project, consider giving it a star!

**Built with ❤️ using Java, Spring Boot, & FFmpeg**

</div>
