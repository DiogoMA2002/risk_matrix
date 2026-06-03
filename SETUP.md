# Setup & Deployment Guide

This guide covers local development setup and production deployment with PostgreSQL.

---

## Prerequisites

| Tool | Version |
|------|---------|
| JDK | 23 (Temurin recommended) |
| Maven | 3.9+ (wrapper included) |
| Node.js | 18+ |
| npm | 9+ |
| PostgreSQL | 14+ (production) |

---

## Local Development (H2)

The default configuration uses an H2 file database — no database installation needed.

### 1. Clone and configure

```bash
git clone https://github.com/DiogoMA2002/risk_matrix.git
cd risk_matrix
```

Create the backend env file:

```bash
cd risk_Matrix
cp .env.example .env
```

Edit `.env` with the minimum required values:

```env
JWT_SECRET=change_me_use_at_least_48_random_characters_here
ADMIN_USERNAME=admin
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=change_me
COOKIE_SECURE=false
CORS_ALLOWED_ORIGINS=http://localhost:8081
TRUSTED_PROXIES=127.0.0.1,::1
SWAGGER_ENABLED=true
```

> Never commit `.env` to version control.

### 2. Start the backend

```bash
cd risk_Matrix
./mvnw spring-boot:run
```

Backend starts at `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### 3. Start the frontend

```bash
cd risk_matrix_frontend
npm install
npm run serve
```

Frontend starts at `http://localhost:8081`.

---

## Production Deployment (PostgreSQL)

### 1. Database setup

```sql
CREATE DATABASE matriz;
CREATE USER risk_user WITH PASSWORD 'strong_password';
GRANT ALL PRIVILEGES ON DATABASE matriz TO risk_user;
```

### 2. Switch application.properties to PostgreSQL

Open [risk_Matrix/src/main/resources/application.properties](risk_Matrix/src/main/resources/application.properties).

Comment out the H2 block and uncomment the PostgreSQL block:

```properties
# H2 (disable)
# spring.datasource.url=jdbc:h2:file:./src/DB/DB
# spring.datasource.driverClassName=org.h2.Driver
# spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
# spring.jpa.hibernate.ddl-auto=update
# spring.h2.console.enabled=false

# PostgreSQL (enable)
spring.datasource.url=jdbc:postgresql://localhost:5432/matriz
spring.datasource.driverClassName=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
```

> On first run, temporarily use `ddl-auto=create` so Hibernate creates the schema. Switch back to `validate` immediately after.

### 3. Environment variables

Set all of the following before starting the backend:

```env
# Authentication (required)
JWT_SECRET=<openssl rand -base64 48>
JWT_EXPIRATION_MS=86400000
JWT_PUBLIC_EXPIRATION_MS=7200000
JWT_REFRESH_TOKEN_EXPIRATION_MS=604800000

# Admin bootstrap (used once on first startup)
ADMIN_USERNAME=admin
ADMIN_EMAIL=admin@yourorg.com
ADMIN_PASSWORD=<strong-password>

# Database
DB_USERNAME=risk_user
DB_PASSWORD=<strong-password>

# Security
COOKIE_SECURE=true
CORS_ALLOWED_ORIGINS=https://yourapp.com
TRUSTED_PROXIES=127.0.0.1

# Disable API docs in production
SWAGGER_ENABLED=false
```

### 4. Build the backend

```bash
cd risk_Matrix
./mvnw clean package -DskipTests
```

Run the JAR:

```bash
java -jar target/risk_Matrix-0.0.1-SNAPSHOT.jar
```

### 5. Build the frontend

Before building, confirm the Axios base URL in the frontend points to your production backend domain.

```bash
cd risk_matrix_frontend
npm install
npm run build
```

Output is in `risk_matrix_frontend/dist/`.

### 6. Nginx configuration

Serve the frontend static files and proxy `/api/` to Spring Boot:

```nginx
server {
    listen 443 ssl;
    server_name yourapp.com;

    root /var/www/risk-matrix/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    ssl_certificate /etc/letsencrypt/live/yourapp.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourapp.com/privkey.pem;
}

server {
    listen 80;
    server_name yourapp.com;
    return 301 https://$host$request_uri;
}
```

---

## First-Run Checklist

- [ ] PostgreSQL database `matriz` created and user granted
- [ ] `application.properties` switched to PostgreSQL with `ddl-auto=create`
- [ ] All required env vars set
- [ ] Backend started — schema created, admin user bootstrapped
- [ ] Switch `ddl-auto` to `validate` and restart backend
- [ ] Frontend built and deployed to Nginx root
- [ ] HTTPS confirmed working — `COOKIE_SECURE=true` active
- [ ] Log in at `/login` with bootstrap admin credentials
- [ ] Change admin password immediately after first login
- [ ] Confirm Swagger is unreachable (`SWAGGER_ENABLED=false`)
- [ ] Create categories, questionnaire, and questions from the Admin Dashboard
- [ ] Test full public flow end-to-end

---

## Useful Commands

```bash
# Run backend tests
cd risk_Matrix && ./mvnw test

# Generate JaCoCo coverage report
cd risk_Matrix && ./mvnw test jacoco:report
# Report: risk_Matrix/target/site/jacoco/index.html

# Lint frontend
cd risk_matrix_frontend && npm run lint

# Production frontend build
cd risk_matrix_frontend && npm run build
```

---

## Security Checklist

| Item | Reason |
|------|--------|
| `JWT_SECRET` minimum 48 random bytes | Prevents token forgery |
| `COOKIE_SECURE=true` on HTTPS | Cookies not sent over plain HTTP |
| `SWAGGER_ENABLED=false` in production | Do not expose API schema publicly |
| Strong `ADMIN_PASSWORD`, rotated on first login | Bootstrap credentials are ephemeral |
| `CORS_ALLOWED_ORIGINS` set to exact frontend origin | Prevents cross-origin cookie leakage |
| `ddl-auto=validate` after initial schema creation | Prevents accidental schema mutation |
