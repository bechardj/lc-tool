package us.jbec.lct.models.geometry;

import java.util.List;

public class LabeledRectangle extends OffsetRectangle{

    private String label;

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
