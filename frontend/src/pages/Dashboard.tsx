import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { dashboardApi, EnrolledCourseDto } from '../api/dashboard';
import CourseBrowse from '../components/CourseBrowse';
import styles from './Dashboard.module.css';

export default function Dashboard() {
  const [courses, setCourses] = useState<EnrolledCourseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadEnrollments = useCallback(() => {
    setLoading(true);
    setError('');
    dashboardApi
      .getEnrollments()
      .then((res) => setCourses(res.data))
      .catch(() => setError('Failed to load dashboard. You may need to log in again.'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    loadEnrollments();
  }, [loadEnrollments]);

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>My learning</h1>
      {loading ? (
        <div className={styles.msg}>Loading...</div>
      ) : error ? (
        <div className={styles.error}>
          {error}
          <button type="button" onClick={loadEnrollments} className={styles.retryBtn}>
            Retry
          </button>
        </div>
      ) : courses.length === 0 ? (
        <p className={styles.empty}>
          You are not enrolled in any course yet.{' '}
          <Link to="/courses">Browse courses</Link> below to get started.
        </p>
      ) : (
        <ul className={styles.list}>
          {courses.map((c) => (
            <li key={c.courseId} className={styles.card}>
              <div className={styles.cardMain}>
                <h2 className={styles.cardTitle}>{c.title}</h2>
                <p className={styles.progressText}>
                  {c.completedLessons} / {c.totalLessons} lessons · {c.progressPercent}% complete
                </p>
                <div className={styles.progressBar}>
                  <div
                    className={styles.progressFill}
                    style={{ width: `${c.progressPercent}%` }}
                  />
                </div>
              </div>
              <Link to={`/courses/${c.courseId}/learn`} className={styles.resumeBtn}>
                {c.progressPercent === 100 ? 'Review' : 'Continue'}
              </Link>
            </li>
          ))}
        </ul>
      )}

      <CourseBrowse title="Browse courses" />
    </div>
  );
}
