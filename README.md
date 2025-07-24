# Keycloak Local Development Setup

This project provides a Docker Compose configuration for running Keycloak locally with PostgreSQL persistence.

## Quick Start

```bash
docker compose up -d
```

## What's Included

- **Keycloak 26.3**: Identity and Access Management server
- **PostgreSQL 17.5**: Database for persistent data storage
- **Persistent Volume**: Preserves realms, clients, users, and configurations between restarts

## Access Information

- **Keycloak Admin Console**: http://localhost:8080
- **Default Admin Credentials**:
    - Username: `admin`
    - Password: `admin`
- **PostgreSQL Database**: `localhost:5432`
    - Database: `keycloak`
    - Username: `postgres`
    - Password: `123456`

## Features

- Development mode configuration (`start-dev`)
- Health checks enabled on port 9000
- Metrics endpoint available
- Automatic database initialization
- Data persistence across container restarts

## Configuration

The setup uses environment variables for configuration. Key settings include:

- Database connection to PostgreSQL
- Admin user bootstrap
- Health and metrics endpoints
- Development mode for easier local testing

## Data Persistence

Your Keycloak data (realms, clients, users, roles, etc.) is automatically persisted in the `keycloak-postgres-volume`
Docker volume. This means your configuration will survive container restarts and recreations.

## Documentation

For detailed integration instructions and usage examples, see the comprehensive guide:

> https://vulinhjava.io.vn/blog/spring-boot-3-keycloak-integration

## Stopping the Services

```bash
docker compose down
```

To remove everything including the persistent volume:

```bash
docker compose down -v
```