package com.learnova.dto.lesson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonItemDto {

    private Long id;
    private String title;
    private Integer orderNumber;
    private String youtubeUrl;
    private String sectionTitle;
    private Boolean completed;
}
