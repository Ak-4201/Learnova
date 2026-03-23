import { useState, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import styles from './Navbar.module.css';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setDropdownOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <nav className={styles.nav}>
      <div className={styles.wrap}>
        <Link to="/courses" className={styles.logo}>
          Learnova
        </Link>
        <div className={styles.right} ref={ref}>
          {!isAuthenticated ? (
            <>
              <Link to="/login" className={styles.link}>Login</Link>
              <Link to="/signup" className={styles.btn}>Sign up</Link>
            </>
          ) : (
            <>
              <Link to="/dashboard" className={styles.link}>Dashboard</Link>
              <button
                type="button"
                className={styles.profileBtn}
                onClick={() => setDropdownOpen((o) => !o)}
                aria-expanded={dropdownOpen}
              >
                {user?.fullName || user?.email}
              </button>
              {dropdownOpen && (
                <div className={styles.dropdown}>
                  <div className={styles.dropdownHeader}>
                    {user?.fullName}
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
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
