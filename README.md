# SmartShop - B2B Commercial Management System

SmartShop is a REST API for commercial management, designed for MicroTech Maroc, a B2B computer hardware distributor in Casablanca.

## Features

- Client management with loyalty tier system
- Product catalog management
- Multi-product order processing
- Multi-payment method support (CASH, CHECK, TRANSFER)
- Coupon discount system
- Automatic stock reservation
- HTTP Session authentication

## Tech Stack

- **Java:** 17
- **Framework:** Spring Boot 4.0.0
- **Database:** PostgreSQL 15
- **ORM:** Spring Data JPA / Hibernate
- **Validation:** Bean Validation
- **DTO Mapping:** MapStruct
- **API Documentation:** Swagger/OpenAPI (SpringDoc)
- **Build Tool:** Maven

## Prerequisites

- Java 17 or higher
- Docker & Docker Compose
- Maven 3.6+

## Getting Started

### 1. Start the Database

```bash
docker-compose up -d
```

This will start a PostgreSQL container on port 5432.

### 2. Build the Project

```bash
./mvnw clean install
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080/api`

### 4. Access API Documentation

Once the application is running, access the Swagger UI:

```
http://localhost:8080/api/swagger-ui.html
```

## Project Structure

```
src/main/java/com/smartshop/
├── entity/         # JPA entities
├── repository/     # Spring Data repositories
├── service/        # Business logic layer
├── controller/     # REST controllers
├── dto/            # Data Transfer Objects
├── mapper/         # MapStruct mappers
├── exception/      # Custom exceptions and handlers
├── config/         # Configuration classes
└── enums/          # Enums (UserRole, OrderStatus, etc.)
```

## API Endpoints

All endpoints are prefixed with `/api`

- **Authentication:** `/api/auth/*`
- **Clients:** `/api/clients/*`
- **Products:** `/api/products/*`
- **Orders:** `/api/orders/*`
- **Payments:** `/api/payments/*`
- **Coupons:** `/api/coupons/*`

See [REQUIREMENTS.md](REQUIREMENTS.md) for detailed API documentation.

## Configuration

Key configuration properties in `application.properties`:

- **Database URL:** `jdbc:postgresql://localhost:5432/smartshop_db`
- **Server Port:** `8080`
- **Context Path:** `/api`
- **Session Timeout:** `30m`

## Development

### Database Management

Stop the database:
```bash
docker-compose down
```

Reset the database (removes all data):
```bash
docker-compose down -v
docker-compose up -d
```

### Running Tests

```bash
./mvnw test
```