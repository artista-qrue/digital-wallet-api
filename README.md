# Digital Wallet API

A Spring Boot REST API for managing digital wallets, enabling customers and employees to create wallets, make deposits, withdrawals, and approve transactions.

## Features

- Create and manage customer accounts
- Create multiple wallets with different currencies (TRY, USD, EUR)
- Make deposits and withdrawals
- Approve or deny transactions
- List transactions by wallet, type, or status
- Role-based authorization (employee vs. customer)

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security
- H2 Database (Dev/Test)
- PostgreSQL Database (Production)
- SpringDoc OpenAPI (Swagger UI)
- Micrometer for metrics
- Caffeine for caching
- Resilience4j for resilience
- Maven

## Getting Started

### Prerequisites

- JDK 17 or later
- Maven 3.6+ or use the included Maven wrapper
- Docker and Docker Compose (for containerized deployment)

### Building the Application

```bash
# Clone the repository
git clone https://github.com/artista-qrue/digital-wallet-api.git
cd digital-wallet-api

# Build the application
./mvnw clean package
```

### Running Locally for Development

```bash
# Run the application with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will start on `http://localhost:8080`

### Running with Docker Compose

For local development with a PostgreSQL database:

```bash
# Start the application with PostgreSQL
docker-compose up -d
```

### Running Tests

```bash
# Run unit tests
./mvnw test

# Run with test profile including integration tests
./mvnw verify -P test
```

### Code Quality and Security Checks

```bash
# Check code style
./mvnw spotless:check

# Vulnerability scanning
./mvnw dependency-check:check
```

### Accessing the H2 Database Console (Development)

The H2 database console is available at `http://localhost:8080/h2-console` with the following parameters:

- JDBC URL: `jdbc:h2:mem:walletdb`
- Username: `sa`
- Password: `password`

### API Documentation

The API documentation is available through Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

You can explore all endpoints, models, and test the API directly from the Swagger UI interface.

The raw OpenAPI specification is available at:
```
http://localhost:8080/api-docs
```

## Profiles

The application has multiple profiles for different environments:

- **dev**: For local development with H2 in-memory database
- **test**: For testing with H2 database and additional logging
- **prod**: For production deployment with PostgreSQL and hardened security

## Deployment

### Production Prerequisites

- PostgreSQL database server
- JVM environment (Java 17+)
- Environment variables for configuration
- Docker (recommended)

### Building for Production

```bash
# Build the application with production profile
./mvnw clean package -P prod -DskipTests
```

### Running in Production

```bash
# Start the application with production profile
java -jar target/wallet-api.jar --spring.profiles.active=prod
```

### Required Environment Variables for Production

```
# Database Configuration
DB_URL=jdbc:postgresql://your-db-host:5432/walletdb
DB_USERNAME=yourdbuser
DB_PASSWORD=yourdbpassword

# Security Configuration
JWT_SECRET=your-very-secure-jwt-secret-key
```

### Docker Deployment

```bash
# Build the Docker image
docker build -t wallet-api:latest .

# Run the Docker container
docker run -d -p 8080:8080 \
  --name wallet-api \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://your-db-host:5432/walletdb \
  -e DB_USERNAME=yourdbuser \
  -e DB_PASSWORD=yourdbpassword \
  -e JWT_SECRET=your-very-secure-jwt-secret-key \
  wallet-api:latest
```

### Monitoring in Production

The application exposes metrics and health information via Spring Boot Actuator:

- Health check: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Prometheus endpoint: `http://localhost:8080/actuator/prometheus`

## API Endpoints

### Customer Endpoints

- `POST /api/customers` - Create a new customer
- `GET /api/customers/{id}` - Get customer by ID
- `GET /api/customers/tckn/{tckn}` - Get customer by TCKN
- `GET /api/customers` - Get all customers

### Wallet Endpoints

- `POST /api/wallets` - Create a new wallet
- `GET /api/wallets/customer/{customerId}` - Get wallets by customer ID
- `GET /api/wallets/customer/{customerId}/currency/{currency}` - Get wallets by customer ID and currency
- `GET /api/wallets/{id}` - Get wallet by ID

### Transaction Endpoints

- `POST /api/transactions/deposit` - Make a deposit
- `POST /api/transactions/withdraw` - Make a withdrawal
- `PUT /api/transactions/{id}/approve` - Approve a transaction
- `GET /api/transactions/wallet/{walletId}` - Get transactions by wallet ID
- `GET /api/transactions/{id}` - Get transaction by ID

## Request and Response Examples

### Create a Customer

Request:
```json
POST /api/customers
Content-Type: application/json

{
  "name": "John",
  "surname": "Doe",
  "tckn": "12345678901",
  "isEmployee": true
}
```

Response:
```json
{
  "result": "SUCCESS",
  "message": "Customer created successfully",
  "data": {
    "id": 1,
    "name": "John",
    "surname": "Doe",
    "tckn": "12345678901",
    "isEmployee": true
  }
}
```

### Get Customer by ID

Request:
```
GET /api/customers/1
X-Customer-Id: 1
```

Response:
```json
{
  "result": "SUCCESS",
  "message": "Customer retrieved successfully",
  "data": {
    "id": 1,
    "name": "John",
    "surname": "Doe",
    "tckn": "12345678901",
    "isEmployee": true
  }
}
```

### Create a Wallet

Request:
```json
POST /api/wallets
X-Customer-Id: 1
Content-Type: application/json

{
  "walletName": "My TRY Wallet",
  "currency": "TRY",
  "activeForShopping": true,
  "activeForWithdraw": true,
  "customerId": 1
}
```

Response:
```json
{
  "result": "SUCCESS",
  "message": "Wallet created successfully",
  "data": {
    "id": 1,
    "walletName": "My TRY Wallet",
    "currency": "TRY",
    "activeForShopping": true,
    "activeForWithdraw": true,
    "balance": 0.00,
    "usableBalance": 0.00,
    "customerId": 1
  }
}
```

### Get Wallets by Customer ID

Request:
```
GET /api/wallets/customer/1
X-Customer-Id: 1
```

Response:
```json
{
  "result": "SUCCESS",
  "message": "Wallets retrieved successfully",
  "data": [
    {
      "id": 1,
      "walletName": "My TRY Wallet",
      "currency": "TRY",
      "activeForShopping": true,
      "activeForWithdraw": true,
      "balance": 500.00,
      "usableBalance": 300.00,
      "customerId": 1
    },
    {
      "id": 2,
      "walletName": "My USD Wallet",
      "currency": "USD",
      "activeForShopping": true,
      "activeForWithdraw": false,
      "balance": 100.00,
      "usableBalance": 100.00,
      "customerId": 1
    }
  ]
}
```

### Make a Deposit

Request:
```json
POST /api/transactions/deposit
X-Customer-Id: 1
Content-Type: application/json

{
  "amount": 5000.00,
  "walletId": 1,
  "source": "TR123456789012345678901234",
  "sourceType": "IBAN"
}
```

Response:
```json
{
  "result": "SUCCESS",
  "message": "Deposit created successfully",
  "data": {
    "id": 1,
    "walletId": 1,
    "amount": 5000,
    "type": "DEPOSIT",
    "oppositePartyType": "IBAN",
    "oppositeParty": "TR123456789012345678901234",
    "status": "PENDING",
    "transactionDate": "2023-12-21T14:30:00"
  }
}
```

### Make a Large DEPOSIT (Requires Approval)
Request:
```json
POST /api/transactions/approve
X-Customer-Id: 1
Content-Type: application/json

{
"transactionId":1,
"status": "APPROVED"
}
```

Response:
```json
{
  "result": "SUCCESS",
  "message": "Transaction status updated successfully",
  "data": {
    "id": 1,
    "walletId": 1,
    "amount": 5000,
    "type": "DEPOSIT",
    "oppositePartyType": "IBAN",
    "oppositeParty": "TR123456789012345678901234",
    "status": "APPROVED",
    "transactionDate": "2025-03-21T16:46:58.675127"
  }
}
```

### Make a Withdrawal

Request:
```json
POST /api/transactions/withdraw
X-Customer-Id: 1
Content-Type: application/json

{
  "amount": 200.00,
  "walletId": 1,
  "destination": "PaymentProvider12345",
  "destinationType": "PAYMENT"
}
```

Response:
```json
{
  "result": "SUCCESS",
  "message": "Withdrawal created successfully",
  "data": {
    "id": 2,
    "walletId": 1,
    "amount": 200,
    "type": "WITHDRAW",
    "oppositePartyType": "PAYMENT",
    "oppositeParty": "PaymentProvider12345",
    "status": "APPROVED",
    "transactionDate": "2023-12-21T14:35:00"
  }
}
```

### Make a Large Withdrawal (Requires Approval)

Request:
```json
POST /api/transactions/withdraw
X-Customer-Id: 1
Content-Type: application/json

{
  "amount": 2000.00,
  "walletId": 1,
  "destination": "TR987654321098765432109876",
  "destinationType": "IBAN"
}
```

Response:
```json
{
  "result": "SUCCESS",
  "message": "Withdrawal created successfully",
  "data": {
    "id": 3,
    "walletId": 1,
    "amount": 2000,
    "type": "WITHDRAW",
    "oppositePartyType": "IBAN",
    "oppositeParty": "TR987654321098765432109876",
    "status": "PENDING",
    "transactionDate": "2023-12-21T14:40:00"
  }
}
```

### Approve a Transaction

Request:
```
PUT /api/transactions/3/approve
X-Customer-Id: 2
```

Response:
```json
{
  "result": "SUCCESS",
  "message": "Transaction approved successfully",
  "data": {
    "id": 3,
    "walletId": 1,
    "amount": 2000,
    "type": "WITHDRAW",
    "oppositePartyType": "IBAN",
    "oppositeParty": "TR987654321098765432109876",
    "status": "APPROVED",
    "transactionDate": "2023-12-21T14:40:00"
  }
}
```

### Get Transactions by Wallet ID

Request:
```
GET /api/transactions/wallet/1
X-Customer-Id: 1
```

Response:
```json
{
  "result": "SUCCESS",
  "message": "Transactions retrieved successfully",
  "data": [
    {
      "id": 1,
      "walletId": 1,
      "amount": 500,
      "type": "DEPOSIT",
      "oppositePartyType": "IBAN",
      "oppositeParty": "TR123456789012345678901234",
      "status": "APPROVED",
      "transactionDate": "2023-12-21T14:30:00"
    },
    {
      "id": 2,
      "walletId": 1,
      "amount": 200,
      "type": "WITHDRAW",
      "oppositePartyType": "PAYMENT",
      "oppositeParty": "PaymentProvider12345",
      "status": "APPROVED",
      "transactionDate": "2023-12-21T14:35:00"
    },
    {
      "id": 3,
      "walletId": 1,
      "amount": 2000,
      "type": "WITHDRAW",
      "oppositePartyType": "IBAN",
      "oppositeParty": "TR987654321098765432109876",
      "status": "APPROVED",
      "transactionDate": "2023-12-21T14:40:00"
    }
  ]
}
```

## Authorization

All API requests require a `X-Customer-Id` header that specifies the ID of the customer making the request. This is used to enforce the following rules:

- Regular customers can only operate on their own wallets and transactions.
- Employees can operate on all wallets and transactions.

In a production environment, this would be replaced with proper JWT-based authentication and authorization.

## Error Handling

The API follows a consistent error response format:

```json
{
  "result": "ERROR",
  "message": "Detailed error message",
  "data": null
}
```

Common error scenarios:

- **400 Bad Request**: Invalid input data or validation failures
- **403 Forbidden**: Insufficient permissions to access the resource
- **404 Not Found**: Requested resource does not exist
- **409 Conflict**: Resource already exists (e.g., duplicate TCKN)
- **500 Internal Server Error**: Unexpected server-side errors

## License

MIT License 