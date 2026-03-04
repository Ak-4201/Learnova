import { useEffect, useState, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  lessonsApi,
  LessonListResponse,
  LessonDto,
  ProgressResponse,
} from '../api/lessons';
import { coursesApi } from '../api/courses';
import styles from './Learning.module.css';

export default function Learning() {
  const { courseId } = useParams<{ courseId: string }>();
  const [data, setData] = useState<LessonListResponse | null>(null);
  const [currentLesson, setCurrentLesson] = useState<LessonDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [progress, setProgress] = useState<{ percent: number; completed: number; total: number }>({
    percent: 0,
    completed: 0,
    total: 0,
  });
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const loadLessons = useCallback(async () => {
    if (!courseId) return;
    try {
      const res = await lessonsApi.getByCourseId(Number(courseId));
      setData(res.data);
      setProgress({
        percent: res.data.progressPercent,
        completed: res.data.completedCount,
        total: res.data.totalLessons,
      });
      const lessons = res.data.lessons;
      const lastId = res.data.lastWatchedLessonId;
      const first = lessons[0];
      const targetId = lastId || first?.id;
      const target = lessons.find((l) => l.id === targetId) || first;
      setCurrentLesson(target || null);
    } catch {
      setData(null);
      setCurrentLesson(null);
    } finally {
      setLoading(false);
    }
  }, [courseId]);

  useEffect(() => {
    loadLessons();
  }, [loadLessons]);

  useEffect(() => {
    if (!isAuthenticated && courseId) {
      coursesApi.getById(Number(courseId)).then(() => {}).catch(() => {});
    }
  }, [isAuthenticated, courseId]);

  const selectLesson = (lesson: LessonDto) => {
    setCurrentLesson(lesson);
    if (isAuthenticated && courseId) {
      lessonsApi.updateLastWatched(Number(courseId), lesson.id).catch(() => {});
    }
  };

  const markCompleted = async () => {
    if (!currentLesson || !courseId || !isAuthenticated) return;
    try {
      const res = await lessonsApi.recordProgress(
        Number(courseId),
        currentLesson.id,
        true
      );
      const d = res.data as ProgressResponse;
      setProgress({
        percent: d.progressPercent,
        completed: d.completedCount,
        total: d.totalLessons,
      });
      setData((prev) => {
        if (!prev) return prev;
        return {
          ...prev,
          lessons: prev.lessons.map((l) =>
            l.id === currentLesson.id ? { ...l, completed: true } : l
          ),
          completedCount: d.completedCount,
          progressPercent: d.progressPercent,
        };
      });
      const lessons = data?.lessons ?? [];
      const idx = lessons.findIndex((l) => l.id === currentLesson.id);
      if (idx >= 0 && idx < lessons.length - 1) {
        setCurrentLesson(lessons[idx + 1]);
        lessonsApi.updateLastWatched(Number(courseId), lessons[idx + 1].id).catch(() => {});
      }
    } catch {
      // ignore
    }
  };

  const goPrev = () => {
    if (!data?.lessons.length || !currentLesson) return;
    const idx = data.lessons.findIndex((l) => l.id === currentLesson.id);
    if (idx > 0) {
      const prev = data.lessons[idx - 1];
      selectLesson(prev);
    }
  };

  const goNext = () => {
    if (!data?.lessons.length || !currentLesson) return;
    const idx = data.lessons.findIndex((l) => l.id === currentLesson.id);
    if (idx >= 0 && idx < data.lessons.length - 1) {
      const next = data.lessons[idx + 1];
      selectLesson(next);
    }
  };

  if (loading) {
    return (
      <div className={styles.wrap}>
        <p className={styles.muted}>Loading course...</p>
      </div>
    );
  }

  if (!data) {
    return (
      <div className={styles.wrap}>
        <p className={styles.muted}>Course not found.</p>
        <Link to="/">Back to courses</Link>
      </div>
    );
  }

  if (isAuthenticated && data.enrolled === false) {
    return (
      <div className={styles.wrap}>
        <p className={styles.muted}>You need to enroll in this course to access lessons and track progress.</p>
        <Link to={`/courses/${courseId}`}>Go to course details & enroll</Link>
      </div>
    );
  }

  const lessons = data.lessons;
  const currentIndex = lessons.findIndex((l) => l.id === currentLesson?.id);
  const hasPrev = currentIndex > 0;
  const hasNext = currentIndex >= 0 && currentIndex < lessons.length - 1;
  const embedUrl = currentLesson?.youtubeUrl || '';

  return (
    <div className={styles.wrap}>
      <div className={styles.progressBarWrap}>
        <div className={styles.progressBar}>
          <div
            className={styles.progressFill}
            style={{ width: `${progress.percent}%` }}
          />
        </div>
        <span className={styles.progressText}>
          {progress.completed} / {progress.total} completed ({progress.percent}%)
        </span>
      </div>

      <div className={styles.main}>
        <div className={styles.playerSection}>
          <div className={styles.videoWrap}>
            {embedUrl ? (
              <iframe
                src={embedUrl}
                title={currentLesson?.title || 'Video'}
                className={styles.iframe}
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
              />
            ) : (
              <div className={styles.noVideo}>No video for this lesson.</div>
            )}
          </div>
          <div className={styles.videoTitle}>
            {currentLesson ? (
              <>
                <span className={styles.sectionBadge}>{currentLesson.sectionTitle}</span>
                {currentLesson.title}
              </>
            ) : (
              'Select a lesson'
            )}
          </div>
          <div className={styles.actions}>
            <button
              type="button"
              className={styles.navBtn}
              onClick={goPrev}
              disabled={!hasPrev}
            >
              ← Previous
            </button>
            {isAuthenticated && (
              <button
                type="button"
                className={styles.completeBtn}
                onClick={markCompleted}
                disabled={!currentLesson || currentLesson.completed}
              >
                {currentLesson?.completed ? 'Completed' : 'Mark as completed'}
              </button>
            )}
            <button
              type="button"
              className={styles.navBtn}
              onClick={goNext}
              disabled={!hasNext}
            >
              Next →
            </button>
          </div>
        </div>

        <aside className={styles.sidebar}>
          <h3 className={styles.sidebarTitle}>Lessons</h3>
          <ul className={styles.lessonList}>
            {lessons.map((l) => (
              <li key={l.id}>
                <button
                  type="button"
                  className={`${styles.lessonItem} ${currentLesson?.id === l.id ? styles.lessonActive : ''}`}
                  onClick={() => selectLesson(l)}
                >
                  <span className={styles.lessonNum}>{l.orderNumber}</span>
                  <span className={styles.lessonTitle}>{l.title}</span>
                  {l.completed && <span className={styles.check}>✓</span>}
                </button>
              </li>
            ))}
          </ul>
        </aside>
      </div>
    </div>
  );
}
