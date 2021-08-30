package us.jbec.lct.validators;

import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.capture.CaptureData;
import us.jbec.lct.models.capture.CaptureDataRecordType;
import us.jbec.lct.models.capture.LineCaptureData;
import us.jbec.lct.models.capture.WordCaptureData;
import us.jbec.lct.models.geometry.LineSegment;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class GenericLineCaptureDataValidator implements ConstraintValidator<GenericLineCaptureDataConstraint, CaptureData> {
    @Override
    public void initialize(GenericLineCaptureDataConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(CaptureData captureData, ConstraintValidatorContext context) {
        if (!(captureData instanceof LineCaptureData) && !(captureData instanceof WordCaptureData)) {
            throw new LCToolException("Attempt was made to validate an instance of CaptureData but received bad class type");
        } else {
            LineSegment underlyingLineSegment;
            if (captureData instanceof LineCaptureData) {
                underlyingLineSegment = ((LineCaptureData) captureData).getLineSegment();
            } else {
                underlyingLineSegment = ((WordCaptureData) captureData).getLineSegment();
            }
            if (underlyingLineSegment == null) {
                return false;
            }
            if (captureData.getCaptureDataRecordType() != CaptureDataRecordType.DELETE &&
                    !underlyingLineSegment.getCoordinatesAsList().stream().allMatch(Objects::nonNull)) {
                return false;
            }
            return true;
        }
    }
}
