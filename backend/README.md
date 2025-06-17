# 🧾 Online Asset Management (OAM)

A Spring Boot-based backend system for managing organizational assets, supporting secure authentication, role-based access control, and a PostgreSQL database setup via Docker.

## 🚀 Features

- JWT-based authentication (with `jjwt`)
- Spring Security integration
- PostgreSQL with Docker container support
- Flyway for database migrations
- MapStruct for object mapping
- Lombok to reduce boilerplate
- Dotenv support for environment configuration
- SpringDoc OpenAPI (Swagger UI) for API documentation

---

## 🛠️ Tech Stack

- Java 21
- Spring Boot 3.4.5
- PostgreSQL (via Docker)
- Maven
- Flyway
- MapStruct
- JWT
- Dotenv

---

## 📂 Project Structure
```
rookies.oam/
│
├── src/
├── docker-compose.yml
├── .env
├── env.example
├── pom.xml
└── README.md
```

## 🐳 Docker Setup (PostgreSQL)
This project uses Docker to run the PostgreSQL database. You must have Docker and Docker Compose installed.

### 1. Setup `.env` File
- Create a `.env` file in the project root (or copy from the template):
- Fill in the necessary environment variables:
```
POSTGRES_USER=your_db_user
POSTGRES_PASSWORD=your_db_password
```

### 2. Start PostgreSQL
Run the following command in the project root:
```bash
docker-compose up -d
```

## 🔧 Build and Run
### 1. Build the Project
```bash
mvn clean install
```
### 2. Run the Application
```bash
mvn spring-boot:run
```

## 🌐 API Documentation
Once the application is running, you can access the API documentation at:
```
http://localhost:8080/swagger-ui/index.html
```

## 🛠️ Developer Notes
- Flyway will auto-apply any migration files in src/main/resources/db/migration.

- Spring Boot Devtools is enabled for hot reloading.

- JWT Secret Keys and other config values can be placed in .env or application.properties.