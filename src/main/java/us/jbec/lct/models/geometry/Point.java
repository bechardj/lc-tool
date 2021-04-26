package us.jbec.lct.models.geometry;

/**
 * Model representing a 2D Point
 */
public class Point {
    private double x;
    private double y;

    /**
     * Constructor for a 2D Point
     * @param x X Coordinate
     * @param y Y Coordinate
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

}
