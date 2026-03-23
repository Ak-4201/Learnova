import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { coursesApi, CourseDto } from '../api/courses';
import styles from './CourseBrowse.module.css';

const CATEGORIES = [
  { value: '', label: 'All' },
  { value: 'Programming', label: 'Programming' },
  { value: 'Web Development', label: 'Web Development' },
  { value: 'Data Structures', label: 'Data Structures' },
  { value: 'Data Science', label: 'Data Science' },
  { value: 'Backend', label: 'Backend' },
];

const DEBOUNCE_MS = 300;

export interface CourseBrowseProps {
  title?: string;
}

export default function CourseBrowse({ title = 'Courses' }: CourseBrowseProps) {
  const [courses, setCourses] = useState<CourseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [searchParam, setSearchParam] = useState('');
  const [category, setCategory] = useState('');

  useEffect(() => {
    const t = setTimeout(() => setSearchParam(searchInput.trim()), DEBOUNCE_MS);
    return () => clearTimeout(t);
  }, [searchInput]);

  const fetchCourses = useCallback(() => {
    setLoading(true);
    setError('');
    const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8081/api';
    coursesApi
      .list({
        search: searchParam || undefined,
        category: category || undefined,
      })
      .then((res) => setCourses(res.data))
      .catch((err) => {
        const isNetworkError = err?.code === 'ERR_NETWORK' || err?.message === 'Network Error';
        setError(
          isNetworkError
            ? `Cannot reach the server. Make sure the backend is running at ${baseURL.replace(/\/api\/?$/, '')} (e.g. \`./mvnw spring-boot:run\` in the backend folder).`
            : 'Failed to load courses.'
        );
      })
      .finally(() => setLoading(false));
  }, [searchParam, category]);

  useEffect(() => {
    fetchCourses();
  }, [fetchCourses]);

  if (error) return <div className={styles.error} role="alert">{error}</div>;

  return (
    <section className={styles.section}>
      <h2 className={styles.title}>{title}</h2>
      <div className={styles.filters}>
        <input
          type="search"
          placeholder="Search courses..."
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          className={styles.searchInput}
          aria-label="Search courses"
        />
        <select
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          className={styles.categorySelect}
          aria-label="Course type"
        >
          {CATEGORIES.map((c) => (
            <option key={c.value || 'all'} value={c.value}>
              {c.label}
            </option>
          ))}
        </select>
      </div>
      {loading ? (
        <div className={styles.msg}>Loading courses...</div>
      ) : (
        <div className={styles.grid}>
          {courses.map((course) => (
            <article key={course.id} className={styles.card}>
              <div className={styles.thumbWrap}>
                {course.thumbnailUrl ? (
                  <img
                    key={`${course.id}-${course.thumbnailUrl}`}
                    src={course.thumbnailUrl}
                    alt=""
                    className={styles.thumb}
                  />
                ) : (
                  <div className={styles.thumbPlaceholder}>No image</div>
                )}
              </div>
              <div className={styles.body}>
                <h3 className={styles.cardTitle}>{course.title}</h3>
                <p className={styles.instructor}>{course.instructorName}</p>
                <p className={styles.desc}>{course.description}</p>
                <p className={styles.meta}>{course.totalLessons} lessons</p>
                <div className={styles.actions}>
                  <Link to={`/courses/${course.id}`} className={styles.detailsLink}>
                    View details
                  </Link>
                  {course.enrolled ? (
                    <Link to={`/courses/${course.id}/learn`} className={styles.enrollBtn}>
                      Go to course
                    </Link>
                  ) : (
                    <Link to={`/courses/${course.id}`} className={styles.enrollBtn}>
                      Enroll
                    </Link>
                  )}
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
