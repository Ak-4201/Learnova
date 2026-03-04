package com.learnova.service;

import com.learnova.dto.progress.ProgressRequest;
import com.learnova.dto.progress.ProgressResponse;
import com.learnova.entity.Lesson;
import com.learnova.entity.Progress;
import com.learnova.entity.User;
import com.learnova.repository.EnrollmentRepository;
import com.learnova.repository.LessonRepository;
import com.learnova.repository.ProgressRepository;
import com.learnova.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * Mark lesson as completed and update last watched. Returns updated progress stats.
     */
    @Transactional
    public ProgressResponse recordProgress(Long userId, Long courseId, ProgressRequest request) {
        if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new RuntimeException("Not enrolled in this course");
        }
        Lesson lesson = lessonRepository.findById(request.getLessonId())
            .orElseThrow(() -> new RuntimeException("Lesson not found"));
        if (!lesson.getSection().getCourse().getId().equals(courseId)) {
            throw new RuntimeException("Lesson does not belong to this course");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Progress progress = progressRepository.findByUserIdAndLessonId(userId, lesson.getId())
            .orElse(Progress.builder()
                .user(user)
                .lesson(lesson)
                .completed(false)
                .build());
        progress.setLastWatchedAt(Instant.now());
        if (Boolean.TRUE.equals(request.getCompleted())) {
            progress.setCompleted(true);
        }
        progressRepository.save(progress);

        long total = lessonRepository.countByCourseId(courseId);
        long completedCount = progressRepository.findCompletedByUserIdAndCourseId(userId, courseId).size();
        int percent = total == 0 ? 0 : (int) Math.round(100.0 * completedCount / total);

        return ProgressResponse.builder()
            .lessonId(lesson.getId())
            .completed(progress.getCompleted())
            .completedCount(completedCount)
            .totalLessons(total)
            .progressPercent(percent)
            .build();
    }

    /**
     * Update last watched (e.g. when user switches lesson) without marking completed.
     */
    @Transactional
    public void updateLastWatched(Long userId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) return;
        Long courseId = lesson.getSection().getCourse().getId();
        if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) return;
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Progress progress = progressRepository.findByUserIdAndLessonId(userId, lessonId)
            .orElse(Progress.builder()
                .user(user)
                .lesson(lesson)
                .completed(false)
                .build());
        progress.setLastWatchedAt(Instant.now());
        progressRepository.save(progress);
    }
}
