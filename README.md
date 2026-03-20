# Bombazine Setlist Builder

A full-stack web application for managing concert setlists for the band bombazine. Built with Spring Boot and React.

---

## Features

- **Authentication** - JWT-based login with role-based access control (ADMIN / MEMBER)
- **Song management** - view, add, edit and delete songs sourced from the Last.fm API
- **Setlist management** - create, edit and delete setlists with drag-and-drop song ordering
- **PDF export** - download any setlist as a formatted PDF
- **Setlist generation** - auto-generate a setlist based on configurable criteria

---

## Tech Stack

### Backend
- Java 21
- Spring Boot 3.x
- Spring Security (stateless JWT)
- PostgreSQL 16
- Flyway (database migrations)
- Hibernate / Spring Data JPA

### Frontend
- React 19 + TypeScript
- Vite
- Mantine 7 (UI components)
- @dnd-kit (drag and drop)

---

## Project Structure

```
setlist-builder/
├── backend/        # Spring Boot application
└── frontend/       # React + Vite application
```

---

## Getting Started

### Prerequisites

- Java 21
- Node.js 18+
- Docker (for local PostgreSQL)
- A [Last.fm API key](https://www.last.fm/api/account/create)

### Backend

1. Start a local PostgreSQL instance:

```bash
docker run --name setlist-db -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=setlist_builder -p 5432:5432 -d postgres:16
```

2. Create an `application-local.properties` or set environment variables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/setlist_builder
spring.datasource.username=postgres
spring.datasource.password=postgres
JWT_SECRET=your-secret-key
LAST_FM_API_KEY=your-lastfm-api-key
```

3. Run the application:

```bash
cd backend
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

### Frontend

1. Create a `.env.local` file:

```dotenv
VITE_API_BASE_URL=http://localhost:8080/api
```

2. Install dependencies and start the dev server:

```bash
cd frontend
npm install
npm run dev
```

The app will be available at `http://localhost:5173`.

---

## Environment Variables

### Backend

| Variable | Description |
|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `JWT_SECRET` | Secret key for signing JWT tokens |
| `JWT_EXPIRATION_MS` | Token expiry in milliseconds (default: 86400000) |
| `LAST_FM_API_KEY` | Last.fm API key for song data |

### Frontend

| Variable | Description |
|---|---|
| `VITE_API_BASE_URL` | Base URL of the backend API |

---

## API Overview

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/api/auth/login` | Public | Authenticate and receive JWT |
| POST | `/api/auth/register` | ADMIN | Register a new user |
| GET | `/api/auth/users` | ADMIN | List all users |
| GET | `/api/songs` | Authenticated | List all songs |
| POST | `/api/songs` | Authenticated | Add a song |
| PUT | `/api/songs/:id` | Authenticated | Update a song |
| DELETE | `/api/songs/:id` | Authenticated | Delete a song |
| GET | `/api/setlists` | Authenticated | List all setlists |
| POST | `/api/setlists` | Authenticated | Create a setlist |
| PUT | `/api/setlists/:id` | Authenticated | Update a setlist |
| DELETE | `/api/setlists/:id` | Authenticated | Delete a setlist |
| GET | `/api/setlists/:id/pdf` | Authenticated | Download setlist as PDF |
| POST | `/api/setlists/generate` | Authenticated | Auto-generate a setlist |

---

## Database Migrations

Flyway migrations are located in `backend/src/main/resources/db/migration` and run automatically on startup.

---

## Deployment

The application is deployed on:

- **Frontend** - [Vercel](https://vercel.com)
- **Backend + Database** - [Railway](https://railway.app)

### Backend (Railway)

The backend is built using the `backend/Dockerfile`. The following environment variables must be set in Railway:

```
SPRING_DATASOURCE_URL=${{Postgres.DATABASE_URL}}
SPRING_DATASOURCE_USERNAME=${{Postgres.PGUSER}}
SPRING_DATASOURCE_PASSWORD=${{Postgres.PGPASSWORD}}
JWT_SECRET=<your-secret>
LAST_FM_API_KEY=<your-key>
```

### Frontend (Vercel)

Set the root directory to `frontend` and configure:

```
VITE_API_BASE_URL=https://<your-railway-backend-url>/api
```

---

## Running Tests

```bash
cd backend
./mvnw test
```

Integration tests use Testcontainers and require Docker to be running.
