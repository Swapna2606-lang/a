# 🎓 ClassroomIQ – Real-Time Classroom Intelligence System

> AI-powered platform that analyzes student engagement, emotion, and attention in real-time.

---

## 🏗️ Architecture

```
classroom-intelligence/
├── backend/                          # Spring Boot 3 (Java 17)
│   └── src/main/java/com/classroom/
│       ├── model/                    # JPA Entities
│       │   ├── Student.java          # Student with engagement data
│       │   ├── ClassSession.java     # Class session management
│       │   └── EngagementEvent.java  # Event log
│       ├── repository/               # Spring Data JPA Repositories
│       ├── service/
│       │   ├── EngagementService.java  # Core business logic + AI simulation
│       │   └── DataInitService.java    # Demo data seeder
│       ├── controller/
│       │   ├── SessionController.java  # REST: /api/sessions
│       │   └── StudentController.java  # REST: /api/students
│       └── config/
│           ├── WebSocketConfig.java    # STOMP over SockJS
│           └── SecurityConfig.java     # CORS + Security
│
├── frontend/                         # React 18 + Vite
│   └── src/
│       ├── services/api.js           # Axios API client
│       ├── App.jsx                   # Root + Header + Session Nav
│       ├── App.css                   # Dark theme styles
│       └── pages/
│           └── Dashboard.jsx         # Main teacher dashboard
│
└── README.md
```

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+

### 1. Start Backend

```bash
cd backend
mvn spring-boot:run
```

Backend runs at: **http://localhost:8080**  
H2 Console: **http://localhost:8080/h2-console**

> Auto-seeds 20 students in an active session on startup!

### 2. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at: **http://localhost:5173**

---

## 📡 REST API Reference

### Sessions
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/sessions` | List all sessions |
| POST | `/api/sessions` | Create new session |
| GET | `/api/sessions/{id}` | Get session details |
| POST | `/api/sessions/{id}/start` | Start session |
| POST | `/api/sessions/{id}/end` | End session |
| GET | `/api/sessions/{id}/dashboard` | Dashboard stats |
| GET | `/api/sessions/{id}/students` | Session students |

### Students
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/students` | Add student |
| PUT | `/api/students/{id}/attendance?status=PRESENT` | Mark attendance |
| PUT | `/api/students/{id}/raise-hand` | Record hand raise |
| PUT | `/api/students/engagement` | Update engagement data |

### WebSocket (STOMP)
- **Connect:** `/ws` (SockJS)
- **Subscribe:** `/topic/session/{id}/students` — real-time student updates
- **Subscribe:** `/topic/session/{id}/status` — session status changes

---

## 🧠 Core Features

### ✅ Core (Implemented)
- **Classroom Analytics Dashboard** — live engagement, attention, effectiveness rings
- **Attendance Automation** — mark present/absent/late with auto-timestamp
- **Engagement Scoring** — 0–100 score per student, updated every 5 seconds
- **Student Participation Tracking** — hand raises, question counts, active flags
- **Teacher Insights Panel** — top students, at-risk alerts, confusion detection

### 🔬 Advanced (AI Simulation Included)
- **Emotion/Attention Detection** — simulates 7 emotion states (ENGAGED, CONFUSED, BORED, etc.)
- **Real-Time Confusion Detection** — alerts when 3+ students show confusion
- **AI Teaching Effectiveness Analysis** — weighted score from engagement + attention + participation
- **Smart Seating Analytics** — color-coded grid map of all student seats
- **Voice Interaction Analytics** — event type tracking via API

---

## 🎨 Dashboard Tabs

| Tab | Features |
|-----|----------|
| **Overview** | Live engagement charts, emotion pie chart, smart alerts |
| **Seating** | 4×5 color-coded seating chart with real-time scores |
| **Students** | Individual cards with engagement bars, emotion badges |
| **Insights** | Leaderboard, at-risk list, teacher effectiveness breakdown |

---

## 🔧 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3, Spring Data JPA, Spring WebSocket |
| Database | H2 (dev) → MySQL/PostgreSQL (production) |
| Security | Spring Security, CORS, JWT-ready |
| Real-time | STOMP + SockJS WebSocket |
| Frontend | React 18, Vite 5 |
| Charts | Recharts |
| HTTP | Axios |
| Styling | Custom CSS (dark theme) |

---

## 🏭 Production Upgrade Path

### Replace H2 with MySQL
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/classroomdb
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

### Add Real AI (OpenCV + TensorFlow)
- Replace `simulateEngagementUpdates()` in `EngagementService` with actual video frame processing
- Use OpenCV for face detection, TensorFlow for emotion classification
- Stream frames via WebRTC from the browser

### Add JWT Authentication
```java
// Add to SecurityConfig:
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
```

---

## 📊 Judging Criteria Coverage

| Criteria | Implementation |
|----------|---------------|
| Engagement/emotion accuracy | AI simulation engine with 7 states |
| Real-time analytics | 3-second polling + WebSocket push |
| Dashboard usability | 4-tab teacher-first design |
| Attendance automation | REST API + auto-timestamp |
| Platform scalability | Stateless REST + WebSocket sessions |
| Real-time AI responsiveness | 5-second background scheduler |
| Ethical data handling | No PII beyond name/email; no recording |
| Online & offline support | REST APIs work standalone |
