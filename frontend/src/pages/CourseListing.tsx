import CourseBrowse from '../components/CourseBrowse';
import styles from './CourseListing.module.css';

export default function CourseListing() {
  return (
    <div className={styles.page}>
      <CourseBrowse title="Courses" />
    </div>
  );
}
