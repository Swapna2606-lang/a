package com.classroom.controller;

import com.classroom.dto.*;
import com.classroom.model.*;
import com.classroom.service.EngagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class SessionController {

    private final EngagementService engagementService;

    @PostMapping
    public ResponseEntity<ClassSession> createSession(@RequestBody SessionCreateRequest request) {
        return ResponseEntity.ok(engagementService.createSession(request));
    }

    @GetMapping
    public ResponseEntity<List<ClassSession>> getAllSessions() {
        return ResponseEntity.ok(engagementService.getAllSessions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassSession> getSession(@PathVariable Long id) {
        return ResponseEntity.ok(engagementService.getSession(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ClassSession> startSession(@PathVariable Long id) {
        return ResponseEntity.ok(engagementService.startSession(id));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<ClassSession> endSession(@PathVariable Long id) {
        return ResponseEntity.ok(engagementService.endSession(id));
    }

    @GetMapping("/{id}/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboard(@PathVariable Long id) {
        return ResponseEntity.ok(engagementService.getDashboardStats(id));
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<List<Student>> getStudents(@PathVariable Long id) {
        return ResponseEntity.ok(engagementService.getStudentsBySession(id));
    }
}
