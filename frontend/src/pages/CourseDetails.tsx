import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { coursesApi, CourseDto } from '../api/courses';
import { useAuth } from '../context/AuthContext';
import styles from './CourseDetails.module.css';

function formatDuration(seconds: number) {
  if (!seconds) return '—';
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  if (h > 0) return `${h}h ${m}m`;
  return `${m} min`;
}

export default function CourseDetails() {
  const { courseId } = useParams<{ courseId: string }>();
  const [course, setCourse] = useState<CourseDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [enrolling, setEnrolling] = useState(false);
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!courseId) return;
    coursesApi
      .getById(Number(courseId))
      .then((res) => setCourse(res.data))
      .catch(() => setCourse(null))
      .finally(() => setLoading(false));
  }, [courseId]);

  const handleEnroll = async () => {
    if (!courseId || !isAuthenticated) {
      navigate('/login');
      return;
    }
    setEnrolling(true);
    try {
      await coursesApi.enroll(Number(courseId));
      const { data } = await coursesApi.getById(Number(courseId));
      setCourse(data);
      navigate(`/learn/${courseId}`);
    } catch {
      // already enrolled or error
      const { data } = await coursesApi.getById(Number(courseId)).catch(() => ({ data: course }));
      if (data) setCourse(data);
    } finally {
      setEnrolling(false);
    }
  };

  if (loading || !course) {
    return (
      <div className={styles.wrap}>
        <p className={styles.muted}>{loading ? 'Loading...' : 'Course not found.'}</p>
      </div>
    );
  }

  const learnUrl = `/learn/${courseId}`;

  return (
    <div className={styles.wrap}>
      <div className={styles.hero}>
        <div className={styles.thumbWrap}>
          {course.thumbnailUrl ? (
            <img src={course.thumbnailUrl} alt="" className={styles.thumb} />
          ) : (
            <div className={styles.thumbPlaceholder}>No thumbnail</div>
          )}
        </div>
        <div className={styles.heroBody}>
          <span className={styles.category}>{course.category || 'Course'}</span>
          <h1 className={styles.title}>{course.title}</h1>
          <p className={styles.instructor}>Instructor: {course.instructorName || '—'}</p>
          <p className={styles.meta}>
            {course.totalLessons} lessons · {formatDuration(course.totalDurationSeconds || 0)} total
          </p>
          <div className={styles.actions}>
            {course.enrolled ? (
              <Link to={learnUrl} className={styles.btnPrimary}>
                Go to course
              </Link>
            ) : (
              <button
                type="button"
                className={styles.btnPrimary}
                onClick={handleEnroll}
                disabled={enrolling}
              >
                {enrolling ? 'Enrolling...' : 'Enroll'}
              </button>
            )}
          </div>
        </div>
      </div>

      <div className={styles.content}>
        <section>
          <h2 className={styles.sectionTitle}>Description</h2>
          <p className={styles.desc}>
            {course.description || 'No description provided.'}
          </p>
        </section>
        {course.whatYouWillLearn && (
          <section>
            <h2 className={styles.sectionTitle}>What you will learn</h2>
            <p className={styles.desc}>{course.whatYouWillLearn}</p>
          </section>
        )}
        <section>
          <h2 className={styles.sectionTitle}>Course content</h2>
          <p className={styles.muted}>
            {course.totalLessons} lessons · {formatDuration(course.totalDurationSeconds || 0)}
          </p>
          {course.enrolled && (
            <Link to={learnUrl} className={styles.btnSecondary}>
              Start learning →
            </Link>
          )}
        </section>
      </div>
    </div>
  );
}
