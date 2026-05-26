#!/bin/bash

echo "🎓 Starting ClassroomIQ - Real-Time Classroom Intelligence System"
echo "=================================================================="

# Check Java
find_java_17() {
    for candidate in "/usr/lib/jvm/java-17-openjdk-amd64" "/usr/lib/jvm/java-17-openjdk" "/usr/lib/jvm/java-17-openjdk-amd64" "/usr/local/sdkman/candidates/java/current"; do
        if [ -x "$candidate/bin/java" ]; then
            local ver=$("$candidate/bin/java" -version 2>&1 | awk -F '"' 'NR==1 {print $2; exit}')
            local major=$(echo "$ver" | awk -F[._-] '{print $1}')
            if [ "$major" -ge 17 ]; then
                echo "$candidate"
                return 0
            fi
        fi
    done
    if [ -d "/usr/lib/jvm" ]; then
        for candidate in /usr/lib/jvm/*; do
            if [ -x "$candidate/bin/java" ]; then
                local ver=$("$candidate/bin/java" -version 2>&1 | awk -F '"' 'NR==1 {print $2; exit}')
                local major=$(echo "$ver" | awk -F[._-] '{print $1}')
                if [ "$major" -ge 17 ]; then
                    echo "$candidate"
                    return 0
                fi
            fi
        done
    fi
    return 1
}

JAVA_HOME=${JAVA_HOME:-}
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    java_ver=$("$JAVA_HOME/bin/java" -version 2>&1 | awk -F '"' 'NR==1 {print $2; exit}')
    java_major=$(echo "$java_ver" | awk -F[._-] '{print $1}')
    if [ -z "$java_major" ] || [ "$java_major" -lt 17 ]; then
        JAVA_HOME=
    fi
fi

if [ -z "$JAVA_HOME" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
    JAVA_HOME="$(find_java_17)"
fi

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    export JAVA_HOME
    export PATH="$JAVA_HOME/bin:$PATH"
fi

if ! command -v java &> /dev/null; then
    echo "❌ Java 17+ is required. Install from https://adoptium.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' 'NR==1 {print $2; exit}')
JAVA_MAJOR=$(echo "$JAVA_VERSION" | awk -F[._-] '{print $1}')
if [ -z "$JAVA_MAJOR" ] || [ "$JAVA_MAJOR" -lt 17 ]; then
    echo "❌ Java 17+ is required. Found Java $JAVA_VERSION."
    if [ -n "$JAVA_HOME" ]; then
        echo "   JAVA_HOME is currently set to $JAVA_HOME"
    fi
    echo "   If Java 17 is installed, set JAVA_HOME and update PATH before running this script."
    echo "   Example: export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64"
    echo "            export PATH=\"$JAVA_HOME/bin:$PATH\""
    exit 1
fi

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven 3.8+ is required to build the backend. Install Maven and try again."
    exit 1
fi
MAVEN_VERSION=$(mvn -v 2>/dev/null | head -n 1)

# Check Node
if ! command -v node &> /dev/null; then
    echo "❌ Node.js 18+ is required. Install from https://nodejs.org/"
    exit 1
fi
NODE_VERSION=$(node -v)

echo "✅ Java detected: $JAVA_VERSION"
echo "✅ Maven detected: $MAVEN_VERSION"
echo "✅ Node detected: $NODE_VERSION"

ROOT_DIR="$(pwd)"

# Start Backend
echo ""
echo "📦 Starting Spring Boot backend on port 8080..."
(
  cd "$ROOT_DIR/backend" || exit 1
  mvn spring-boot:run -q
) &
BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"

echo "⏳ Waiting for backend to initialize..."
for i in $(seq 1 20); do
  if curl -s http://127.0.0.1:8080/ >/dev/null 2>&1; then
    echo "✅ Backend is responding on port 8080."
    break
  fi
  if ! kill -0 "$BACKEND_PID" 2>/dev/null; then
    echo "❌ Backend process exited unexpectedly. Check backend logs."
    exit 1
  fi
  sleep 1
done

if ! curl -s http://127.0.0.1:8080/ >/dev/null 2>&1; then
  echo "❌ Backend failed to start on port 8080."
  exit 1
fi

# Start Frontend
echo ""
echo "⚡ Starting React frontend on port 5173..."
(
  cd "$ROOT_DIR/frontend" || exit 1
  npm install -s
  npm run dev -- --host 0.0.0.0 --port 5173 --strictPort
) &
FRONTEND_PID=$!

echo "⏳ Waiting for frontend to initialize..."
for i in $(seq 1 20); do
  if curl -s http://127.0.0.1:5173/ >/dev/null 2>&1; then
    echo "✅ Frontend is responding on port 5173."
    break
  fi
  if ! kill -0 "$FRONTEND_PID" 2>/dev/null; then
    echo "❌ Frontend process exited unexpectedly. Check frontend logs."
    kill $BACKEND_PID 2>/dev/null || true
    exit 1
  fi
  sleep 1
done

if ! curl -s http://127.0.0.1:5173/ >/dev/null 2>&1; then
  echo "❌ Frontend failed to start on port 5173."
  kill $BACKEND_PID $FRONTEND_PID 2>/dev/null || true
  exit 1
fi

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
