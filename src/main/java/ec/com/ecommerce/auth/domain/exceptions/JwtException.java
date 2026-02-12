package ec.com.ecommerce.auth.domain.exceptions;

/**
 * Base exception for JWT-related errors.
 */
public class JwtException extends RuntimeException {
    public JwtException(String message) {
        super(message);
    }

    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
