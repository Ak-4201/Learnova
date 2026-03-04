package com.learnova.config;

import com.learnova.entity.*;
import com.learnova.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        User instructor = User.builder()
            .email("instructor@learnova.com")
            .passwordHash(passwordEncoder.encode("instructor123"))
            .fullName("Jane Instructor")
            .role(User.Role.INSTRUCTOR)
            .build();
        instructor = userRepository.save(instructor);

        User admin = User.builder()
            .email("admin@learnova.com")
            .passwordHash(passwordEncoder.encode("admin123"))
            .fullName("Admin User")
            .role(User.Role.ADMIN)
            .build();
        userRepository.save(admin);

        createCourse("Java Programming", "Master Java from basics to advanced.", instructor,
            "Object-oriented programming, Collections, Streams, Spring Boot",
            "Programming", "https://img.youtube.com/vi/eWRfhZUzrAc/mqdefault.jpg",
            List.of(
                new SectionData("Getting Started", List.of(
                    new LessonData("Introduction to Java", 1, "eWRfhZUzrAc", 600),
                    new LessonData("Setting up IDE", 2, "eWRfhZUzrAc", 480)
                )),
                new SectionData("Core Java", List.of(
                    new LessonData("Variables and Data Types", 1, "eWRfhZUzrAc", 720),
                    new LessonData("Control Flow", 2, "eWRfhZUzrAc", 900)
                ))
            ));

        createCourse("Python for Beginners", "Learn Python programming from scratch.", instructor,
            "Variables, Functions, Data structures, File I/O, Basics of OOP",
            "Programming", "https://img.youtube.com/vi/_uQrJ0TkZlc/mqdefault.jpg",
            List.of(
                new SectionData("Basics", List.of(
                    new LessonData("Python Introduction", 1, "_uQrJ0TkZlc", 600),
                    new LessonData("Variables and Types", 2, "_uQrJ0TkZlc", 540)
                )),
                new SectionData("Functions", List.of(
                    new LessonData("Defining Functions", 1, "_uQrJ0TkZlc", 720)
                ))
            ));

        createCourse("Machine Learning Fundamentals", "Introduction to ML concepts and tools.", instructor,
            "Supervised learning, Regression, Classification, Neural networks basics",
            "Data Science", "https://img.youtube.com/vi/JB8T_zN7YC0/mqdefault.jpg",
            List.of(
                new SectionData("Introduction", List.of(
                    new LessonData("What is Machine Learning?", 1, "JB8T_zN7YC0", 720),
                    new LessonData("Types of ML", 2, "JB8T_zN7YC0", 600)
                )),
                new SectionData("Hands-On", List.of(
                    new LessonData("Your First Model", 1, "JB8T_zN7YC0", 900)
                ))
            ));

        createCourse("Web Development with React", "Build modern UIs with React.", instructor,
            "Components, Hooks, State, Routing, API integration",
            "Web Development", "https://img.youtube.com/vi/SqcY0GlETpg/mqdefault.jpg",
            List.of(
                new SectionData("React Basics", List.of(
                    new LessonData("React Overview", 1, "SqcY0GlETpg", 600),
                    new LessonData("Components", 2, "SqcY0GlETpg", 800)
                ))
            ));
    }

    private void createCourse(String title, String description, User instructor, String whatYouWillLearn,
                              String category, String thumbnailUrl, List<SectionData> sections) {
        Course course = Course.builder()
            .title(title)
            .description(description)
            .whatYouWillLearn(whatYouWillLearn)
            .category(category)
            .thumbnailUrl(thumbnailUrl)
            .instructor(instructor)
            .build();
        course = courseRepository.save(course);
        int sectionOrder = 0;
        for (SectionData sd : sections) {
            Section section = Section.builder()
                .title(sd.title)
                .orderNumber(++sectionOrder)
                .course(course)
                .build();
            section = sectionRepository.save(section);
            int lessonOrder = 0;
            for (LessonData ld : sd.lessons) {
                Lesson lesson = Lesson.builder()
                    .title(ld.title)
                    .orderNumber(++lessonOrder)
                    .youtubeUrl(ld.youtubeVideoId)
                    .durationSeconds(ld.durationSeconds)
                    .section(section)
                    .build();
                lessonRepository.save(lesson);
            }
        }
    }

    private record SectionData(String title, List<LessonData> lessons) {}
    private record LessonData(String title, int order, String youtubeVideoId, int durationSeconds) {}
}
