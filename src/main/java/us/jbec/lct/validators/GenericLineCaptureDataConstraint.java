package us.jbec.lct.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = GenericLineCaptureDataValidator.class)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GenericLineCaptureDataConstraint {
    String message() default "Invalid capture line";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
