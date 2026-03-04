package com.learnova.service;

import com.learnova.dto.lesson.LessonDto;
import com.learnova.dto.lesson.LessonListResponse;
import com.learnova.entity.Lesson;
import com.learnova.repository.EnrollmentRepository;
import com.learnova.repository.LessonRepository;
import com.learnova.repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ProgressRepository progressRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * Fetch all lessons for a course. If user is enrolled, include completion status and last watched.
     */
    @Transactional(readOnly = true)
    public LessonListResponse getLessonsByCourseId(Long courseId, Long userId) {
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderBySectionAndOrder(courseId);
        final Set<Long> completedLessonIds;
        Long lastWatchedLessonId = null;
        if (userId != null && enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            var progressList = progressRepository.findByUserIdAndCourseId(userId, courseId);
            completedLessonIds = progressList.stream()
                .filter(p -> Boolean.TRUE.equals(p.getCompleted()))
                .map(p -> p.getLesson().getId())
                .collect(Collectors.toSet());
            lastWatchedLessonId = progressRepository.findLastWatchedByUserIdAndCourseId(userId, courseId)
                .stream()
                .findFirst()
                .map(p -> p.getLesson().getId())
                .orElse(null);
        } else {
            completedLessonIds = Set.of();
        }
        final Set<Long> completedIds = completedLessonIds;
        List<LessonDto> dtos = lessons.stream()
            .map(l -> LessonDto.builder()
                .id(l.getId())
                .title(l.getTitle())
                .orderNumber(l.getOrderNumber())
                .youtubeUrl(normalizeYoutubeUrl(l.getYoutubeUrl()))
                .durationSeconds(l.getDurationSeconds())
                .sectionId(l.getSection().getId())
                .sectionTitle(l.getSection().getTitle())
                .completed(completedIds.contains(l.getId()))
                .build())
            .collect(Collectors.toList());
        long completedCount = completedLessonIds.size();
        long total = lessons.size();
        int percent = total == 0 ? 0 : (int) Math.round(100.0 * completedCount / total);
        Long firstLessonId = lessons.isEmpty() ? null : lessons.get(0).getId();
        boolean enrolled = userId != null && enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
        return LessonListResponse.builder()
            .courseId(courseId)
            .lessons(dtos)
            .totalLessons(total)
            .completedCount(completedCount)
            .progressPercent(percent)
            .lastWatchedLessonId(lastWatchedLessonId != null ? lastWatchedLessonId : firstLessonId)
            .enrolled(enrolled)
            .build();
    }

    /**
     * Get single lesson by ID (for iframe URL when user clicks a lesson).
     */
    @Transactional(readOnly = true)
    public LessonDto getLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new RuntimeException("Lesson not found: " + lessonId));
        return LessonDto.builder()
            .id(lesson.getId())
            .title(lesson.getTitle())
            .orderNumber(lesson.getOrderNumber())
            .youtubeUrl(normalizeYoutubeUrl(lesson.getYoutubeUrl()))
            .durationSeconds(lesson.getDurationSeconds())
            .sectionId(lesson.getSection().getId())
            .sectionTitle(lesson.getSection().getTitle())
            .build();
    }

    /** Ensure URL is in form https://www.youtube.com/embed/VIDEO_ID for iframe. */
    private String normalizeYoutubeUrl(String urlOrId) {
        if (urlOrId == null || urlOrId.isBlank()) return "";
        String trimmed = urlOrId.trim();
        if (trimmed.length() == 11 && !trimmed.contains("/") && !trimmed.contains(".")) {
            return "https://www.youtube.com/embed/" + trimmed;
        }
        if (trimmed.contains("youtube.com/watch?v=")) {
            int i = trimmed.indexOf("v=");
            if (i >= 0) {
                String id = trimmed.substring(i + 2);
                int amp = id.indexOf('&');
                if (amp > 0) id = id.substring(0, amp);
                return "https://www.youtube.com/embed/" + id;
            }
        }
        if (trimmed.contains("youtu.be/")) {
            int i = trimmed.indexOf("youtu.be/");
            String id = trimmed.substring(i + 9);
            int q = id.indexOf('?');
            if (q > 0) id = id.substring(0, q);
            return "https://www.youtube.com/embed/" + id;
        }
        if (!trimmed.startsWith("http")) {
            return "https://www.youtube.com/embed/" + trimmed;
        }
        return trimmed;
    }
}
