package us.jbec.lct.models.capture;

import us.jbec.lct.models.geometry.LineSegment;

/**
 * CaptureData implementation for line capture data
 */
public class LineCaptureData extends CaptureData {
    private LineSegment lineSegment;

    public LineCaptureData() {
        super();
    }

    public LineCaptureData(LineCaptureData source) {
        super(source);
        lineSegment = null == source.getLineSegment() ? null : new LineSegment(source.getLineSegment());
    }

    public LineSegment getLineSegment() {
        return lineSegment;
    }

    public void setLineSegment(LineSegment lineSegment) {
        this.lineSegment = lineSegment;
    }

}
