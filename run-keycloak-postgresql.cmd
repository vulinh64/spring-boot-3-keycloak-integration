@echo off
setlocal enabledelayedexpansion

:: Configuration Variables
SET NETWORK_NAME=keycloak-network
SET VOLUME_NAME=keycloak-postgres-volume
SET PG_CONTAINER_NAME=standalone-postgresql
SET KC_CONTAINER_NAME=standalone-keycloak

SET DB_NAME=keycloak
SET DB_USER=postgres
SET DB_PASSWORD=123456
SET PG_IMAGE=postgres:17.5-alpine

SET KC_IMAGE=quay.io/keycloak/keycloak:26.3
SET KC_ADMIN_USER=admin
SET KC_ADMIN_PASSWORD=admin

SET PG_PORT=5432
SET KC_HTTP_PORT=8080
SET KC_MGMT_PORT=9000

SET HEALTH_INTERVAL=10s
SET HEALTH_TIMEOUT=5s
SET HEALTH_RETRIES=5
SET HEALTH_CHECK_TIMEOUT=60

:: Check if Docker is installed
where docker >nul 2>&1
if !errorlevel! neq 0 (
    echo ERROR: Docker is not installed. Please install Docker and try again.
    exit /b 1
)

:: Check if Docker daemon is running
docker info >nul 2>&1
if !errorlevel! neq 0 (
    echo ERROR: Docker daemon is not running. Please start Docker and try again.
    exit /b 1
)

:: Check if network exists, create it only if it doesn't
docker network ls | findstr !NETWORK_NAME! >nul
if !errorlevel! equ 0 (
    echo Network !NETWORK_NAME! already exists, skipping creation...
) else (
    echo Creating network !NETWORK_NAME!...
    docker network create !NETWORK_NAME!
    if !errorlevel! neq 0 (
        echo ERROR: Failed to create network !NETWORK_NAME!.
        exit /b 1
    )
)

:: Check if PostgreSQL container exists and remove it if it does
docker ps -a | findstr !PG_CONTAINER_NAME! >nul
if !errorlevel! equ 0 (
    echo Removing existing PostgreSQL container !PG_CONTAINER_NAME!...
    docker rm -f !PG_CONTAINER_NAME!
    if !errorlevel! neq 0 (
        echo ERROR: Failed to remove PostgreSQL container !PG_CONTAINER_NAME!.
        exit /b 1
    )
)

:: Create PostgreSQL container
echo Creating PostgreSQL container !PG_CONTAINER_NAME!...
docker run -d --name !PG_CONTAINER_NAME! --network !NETWORK_NAME! -v !VOLUME_NAME!:/var/lib/postgresql/data -e POSTGRES_DB=!DB_NAME! -e POSTGRES_USER=!DB_USER! -e POSTGRES_PASSWORD=!DB_PASSWORD! -p !PG_PORT!:!PG_PORT! --health-cmd="pg_isready -U !DB_USER! -d !DB_NAME!" --health-interval=!HEALTH_INTERVAL! --health-timeout=!HEALTH_TIMEOUT! --health-retries=!HEALTH_RETRIES! !PG_IMAGE!
if !errorlevel! neq 0 (
    echo ERROR: Failed to create PostgreSQL container !PG_CONTAINER_NAME!.
    exit /b 1
)

:: Wait for PostgreSQL to be healthy with timeout
echo Waiting for PostgreSQL to be healthy...
set /a MAX_WAIT=%HEALTH_CHECK_TIMEOUT%
set /a WAIT_COUNT=0
:CHECK_PG_HEALTH
timeout /t 2 /nobreak >nul
docker inspect --format="{{.State.Health.Status}}" !PG_CONTAINER_NAME! | findstr "healthy" >nul
if !errorlevel! equ 0 (
    echo PostgreSQL is healthy!
    goto :PG_HEALTHY
)
set /a WAIT_COUNT+=2
if !WAIT_COUNT! geq %HEALTH_CHECK_TIMEOUT% (
    echo ERROR: PostgreSQL failed to become healthy within %HEALTH_CHECK_TIMEOUT% seconds.
    exit /b 1
)
echo PostgreSQL is not yet healthy, checking again...
goto :CHECK_PG_HEALTH
:PG_HEALTHY

:: Check if Keycloak container exists and remove it if it does
docker ps -a | findstr !KC_CONTAINER_NAME! >nul
if !errorlevel! equ 0 (
    echo Removing existing Keycloak container !KC_CONTAINER_NAME!...
    docker rm -f !KC_CONTAINER_NAME!
    if !errorlevel! neq 0 (
        echo ERROR: Failed to remove Keycloak container !KC_CONTAINER_NAME!.
        exit /b 1
    )
)

:: Create Keycloak container
echo Creating Keycloak container !KC_CONTAINER_NAME!...
docker run -d --name !KC_CONTAINER_NAME! --network !NETWORK_NAME! -e KC_DB=postgres -e KC_DB_URL_HOST=!PG_CONTAINER_NAME! -e KC_DB_URL_DATABASE=!DB_NAME! -e KC_DB_USERNAME=!DB_USER! -e KC_DB_PASSWORD=!DB_PASSWORD! -e KC_BOOTSTRAP_ADMIN_USERNAME=!KC_ADMIN_USER! -e KC_BOOTSTRAP_ADMIN_PASSWORD=!KC_ADMIN_PASSWORD! -e KC_HEALTH_ENABLED=true -e KC_METRICS_ENABLED=true -p !KC_HTTP_PORT!:!KC_HTTP_PORT! -p !KC_MGMT_PORT!:!KC_MGMT_PORT! !KC_IMAGE! start-dev
if !errorlevel! neq 0 (
    echo ERROR: Failed to create Keycloak container !KC_CONTAINER_NAME!.
    exit /b 1
)

echo Setup completed successfully!