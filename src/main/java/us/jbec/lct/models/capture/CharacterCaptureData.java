package us.jbec.lct.models.capture;

import us.jbec.lct.models.geometry.LabeledRectangle;

/**
 * CaptureData implementation for character data
 */
public class CharacterCaptureData extends CaptureData {

    private LabeledRectangle labeledRectangle;

    public CharacterCaptureData() {
        super();
    }

    public CharacterCaptureData(CharacterCaptureData source) {
        super(source);
        labeledRectangle = new LabeledRectangle(source.getLabeledRectangle());
    }

    public LabeledRectangle getLabeledRectangle() {
        return labeledRectangle;
    }

    public void setLabeledRectangle(LabeledRectangle labeledRectangle) {
        this.labeledRectangle = labeledRectangle;
    }

}
