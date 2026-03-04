import api from './client';

export interface EnrolledCourseDto {
  courseId: number;
  title: string;
  thumbnailUrl: string;
  progressPercent: number;
  completedLessons: number;
  totalLessons: number;
  lastWatchedLessonId: number | null;
}

export const dashboardApi = {
  getEnrollments: () => api.get<EnrolledCourseDto[]>('/dashboard/enrollments'),
};
