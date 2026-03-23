package com.learnova.service;

import com.learnova.b2c.B2cCourseYoutubeCatalogLoader;
import com.learnova.dto.lesson.LessonDto;
import com.learnova.dto.lesson.LessonItemDto;
import com.learnova.dto.lesson.LessonListResponse;
import com.learnova.entity.Course;
import com.learnova.entity.Lesson;
import com.learnova.entity.Progress;
import com.learnova.entity.Section;
import com.learnova.repository.CourseRepository;
import com.learnova.repository.EnrollmentRepository;
import com.learnova.repository.LessonRepository;
import com.learnova.repository.ProgressRepository;
import com.learnova.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final ProgressRepository progressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final B2cCourseYoutubeCatalogLoader b2cCourseYoutubeCatalogLoader;

    @Transactional(readOnly = true)
    public LessonListResponse getLessonsByCourseId(Long courseId, Long userId) {
        String b2cWatchUrl = courseRepository.findById(courseId)
                .flatMap(c -> b2cCourseYoutubeCatalogLoader.getCatalog().watchUrlForTitle(c.getTitle()))
                .orElse(null);

        List<Section> sections = sectionRepository.findByCourseIdOrderByOrderNumberAsc(courseId);
        List<LessonItemDto> items = new ArrayList<>();
        int totalLessons = 0;
        Set<Long> completedLessonIds = userId != null
                ? progressRepository.findByUserIdAndCourseId(userId, courseId).stream()
                        .filter(Progress::getCompleted)
                        .map(p -> p.getLessonId())
                        .collect(Collectors.toSet())
                : Set.of();
        Long lastWatchedLessonId = null;
        if (userId != null) {
            lastWatchedLessonId = progressRepository.findTop1ByUserIdAndCourseIdOrderByLastWatchedAtDesc(userId, courseId)
                    .map(Progress::getLessonId)
                    .orElse(null);
        }
        for (Section section : sections) {
            List<Lesson> lessons = lessonRepository.findBySectionIdOrderByOrderNumberAsc(section.getId());
            for (Lesson lesson : lessons) {
                items.add(LessonItemDto.builder()
                        .id(lesson.getId())
                        .title(lesson.getTitle())
                        .orderNumber(lesson.getOrderNumber())
                        .youtubeUrl(b2cWatchUrl != null ? b2cWatchUrl : lesson.getYoutubeUrl())
                        .sectionTitle(section.getTitle())
                        .completed(completedLessonIds.contains(lesson.getId()))
                        .build());
                totalLessons++;
            }
        }
        int completedCount = completedLessonIds.size();
        int progressPercent = totalLessons > 0 ? (completedCount * 100 / totalLessons) : 0;
        boolean enrolled = userId != null && enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
        return LessonListResponse.builder()
                .courseId(courseId)
                .lessons(items)
                .totalLessons(totalLessons)
                .progressPercent(progressPercent)
                .completedCount(completedCount)
                .lastWatchedLessonId(lastWatchedLessonId)
                .enrolled(enrolled)
                .build();
    }

    @Transactional(readOnly = true)
    public LessonDto getLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        Section section = sectionRepository.findById(lesson.getSectionId())
                .orElseThrow(() -> new RuntimeException("Section not found"));
        Course course = courseRepository.findById(section.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));
        String b2cWatchUrl = b2cCourseYoutubeCatalogLoader.getCatalog().watchUrlForTitle(course.getTitle())
                .orElse(null);
        return LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .youtubeUrl(b2cWatchUrl != null ? b2cWatchUrl : lesson.getYoutubeUrl())
                .build();
    }
}
