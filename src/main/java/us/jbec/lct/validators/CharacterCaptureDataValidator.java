package us.jbec.lct.validators;

import org.apache.commons.lang3.StringUtils;
import us.jbec.lct.models.capture.CaptureDataRecordType;
import us.jbec.lct.models.capture.CharacterCaptureData;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class CharacterCaptureDataValidator implements ConstraintValidator<CharacterCaptureDataConstraint, CharacterCaptureData> {
    @Override
    public void initialize(CharacterCaptureDataConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(CharacterCaptureData captureData, ConstraintValidatorContext context) {
        // todo: redundant?
        if (StringUtils.isEmpty(captureData.getUuid())) {
            return false;
        }
        if (captureData.getLabeledRectangle().getLabel() != null) {
            if (captureData.getLabeledRectangle().getLabel().length() > 1) {
                return true;
            }
        }
        if (captureData.getCaptureDataRecordType() != CaptureDataRecordType.DELETE &&
                !captureData.getLabeledRectangle().generateCoordinatesAsList().stream().allMatch(Objects::nonNull)) {
            return false;
        }
        return true;
    }
}
