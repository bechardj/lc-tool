package us.jbec.lct.models.geometry;

import java.util.List;
import java.util.Objects;

public class OffsetRectangle {

    private double x1;
    private double y1;
    private double width;
    private double height;

    public OffsetRectangle(List<Double> coordinates) {
        x1 = coordinates.get(0);
        y1 = coordinates.get(1);
        width = coordinates.get(2);
        height = coordinates.get(3);

        if (width < 0) {
            x1 = x1 + width;
            width *= -1;
        }
        if (height < 0) {
            y1 = y1 + height;
            height *= -1;
        }
    }

    public double getX1() {
        return x1;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public double getY1() {
        return y1;
    }

    public void setY1(double y1) {
        this.y1 = y1;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getX2() {
        return x1 + width;
    }

    public double getY2() {
        return y1 + height;
    }

    public LineSegment getLeftEdge() {
        return new LineSegment(x1, y1, x1, getY2());
    }

    public LineSegment getRightEdge() {
        return new LineSegment(getX2(), y1, getX2(), getY2());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OffsetRectangle that = (OffsetRectangle) o;
        return Double.compare(that.x1, x1) == 0 && Double.compare(that.y1, y1) == 0 && Double.compare(that.width, width) == 0 && Double.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x1, y1, width, height);
    }
}
