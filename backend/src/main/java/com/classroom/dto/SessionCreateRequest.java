package com.classroom.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionCreateRequest {
    private String sessionName;
    private String subject;
    private String teacherName;
    private String roomNumber;
}
