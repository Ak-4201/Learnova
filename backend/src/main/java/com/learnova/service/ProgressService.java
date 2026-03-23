package com.learnova.service;

import com.learnova.dto.progress.ProgressRequest;
import com.learnova.dto.progress.ProgressResponse;
import com.learnova.entity.Lesson;
import com.learnova.entity.Progress;
import com.learnova.repository.EnrollmentRepository;
import com.learnova.repository.LessonRepository;
import com.learnova.repository.ProgressRepository;
import com.learnova.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public ProgressResponse recordProgress(Long userId, Long courseId, ProgressRequest request) {
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        Long sectionId = lesson.getSectionId();
        if (sectionId == null || !belongsToCourse(sectionId, courseId)) {
            throw new RuntimeException("Lesson does not belong to course");
        }
        if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new RuntimeException("Not enrolled in course");
        }
        Optional<Progress> existing = progressRepository.findByUserIdAndCourseIdAndLessonId(userId, courseId, request.getLessonId());
        Progress progress;
        if (existing.isPresent()) {
            progress = existing.get();
            progress.setCompleted(progress.getCompleted() || request.isCompleted());
            progress.setLastWatchedAt(Instant.now());
        } else {
            progress = Progress.builder()
                    .userId(userId)
                    .courseId(courseId)
                    .lessonId(request.getLessonId())
                    .completed(request.isCompleted())
                    .lastWatchedAt(Instant.now())
                    .build();
        }
        progressRepository.save(progress);
        int totalLessons = countLessonsInCourse(courseId);
        int completedCount = (int) progressRepository.countByUserIdAndCourseIdAndCompletedTrue(userId, courseId);
        int progressPercent = totalLessons > 0 ? (completedCount * 100 / totalLessons) : 0;
        return ProgressResponse.builder()
                .lessonId(request.getLessonId())
                .completed(progress.getCompleted())
                .completedCount(completedCount)
                .totalLessons(totalLessons)
                .progressPercent(progressPercent)
                .build();
    }

    @Transactional
    public void updateLastWatched(Long userId, Long courseId, Long lessonId) {
        if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) return;
        Optional<Progress> opt = progressRepository.findByUserIdAndCourseIdAndLessonId(userId, courseId, lessonId);
        Progress p;
        if (opt.isPresent()) {
            p = opt.get();
            p.setLastWatchedAt(Instant.now());
        } else {
            p = Progress.builder()
                    .userId(userId)
                    .courseId(courseId)
                    .lessonId(lessonId)
                    .completed(false)
                    .lastWatchedAt(Instant.now())
                    .build();
        }
        progressRepository.save(p);
    }

    private boolean belongsToCourse(Long sectionId, Long courseId) {
        return sectionRepository.findById(sectionId)
                .map(s -> s.getCourseId().equals(courseId))
                .orElse(false);
    }

    private int countLessonsInCourse(Long courseId) {
        return sectionRepository.findByCourseIdOrderByOrderNumberAsc(courseId).stream()
                .mapToInt(s -> lessonRepository.findBySectionIdOrderByOrderNumberAsc(s.getId()).size())
                .sum();
    }
}
