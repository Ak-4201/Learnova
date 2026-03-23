package com.learnova.b2c;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * B2C JSON schema for {@code classpath:b2c/course-youtube-urls.json}.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class B2cCourseYoutubeCatalog {

    private String format;
    private String version;
    private List<B2cCourseYoutubeEntry> courses = Collections.emptyList();

    public Map<String, B2cCourseYoutubeEntry> byTitle() {
        return courses.stream()
                .filter(e -> e.getCourseTitle() != null && !e.getCourseTitle().isBlank())
                .collect(Collectors.toMap(B2cCourseYoutubeEntry::getCourseTitle, Function.identity(), (a, b) -> a));
    }

    public Optional<String> watchUrlForTitle(String courseTitle) {
        return courses.stream()
                .filter(e -> courseTitle != null && courseTitle.equals(e.getCourseTitle()))
                .map(B2cCourseYoutubeEntry::getYoutubeWatchUrl)
                .findFirst();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class B2cCourseYoutubeEntry {
        private String courseTitle;
        /** Canonical consumer YouTube watch URL (may include &list= for playlists). */
        private String youtubeWatchUrl;
    }
}
