# Templating

_This service provides functionality for storing and rendering templates. It supports rendering templates into various
text-based formats such as HTML and plain text, as well as converting them into PDF documents. The service is designed
to handle different types of templates, including Pebble and Word templates, and offers a flexible API for rendering
templates with dynamic data._

## Getting Started

### Prerequisites

- **Java 25 or higher**
- **Maven**
- **MariaDB**
- **Git**

### Installation

1. **Clone the repository:**

   ```bash
   git clone git@github.com:Sundsvallskommun/api-service-templating.git
   ```
2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#Configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**

   If this microservice depends on other services, make sure they are up and accessible.
   See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

   ```bash
   mvn spring-boot:run
   ```

## API Documentation

Access the API documentation via Swagger UI:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Usage

### API Endpoints

Refer to the [API Documentation](#api-documentation) for detailed information on available endpoints.

### Example Request

```bash
curl -X GET http://localhost:8080/api/2281/templates
```

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in
`application.yml`.

### Key Configuration Parameters

- **Server Port:**

  ```yaml
  server:
    port: 8080
  ```
- **Database Settings:**

  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3306/your_database
      username: your_db_username
      password: your_db_password
  ```

### Database Initialization

The project is set up with [Flyway](https://github.com/flyway/flyway) for database migrations. Flyway is disabled by
default so you will have to enable it to automatically populate the database schema upon application startup.

```yaml
spring:
  flyway:
    enabled: true
```

- **No additional setup is required** for database initialization, as long as the database connection settings are
  correctly configured.

### Additional Notes

- **Application Profiles:**

  Use Spring profiles (`dev`, `prod`, etc.) to manage different configurations for different environments.

- **Logging Configuration:**

  Adjust logging levels if necessary.

## Contributing

Contributions are welcome! Please
see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Code status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-templating&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-templating)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-templating&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-templating)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-templating&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-templating)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-templating&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-templating)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-templating&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-templating)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-templating&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-templating)

---

© 2024 Sundsvalls kommun
