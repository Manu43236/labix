#!/usr/bin/env bash
# Start LABIX locally: MySQL → API → Web
# Usage: ./start-local.sh [api|web|db|all]  (default: all)
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"

# Load env vars
if [ -f "$ROOT/.env" ]; then
  set -a && source "$ROOT/.env" && set +a
else
  echo "ERROR: $ROOT/.env not found. Copy .env.example and fill in JWT_SECRET."
  exit 1
fi

CMD="${1:-all}"

start_db() {
  echo "→ Starting MySQL..."
  docker compose -f "$ROOT/docker-compose.yml" up -d
  echo "  Waiting for MySQL to be healthy..."
  until docker exec labix-mysql mysqladmin ping -hlocalhost --silent 2>/dev/null; do sleep 1; done
  echo "  MySQL ready."
}

start_api() {
  echo "→ Starting API on :8080..."
  cd "$ROOT/api"
  mkdir -p uploads
  mvn spring-boot:run -q
}

start_web() {
  echo "→ Starting Web on :5173..."
  cd "$ROOT/web"
  npm run dev
}

case "$CMD" in
  db)  start_db ;;
  api) start_db && start_api ;;
  web) start_web ;;
  all)
    start_db
    echo ""
    echo "Run these in separate terminals:"
    echo "  cd api  && source ../.env && export \$(cut -d= -f1 ../.env | grep -v '#' | grep -v '^$') && mvn spring-boot:run"
    echo "  cd web  && npm run dev"
    echo ""
    echo "Or just:"
    echo "  Terminal 1: ./start-local.sh api"
    echo "  Terminal 2: ./start-local.sh web"
    ;;
esac
