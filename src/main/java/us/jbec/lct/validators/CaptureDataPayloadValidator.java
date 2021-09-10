package us.jbec.lct.validators;

import us.jbec.lct.models.capture.CaptureDataPayload;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Validate CaptureDataPayloadValidator
 */
public class CaptureDataPayloadValidator implements ConstraintValidator<CaptureDataPayloadConstraint, CaptureDataPayload> {

    private CharacterCaptureDataValidator characterCaptureDataValidator;
    private GenericLineCaptureDataValidator genericLineCaptureDataValidator;

    @Override
    public void initialize(CaptureDataPayloadConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        characterCaptureDataValidator = new CharacterCaptureDataValidator();
        genericLineCaptureDataValidator = new GenericLineCaptureDataValidator();
    }

    @Override
    public boolean isValid(CaptureDataPayload payload, ConstraintValidatorContext context) {

        // Clients may not request complete sync directly
        if (payload.getRequestCompleteSync() != null && payload.getRequestCompleteSync()) {
            return false;
        }

        var populatedFieldCount = Stream.of(payload.getCharacterCaptureData(), payload.getLineCaptureData(), payload.getWordCaptureData())
                .filter(Objects::nonNull)
                .count();
        if (populatedFieldCount != 1) {
            return false;
        }
        if (payload.getCharacterCaptureData() != null) {
            if (!characterCaptureDataValidator.isValid(payload.getCharacterCaptureData(), context)) {
                return false;
            }
        }
        if (payload.getWordCaptureData() != null) {
            if (!genericLineCaptureDataValidator.isValid(payload.getWordCaptureData(), context)) {
                return false;
            }
        }
        if (payload.getLineCaptureData() != null) {
            if (!genericLineCaptureDataValidator.isValid(payload.getLineCaptureData(), context)) {
                return false;
            }
        }
        return true;
    }
}
