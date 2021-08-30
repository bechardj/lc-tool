package us.jbec.lct.models.capture;

import us.jbec.lct.models.geometry.LabeledRectangle;

public class CharacterCaptureData extends CaptureData implements Cloneable {

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

    public CharacterCaptureData clone() throws CloneNotSupportedException {
        CharacterCaptureData clone = (CharacterCaptureData) super.clone();
        clone.setLabeledRectangle(new LabeledRectangle(this.getLabeledRectangle()));
        return clone;
    }
}
