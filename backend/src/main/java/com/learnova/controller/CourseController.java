package com.learnova.controller;

import com.learnova.dto.course.CourseDto;
import com.learnova.security.UserPrincipal;
import com.learnova.service.CourseService;
import com.learnova.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    @GetMapping("/courses")
    public ResponseEntity<List<CourseDto>> listCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(courseService.listCourses(userId, search, category));
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<CourseDto> getCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        try {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(courseService.getCourseById(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/courses/{courseId}/enroll")
    public ResponseEntity<Void> enroll(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            enrollmentService.enroll(principal.getId(), courseId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
