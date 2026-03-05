# Learnova frontend – features

## Progress tracking

- **Learning page**
  - Progress bar at the top: “X / Y completed (Z%)”.
  - Lesson list in the sidebar: completed lessons show a checkmark (✓).
  - “Mark as completed” button: marks the current lesson completed, updates the bar, and advances to the next lesson.
  - Resume from last watched: when you open a course, the video starts at the last lesson you were on.
  - Next/Previous buttons to move between lessons; switching a lesson updates “last watched” in the backend.
- **Dashboard**
  - Enrolled courses show progress (e.g. “3 / 10 lessons · 30%”) and a progress bar on each card.
  - “Continue” takes you to the Learning page for that course.
- **Backend**
  - Progress is stored per user/course/lesson (completed flag, last watched time).
  - APIs: `POST /api/courses/:id/progress` (mark completed), `POST /api/courses/:id/lessons/:lessonId/watch` (last watched), `GET /api/courses/:id/lessons` and `GET /api/dashboard/enrollments` return progress data.

## API URL (env)

Set the backend URL in `frontend/.env` (copy from `.env.example`). Use `VITE_API_URL`; do not commit `.env` to Git. See root README for setup.
