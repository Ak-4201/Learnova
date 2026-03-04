import api from './client';

export interface CourseDto {
  id: number;
  title: string;
  description: string;
  whatYouWillLearn: string;
  thumbnailUrl: string;
  category: string;
  instructorName: string;
  instructorId: number;
  totalLessons: number;
  totalDurationSeconds: number;
  enrolled: boolean;
}

export const coursesApi = {
  list: () => api.get<CourseDto[]>('/courses'),
  getById: (id: number) => api.get<CourseDto>(`/courses/${id}`),
  enroll: (courseId: number) => api.post(`/courses/${courseId}/enroll`),
};
