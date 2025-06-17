
# 🧾 Online Asset Management System (Full Stack)

A full-stack asset management platform for internal company use. Designed for admins and staff to manage assets, assignments, and return requests — with secure authentication and role-based access. Built with Spring Boot (backend) and React + TypeScript (frontend), connected via a RESTful API.

## 🧩 Tech Overview


| Layer    | Stack                                                             |
| -------- | ----------------------------------------------------------------- |
| Frontend | React, TypeScript, Vite, TailwindCSS, ShadCN UI, SWR, JWT, Vitest |
| Backend  | Java 21, Spring Boot 3.4.5, PostgreSQL, MapStruct, Flyway, JWT    |
| DevOps   | Docker (PostgreSQL), dotenv, Swagger UI (SpringDoc)               |

## 📂 Project Structure

```
online-asset-management/
├── frontend/         # React + TypeScript frontend
│   └── README.md     # Frontend-specific docs
├── backend/          # Spring Boot backend
│   └── README.md     # Backend-specific docs
├── README.md         # top-level overview
```

## 🚀 Running the Project Locally
### 🔧 Prerequisites

- Java 21
- Node.js
- Docker & Docker Compose
- Maven

### 🐳 Step 1: Start PostgreSQL (Docker)

```
cd backend
cp env.example .env
# edit .env to match your environment
docker-compose up -d
```

### 🔨 Step 2: Start Backend

```
cd backend
mvn clean install
mvn spring-boot:run
```
API available at http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui/index.html

### 💻 Step 3: Start Frontend

```
cd frontend
npm install
npm run dev
```
Open http://localhost:3000 in your browser.

### 📊 Functional Pages

| Page                  | Access      | Description                                      |
| --------------------- | ----------- | ------------------------------------------------ |
| `/login`              | All         | Secure login screen                              |
| `/`                   | All         | Role-based home page                             |
| `/assets`             | Admin       | Manage asset records (CRUD, search, pagination)  |
| `/assignments`        | Admin       | Assign assets to users, view/edit assignments    |
| `/returning-requests` | Admin/Staff | Staff submit requests, admin reviews & processes |
| `/report`             | Admin       | View monthly asset summary reports               |


### 🧠 Why This Project?
This project simulates a real-world enterprise system with layered architecture, proper separation of concerns, and thoughtful UI/UX design. It highlights:

- Strong backend fundamentals (DTOs, security, testing)
- Clean frontend architecture (hooks, state, styling)
- Realistic deployment setup (Docker, env configs)
- Production-minded code quality

### 📎 Related Links

- [Frontend README](https://github.com/sweetiesss/asset-management-system/blob/main/frontend/README.md)
- [Backend README](https://github.com/sweetiesss/asset-management-system/blob/main/backend/README.md)
- [Author](https://www.linkedin.com/in/b%E1%BA%A3o-l%C3%AA-6585a7279/)







