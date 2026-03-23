package com.learnova.service;

import com.learnova.b2c.B2cCourseYoutubeCatalogLoader;
import com.learnova.b2c.YoutubeUrlUtil;
import com.learnova.dto.course.CourseDto;
import com.learnova.entity.Course;
import com.learnova.entity.Lesson;
import com.learnova.entity.User;
import com.learnova.repository.CourseRepository;
import com.learnova.repository.EnrollmentRepository;
import com.learnova.repository.LessonRepository;
import com.learnova.repository.SectionRepository;
import com.learnova.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final B2cCourseYoutubeCatalogLoader b2cCourseYoutubeCatalogLoader;

    @Transactional(readOnly = true)
    public List<CourseDto> listCourses(Long userId, String search, String category) {
        String searchNorm = (search != null && search.isBlank()) ? null : search;
        String categoryNorm = (category != null && category.isBlank()) ? null : category;
        List<Course> courses = (searchNorm == null && categoryNorm == null)
                ? courseRepository.findAllByOrderByIdAsc()
                : courseRepository.findAllBySearchAndCategory(searchNorm, categoryNorm);
        return courses.stream()
                .map(c -> toDto(c, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDto getCourseById(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return toDto(course, userId);
    }

    private CourseDto toDto(Course course, Long userId) {
        User instructor = userRepository.findById(course.getInstructorId()).orElse(null);
        String instructorName = instructor != null ? instructor.getFullName() : "Unknown";
        int totalLessons = countLessons(course.getId());
        long totalDuration = sectionRepository.findByCourseIdOrderByOrderNumberAsc(course.getId()).stream()
                .flatMap(s -> lessonRepository.findBySectionIdOrderByOrderNumberAsc(s.getId()).stream())
                .mapToLong(l -> l.getDurationSeconds() != null ? l.getDurationSeconds() : 0)
                .sum();
        boolean enrolled = userId != null && enrollmentRepository.existsByUserIdAndCourseId(userId, course.getId());
        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .whatYouWillLearn(course.getWhatYouWillLearn())
                .thumbnailUrl(resolveThumbnailForApi(course))
                .category(course.getCategory())
                .instructorName(instructorName)
                .instructorId(course.getInstructorId())
                .totalLessons(totalLessons)
                .totalDurationSeconds(totalDuration)
                .enrolled(enrolled)
                .build();
    }

    private int countLessons(Long courseId) {
        return sectionRepository.findByCourseIdOrderByOrderNumberAsc(courseId).stream()
                .mapToInt(s -> lessonRepository.findBySectionIdOrderByOrderNumberAsc(s.getId()).size())
                .sum();
    }

    /**
     * Prefer B2C catalog URL for this course title, then first lesson URL, then stored column —
     * so list/detail thumbnails match curated YouTube videos even if the DB was not re-synced.
     */
    private String resolveThumbnailForApi(Course course) {
        Optional<String> b2c = b2cCourseYoutubeCatalogLoader.getCatalog().watchUrlForTitle(course.getTitle());
        String source = b2c.or(() -> Optional.ofNullable(firstLessonYoutubeUrl(course.getId())))
                .filter(u -> !u.isBlank())
                .orElse(null);
        if (source != null) {
            String fromVideo = YoutubeUrlUtil.toMqDefaultThumbnail(source);
            if (fromVideo != null) {
                return fromVideo;
            }
        }
        return course.getThumbnailUrl();
    }

    private String firstLessonYoutubeUrl(Long courseId) {
        return sectionRepository.findByCourseIdOrderByOrderNumberAsc(courseId).stream()
                .flatMap(s -> lessonRepository.findBySectionIdOrderByOrderNumberAsc(s.getId()).stream())
                .map(Lesson::getYoutubeUrl)
                .filter(u -> u != null && !u.isBlank())
                .findFirst()
                .orElse(null);
    }
}
