package com.learnova.service;

import com.learnova.dto.dashboard.EnrolledCourseDto;
import com.learnova.entity.Enrollment;
import com.learnova.repository.CourseRepository;
import com.learnova.repository.EnrollmentRepository;
import com.learnova.repository.LessonRepository;
import com.learnova.repository.ProgressRepository;
import com.learnova.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final ProgressRepository progressRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;

    @Transactional(readOnly = true)
    public List<EnrolledCourseDto> getEnrolledCourses(Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(userId);
        return enrollments.stream()
                .map(e -> {
                    int totalLessons = countLessonsInCourse(e.getCourseId());
                    int completedLessons = (int) progressRepository.countByUserIdAndCourseIdAndCompletedTrue(userId, e.getCourseId());
                    int progressPercent = totalLessons > 0 ? (completedLessons * 100 / totalLessons) : 0;
                    String title = getCourseTitle(e.getCourseId());
                    return EnrolledCourseDto.builder()
                            .courseId(e.getCourseId())
                            .title(title)
                            .progressPercent(progressPercent)
                            .completedLessons(completedLessons)
                            .totalLessons(totalLessons)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String getCourseTitle(Long courseId) {
        return courseRepository.findById(courseId)
                .map(com.learnova.entity.Course::getTitle)
                .orElse("Course");
    }

    private int countLessonsInCourse(Long courseId) {
        return sectionRepository.findByCourseIdOrderByOrderNumberAsc(courseId).stream()
                .mapToInt(s -> lessonRepository.findBySectionIdOrderByOrderNumberAsc(s.getId()).size())
                .sum();
    }
}
