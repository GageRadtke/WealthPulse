#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cleanup() {
  kill "${BACKEND_PID:-}" "${FRONTEND_PID:-}" 2>/dev/null || true
}

trap cleanup EXIT INT TERM

(
  cd "$ROOT_DIR/backend"
  ./mvnw spring-boot:run
) &
BACKEND_PID=$!

(
  cd "$ROOT_DIR/frontend"
  npm run dev
) &
FRONTEND_PID=$!

wait
