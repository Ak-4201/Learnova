import api from './client';

export interface EnrolledCourseDto {
  courseId: number;
  title: string;
  progressPercent: number;
  completedLessons: number;
  totalLessons: number;
}

export const dashboardApi = {
  getEnrollments: () =>
    api.get<EnrolledCourseDto[]>('/dashboard/enrollments'),
};
