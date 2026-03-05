# Learnova – Learning Management System

A scalable LMS with a **React** frontend and **Spring Boot** backend. Courses use YouTube-embedded lessons; progress is tracked in the backend and synced to the UI.

## Features

- **Authentication**: Signup, Login, JWT, roles (Student / Instructor / Admin). Passwords stored as BCrypt hashes.
- **Course listing**: Thumbnail, instructor, short description, Enroll / View details.
- **Course details**: Description, what you'll learn, lesson count, duration, Enroll.
- **Learning page**: YouTube iframe, lesson list sidebar, progress bar, Next/Previous, mark lesson completed, resume from last watched.
- **Progress tracking**: Progress bar and “X / Y completed (Z%)” on the Learning page; “Mark as completed” updates the backend and moves to the next lesson; completed lessons show a checkmark in the sidebar; resume from last watched lesson when re-opening a course; dashboard shows progress per enrolled course (completed lessons, percentage). All progress is stored in the backend (user, course, lesson, completed, last watched) and synced to the UI.
- **Dashboard**: After login, shows welcome and enrolled courses with progress. Optional: set `VITE_DASHBOARD_CONTENT_URL` to embed an external URL in an iframe (see below).

## Tech Stack

- **Frontend**: React 18, Vite, React Router, Axios, CSS modules.
- **Backend**: Java 17, Spring Boot 3, Spring Security, JWT (jjwt), JPA/Hibernate, PostgreSQL (Aiven).
- **Database**: PostgreSQL on Aiven. Only metadata (e.g. YouTube URLs/IDs) is stored, not video files.

## Database (Aiven)

- Connection is configured in `backend/src/main/resources/application.properties`.
- Use environment variable `SPRING_DATASOURCE_PASSWORD` to override the password in production.
- Ensure the machine running the backend has its IP allowed in the Aiven project allowlist.

## API URL and environment (safe for Git)

The frontend reads the backend API URL from an environment variable so you can push the repo without hardcoded URLs or secrets.

1. **Copy the example env file** (do not commit your real `.env`):
   ```bash
   cd frontend
   cp .env.example .env
   ```
2. **Edit `frontend/.env`** and set your backend URL:
   ```env
   VITE_API_URL=http://localhost:8081
   ```
   Use your backend’s URL (e.g. `http://localhost:8081` or your deployed backend URL). No trailing slash.
3. `.env` is in `.gitignore`; only `.env.example` is committed. Never commit real passwords or production URLs.

## Run locally

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

Runs on **http://localhost:8081** (or set `server.port` in `application.properties`).

### Frontend

```bash
cd frontend
cp .env.example .env   # first time only; set VITE_API_URL to your backend URL
npm install
npm run dev
```

Runs on **http://localhost:5173** and proxies `/api` to the URL in `VITE_API_URL`.

### Default users (seeded)

- **Student**: sign up from the UI (or use any email/password).
- **Instructor**: `instructor@learnova.com` / `instructor123`
- **Admin**: `admin@learnova.com` / `admin123`

Seeded courses: Java Programming, Python for Beginners, Machine Learning Fundamentals, Web Development with React.

## Dashboard content from URL

To show external content on the dashboard after login:

1. Create `frontend/.env` (or `.env.local`):
   ```env
   VITE_DASHBOARD_CONTENT_URL=https://example.com/your-page
   ```
2. Restart the frontend dev server.
3. The dashboard will render an iframe with that URL above “My courses”.

If `VITE_DASHBOARD_CONTENT_URL` is not set, the dashboard only shows the welcome message and enrolled courses.

## API overview

- `POST /api/auth/signup` – Register (body: email, password, fullName, role?)
- `POST /api/auth/login` – Login (body: email, password)
- `GET /api/courses` – List courses (optional auth for enrolled flag)
- `GET /api/courses/:id` – Course details (optional auth)
- `GET /api/courses/:id/lessons` – All lessons for course (optional auth for progress)
- `GET /api/lessons/:id` – Single lesson (e.g. YouTube URL)
- `POST /api/courses/:id/enroll` – Enroll (auth required)
- `POST /api/courses/:id/progress` – Mark lesson completed (auth, body: lessonId, completed)
- `POST /api/courses/:id/lessons/:lessonId/watch` – Update last watched (auth)
- `GET /api/dashboard/enrollments` – My enrolled courses with progress (auth)

## Project structure

```
Learnova/
├── backend/           # Spring Boot
│   └── src/main/java/com/learnova/
│       ├── config/    # Security, CORS, DataSeeder
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── repository/
│       ├── security/   # JWT, UserPrincipal
│       └── service/
├── frontend/          # React + Vite
│   └── src/
│       ├── api/
│       ├── components/
│       ├── context/
│       └── pages/
└── README.md
```
