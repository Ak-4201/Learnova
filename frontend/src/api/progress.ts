import api from './client';

export interface ProgressRequest {
  lessonId: number;
  completed: boolean;
}

export interface ProgressResponse {
  lessonId: number;
  completed: boolean;
  completedCount: number;
  totalLessons: number;
  progressPercent: number;
}

export const progressApi = {
  record: (courseId: number, payload: ProgressRequest) =>
    api.post<ProgressResponse>(`/courses/${courseId}/progress`, payload),
};
