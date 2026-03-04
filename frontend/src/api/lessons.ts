import api from './client';

export interface LessonDto {
  id: number;
  title: string;
  orderNumber: number;
  youtubeUrl: string;
  durationSeconds: number | null;
  sectionId: number;
  sectionTitle: string;
  completed: boolean;
}

export interface LessonListResponse {
  courseId: number;
  lessons: LessonDto[];
  totalLessons: number;
  completedCount: number;
  progressPercent: number;
  lastWatchedLessonId: number | null;
  enrolled?: boolean;
}

export interface ProgressResponse {
  lessonId: number;
  completed: boolean;
  completedCount: number;
  totalLessons: number;
  progressPercent: number;
}

export const lessonsApi = {
  getByCourseId: (courseId: number) =>
    api.get<LessonListResponse>(`/courses/${courseId}/lessons`),
  getById: (lessonId: number) => api.get<LessonDto>(`/lessons/${lessonId}`),
  recordProgress: (courseId: number, lessonId: number, completed: boolean) =>
    api.post<ProgressResponse>(`/courses/${courseId}/progress`, { lessonId, completed }),
  updateLastWatched: (courseId: number, lessonId: number) =>
    api.post(`/courses/${courseId}/lessons/${lessonId}/watch`),
};
