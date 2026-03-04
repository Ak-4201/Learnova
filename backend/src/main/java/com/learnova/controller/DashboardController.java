package com.learnova.controller;

import com.learnova.dto.course.EnrolledCourseDto;
import com.learnova.security.UserPrincipal;
import com.learnova.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final CourseService courseService;

    @GetMapping("/dashboard/enrollments")
    public ResponseEntity<List<EnrolledCourseDto>> getMyEnrollments(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(courseService.getEnrolledCourses(principal.getId()));
    }
}
