import api from './client';

export interface LessonItemDto {
  id: number;
  title: string;
  orderNumber: number;
  youtubeUrl: string;
  sectionTitle: string;
  completed: boolean;
}

export interface LessonListResponse {
  courseId: number;
  lessons: LessonItemDto[];
  totalLessons: number;
  progressPercent: number;
  completedCount: number;
  lastWatchedLessonId: number | null;
  enrolled: boolean;
}

export interface LessonDto {
  id: number;
  title: string;
  youtubeUrl: string;
}

export const lessonsApi = {
  getByCourseId: (courseId: number) =>
    api.get<LessonListResponse>(`/courses/${courseId}/lessons`),

  getById: (lessonId: number) =>
    api.get<LessonDto>(`/lessons/${lessonId}`),
};
