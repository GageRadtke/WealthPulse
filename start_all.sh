#!/bin/bash
# Local dev defaults: backend listens on port 8283 and frontend talks to it via VITE_API_URL.
# Local dev defaults; sensitive values should come from .env or the environment.
export DB_USERNAME=${DB_USERNAME:-postgres}
export DB_PASSWORD=${DB_PASSWORD:-}
export DB_URL=${DB_URL:-jdbc:postgresql://localhost:5432/wealthpulse}
export ALPHA_VANTAGE_KEY=${ALPHA_VANTAGE_KEY:-}
export GOLD_API_KEY=${GOLD_API_KEY:-}
export JWT_SECRET=${JWT_SECRET:-wealthpulse-local-development-secret-change-before-deployment}
export SERVER_PORT=${SERVER_PORT:-8283}
export VITE_API_URL=${VITE_API_URL:-http://localhost:8283}

# 1. Get the directory where this script lives
SCRIPT_DIR="/home/gage/WealthPulse"

# 2. Look for the .env file in the root or backend folder and load it into the shell environment
if [ -f "$SCRIPT_DIR/.env" ]; then
    echo "Loading environment variables from root .env..."
    export $(grep -v '^#' "$SCRIPT_DIR/.env" | xargs)
elif [ -f "$SCRIPT_DIR/backend/.env" ]; then
    echo "Loading environment variables from backend .env..."
    export $(grep -v '^#' "$SCRIPT_DIR/backend/.env" | xargs)
else
    echo "WARNING: No .env configuration file found!"
fi

# 3. Spin up Frontend (Vite)
cd "$SCRIPT_DIR/frontend" && npm run dev &

# 4. Spin up Backend (Spring Boot)
cd "$SCRIPT_DIR/backend" && ./mvnw spring-boot:run -e
