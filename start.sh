#!/bin/bash

echo "🎓 Starting ClassroomIQ - Real-Time Classroom Intelligence System"
echo "=================================================================="

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java 17+ is required. Install from https://adoptium.net/"
    exit 1
fi

# Check Node
if ! command -v node &> /dev/null; then
    echo "❌ Node.js 18+ is required. Install from https://nodejs.org/"
    exit 1
fi

# Start Backend
echo ""
echo "📦 Starting Spring Boot backend on port 8080..."
cd backend && mvn spring-boot:run -q &
BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"

# Wait for backend to start
echo "⏳ Waiting for backend to initialize..."
sleep 12

# Start Frontend
echo ""
echo "⚡ Starting React frontend on port 5173..."
cd ../frontend && npm install -s && npm run dev &
FRONTEND_PID=$!

echo ""
echo "✅ ClassroomIQ is running!"
echo "   Frontend: http://localhost:5173"
echo "   Backend:  http://localhost:8080"
echo "   API Docs: http://localhost:8080/api/sessions"
echo "   H2 DB:    http://localhost:8080/h2-console"
echo ""
echo "Press Ctrl+C to stop all services"

# Wait for Ctrl+C
trap "echo '🛑 Shutting down...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit" INT
wait
