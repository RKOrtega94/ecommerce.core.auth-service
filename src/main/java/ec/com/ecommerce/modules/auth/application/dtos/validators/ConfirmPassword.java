package ec.com.ecommerce.modules.auth.application.dtos.validators;

import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchValidator.class)
@Documented
public @interface ConfirmPassword {
    String message() default "Passwords do not match";

    Class<?>[] groups() default {};

    Class<? extends jakarta.validation.Payload>[] payload() default {};

    String password() default "password";

    String confirmPassword() default "confirmPassword";
}
