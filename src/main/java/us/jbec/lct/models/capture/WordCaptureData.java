package us.jbec.lct.models.capture;

import us.jbec.lct.models.geometry.LineSegment;

/**
 * CaptureData implementation for word capture data
 */
public class WordCaptureData extends CaptureData {

    public WordCaptureData() {
        super();
    }

    public WordCaptureData(WordCaptureData source) {
        super(source);
        lineSegment = null == source.getLineSegment() ? null : new LineSegment(source.getLineSegment());
    }

    private LineSegment lineSegment;

    public LineSegment getLineSegment() {
        return lineSegment;
    }

    public void setLineSegment(LineSegment lineSegment) {
        this.lineSegment = lineSegment;
    }

}
