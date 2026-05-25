package com.classroom.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentResponse {
    private Long id;
    private String name;
    private String studentId;
    private String email;
    private String seatPosition;
    private String attendanceStatus;
    private Double engagementScore;
    private Double attentionLevel;
    private String currentEmotion;
    private Boolean hasRaisedHand;
    private Boolean isParticipating;
    private Integer participationCount;
    private LocalDateTime lastActiveAt;
}
