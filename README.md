<!--- AUTO-GENERATED FULL PROJECT README (2026-04-25) -->
# Web Client Library

A flexible, configurable authentication library for Spring WebFlux projects. It supports both internal and external token validation with pluggable claims mapping, and is designed for extensibility and integration in modern Kotlin/Spring applications.

---

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Architecture & Main Components](#architecture--main-components)
- [Project Structure](#project-structure)
- [Setup & Installation](#setup--installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Customization & Extension Points](#customization--extension-points)
- [Testing](#testing)
- [Build & Run](#build--run)
- [Contribution Guidelines](#contribution-guidelines)
- [License](#license)

---

## Overview
Web Client Auth Library provides a robust authentication solution for Spring WebFlux applications. It validates JWT or opaque tokens, supports both internal and external authentication, and allows for deep customization via Spring beans and configuration.

## Features
- Validates JWT or opaque tokens via HTTP endpoints
- Supports both internal and external system authentication
- All URLs, error codes, required claims, and messages are externalized via Spring configuration
- Claims mapping is fully customizable via the `ClaimsMapper` interface
- Sensible defaults for mobile number, role, and login metadata
- Override any bean for your project needs (e.g., `ClaimsMapper`, `WebClient`)
- Enforces required claims as configured
- Pluggable error handling (error code/message configurable)
- Advanced extension points for multi-tenancy, custom error handling, and more

## Architecture & Main Components

**Core Classes & Packages:**
- `security/` — Core authentication logic, including:
  - `TokenService`, `ExternalTokenValidationService`: Validate tokens (internal/external)
  - `ClaimsMapper`: Interface for mapping claims to authentication data
  - `TokenServiceProperties`: Configuration properties for endpoints, claims, etc.
  - `AuthenticationManager`, `AuthenticationToken`, `ExternalSystemAuthenticationToken`: Integrate with Spring Security
  - `authorization/`: Aspect for role-based authorization
- `expection_handling/` — Centralized error handling, custom exceptions, and error codes
- `filters/` — WebClient filters for headers and logging
- `logging/` — Logging utilities and context propagation
- `serializer/` — Serialization helpers
- `web_client/` — WebClient builder and wrappers

**Extension Points:**
- Override any bean (`ClaimsMapper`, `WebClient`, `TokenServiceProperties`, etc.)
- Provide custom error handling via exception classes and advisors
- Extend for multi-tenancy or new token types

## Project Structure

```
src/
  main/
    kotlin/com/anmol/web_client_lib/
      expection_handling/      # Error handling and exception classes
      filters/                 # WebClient filters
      logging/                 # Logging utilities
      security/                # Authentication, authorization, and core logic
        authorization/         # Role-based authorization aspect
      serializer/              # Serialization helpers
      web_client/              # WebClient builder and wrappers
  test/
    kotlin/com/anmol/web_client_lib/
      WebClientLibApplicationTests.kt # Basic context load test
```

## Setup & Installation

**Prerequisites:**
- JDK 21 (Kotlin 1.9.25)
- Gradle (wrapper included)

**Clone and Build:**
```sh
git clone <your-repo-url>
cd web-client-lib
./gradlew build
```

**Add as a Dependency:**
Include this library as a dependency in your Spring Boot project (see your internal artifact repository or build instructions).

## Configuration

Add the following to your `application.yml` or `application.properties`:

```yaml
web-client-lib:
  token-service:
    validation-url: "https://your-internal-token-validation-url"
    external-token-validation-url: "https://your-external-token-validation-url"
    required-claims:
      - mobileNumber
      - role
  security:
    unauthenticated-endpoints:
      - /public/**
      - /health
    externally-exposed-endpoints:
      external:
        authenticated:
          - /external-api/**
      internal:
        authenticated:
          - /internal-api/**

```

**Configurable Properties:**
- `validation-url`: Internal token validation endpoint
- `external-token-validation-url`: External token validation endpoint
- `required-claims`: List of claims that must be present in the token response
- `security.unauthenticated-endpoints`: Endpoints that do not require authentication
- `security.externally-exposed-endpoints`: Map of external/internal endpoints and their authentication requirements
- `info.app.name`: Application name used for logging and context

## Usage

Inject and use the `TokenService` or `ExternalTokenValidationService` in your beans/controllers. The library will automatically use your custom `ClaimsMapper` if present.

```kotlin
@Autowired
lateinit var tokenService: TokenService

fun authenticate(token: String, url: String) {
    tokenService.validate(token, url)
        .subscribe({ authData ->
            // use authData
        }, { error ->
            // handle error
        })
}
```

## Customization & Extension Points

### Customizing Claims Mapping
By default, the library expects claims like:
```json
{
  "valid": true,
  "claims": {
    "mobileNumber": "...",
    "role": "customer",
    "loginMetadata": {}
  }
}
```
If your claims structure is different, provide your own `ClaimsMapper`:

```kotlin
@Component
class MyClaimsMapper : ClaimsMapper {
    override fun map(claims: Map<String, Any>): CustomerAuthenticationData {
        val userId = claims["user_id"] as? String ?: throw IllegalArgumentException("Missing user_id")
        val role = Role.CUSTOMER // or custom logic
        val metadata = claims["meta"] as? Map<String, Any> ?: emptyMap()
        return CustomerAuthenticationData(id = userId, loginMetadata = metadata, role = role)
    }
}
```

### Overriding Beans
You can override any bean (e.g., `ClaimsMapper`, `WebClient`, `TokenServiceProperties`) by providing your own `@Component` or `@Bean` in your project.

### WebClient Customization
You can provide your own `WebClient` bean for custom interceptors, timeouts, or logging. The library will use your bean if present.

### Advanced Extension Points
- Override `ClaimsMapper` for custom claims logic
- Override `TokenServiceProperties` for advanced configuration
- Add your own error handling by customizing error codes/messages
- Support for multi-tenancy or multiple token types by extending the services

## Testing

For local/test profiles, the library provides stub implementations that always return a dummy authenticated user.

Basic test is provided in `WebClientLibApplicationTests.kt` to verify Spring context loads.

## Build & Run

**Build:**
```sh
./gradlew build
```

**Run Tests:**
```sh
./gradlew test
```

**Build Native Image (GraalVM):**
```sh
./gradlew nativeCompile
```

**Build Docker Image:**
```sh
./gradlew bootBuildImage
docker run --rm -p 8080:8080 web-client-lib:0.0.1-SNAPSHOT
```

## Contribution Guidelines

Contributions are welcome! Please follow standard GitHub flow:
- Fork the repository
- Create a feature branch
- Commit your changes with clear messages
- Open a pull request

Please ensure code is tested and documented. For major changes, open an issue first to discuss your proposal.

## License

This project is licensed under the MIT License. See the source header or contact the maintainer for details.

---

*Auto-generated on 2026-04-25. For questions, open an issue or contact the maintainer.*
