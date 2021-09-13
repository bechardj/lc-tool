package us.jbec.lct.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = CharacterCaptureDataValidator.class)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CharacterCaptureDataConstraint {
    String message() default "Invalid capture rectangle";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
