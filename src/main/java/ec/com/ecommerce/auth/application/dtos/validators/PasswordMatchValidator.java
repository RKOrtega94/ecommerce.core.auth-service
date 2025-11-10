package ec.com.ecommerce.auth.application.dtos.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Objects;

public class PasswordMatchValidator implements ConstraintValidator<ConfirmPassword, Object> {
    private static final Logger log = LoggerFactory.getLogger(PasswordMatchValidator.class);
    String passwordField;
    String confirmPasswordField;

    @Override
    public void initialize(ConfirmPassword constraintAnnotation) {
        this.passwordField = constraintAnnotation.password();
        this.confirmPasswordField = constraintAnnotation.confirmPassword();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null || passwordField == null || passwordField.isEmpty() || confirmPasswordField == null || confirmPasswordField.isEmpty())
            return true;

        try {
            Field passwordObj = value.getClass().getDeclaredField(passwordField);
            Field confirmPasswordObj = value.getClass().getDeclaredField(confirmPasswordField);

            passwordObj.setAccessible(true);
            confirmPasswordObj.setAccessible(true);

            String password = (String) passwordObj.get(value);
            String confirmPassword = (String) confirmPasswordObj.get(value);

            boolean isValid = Objects.equals(password, confirmPassword);

            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode(confirmPasswordField).addConstraintViolation();
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error during password match validation", e);
            return false;
        }
    }
}
