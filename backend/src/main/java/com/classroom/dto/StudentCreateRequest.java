package com.classroom.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentCreateRequest {
    private String name;
    private String studentId;
    private String email;
    private String seatPosition;
    private Long sessionId;
}
