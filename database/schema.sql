-- Learnova LMS - PostgreSQL schema (matches JPA entities)
-- Use this for documentation or manual deployment. With ddl-auto=update, Hibernate creates/updates tables automatically.

-- Users (auth: Student/Instructor/Admin)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- Courses (metadata; videos are YouTube URLs only)
CREATE TABLE IF NOT EXISTS courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    what_you_will_learn TEXT,
    thumbnail_url VARCHAR(512),
    category VARCHAR(255),
    instructor_id BIGINT NOT NULL,
    CONSTRAINT fk_courses_instructor FOREIGN KEY (instructor_id) REFERENCES users(id)
);

-- Sections (group lessons within a course)
CREATE TABLE IF NOT EXISTS sections (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    order_number INT NOT NULL,
    CONSTRAINT fk_sections_course FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Lessons (YouTube URL or video ID stored; no video files)
CREATE TABLE IF NOT EXISTS lessons (
    id BIGSERIAL PRIMARY KEY,
    section_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    order_number INT NOT NULL,
    youtube_url VARCHAR(512),
    duration_seconds INT,
    CONSTRAINT fk_lessons_section FOREIGN KEY (section_id) REFERENCES sections(id)
);

-- Enrollments (student-course mapping)
CREATE TABLE IF NOT EXISTS enrollments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrolled_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_enrollments_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT uq_enrollments_user_course UNIQUE (user_id, course_id)
);

-- Progress (completed lessons, last watched)
CREATE TABLE IF NOT EXISTS progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    completed BOOLEAN NOT NULL,
    last_watched_at TIMESTAMP,
    CONSTRAINT fk_progress_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_progress_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_progress_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id),
    CONSTRAINT uq_progress_user_course_lesson UNIQUE (user_id, course_id, lesson_id)
);

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_courses_category ON courses(category);
CREATE INDEX IF NOT EXISTS idx_courses_instructor ON courses(instructor_id);
CREATE INDEX IF NOT EXISTS idx_sections_course ON sections(course_id);
CREATE INDEX IF NOT EXISTS idx_lessons_section ON lessons(section_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_user ON enrollments(user_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course ON enrollments(course_id);
CREATE INDEX IF NOT EXISTS idx_progress_user_course ON progress(user_id, course_id);
