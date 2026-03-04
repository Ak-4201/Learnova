package com.learnova.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer orderNumber;

    /**
     * YouTube video URL or video ID (e.g. dQw4w9WgXcQ).
     * Backend stores only this; frontend embeds via iframe.
     */
    @Column(name = "youtube_url", nullable = false, length = 500)
    private String youtubeUrl;

    /** Duration in seconds (optional, for display). */
    private Integer durationSeconds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;
}
