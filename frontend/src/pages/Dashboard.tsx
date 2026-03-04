import { useEffect, useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { dashboardApi, EnrolledCourseDto } from '../api/dashboard';
import styles from './Dashboard.module.css';

export default function Dashboard() {
  const { user, isAuthenticated, isLoading } = useAuth();
  const [enrollments, setEnrollments] = useState<EnrolledCourseDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }
    dashboardApi
      .getEnrollments()
      .then((res) => setEnrollments(res.data))
      .catch(() => setEnrollments([]))
      .finally(() => setLoading(false));
  }, [isAuthenticated]);

  if (isLoading) {
    return (
      <div className={styles.wrap}>
        <p className={styles.muted}>Loading...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  const dashboardContentUrl = import.meta.env.VITE_DASHBOARD_CONTENT_URL as string | undefined;

  return (
    <div className={styles.wrap}>
      <header className={styles.header}>
        <h1 className={styles.title}>Dashboard</h1>
        <p className={styles.welcome}>
          Welcome back, <strong>{user?.fullName}</strong>.
        </p>
      </header>

      {dashboardContentUrl && (
        <section className={styles.embedSection}>
          <iframe
            title="Dashboard content"
            src={dashboardContentUrl}
            className={styles.embedIframe}
          />
        </section>
      )}

      <section className={styles.section}>
        <h2 className={styles.sectionTitle}>My courses</h2>
        {loading ? (
          <p className={styles.muted}>Loading your courses...</p>
        ) : enrollments.length === 0 ? (
          <p className={styles.muted}>
            You haven't enrolled in any course yet.{' '}
            <Link to="/">Browse courses</Link>
          </p>
        ) : (
          <div className={styles.grid}>
            {enrollments.map((e) => (
              <Link
                key={e.courseId}
                to={`/learn/${e.courseId}`}
                className={styles.card}
              >
                <div className={styles.cardThumb}>
                  {e.thumbnailUrl ? (
                    <img src={e.thumbnailUrl} alt="" />
                  ) : (
                    <div className={styles.thumbPlaceholder}>Course</div>
                  )}
                  <div className={styles.progressOverlay}>
                    <div
                      className={styles.progressBar}
                      style={{ width: `${e.progressPercent}%` }}
                    />
                  </div>
                </div>
                <div className={styles.cardBody}>
                  <h3 className={styles.cardTitle}>{e.title}</h3>
                  <p className={styles.progressText}>
                    {e.completedLessons} / {e.totalLessons} lessons · {e.progressPercent}%
                  </p>
                </div>
                <span className={styles.continue}>Continue →</span>
              </Link>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
