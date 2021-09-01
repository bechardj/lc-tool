package us.jbec.lct.models.capture;

import us.jbec.lct.models.geometry.LineSegment;

public class LineCaptureData extends CaptureData {
    private LineSegment lineSegment;

    public LineCaptureData() {
        super();
    }

    public LineCaptureData(LineCaptureData source) {
        super(source);
        lineSegment = new LineSegment(source.getLineSegment());
    }

    public LineSegment getLineSegment() {
        return lineSegment;
    }

    public void setLineSegment(LineSegment lineSegment) {
        this.lineSegment = lineSegment;
    }

}
