package us.jbec.lct.models.geometry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Coordinate;

import java.util.Arrays;
import java.util.List;

/**
 * Model for mapping canvas line coordinates (which store the start and end point,
 * and providing other utility methods
 */
@JsonIgnoreProperties({"horizontal", "length", "p0", "p1", "vertical"})
public class LineSegment extends org.locationtech.jts.geom.LineSegment {

    private double x1;
    private double y1;
    private double x2;
    private double y2;

    /**
     * Model for mapping canvas line coordinates (which store the start and end point, and providing other utility methods
     * @param line array consisting of 4 values: Start X, Start Y, End X, End Y
     */
    public LineSegment(List<Double> line) {
        super(line.get(0), line.get(1), line.get(2), line.get(3));
        postConstructor();
    }

    /**
     * Model for mapping canvas line coordinates (which store the start and end point, and providing other utility methods
     * @param x1 Start X coordinate
     * @param y1 Start Y coordinate
     * @param x2 End X coordinate
     * @param y2 End Y coordinate
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public LineSegment(@JsonProperty("x1") double x1, @JsonProperty("y1") double y1, @JsonProperty("x2") double x2, @JsonProperty("y2") double y2) {
        super(x1, y1, x2, y2);
        postConstructor();
    }

    public LineSegment (LineSegment source) {
        super(new Coordinate(source.p0), new Coordinate(source.p1));
        postConstructor();
    }

    private void postConstructor() {
        this.x1 = this.p0.x;
        this.y1 = this.p0.y;
        this.x2 = this.p1.x;
        this.y2 = this.p1.y;
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
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

    @JsonIgnore
    public List<Double> getCoordinatesAsList() {
        return Arrays.asList(p0.x, p0.y, p1.x, p1.y);
    }
}
