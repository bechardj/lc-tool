package us.jbec.lct.models.geometry;

import java.util.List;

/**
 * Model for mapping canvas rectangle coordinates (which store the start point,
 * the width, and the height), converting all the starting points to the top left,
 * storing the label, and providing other utility methods
 */
public class LabeledRectangle extends OffsetRectangle {

    private String label;

    /**
     * Model for mapping canvas rectangle coordinates (which store the start start point,
     * the width, and the height), converting all the starting points to the top left,
     * and providing other utility methods
     * @param coordinates list containing starting x and y coordinates, width and height
     * @param label the label corresponding to this rectangle
     */
    public LabeledRectangle(List<Double> coordinates, String label) {
        super(coordinates);
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
