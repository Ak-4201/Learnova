package com.learnova.controller;

import com.learnova.dto.lesson.LessonDto;
import com.learnova.dto.lesson.LessonListResponse;
import com.learnova.security.UserPrincipal;
import com.learnova.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<LessonListResponse> getLessons(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        try {
            return ResponseEntity.ok(lessonService.getLessonsByCourseId(courseId, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonDto> getLesson(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            return ResponseEntity.ok(lessonService.getLessonById(lessonId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
