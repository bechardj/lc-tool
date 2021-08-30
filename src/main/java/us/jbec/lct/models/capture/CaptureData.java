package us.jbec.lct.models.capture;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public abstract class CaptureData implements Cloneable {

    @NotBlank
    private String uuid;

    @NotNull
    private CaptureDataRecordType captureDataRecordType;

    public CaptureData() {
    }

    public CaptureData(CaptureData source) {
        uuid = source.getUuid();
        captureDataRecordType = source.getCaptureDataRecordType();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public CaptureDataRecordType getCaptureDataRecordType() {
        return captureDataRecordType;
    }

    public void setCaptureDataRecordType(CaptureDataRecordType captureDataRecordType) {
        this.captureDataRecordType = captureDataRecordType;
    }

    public CaptureData clone() throws CloneNotSupportedException {
        CaptureData clone = (CaptureData) super.clone();
        clone.setUuid(this.getUuid());
        clone.setCaptureDataRecordType(this.getCaptureDataRecordType());
        return clone;
    }
}
