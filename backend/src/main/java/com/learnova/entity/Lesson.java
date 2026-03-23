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

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", insertable = false, updatable = false)
    private Section section;

    @Column(nullable = false)
    private String title;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;

    @Column(name = "youtube_url")
    private String youtubeUrl;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;
}
