package ec.com.ecommerce.auth.domain.exceptions;

/**
 * Exception thrown when token generation fails.
 */
public class TokenGenerationException extends JwtException {
    public TokenGenerationException(String message) {
        super(message);
    }

    public TokenGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
