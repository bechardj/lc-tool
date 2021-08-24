package us.jbec.lct.models.geometry;

import org.locationtech.jts.geom.Coordinate;

import java.util.Arrays;
import java.util.List;

/**
 * Model for mapping canvas line coordinates (which store the start and end point,
 * and providing other utility methods
 */
public class LineSegment extends org.locationtech.jts.geom.LineSegment {

    /**
     * Model for mapping canvas line coordinates (which store the start and end point, and providing other utility methods
     * @param line array consisting of 4 values: Start X, Start Y, End X, End Y
     */
    public LineSegment(List<Double> line) {
        super(line.get(0), line.get(1), line.get(2), line.get(3));
    }

    /**
     * Model for mapping canvas line coordinates (which store the start and end point, and providing other utility methods
     * @param x1 Start X coordinate
     * @param y1 Start Y coordinate
     * @param x2 End X coordinate
     * @param y2 End Y coordinate
     */
    public LineSegment(double x1, double y1, double x2, double y2) {
        super(x1, y1, x2, y2);
    }

    public LineSegment (LineSegment source) {
        super(new Coordinate(source.p0), new Coordinate(source.p1));
    }

    /**
     * Does this line intersect the provided rectangle?
     * @param rectangle OffsetRectangle to check if this line intersects
     * @return whether or not this line intersects the provided rectangle
     */
    public boolean intersectsRectangle(OffsetRectangle rectangle) {
        return this.intersection(rectangle.getLeftEdge()) != null
                || this.intersection(rectangle.getRightEdge()) != null
                || this.intersection(rectangle.getTopEdge()) != null
                || this.intersection(rectangle.getBottomEdge()) != null;
    }

    public List<Double> getCoordinatesAsList() {
        return Arrays.asList(p0.x, p0.y, p1.x, p1.y);
    }
}
