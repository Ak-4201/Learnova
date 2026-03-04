package com.learnova.dto.lesson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {
    private Long id;
    private String title;
    private Integer orderNumber;
    /** YouTube URL or video ID for iframe src */
    private String youtubeUrl;
    private Integer durationSeconds;
    private Long sectionId;
    private String sectionTitle;
    private Boolean completed;
}
