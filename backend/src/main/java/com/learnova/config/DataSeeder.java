package com.learnova.config;

import com.learnova.b2c.B2cCourseYoutubeCatalogLoader;
import com.learnova.b2c.YoutubeUrlUtil;
import com.learnova.entity.*;
import com.learnova.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String PLACEHOLDER_VIDEO_ID = "dQw4w9WgXcQ";
    private static final String FALLBACK_WATCH_URL = "https://www.youtube.com/watch?v=" + PLACEHOLDER_VIDEO_ID;
    private static final String FALLBACK_THUMB = "https://img.youtube.com/vi/" + PLACEHOLDER_VIDEO_ID + "/mqdefault.jpg";

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final PasswordEncoder passwordEncoder;
    private final B2cCourseYoutubeCatalogLoader b2cCatalogLoader;

    @Override
    @Transactional
    public void run(String... args) {
        // Do not exit early when count >= 10: B2cCourseMediaSyncRunner updates URLs/thumbnails for
        // existing rows; we only skip creating courses that already exist (per title below).
        User instructor = userRepository.findByEmail("instructor@learnova.com")
                .orElseGet(() -> {
                    User u = User.builder()
                            .email("instructor@learnova.com")
                            .passwordHash(passwordEncoder.encode("instructor123"))
                            .fullName("Demo Instructor")
                            .role(User.Role.INSTRUCTOR)
                            .build();
                    return userRepository.save(u);
                });

        long instructorId = instructor.getId();

        List<CourseSpec> specs = List.of(
                new CourseSpec("Python for Beginners", "Learn Python from scratch. Covers syntax, variables, loops, and functions.",
                        "Variables, Data types, Loops, Functions, Basic I/O", "Programming"),
                new CourseSpec("Advanced Python", "Deep dive into Python: decorators, generators, async, and best practices.",
                        "Decorators, Generators, Async/Await, Testing, Packaging", "Programming"),
                new CourseSpec("Fullstack using Python", "Build full-stack web applications with Python, Django/Flask, and front-end basics.",
                        "Django/Flask, REST APIs, Templates, Database ORM, Deployment", "Web Development"),
                new CourseSpec("Fullstack using Java", "End-to-end web development with Java, Spring Boot, and modern front-end.",
                        "Spring Boot, REST, Security, JPA, Thymeleaf/React", "Web Development"),
                new CourseSpec("MERN Stack Development", "Master MongoDB, Express, React, and Node.js to build modern web apps.",
                        "Node.js, Express, React, MongoDB, JWT Auth", "Web Development"),
                new CourseSpec("Data Structures for Beginners", "Introduction to arrays, linked lists, stacks, queues, and basic algorithms.",
                        "Arrays, Linked Lists, Stacks, Queues, Big O", "Data Structures"),
                new CourseSpec("Advanced DSA", "Advanced data structures and algorithms: trees, graphs, dynamic programming.",
                        "Trees, Graphs, DP, Greedy, Advanced Problem Solving", "Data Structures"),
                new CourseSpec("Machine Learning Fundamentals", "Core ML concepts: regression, classification, and intro to neural networks.",
                        "Regression, Classification, Clustering, Model Evaluation", "Data Science"),
                new CourseSpec("React Development", "Build interactive UIs with React: components, hooks, state, and routing.",
                        "Components, Hooks, State, React Router, API Integration", "Web Development"),
                new CourseSpec("Java Backend Development", "Backend development with Java: Spring Boot, REST APIs, and databases.",
                        "Spring Boot, REST APIs, JPA, Security, Microservices", "Backend")
        );

        for (CourseSpec spec : specs) {
            if (courseRepository.findAllByOrderByIdAsc().stream().anyMatch(c -> c.getTitle().equals(spec.title))) {
                continue;
            }
            String watchUrl = b2cCatalogLoader.getCatalog().watchUrlForTitle(spec.title).orElse(FALLBACK_WATCH_URL);
            String thumb = YoutubeUrlUtil.toMqDefaultThumbnail(watchUrl);
            if (thumb == null) {
                thumb = FALLBACK_THUMB;
            }
            Course course = Course.builder()
                    .title(spec.title)
                    .description(spec.description)
                    .whatYouWillLearn(spec.whatYouWillLearn)
                    .thumbnailUrl(thumb)
                    .category(spec.category)
                    .instructorId(instructorId)
                    .build();
            course = courseRepository.save(course);

            Section section = Section.builder()
                    .courseId(course.getId())
                    .title("Getting Started")
                    .orderNumber(1)
                    .build();
            section = sectionRepository.save(section);

            lessonRepository.saveAll(List.of(
                    Lesson.builder()
                            .sectionId(section.getId())
                            .title("Introduction to " + spec.title)
                            .orderNumber(1)
                            .youtubeUrl(watchUrl)
                            .durationSeconds(600)
                            .build(),
                    Lesson.builder()
                            .sectionId(section.getId())
                            .title("Core Concepts")
                            .orderNumber(2)
                            .youtubeUrl(watchUrl)
                            .durationSeconds(720)
                            .build()
            ));
        }
    }

    private record CourseSpec(String title, String description, String whatYouWillLearn, String category) {}
}
