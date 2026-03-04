package com.learnova.controller;

import com.learnova.dto.progress.ProgressRequest;
import com.learnova.dto.progress.ProgressResponse;
import com.learnova.security.UserPrincipal;
import com.learnova.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping("/{courseId}/progress")
    public ResponseEntity<ProgressResponse> recordProgress(
            @PathVariable Long courseId,
            @RequestBody ProgressRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(progressService.recordProgress(principal.getId(), courseId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{courseId}/lessons/{lessonId}/watch")
    public ResponseEntity<Void> updateLastWatched(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        progressService.updateLastWatched(principal.getId(), lessonId);
        return ResponseEntity.ok().build();
    }
}
