package us.jbec.lct.models.capture;

import us.jbec.lct.models.geometry.LineSegment;

public class WordCaptureData extends CaptureData {

    public WordCaptureData() {
        super();
    }

    public WordCaptureData(WordCaptureData source) {
        super(source);
        lineSegment = new LineSegment(source.getLineSegment());
    }

    private LineSegment lineSegment;

    public LineSegment getLineSegment() {
        return lineSegment;
    }

    public void setLineSegment(LineSegment lineSegment) {
        this.lineSegment = lineSegment;
    }

    public WordCaptureData clone() throws CloneNotSupportedException {
        WordCaptureData clone = (WordCaptureData) super.clone();
        clone.setLineSegment(new LineSegment(this.getLineSegment()));
        return clone;
    }
}
