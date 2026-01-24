#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

cleanup() {
    log_info "Cleaning up..."
    docker compose down -v --remove-orphans 2>/dev/null || true
}

BUILD_WEBAPI=false
KEEP_RUNNING=false
VERBOSE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --build)
            BUILD_WEBAPI=true
            shift
            ;;
        --keep)
            KEEP_RUNNING=true
            shift
            ;;
        --verbose|-v)
            VERBOSE=true
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [options]"
            echo ""
            echo "Options:"
            echo "  --build     Build WebAPI before running tests"
            echo "  --keep      Keep services running after tests"
            echo "  --verbose   Show verbose output"
            echo "  --help      Show this help message"
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

if [ "$KEEP_RUNNING" = false ]; then
    trap cleanup EXIT
fi

if [ "$BUILD_WEBAPI" = true ]; then
    log_info "Building WebAPI..."
    cd ../..
    mvn clean package -DskipTests -Dpackaging.type=jar -P webapi-postgresql,trexsql -B
    cd "$SCRIPT_DIR"
fi

mkdir -p test-results
chmod 777 test-results

log_info "Cleaning up previous runs..."
docker compose down -v --remove-orphans 2>/dev/null || true

log_info "Starting PostgreSQL and mock-oauth2-server..."
docker compose up -d postgres mock-oauth2

log_info "Waiting for PostgreSQL to be ready..."
timeout 60 bash -c 'until docker compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; do sleep 2; done'

log_info "Starting WebAPI..."
docker compose up -d webapi

log_info "Waiting for WebAPI to be healthy..."
timeout 300 bash -c 'until curl -sf http://localhost:18080/WebAPI/info > /dev/null 2>&1; do sleep 10; echo -n "."; done'
echo ""
log_info "WebAPI is ready!"

log_info "Setting up test users..."
docker compose up db-setup

log_info "Running authentication tests..."
if [ "$VERBOSE" = true ]; then
    docker compose up newman
    NEWMAN_EXIT=$?
else
    docker compose up newman 2>&1 | grep -E "(✓|✗|→|Newman|iteration|total|failed|executed)"
    NEWMAN_EXIT=${PIPESTATUS[0]}
fi

if [ -f test-results/auth-test-results.xml ]; then
    log_info "Test results saved to test-results/auth-test-results.xml"

    TOTAL=$(grep -o 'tests="[0-9]*"' test-results/auth-test-results.xml | head -1 | grep -o '[0-9]*')
    FAILURES=$(grep -o 'failures="[0-9]*"' test-results/auth-test-results.xml | head -1 | grep -o '[0-9]*')

    if [ "$FAILURES" = "0" ]; then
        log_info "All $TOTAL tests passed!"
    else
        log_error "$FAILURES of $TOTAL tests failed"
        exit 1
    fi
else
    log_warn "Test results file not found"
    if [ "$NEWMAN_EXIT" != "0" ]; then
        exit $NEWMAN_EXIT
    fi
fi

if [ "$KEEP_RUNNING" = true ]; then
    log_info "Services are still running. Access:"
    echo "  - WebAPI:         http://localhost:18080/WebAPI/info"
    echo "  - mock-oauth2:    http://localhost:9090/default/.well-known/openid-configuration"
    echo ""
    echo "Run 'docker compose down -v' in this directory to stop services."
fi
