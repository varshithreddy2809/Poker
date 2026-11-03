# Online Multiplayer Poker Game

A full-stack multiplayer card game built with **React** and **Spring Boot**. The project brings together a responsive game interface, a Java backend, MySQL persistence, and real-time communication.

> **Project status:** This repository contains the current development implementation. Check the API documentation and database scripts before assuming every feature described in an earlier design is enabled in the current build.

## Features

- Player registration and authenticated access
- Create and join multiplayer game tables
- Server-managed game rounds and card dealing
- Private player hands (players can retrieve only their own cards)
- Showdown and server-side hand evaluation
- Real-time communication using WebSocket and STOMP
- Persistent application data using MySQL
- Responsive React frontend

## Tech Stack

| Area | Technologies |
|---|---|
| Frontend | React 19, JavaScript, Vite |
| Backend | Java 21, Spring Boot 4 |
| APIs | Spring Web MVC, REST |
| Real-time communication | WebSocket, STOMP |
| Security | Spring Security |
| Persistence | Spring Data JPA, Hibernate |
| Database | MySQL 8.0+ |
| Build tools | Maven, npm |

## Architecture

The application is split into two main parts:

```text
POCKER_GAME/
├── frontend/        # React + Vite client
└── poker/           # Spring Boot backend
    ├── src/
    ├── database/    # SQL schema and migration scripts
    └── pom.xml
```

The React client communicates with the Spring Boot backend through REST APIs. WebSocket/STOMP is used for real-time events where supported by the application. The backend handles application rules, authentication, and persistence through Spring Data JPA/Hibernate and MySQL.

## Prerequisites

Install the following before running the project:

- JDK 21
- Node.js and npm
- MySQL 8.0 or later
- Git

## Getting Started

### 1. Clone the repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd POCKER_GAME
```

Replace `<YOUR_GITHUB_REPOSITORY_URL>` with the HTTPS or SSH URL of your GitHub repository.

### 2. Set up the database

1. Start MySQL.
2. Open the SQL file `poker/database/phase-1-schema.sql` in MySQL Workbench or your preferred MySQL client.
3. Execute it to create the `card_game_platform` database and the initial tables.
4. Review the later SQL scripts in `poker/database/` and apply the required phases in order for the features you are running.

Database setup notes are also available in [`poker/database/README.md`](poker/database/README.md).

### 3. Configure the backend

Open `poker/src/main/resources/application.properties` and configure the datasource for your local MySQL installation.

Example local settings:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/card_game_platform?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

Use your own local credentials. Do not commit real passwords, API keys, or other secrets to GitHub.

### 4. Run the Spring Boot backend

Open a terminal:

```bash
cd poker
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

The backend runs on `http://localhost:8080` with the default Spring Boot configuration.

### 5. Run the React frontend

Open a second terminal from the repository root:

```bash
cd frontend
npm install
npm run dev
```

Vite prints the local frontend URL in the terminal (usually `http://localhost:5173`). Open that URL in your browser.

## Useful Commands

### Frontend

```bash
npm run dev       # Start the Vite development server
npm run build     # Create a production build
npm run lint      # Run ESLint
npm run preview   # Preview the production build
```

### Backend

Run these commands from the `poker/` directory:

```bash
.\mvnw.cmd spring-boot:run   # Windows
.\mvnw.cmd test              # Run backend tests
.\mvnw.cmd clean package     # Build the backend JAR
```

For macOS/Linux, use `./mvnw` instead of `.\mvnw.cmd`.

## API Documentation

See [`poker/FRIENDLY_GAME_API.md`](poker/FRIENDLY_GAME_API.md) for the documented player registration, table creation, joining, round start, private-hand retrieval, and showdown API flow.

## Security Notes

- Keep database passwords and other credentials out of source control.
- Use environment-specific configuration for local, test, and production deployments.
- The game is intended for virtual/friendly play; it does not provide real-money gambling or payment processing.

## Future Improvements

Potential areas for further development include automated integration tests, deployment configuration, observability, load testing, and production-grade external messaging or caching where required.

## Author

**Varshith Gowlla**  
Computer Science and Engineering

---

If you find an issue or have a suggestion, feel free to open a GitHub issue.
