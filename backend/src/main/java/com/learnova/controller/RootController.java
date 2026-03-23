package com.learnova.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * So opening http://localhost:8081/ in a browser shows a clear response (UI runs on Vite, e.g. :5173).
 */
@RestController
public class RootController {

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> root() {
        return Map.of(
                "service", "Learnova API",
                "hint", "Course listing: GET /api/courses — SPA: run frontend (npm run dev)"
        );
    }
}
