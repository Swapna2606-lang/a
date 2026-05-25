package com.classroom.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionResponse {
    private Long id;
    private String sessionName;
    private String subject;
    private String teacherName;
    private String roomNumber;
    private String status;
    private Double averageEngagement;
    private Double teacherEffectivenessScore;
    private Integer totalStudents;
    private Integer presentStudents;
    private Integer activeParticipants;
    private LocalDateTime startTime;
    private LocalDateTime createdAt;
}
