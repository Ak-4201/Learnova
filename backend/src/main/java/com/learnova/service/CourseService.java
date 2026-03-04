package com.learnova.service;

import com.learnova.dto.course.CourseDto;
import com.learnova.dto.course.EnrolledCourseDto;
import com.learnova.entity.Course;
import com.learnova.entity.Enrollment;
import com.learnova.repository.CourseRepository;
import com.learnova.repository.EnrollmentRepository;
import com.learnova.repository.LessonRepository;
import com.learnova.repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ProgressRepository progressRepository;

    @Transactional(readOnly = true)
    public List<CourseDto> listCourses(Long currentUserId) {
        List<Course> courses = courseRepository.findAllWithInstructor();
        return courses.stream()
            .map(c -> toDto(c, currentUserId))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDto getCourseById(Long courseId, Long currentUserId) {
        Course course = courseRepository.findByIdWithInstructor(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
        return toDto(course, currentUserId);
    }

    private CourseDto toDto(Course c, Long currentUserId) {
        long totalLessons = lessonRepository.countByCourseId(c.getId());
        Long sum = lessonRepository.sumDurationByCourseId(c.getId());
        long totalDuration = sum != null ? sum : 0L;
        boolean enrolled = currentUserId != null && enrollmentRepository.existsByUserIdAndCourseId(currentUserId, c.getId());
        return CourseDto.builder()
            .id(c.getId())
            .title(c.getTitle())
            .description(c.getDescription())
            .whatYouWillLearn(c.getWhatYouWillLearn())
            .thumbnailUrl(c.getThumbnailUrl())
            .category(c.getCategory())
            .instructorName(c.getInstructor() != null ? c.getInstructor().getFullName() : null)
            .instructorId(c.getInstructor() != null ? c.getInstructor().getId() : null)
            .totalLessons(totalLessons)
            .totalDurationSeconds(totalDuration)
            .enrolled(enrolled)
            .build();
    }

    @Transactional(readOnly = true)
    public List<EnrolledCourseDto> getEnrolledCourses(Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdWithCourse(userId);
        return enrollments.stream()
            .map(e -> {
                Course c = e.getCourse();
                long total = lessonRepository.countByCourseId(c.getId());
                long completed = progressRepository.findCompletedByUserIdAndCourseId(userId, c.getId()).size();
                int percent = total == 0 ? 0 : (int) Math.round(100.0 * completed / total);
                var lastWatched = progressRepository.findLastWatchedByUserIdAndCourseId(userId, c.getId())
                    .stream().findFirst();
                Long lastLessonId = lastWatched.map(p -> p.getLesson().getId()).orElse(null);
                return EnrolledCourseDto.builder()
                    .courseId(c.getId())
                    .title(c.getTitle())
                    .thumbnailUrl(c.getThumbnailUrl())
                    .progressPercent(percent)
                    .completedLessons(completed)
                    .totalLessons(total)
                    .lastWatchedLessonId(lastLessonId)
                    .build();
            })
            .collect(Collectors.toList());
    }
}
