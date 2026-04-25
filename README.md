# Web Client Auth Library

A flexible, configurable authentication library for Spring WebFlux projects. Supports both internal and external token validation with pluggable claims mapping.

## Features
- Validates JWT or opaque tokens via HTTP endpoints
- Supports both internal and external system authentication
- All URLs, error codes, required claims, and messages are externalized via Spring configuration
- Claims mapping is fully customizable via the `ClaimsMapper` interface
- Sensible defaults for mobile number, role, and login metadata
- Override any bean for your project needs (e.g., `ClaimsMapper`, `WebClient`)
- Enforces required claims as configured
- Pluggable error handling (error code/message configurable)

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
    error-code: "THAN1501"
    error-message: "Invalid or missing token claims"
```

### All Configurable Properties
- `validation-url`: Internal token validation endpoint
- `external-token-validation-url`: External token validation endpoint
- `required-claims`: List of claims that must be present in the token response
- `error-code`: Error code to use when validation fails
- `error-message`: Error message to use when validation fails

## Customizing Claims Mapping
By default, the library expects claims like:
```json
{
  "valid": true,
  "claims": {
    "mobileNumber": "...",
    "role": "customer",
    "loginMetadata": { ... }
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

## Overriding Beans
You can override any bean (e.g., `ClaimsMapper`, `WebClient`, `TokenServiceProperties`) by providing your own `@Component` or `@Bean` in your project.

## WebClient Customization
You can provide your own `WebClient` bean for custom interceptors, timeouts, or logging. The library will use your bean if present.

## Testing
For local/test profiles, the library provides stub implementations that always return a dummy authenticated user.

## Advanced Extension Points
- Override `ClaimsMapper` for custom claims logic
- Override `TokenServiceProperties` for advanced configuration
- Add your own error handling by customizing error codes/messages
- Support for multi-tenancy or multiple token types can be added by extending the services

## JavaDoc/KDoc
All public interfaces and classes are documented in code for IDE support.

## License
MIT
