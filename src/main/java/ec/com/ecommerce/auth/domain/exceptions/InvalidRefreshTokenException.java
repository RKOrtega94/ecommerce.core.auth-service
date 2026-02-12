package ec.com.ecommerce.auth.domain.exceptions;

public class InvalidRefreshTokenException extends JwtException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }

    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}