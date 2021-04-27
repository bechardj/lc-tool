package us.jbec.lct.models;

import java.io.Serializable;

/**
 * Model representing a Migration Request from the standalone desktop application, to be
 * processed by the MigrationService and added to the cloud version of the tool
 */
public class MigrationRequest implements Serializable {
    private ImageJob imageJob;
    private String originalFileName;
    private String encodedImage;
    private String firebaseUuid;

    public ImageJob getImageJob() {
        return imageJob;
    }

    public void setImageJob(ImageJob imageJob) {
        this.imageJob = imageJob;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getEncodedImage() {
        return encodedImage;
    }

    public void setEncodedImage(String encodedImage) {
        this.encodedImage = encodedImage;
    }

    public String getFirebaseUuid() {
        return firebaseUuid;
    }

    public void setFirebaseUuid(String firebaseUuid) {
        this.firebaseUuid = firebaseUuid;
    }
}
