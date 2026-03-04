import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import CourseListing from './pages/CourseListing';
import CourseDetails from './pages/CourseDetails';
import Learning from './pages/Learning';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<CourseListing />} />
        <Route path="courses/:courseId" element={<CourseDetails />} />
        <Route path="learn/:courseId" element={<Learning />} />
        <Route path="login" element={<Login />} />
        <Route path="signup" element={<Signup />} />
        <Route path="dashboard" element={<Dashboard />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
