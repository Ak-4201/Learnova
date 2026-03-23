package com.learnova.b2c;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Loads the B2C YouTube URL catalog from {@code b2c/course-youtube-urls.json}.
 */
@Slf4j
@Component
@Getter
public class B2cCourseYoutubeCatalogLoader {

    private static final String RESOURCE = "b2c/course-youtube-urls.json";

    private final B2cCourseYoutubeCatalog catalog;

    public B2cCourseYoutubeCatalogLoader(ObjectMapper objectMapper) {
        B2cCourseYoutubeCatalog loaded = new B2cCourseYoutubeCatalog();
        ClassPathResource resource = new ClassPathResource(RESOURCE);
        if (!resource.exists()) {
            log.warn("B2C catalog missing: {}", RESOURCE);
            this.catalog = loaded;
            return;
        }
        try (InputStream in = resource.getInputStream()) {
            loaded = objectMapper.readValue(in, B2cCourseYoutubeCatalog.class);
            int n = loaded.getCourses() != null ? loaded.getCourses().size() : 0;
            log.info("B2C catalog loaded from {}: {} course(s), format={}", RESOURCE, n, loaded.getFormat());
        } catch (Exception e) {
            log.error("Failed to load B2C catalog {}: {}", RESOURCE, e.getMessage(), e);
        }
        this.catalog = loaded;
    }
}
