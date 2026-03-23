import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { coursesApi, CourseDto } from '../api/courses';
import { useAuth } from '../context/AuthContext';
import styles from './CourseDetails.module.css';

function formatDuration(seconds: number): string {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  if (h > 0) return `${h}h ${m}m`;
  return `${m} min`;
}

export default function CourseDetails() {
  const { id } = useParams<{ id: string }>();
  const { isAuthenticated } = useAuth();
  const [course, setCourse] = useState<CourseDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [enrolling, setEnrolling] = useState(false);

  useEffect(() => {
    if (!id) return;
    coursesApi
      .getById(Number(id))
      .then((res) => setCourse(res.data))
      .catch(() => setError('Course not found.'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleEnroll = () => {
    if (!id || !isAuthenticated) return;
    setEnrolling(true);
    setError('');
    coursesApi
      .enroll(Number(id))
      .then(() => {
        if (course) setCourse({ ...course, enrolled: true });
      })
      .catch((err: { response?: { status?: number } }) => {
        setError(
          err.response?.status === 404
            ? 'Course not found.'
            : 'Enrollment failed. Please log in again or try again.'
        );
      })
      .finally(() => setEnrolling(false));
  };

  if (loading) return <div className={styles.msg}>Loading...</div>;
  if (error || !course) return <div className={styles.error}>{error || 'Course not found.'}</div>;

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        {course.thumbnailUrl && (
          <img
            key={`${course.id}-${course.thumbnailUrl}`}
            src={course.thumbnailUrl}
            alt=""
            className={styles.thumb}
          />
        )}
        <div className={styles.headerText}>
          <h1 className={styles.title}>{course.title}</h1>
          <p className={styles.instructor}>Instructor: {course.instructorName}</p>
          <p className={styles.meta}>
            {course.totalLessons} lessons · {formatDuration(course.totalDurationSeconds || 0)}
          </p>
        </div>
      </div>

      <section className={styles.section}>
        <h2>Description</h2>
        <p className={styles.desc}>{course.description}</p>
      </section>

      <section className={styles.section}>
        <h2>What you will learn</h2>
        <p className={styles.desc}>{course.whatYouWillLearn}</p>
      </section>

      <div className={styles.actions}>
        {course.enrolled ? (
          <Link to={`/courses/${course.id}/learn`} className={styles.primaryBtn}>
            Go to course
          </Link>
        ) : isAuthenticated ? (
          <button
            type="button"
            className={styles.primaryBtn}
            onClick={handleEnroll}
            disabled={enrolling}
          >
            {enrolling ? 'Enrolling...' : 'Enroll'}
          </button>
        ) : (
          <Link to="/login" className={styles.primaryBtn}>
            Log in to enroll
          </Link>
        )}
      </div>
    </div>
  );
}
