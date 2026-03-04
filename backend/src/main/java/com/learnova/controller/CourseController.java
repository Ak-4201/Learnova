package com.learnova.controller;

import com.learnova.dto.course.CourseDto;
import com.learnova.security.UserPrincipal;
import com.learnova.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<List<CourseDto>> listCourses(
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(courseService.listCourses(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> getCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        try {
            return ResponseEntity.ok(courseService.getCourseById(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
