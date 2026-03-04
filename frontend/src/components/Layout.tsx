import { useState, useRef, useEffect } from 'react';
import { Outlet, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import styles from './Layout.module.css';

export default function Layout() {
  const { user, isAuthenticated, logout } = useAuth();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className={styles.layout}>
      <header className={styles.header}>
        <Link to="/" className={styles.logo}>
          Learnova
        </Link>
        <nav className={styles.nav}>
          <Link to="/" className={styles.navLink}>Courses</Link>
          {isAuthenticated && (
            <Link to="/dashboard" className={styles.navLink}>Dashboard</Link>
          )}
          {!isAuthenticated ? (
            <div className={styles.authBtns}>
              <Link to="/login" className={styles.btnSecondary}>Login</Link>
              <Link to="/signup" className={styles.btnPrimary}>Sign up</Link>
            </div>
          ) : (
            <div className={styles.profileWrap} ref={ref}>
              <button
                type="button"
                className={styles.profileBtn}
                onClick={() => setDropdownOpen((o) => !o)}
                aria-expanded={dropdownOpen}
              >
                <span className={styles.avatar}>
                  {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
                </span>
                <span className={styles.profileName}>{user?.fullName}</span>
                <span className={styles.chevron}>▼</span>
              </button>
              {dropdownOpen && (
                <div className={styles.dropdown}>
                  <div className={styles.dropdownUser}>
                    <strong>{user?.fullName}</strong>
                    <span className={styles.dropdownEmail}>{user?.email}</span>
                  </div>
                  <Link
                    to="/dashboard"
                    className={styles.dropdownItem}
                    onClick={() => setDropdownOpen(false)}
                  >
                    Dashboard
                  </Link>
                  <button
                    type="button"
                    className={styles.dropdownItem}
                    onClick={() => {
                      logout();
                      setDropdownOpen(false);
                    }}
                  >
                    Logout
                  </button>
                </div>
              )}
            </div>
          )}
        </nav>
      </header>
      <main className={styles.main}>
        <Outlet />
      </main>
    </div>
  );
}
