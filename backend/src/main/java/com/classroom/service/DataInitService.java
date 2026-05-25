package com.classroom.service;

import com.classroom.dto.*;
import com.classroom.model.*;
import com.classroom.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitService implements CommandLineRunner {

    private final EngagementService engagementService;
    private final ClassSessionRepository sessionRepository;
    private final StudentRepository studentRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing demo data...");

        // Create a demo session
        ClassSession session = engagementService.createSession(
                SessionCreateRequest.builder()
                        .sessionName("Data Structures & Algorithms")
                        .subject("Computer Science")
                        .teacherName("Dr. Priya Sharma")
                        .roomNumber("CS-301")
                        .build()
        );

        // Add 20 students
        String[] names = {
            "Aarav Mehta", "Priya Patel", "Rohan Gupta", "Sneha Reddy", "Arjun Singh",
            "Kavya Nair", "Vivek Kumar", "Ananya Sharma", "Karan Verma", "Pooja Iyer",
            "Rahul Das", "Divya Menon", "Aditya Joshi", "Riya Kapoor", "Siddharth Rao",
            "Neha Bhat", "Vikram Chandra", "Ishaan Malhotra", "Tanvi Shah", "Nikhil Desai"
        };

        String[] seats = {
            "R1C1","R1C2","R1C3","R1C4","R1C5",
            "R2C1","R2C2","R2C3","R2C4","R2C5",
            "R3C1","R3C2","R3C3","R3C4","R3C5",
            "R4C1","R4C2","R4C3","R4C4","R4C5"
        };

        for (int i = 0; i < names.length; i++) {
            String firstName = names[i].split(" ")[0].toLowerCase();
            engagementService.addStudent(
                    StudentCreateRequest.builder()
                            .name(names[i])
                            .studentId("STU" + String.format("%03d", i + 1))
                            .email(firstName + "@university.edu")
                            .seatPosition(seats[i])
                            .sessionId(session.getId())
                            .build()
            );
        }

        // Start the session
        engagementService.startSession(session.getId());

        // Mark all as present
        studentRepository.findBySessionId(session.getId()).forEach(student -> {
            engagementService.markAttendance(student.getId(), "PRESENT");
        });

        log.info("Demo data initialized! Session ID: {}, Students: {}", session.getId(), names.length);
    }
}
