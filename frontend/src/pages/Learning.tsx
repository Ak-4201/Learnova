import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { lessonsApi, LessonItemDto } from '../api/lessons';
import { progressApi } from '../api/progress';
import { getEmbedUrl } from '../utils/youtube';
import styles from './Learning.module.css';

export default function Learning() {
  const { id } = useParams<{ id: string }>();
  const courseId = id ? Number(id) : 0;
  const [lessons, setLessons] = useState<LessonItemDto[]>([]);
  const [progressPercent, setProgressPercent] = useState(0);
  const [completedCount, setCompletedCount] = useState(0);
  const [totalLessons, setTotalLessons] = useState(0);
  const [lastWatchedId, setLastWatchedId] = useState<number | null>(null);
  const [currentLesson, setCurrentLesson] = useState<LessonItemDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [markingComplete, setMarkingComplete] = useState(false);

  useEffect(() => {
    if (!courseId) return;
    setLoading(true);
    lessonsApi
      .getByCourseId(courseId)
      .then((res) => {
        const data = res.data;
        setLessons(data.lessons);
        setProgressPercent(data.progressPercent);
        setCompletedCount(data.completedCount);
        setTotalLessons(data.totalLessons);
        setLastWatchedId(data.lastWatchedLessonId ?? null);
        const first = data.lessons[0];
        const toSelect = data.lastWatchedLessonId
          ? data.lessons.find((l) => l.id === data.lastWatchedLessonId) ?? first
          : first;
        setCurrentLesson(toSelect ?? null);
      })
      .catch(() => setError('Failed to load lessons.'))
      .finally(() => setLoading(false));
  }, [courseId]);

  const refreshProgress = () => {
    lessonsApi.getByCourseId(courseId).then((res) => {
      setProgressPercent(res.data.progressPercent);
      setCompletedCount(res.data.completedCount);
      setLessons((prev) =>
        prev.map((l) => ({
          ...l,
          completed: res.data.lessons.find((x) => x.id === l.id)?.completed ?? l.completed,
        }))
      );
    });
  };

  const handleMarkComplete = () => {
    if (!currentLesson || markingComplete) return;
    setMarkingComplete(true);
    progressApi
      .record(courseId, { lessonId: currentLesson.id, completed: true })
      .then((res) => {
        setProgressPercent(res.data.progressPercent);
        setCompletedCount(res.data.completedCount);
        setLessons((prev) =>
          prev.map((l) =>
            l.id === currentLesson.id ? { ...l, completed: true } : l
          )
        );
        const idx = lessons.findIndex((l) => l.id === currentLesson.id);
        if (idx >= 0 && idx < lessons.length - 1) setCurrentLesson(lessons[idx + 1]);
      })
      .finally(() => setMarkingComplete(false));
  };

  const currentIndex = currentLesson
    ? lessons.findIndex((l) => l.id === currentLesson.id)
    : -1;
  const nextLesson = currentIndex >= 0 && currentIndex < lessons.length - 1
    ? lessons[currentIndex + 1]
    : null;
  const prevLesson = currentIndex > 0 ? lessons[currentIndex - 1] : null;

  if (loading) return <div className={styles.msg}>Loading...</div>;
  if (error) return <div className={styles.error}>{error}</div>;
  if (lessons.length === 0) return <div className={styles.msg}>No lessons in this course.</div>;

  const embedSrc = currentLesson
    ? getEmbedUrl(currentLesson.youtubeUrl)
    : '';

  return (
    <div className={styles.page}>
      <div className={styles.progressBarWrap}>
        <div className={styles.progressLabel}>
          Progress: {completedCount} / {totalLessons} lessons ({progressPercent}%)
        </div>
        <div className={styles.progressTrack}>
          <div
            className={styles.progressFill}
            style={{ width: `${progressPercent}%` }}
          />
        </div>
      </div>

      <div className={styles.layout}>
        <div className={styles.playerSection}>
          <div className={styles.videoWrap}>
            {embedSrc ? (
              <iframe
                title="Lesson video"
                src={embedSrc}
                className={styles.iframe}
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
              />
            ) : (
              <div className={styles.noVideo}>No video for this lesson.</div>
            )}
          </div>
          <div className={styles.videoActions}>
            {currentLesson && (
              <>
                <h2 className={styles.currentTitle}>{currentLesson.title}</h2>
                {currentLesson.completed ? (
                  <span className={styles.completedBadge}>Completed</span>
                ) : (
                  <button
                    type="button"
                    className={styles.completeBtn}
                    onClick={handleMarkComplete}
                    disabled={markingComplete}
                  >
                    {markingComplete ? 'Saving...' : 'Mark as complete'}
                  </button>
                )}
              </>
            )}
            <div className={styles.navBtns}>
              <button
                type="button"
                className={styles.navBtn}
                disabled={!prevLesson}
                onClick={() => prevLesson && setCurrentLesson(prevLesson)}
              >
                Previous
              </button>
              <button
                type="button"
                className={styles.navBtn}
                disabled={!nextLesson}
                onClick={() => nextLesson && setCurrentLesson(nextLesson)}
              >
                Next
              </button>
            </div>
          </div>
        </div>

        <aside className={styles.sidebar}>
          <h3 className={styles.sidebarTitle}>Lessons</h3>
          <ul className={styles.lessonList}>
            {lessons.map((lesson) => (
              <li key={lesson.id}>
                <button
                  type="button"
                  className={`${styles.lessonItem} ${
                    currentLesson?.id === lesson.id ? styles.lessonItemActive : ''
                  }`}
                  onClick={() => {
                    setCurrentLesson(lesson);
                    progressApi.record(courseId, { lessonId: lesson.id, completed: false }).catch(() => {});
                  }}
                >
                  {lesson.completed && <span className={styles.check} aria-hidden>✓</span>}
                  <span className={styles.lessonTitle}>{lesson.title}</span>
                  {lesson.sectionTitle && (
                    <span className={styles.sectionLabel}>{lesson.sectionTitle}</span>
                  )}
                </button>
              </li>
            ))}
          </ul>
        </aside>
      </div>
    </div>
  );
}
