package us.jbec.lct.models.geometry;

import java.util.List;

public class LineSegment extends org.locationtech.jts.geom.LineSegment {

    public LineSegment(List<Double> line) {
        super(line.get(0), line.get(1), line.get(2), line.get(3));
    }

    public LineSegment(double x1, double y1, double x2, double y2) {
        super(x1, y1, x2, y2);
    }

    public boolean interceptsRectangle(OffsetRectangle rectangle) {
        return this.intersection(rectangle.getLeftEdge()) != null ||
                this.intersection(rectangle.getRightEdge()) != null;
    }
}
