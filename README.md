# Learnova – Learning Management System

A scalable LMS with a React (Vite) frontend and Java Spring Boot backend. Users browse courses, enroll, and learn via YouTube-embedded lessons. Progress is tracked (percentage completed, completed lessons, resume from last watched). The backend handles JWT authentication, role-based access (Student/Instructor/Admin), course and lesson metadata, enrollment, and progress; the database stores only metadata (e.g. YouTube URLs), not video files.

## Overview

Learnova provides course listing with search and category filter, course details (description, what you will learn, total lessons, duration), and a learning page with an embedded YouTube player, lesson list, progress bar, and Next/Previous navigation. Students mark lessons complete to advance; progress is persisted and the UI shows completion percentage and supports resuming from the last watched lesson. Authentication is JWT-based; passwords are hashed with BCrypt.

## Architecture

```mermaid
flowchart LR
  subgraph client [Client]
    Browser[Browser]
  end
  subgraph frontend [Frontend React]
    Pages[Pages]
    API[API Client]
  end
  subgraph backend [Backend Spring Boot]
    Auth[Auth]
    Courses[Courses]
    Lessons[Lessons]
    Progress[Progress]
  end
  subgraph db [Aiven PostgreSQL]
    Tables[(Users, Courses, Sections, Lessons, Enrollments, Progress)]
  end
  Browser --> Pages
  Pages --> API
  API --> Auth
  API --> Courses
  API --> Lessons
  API --> Progress
  Auth --> Tables
  Courses --> Tables
  Lessons --> Tables
  Progress --> Tables
```

- **Frontend**: React (Vite), Axios, React Router. Displays courses and lessons, embeds YouTube via iframe, shows progress bar and percentage, calls REST APIs.
- **Backend**: Spring Boot, MVC, JWT, JPA/Hibernate. REST APIs under `/api/*`; stateless sessions.
- **Database**: PostgreSQL (Aiven). Tables: users, courses, sections, lessons, enrollments, progress.

See [docs/architecture.md](docs/architecture.md) for more detail.

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/signup` | No | Register (fullName, email, password) |
| POST | `/api/auth/login` | No | Login (email, password); returns JWT |
| GET | `/api/courses` | Optional | List courses; query params: `search`, `category` |
| GET | `/api/courses/{id}` | Optional | Course by id (enrolled flag when authenticated) |
| POST | `/api/courses/{courseId}/enroll` | Yes | Enroll current user in course |
| GET | `/api/courses/{courseId}/lessons` | Optional | Lesson list + progress + lastWatchedLessonId |
| GET | `/api/lessons/{lessonId}` | No | Single lesson (e.g. YouTube URL) |
| POST | `/api/courses/{courseId}/progress` | Yes | Record progress (lessonId, completed) |
| GET | `/api/dashboard/enrollments` | Yes | Enrolled courses with progress for current user |

## Database Schema

- **users**: id, email, password_hash, full_name, role (USER/ADMIN/INSTRUCTOR)
- **courses**: id, title, description, what_you_will_learn, thumbnail_url, category, instructor_id
- **sections**: id, course_id, title, order_number
- **lessons**: id, section_id, title, order_number, youtube_url, duration_seconds
- **enrollments**: id, user_id, course_id, enrolled_at (unique on user_id, course_id)
- **progress**: id, user_id, course_id, lesson_id, completed, last_watched_at (unique on user_id, course_id, lesson_id)

Full DDL: [database/schema.sql](database/schema.sql).

## Setup Instructions

**Prerequisites:** Node.js (v18+), Java 17+, Maven 3.8+, Aiven PostgreSQL (or compatible).

1. **Database (Aiven)**  
   Create a PostgreSQL service. Add your **backend server’s public IP** to the service **allowlist**. Connection format:  
   `postgres://avnadmin:PASSWORD@HOST:PORT/defaultdb?sslmode=require`  
   Do not commit the password; use environment variables.

2. **Backend**  
   - In `backend/`, set the DB password:  
     `set SPRING_DATASOURCE_PASSWORD=your_aiven_password` (Windows) or  
     `export SPRING_DATASOURCE_PASSWORD=...` (Unix/macOS).  
   - Run: `mvn spring-boot:run`  
   - Server runs on **8081**. JPA `ddl-auto=update` creates/updates tables. Seed data (10 courses) runs on first startup if the DB is empty.

3. **Frontend**  
   - In `frontend/`: `npm install` then `npm run dev`  
   - App: **http://localhost:5173**  
   - Optional: copy `.env.example` to `.env` and set `VITE_API_URL` (default `http://localhost:8081/api`).

See [docs/setup.md](docs/setup.md) for step-by-step setup.

## Ports

- Backend: **8081**
- Frontend dev: **5173**

## Security and configuration

- **Passwords**: BCrypt hashes only; never store plain text.
- **JWT**: Set `learnova.jwt.secret` (and optionally expiration) in production; do not commit secrets.
- **CORS**: Configure allowed origins (e.g. `learnova.cors.allowed-origins`) for your frontend URL.
- **Secrets**: Use `.env` (frontend) and `SPRING_DATASOURCE_PASSWORD` (backend); both are in `.gitignore`. Use `.env.example` as a template.

## Scalability

- **Stateless backend**: JWT allows horizontal scaling of API servers.
- **Database**: Use connection pooling (default in Spring Boot); consider read replicas for heavy read traffic.
- **Frontend/backend split**: Deploy separately; put frontend behind CDN for static assets.
- **Future**: Cache course list (e.g. Redis), serve thumbnails via CDN, optional rate limiting.

## B2C course YouTube catalog (backend)

Canonical **business-to-consumer** YouTube links live in JSON (not hard-coded only in Java):

- **[backend/src/main/resources/b2c/course-youtube-urls.json](backend/src/main/resources/b2c/course-youtube-urls.json)** — `format: "b2c"`, one `youtubeWatchUrl` per `courseTitle` (playlist URLs allowed; primary video id drives the thumbnail).
- On startup, **DataSeeder** uses this file when creating courses (thumbnails from `img.youtube.com/vi/{id}/mqdefault.jpg`).
- **B2cCourseMediaSyncRunner** re-applies URLs and thumbnails to existing rows so updates to the JSON file take effect after restart without wiping the database.

To change a course’s video or card image, edit the JSON and restart the backend.

## Repository structure

```
Learnova/
  frontend/       # React (Vite) app
  backend/        # Spring Boot app
  backend/src/main/resources/b2c/   # B2C YouTube URL catalog (JSON)
  database/       # schema.sql
  docs/           # architecture.md, setup.md
  README.md
```

## After code changes (if the UI still looks old)

1. **Restart the backend** so `DataSeeder` + **B2C sync** run again (`B2cCourseMediaSyncRunner` applies `b2c/course-youtube-urls.json` on every startup). From `backend/`: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"` or `.\start-local.ps1`.
2. **Rebuild** if you use a production frontend bundle: `cd frontend && npm run build` (then re-serve `dist/`).
3. **Hard refresh** the browser (Ctrl+F5) or disable cache in DevTools so course list JSON and thumbnails are not stale.

## Features

- **Auth**: Signup, login, JWT, roles (USER, ADMIN, INSTRUCTOR).
- **Courses**: List with search and category filter; details (thumbnail, instructor, description, what you’ll learn, lesson count, duration); enrollment.
- **Lessons**: Sections and lessons with YouTube URL; progress (completed, %, last watched); Learning page with iframe, sidebar, Next/Previous, Mark as complete.
- **Dashboard**: “My learning” (enrolled courses + progress) and “Browse courses” (same listing as Courses page).
