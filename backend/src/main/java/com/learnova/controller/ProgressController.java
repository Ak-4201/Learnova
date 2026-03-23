package com.learnova.controller;

import com.learnova.dto.progress.ProgressRequest;
import com.learnova.dto.progress.ProgressResponse;
import com.learnova.security.UserPrincipal;
import com.learnova.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping("/courses/{courseId}/progress")
    public ResponseEntity<ProgressResponse> recordProgress(
            @PathVariable Long courseId,
            @Valid @RequestBody ProgressRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            return ResponseEntity.ok(progressService.recordProgress(principal.getId(), courseId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
