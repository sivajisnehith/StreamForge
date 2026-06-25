<div align="center">

# 🎬 StreamForge

### Distributed Video Processing Platform

*A scalable backend system for asynchronous video processing inspired by modern streaming platforms.*

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis)
![FFmpeg](https://img.shields.io/badge/FFmpeg-007808?style=for-the-badge&logo=ffmpeg)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker)
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana)

---

**Reliable • Scalable • Fault-Tolerant • Distributed**

</div>

---

# 📖 Overview

StreamForge is a distributed video processing platform that simulates the backend architecture used by modern video streaming services.

Instead of processing uploaded videos immediately, StreamForge creates background processing jobs and distributes them to dedicated worker services through a message queue. Workers transcode videos into multiple streaming-ready resolutions using FFmpeg while ensuring reliability, scalability, and fault tolerance.

The project focuses on backend engineering concepts such as distributed systems, asynchronous processing, worker orchestration, fault recovery, monitoring, and horizontal scalability.

---

# ✨ Features

- 🎥 Video Upload API
- 📂 Background Video Processing
- 📨 RabbitMQ Job Queue
- 👷 Distributed Worker Services
- 🎬 FFmpeg Video Transcoding
- 📺 Multiple Resolution Generation (1080p / 720p / 480p)
- 🔄 Automatic Retry Mechanism
- 💥 Worker Crash Recovery
- ✅ Exactly-Once Job Processing
- 📊 Processing Metrics
- 📈 Queue Monitoring
- 📦 Dockerized Deployment
- 📝 Structured Logging
- ❤️ Health Checks
- ⚡ Horizontal Scalability

---

# 🛠 Tech Stack

## Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Validation
- Maven

## Database

- PostgreSQL

## Message Queue

- RabbitMQ

## Video Processing

- FFmpeg

## Cache

- Redis

## Storage

- MinIO
- Local Storage (Development)

## Monitoring

- Spring Boot Actuator
- Micrometer
- Prometheus
- Grafana

## DevOps

- Docker
- Docker Compose
- GitHub Actions

## Testing

- JUnit
- Mockito

---

# 🏗 Architecture

```
                Upload Video
                     │
                     ▼
             Spring Boot API
                     │
                     ▼
            Store Original Video
                     │
                     ▼
              PostgreSQL Database
                     │
             Create Processing Job
                     │
                     ▼
                 RabbitMQ Queue
                     │
      ┌──────────────┼──────────────┐
      ▼              ▼              ▼
   Worker 1      Worker 2      Worker 3
      │              │              │
      └──────────────┼──────────────┘
                     ▼
                  FFmpeg
                     │
     ┌───────────────┼───────────────┐
     ▼               ▼               ▼
 1080p Video     720p Video      480p Video
                     │
                     ▼
              Update Job Status
                     │
                     ▼
                Processing Complete
```

---

# 📂 Project Structure

```
StreamForge
│
├── api/
├── worker/
├── common/
├── docs/
├── docker/
├── scripts/
├── uploads/
├── processed/
└── README.md
```

---

# 🔄 Job Lifecycle

```
Pending
    │
    ▼
Processing
    │
    ├──────────────► Failed
    │                    │
    │                    ▼
    │                 Retry
    │                    │
    ▼                    │
Completed ◄──────────────┘
```

---

# 📊 Metrics

The platform tracks important operational metrics including:

- Total Processed Videos
- Failed Jobs
- Successful Jobs
- Retry Count
- Queue Waiting Time
- Average Processing Duration
- Worker Utilization
- Worker Crash Recoveries
- Queue Size
- System Throughput

---

# 📝 Development Roadmap

## Phase 1 — Foundation

- [ ] Initialize Spring Boot Project
- [ ] Configure Maven
- [ ] Configure PostgreSQL
- [ ] Configure Docker
- [ ] Logging Setup
- [ ] Global Exception Handling

---

## Phase 2 — Database

- [ ] Design Database Schema
- [ ] Video Entity
- [ ] Job Entity
- [ ] Repository Layer
- [ ] Migrations

---

## Phase 3 — Upload Service

- [ ] Upload API
- [ ] File Validation
- [ ] Save Original Video
- [ ] Store Metadata

---

## Phase 4 — RabbitMQ

- [ ] Configure RabbitMQ
- [ ] Producer
- [ ] Consumer
- [ ] Dead Letter Queue
- [ ] Retry Queue

---

## Phase 5 — Worker Service

- [ ] Worker Registration
- [ ] Queue Listener
- [ ] Background Processing
- [ ] Graceful Shutdown

---

## Phase 6 — FFmpeg

- [ ] Video Validation
- [ ] Generate 1080p
- [ ] Generate 720p
- [ ] Generate 480p
- [ ] Thumbnail Generation

---

## Phase 7 — Reliability

- [ ] Retry Mechanism
- [ ] Crash Recovery
- [ ] Job Locking
- [ ] Timeout Detection
- [ ] Exactly-Once Processing

---

## Phase 8 — Monitoring

- [ ] Spring Boot Actuator
- [ ] Prometheus
- [ ] Grafana
- [ ] Metrics Dashboard

---

## Phase 9 — Docker

- [ ] API Container
- [ ] Worker Container
- [ ] PostgreSQL Container
- [ ] RabbitMQ Container
- [ ] Redis Container
- [ ] MinIO Container
- [ ] Prometheus Container
- [ ] Grafana Container

---

## Phase 10 — Production Ready

- [ ] Horizontal Scaling
- [ ] Load Testing
- [ ] Performance Optimization
- [ ] CI/CD Pipeline
- [ ] Complete Documentation

---

# 🎯 Learning Objectives

This project demonstrates practical backend engineering concepts including:

- Distributed Systems
- Message Queues
- Asynchronous Processing
- Worker Orchestration
- Fault Tolerance
- Process Management
- Concurrency
- Scalability
- Monitoring & Observability
- Dockerized Deployment
- System Reliability

---

# 🚀 Future Improvements

- Adaptive Bitrate Streaming (HLS)
- Video Compression Optimization
- GPU Accelerated Encoding
- Kubernetes Deployment
- AWS S3 Integration
- CDN Integration
- Authentication & Authorization
- Web Dashboard
- Email Notifications
- Distributed Tracing
- Auto Scaling Workers

---

# 🤝 Contributing

Contributions, suggestions, and improvements are welcome.

Feel free to fork the repository and submit a pull request.

---

# 📄 License

This project is licensed under the MIT License.

---

<div align="center">

### ⭐ If you like this project, consider giving it a star!

**Built with ❤️ using Java & Spring Boot**

</div>
