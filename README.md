## Rock Paper Scissors:
#### Spring (Kotlin) Backend and Angular Frontend Sample Project

### Overview

This repository contains a monorepo for a project with a Spring Kotlin backend and an Angular frontend. The backend is implemented with Event Sourcing using Axon.

### Prerequisites

JDK (Java Development Kit)
Node.js and npm (Node Package Manager)
Docker (for Postgres dependencies)
Gradle (Build tool for the backend)

### Project Structure

- `src/main/kotlin`: Contains the source code for the Spring Kotlin backend
- `src/test/kotlin`: Contains the tests for the backend
- `src/main/typescript`: Contains the source code for the Angular frontend

### Event Sourcing with Axon

The backend uses Axon for Event Sourcing, offering key advantages:

1. Traceability: Captures each player move as an event, enabling accurate tracking and debugging.
2. Maintenance and Extension: Facilitates adding new features and modifying existing ones without impacting the core game.
3. Efficient Metrics: Simplifies the extraction of game metrics and statistics, providing insights into player behavior and game trends.

This approach enhances the game's scalability, performance, and ease of development.

### Starting Dependencies (Postgres)

To start the Postgres database dependencies, use the following commands:

First time setup:
```shell
make setup
```

To start: 
```shell
make up
```
To stop: 
```shell
make down
```

### Testing

To run tests, execute the command ./gradlew test for the backend and npm test for the frontend.
```shell
./gradlew test
```
```shell
cd src/main/typescript && npm test
```

### Backend

The backend is accessible by default at:

URL: http://localhost:8080

### Frontend

The frontend is accessible by default at:

URL: http://localhost:4200

### Getting Started Guide

1. Clone the repository.
2. Start the Postgres dependencies with make setup & make up.
3. Run the backend with ./gradlew bootRun.
4. Start the frontend in a new terminal with npm start.