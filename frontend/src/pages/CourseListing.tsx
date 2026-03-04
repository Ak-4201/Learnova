import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { coursesApi, CourseDto } from '../api/courses';
import styles from './CourseListing.module.css';

function formatDuration(seconds: number) {
  if (!seconds) return '—';
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  if (h > 0) return `${h}h ${m}m`;
  return `${m} min`;
}

export default function CourseListing() {
  const [courses, setCourses] = useState<CourseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    coursesApi
      .list()
      .then((res) => setCourses(res.data))
      .catch(() => setError('Failed to load courses.'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className={styles.wrap}>
        <p className={styles.muted}>Loading courses...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.wrap}>
        <p className={styles.error}>{error}</p>
      </div>
    );
  }

  return (
    <div className={styles.wrap}>
      <h1 className={styles.title}>Courses</h1>
      <p className={styles.subtitle}>Choose a course and start learning.</p>
      <div className={styles.grid}>
        {courses.map((c) => (
          <article key={c.id} className={styles.card}>
            <Link to={`/courses/${c.id}`} className={styles.cardLink}>
              <div className={styles.thumb}>
                {c.thumbnailUrl ? (
                  <img src={c.thumbnailUrl} alt="" />
                ) : (
                  <div className={styles.thumbPlaceholder}>No thumbnail</div>
                )}
              </div>
              <div className={styles.body}>
                <span className={styles.category}>{c.category || 'Course'}</span>
                <h2 className={styles.cardTitle}>{c.title}</h2>
                <p className={styles.desc}>
                  {c.description ? (c.description.slice(0, 120) + (c.description.length > 120 ? '…' : '')) : 'No description.'}
                </p>
                <p className={styles.instructor}>Instructor: {c.instructorName || '—'}</p>
              </div>
            </Link>
            <div className={styles.footer}>
              <span className={styles.meta}>
                {c.totalLessons} lessons · {formatDuration(c.totalDurationSeconds || 0)}
              </span>
              <Link
                to={c.enrolled ? `/learn/${c.id}` : `/courses/${c.id}`}
                className={c.enrolled ? styles.btnGo : styles.btnEnroll}
              >
                {c.enrolled ? 'Go to course' : 'View details'}
              </Link>
            </div>
          </article>
        ))}
      </div>
      {courses.length === 0 && (
        <p className={styles.muted}>No courses available yet.</p>
      )}
    </div>
  );
}
