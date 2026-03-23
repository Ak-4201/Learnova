package com.learnova.config;

import com.learnova.b2c.B2cCourseYoutubeCatalog;
import com.learnova.b2c.B2cCourseYoutubeCatalogLoader;
import com.learnova.b2c.YoutubeUrlUtil;
import com.learnova.entity.Course;
import com.learnova.entity.Lesson;
import com.learnova.repository.CourseRepository;
import com.learnova.repository.LessonRepository;
import com.learnova.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Applies B2C YouTube URLs and thumbnails to courses on every startup so DB stays aligned with
 * {@code b2c/course-youtube-urls.json} (including environments seeded before URLs were added).
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class B2cCourseMediaSyncRunner implements CommandLineRunner {

    private final B2cCourseYoutubeCatalogLoader catalogLoader;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;

    @Override
    @Transactional
    public void run(String... args) {
        B2cCourseYoutubeCatalog cat = catalogLoader.getCatalog();
        if (cat.getCourses() == null || cat.getCourses().isEmpty()) {
            log.warn("B2C media sync skipped: catalog has no courses (check {} on classpath and rebuild).",
                    "b2c/course-youtube-urls.json");
            return;
        }
        int synced = 0;
        int missing = 0;
        for (B2cCourseYoutubeCatalog.B2cCourseYoutubeEntry entry : cat.getCourses()) {
            if (entry.getCourseTitle() == null || entry.getYoutubeWatchUrl() == null) {
                continue;
            }
            String title = entry.getCourseTitle().trim();
            Optional<Course> course = courseRepository.findByTitle(title)
                    .or(() -> courseRepository.findFirstByTitleIgnoreCase(title));
            if (course.isEmpty()) {
                log.warn("B2C: no course in DB matching title \"{}\" — create/seed course or fix JSON title.", title);
                missing++;
                continue;
            }
            applyToCourse(course.get(), entry.getYoutubeWatchUrl().trim());
            synced++;
        }
        log.info("B2C media sync finished: {} course(s) updated, {} unmatched catalog title(s).", synced, missing);
    }

    private void applyToCourse(Course course, String watchUrl) {
        String thumb = YoutubeUrlUtil.toMqDefaultThumbnail(watchUrl);
        if (thumb != null) {
            course.setThumbnailUrl(thumb);
            courseRepository.save(course);
        }
        sectionRepository.findByCourseIdOrderByOrderNumberAsc(course.getId()).forEach(section -> {
            List<Lesson> lessons = lessonRepository.findBySectionIdOrderByOrderNumberAsc(section.getId());
            for (Lesson lesson : lessons) {
                lesson.setYoutubeUrl(watchUrl);
                lessonRepository.save(lesson);
            }
        });
    }
}
