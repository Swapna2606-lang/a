package com.classroom.controller;

import com.classroom.dto.*;
import com.classroom.model.Student;
import com.classroom.service.EngagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class StudentController {

    private final EngagementService engagementService;

    @PostMapping
    public ResponseEntity<Student> addStudent(@RequestBody StudentCreateRequest request) {
        return ResponseEntity.ok(engagementService.addStudent(request));
    }

    @PutMapping("/{id}/attendance")
    public ResponseEntity<Student> markAttendance(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(engagementService.markAttendance(id, status));
    }

    @PutMapping("/{id}/raise-hand")
    public ResponseEntity<Student> raiseHand(@PathVariable Long id) {
        return ResponseEntity.ok(engagementService.raiseHand(id));
    }

    @PutMapping("/engagement")
    public ResponseEntity<Student> updateEngagement(@RequestBody EngagementUpdateRequest request) {
        return ResponseEntity.ok(engagementService.updateEngagement(request));
    }
}
