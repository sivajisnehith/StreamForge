# StreamForge

StreamForge is a distributed video processing backend that handles asynchronous
HLS transcoding, metadata extraction, and streaming.

## Project Overview

StreamForge is designed to handle video ingestion and streaming at scale.
Instead of performing resource-intensive video transcoding synchronously within
the HTTP request thread, the application decouples the upload lifecycle from
the processing pipeline using an asynchronous message-driven architecture.

When a client uploads a video, the request is received by the Spring Boot API,
which writes the original raw video file to a MinIO object storage bucket and
saves the initial video metadata (such as title, description, and status set to
`UPLOADED`) to a PostgreSQL database. 

The API then publishes a message to a RabbitMQ queue and returns a success
response with the video ID. A background consumer process listens to the
queue, downloads the original video, executes FFmpeg to perform
multi-resolution HLS transcoding, invokes FFprobe to analyze stream metadata,
uploads the final HLS segments and thumbnail back to MinIO, and updates the
database record with the new status and video properties.

## System Architecture

The system is composed of several decoupled layers to ensure strict separation
of concerns and resource isolation during processing:

* **Client Interface**: Directs file uploads and HLS playback requests.
* **API Controller Layer**: Handles HTTP requests, manages authentication filters,
  validates payloads, and interacts with metadata repositories.
* **Message Broker (RabbitMQ)**: Buffers video processing tasks to protect
  application resources from spikes in upload traffic.
* **Background Worker Layer**: Consumes tasks from the queue and processes them
  in isolated filesystem sandbox directories.
* **Object Storage (MinIO)**: Serves as the central repository for raw video
  files and transcoded media streams.
* **Metadata Store (PostgreSQL)**: Persists user accounts and video profiles.

<p align="center">
  <img src="docs/images/architecture.png" width="900">
</p>

## Key Features

* **Video Upload Ingestion**: Validates file size constraints, checks mime types,
  and handles direct streaming uploads to MinIO storage.
* **Background Processing**: Uses RabbitMQ queues to decouple video transcoding
  from API response times, preventing HTTP request timeouts.
* **Adaptive HLS Streaming**: Transcodes source videos into HLS-compliant
  directories containing `480p`, `720p`, and `1080p` streams accompanied by a
  master `.m3u8` playlist.
* **Thumbnail Generation**: Automatically extracts a poster frame at the
  1-second mark of the video using FFmpeg.
* **Metadata Extraction**: Runs FFprobe to parse media characteristics such as
  video/audio codecs, bitrate, width, height, and duration.
* **JWT Authentication**: Protects endpoints using Spring Security and
  stateless JSON Web Tokens.
* **Resource Authorization**: Prevents unauthorized modifications by verifying
  that only the user who uploaded a video can update its status or delete it.
* **Paginated Retrievals**: Serves video lists using Spring Data Pagination
  to limit database memory consumption.
* **Keyword Search**: Filters video records using case-insensitive substring
  matching against the video title.
* **Request Validation**: Validates incoming payload constraints using Jakarta
  validation annotations (e.g., username length between 3 and 50 characters,
  password minimum of 6 characters, and non-empty video titles up to 100
  characters).
* **Global Exception Handling**: Maps custom application errors to uniform
  HTTP response payloads.
* **API Documentation**: Integrates Swagger UI to provide a visual interface
  for endpoint testing.

## Video Processing Pipeline

The transcoding pipeline operates sequentially, moving raw video inputs through
the following lifecycle stages:

<p align="center">
  <img src="docs/images/pipeline.png" width="900">
</p>

1. **Ingestion & Validation**: The API validates that the input file is not
   empty and corresponds to an allowed mime type (`video/mp4`, `video/x-matroska`,
   `video/quicktime`).
2. **MinIO Persistence**: The file is stored in the `originals` bucket using
   a randomly generated UUID as the filename.
3. **Queue Dispatching**: A JSON message containing the video ID and filename
   is sent to the `video.processing` queue.
4. **Task Acquisition**: The worker consumes the task, updates the database
   status to `PROCESSING`, and downloads the file to an isolated local workspace.
5. **Transcoding**: FFmpeg encodes the video file into segmented HLS streams at
   multiple bitrates.
6. **Metadata Parsing & Thumbnailing**: FFprobe extracts media dimensions,
   durations, and codecs. FFmpeg captures the poster thumbnail.
7. **Storage Sync**: The segments, playlists, and thumbnail are uploaded to the
   `processed` bucket under a directory prefixed with the video UUID.
8. **Finalization**: Local workspace files are deleted, and the PostgreSQL
   record is updated to `COMPLETED` along with the extracted metadata.

## Authentication & Authorization

Securing the platform relies on token-based authentication and method-level
security:

<p align="center">
  <img src="docs/images/authentication_flow.png" width="900">
</p>

* **User Registration & Login**: Users create accounts with BCrypt-hashed
  passwords. A successful login returns a JWT containing the user's username and
  signature.
* **Request Filtering**: The application intercepts incoming requests using a
  custom security filter. It extracts the JWT from the `Authorization: Bearer <token>`
  header, validates its signature, and establishes a security context.
* **Ownership Checks**: Endpoints that modify state retrieve the target record
  from PostgreSQL and verify that the authenticated user ID matches the owner
  ID associated with the video.

## Database Design

The relational database schema is designed around two main tables:

<p align="center">
  <img src="docs/images/database_er_diagram.png" width="900">
</p>

* **users**: Stores the unique identifier, username, email, encrypted password
  hash, and timestamps.
* **videos**: Stores the video ID, original filename, file size, content type,
  processing status, title, description, uploaded timestamp, and technical
  attributes (duration, height, width, codecs). A foreign key binds each video
  to a specific record in the `users` table.

## Deployment Architecture

The database, message broker, and storage services are defined as containerized
services, establishing separate networks:

<p align="center">
  <img src="docs/images/deployment_diagram.png" width="900">
</p>

* **postgres**: Relational database container exposing port `5432` with a
  persistent volume mapping.
* **rabbitmq**: Message broker running the management plugin on port `15672`
  and AMQP protocol services on port `5672`.
* **minio**: S3-compliant object storage server exposing API requests on port
  `9000` and the management console on port `9001`.
* **app**: The Spring Boot container, which connects to the database, broker,
  and storage services.

## Technology Stack

| Component | Technology | Version / Purpose |
|---|---|---|
| Language | Java 21 | Long-Term Support JDK release |
| Web Framework | Spring Boot 3 | Web MVC and dependency injection |
| Security | Spring Security | Filters request authentication |
| Token Provider | JWT (io.jsonwebtoken) | Generates and parses signed tokens |
| Database | PostgreSQL 17 | Relational database engine |
| Message Broker | RabbitMQ | Decoupled message queueing |
| Object Storage | MinIO | S3-compatible file storage |
| Media Tools | FFmpeg / FFprobe | Transcoding, segmenting, and metadata parsing |
| Dependency Manager| Maven | Project compilation and dependency assembly |
| API Docs | Swagger / SpringDoc | OpenAPI UI console |

## REST API Overview

* `POST /api/auth/register` - Register a new user account.
* `POST /api/auth/login` - Authenticate credentials and return a JWT access token.
* `POST /api/files/upload` - Upload a video file with title and description
  fields (authenticated only).
* `GET /api/videos` - Retrieve a paginated list of videos, optional search
  by title keyword.
* `GET /api/videos/{videoId}` - Retrieve video metadata details.
* `DELETE /api/videos/{videoId}` - Delete video record and MinIO files (owner only).
* `PATCH /api/videos/{videoId}/status` - Manually update video status (owner only).
* `GET /api/videos/{videoId}/thumbnail.jpg` - Stream the video poster image.
* `GET /api/videos/{videoId}/{fileName}` - Stream HLS manifest files (`.m3u8`) and
  video chunks (`.ts`).

## Project Structure

```
StreamForge
├── docs/
│   └── images/                  # Design diagrams
├── src/
│   ├── main/
│   │   ├── java/com/Opsfusionn/StreamForge/
│   │   │   ├── config/          # Configurations (Security, MinIO, RabbitMQ, OpenAPI)
│   │   │   ├── controller/      # REST API entry points
│   │   │   ├── dto/             # Request/Response payloads
│   │   │   ├── exception/       # Custom exceptions and handler advice
│   │   │   ├── messaging/       # RabbitMQ consumers, producers, and errors
│   │   │   ├── model/           # Database entities and status enums
│   │   │   ├── security/        # JWT filters and EntryPoint helper
│   │   │   ├── service/         # Services (FFmpeg, MinIO, database CRUD)
│   │   │   └── StreamForgeApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/                    # Integration and unit tests
├── docker-compose.yml           # Local infrastructure docker configuration
└── pom.xml                      # Maven dependencies configuration
```

## Getting Started

### Prerequisites

* Java Development Kit (JDK) 21 installed and configured on your system path.
* Maven 3.x build tool.
* Docker and Docker Compose installed.
* FFmpeg and FFprobe binaries installed on the host system path (required for
  local development).

### Running Locally

1. **Start backing services**:
   Navigate to the project root directory and start Postgres, RabbitMQ, and
   MinIO in the background:
   ```bash
   docker-compose up -d
   ```

2. **Configure local variables**:
   Verify that your local variables in `src/main/resources/application.properties`
   correspond to the container ports. By default, the application connects to
   `localhost` ports.

3. **Start the application**:
   Compile the source code and start the Spring Boot runtime:
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Verify Database Connection**:
   Check if the database initialized and compiled tables successfully by running:
   ```bash
   docker exec -it streamforge-postgres psql -U postgres -d streamforge -c "\dt"
   ```

### Running with Docker

To build the Spring Boot application container and run it inside the
docker-compose network alongside the backing services:
```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up --build -d
```

### Swagger URL

Once the application is running, open your web browser and navigate to the
interactive API console to test the endpoints:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Future Improvements

* Integrate support for chunked multipart uploads to handle large media files
  without high JVM heap usage.
* Configure a dead-letter exchange (DLX) in RabbitMQ to isolate and inspect
  failing transcoding messages.
* Implement Redis cache integration for the metadata query endpoints to reduce
  database read pressure.

## License

Distributed under the MIT License. See `LICENSE` for details.
