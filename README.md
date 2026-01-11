# Media Ratings Platform (MRP)

## Overview
The Media Ratings Platform (MRP) is a standalone RESTful HTTP backend service implemented in Java.
It provides an API for managing media content such as movies, series, and games. Users can register,
authenticate, create media entries, rate them, and interact with the platform through ratings,
favorites, and recommendations.

The project is developed according to the **Media Ratings Platform specification** provided by
Technikum Wien and follows a pure HTTP approach without using web frameworks such as Spring or ASP.NET.

---

## Features
- User registration and login with token-based authentication
- CRUD operations for media entries (movies, series, games)
- Rating system with star ratings and optional comments
- Like system for ratings
- Favorites management
- Search and filtering capabilities
- Recommendation logic based on user ratings
- Public leaderboard and user statistics

---

## Technical Architecture
The application follows a layered architecture:

- **HTTP Layer**
  - Java HttpServer-based handlers
  - Endpoint routing and request/response handling

- **Service Layer**
  - Business logic for authentication, media, ratings, profiles

- **Persistence Layer**
  - DAO classes using JDBC
  - PostgreSQL database

- **Model Layer**
  - Domain entities (User, Media, Rating, Profile)

- **Utility Layer**
  - Token handling
  - Password hashing
  - JSON serialization

This structure ensures separation of concerns, maintainability, and testability.

---

## Technology Stack
- Java
- Java HttpServer (pure HTTP, no frameworks)
- PostgreSQL
- Docker
- JDBC
- JUnit
- curl (integration testing)

---

## Security
The API uses **token-based authentication**.
After successful login, a token is returned and must be included in all subsequent requests using
the `Authentication: Bearer <token>` header.

---

## Testing
- **Unit Tests**
  - Focus on core business logic (authentication, media, ratings, utilities)
- **Integration / Regression Tests**
  - Performed using a shell script (`test.sh`)
  - Validate endpoint behavior and HTTP status codes

---

## How to Run

- Start the PostgreSQL database (Docker)
- Execute the database schema script
- Build and start the application
- Run unit tests and regression tests as needed

## Commands

### Database (Docker & SQL Scripts)
Execute SQL scripts inside the running PostgreSQL Docker container:

```bash
docker exec -i mediaratingsplatform-db-1 psql -U jess -d mrp < sql/schema.sql
docker exec -i mediaratingsplatform-db-1 psql -U jess -d mrp < sql/drop_all.sql
```

### Unit Tests 
```bash
mvn clean test
```

### Integration Tests 
```bash
./test.sh
```

