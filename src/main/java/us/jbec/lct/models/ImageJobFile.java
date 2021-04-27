package us.jbec.lct.models;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * Convenience model for grouping an image file, and the corresponding ImageJob
 */
public class ImageJobFile {
    private File imageFile;
    private ImageJob imageJob;

    /**
     * Convenience model for grouping an image file, and the corresponding ImageJob
     *
     * @param imageFile The image file for the corresponding ImageJob information
     * @param imageJob The corresponding deserialized ImageJob JSON
     */
    public ImageJobFile(File imageFile, ImageJob imageJob) {
        this.imageFile = imageFile;
        this.imageJob = imageJob;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }

    public ImageJob getImageJob() {
        return imageJob;
    }

    public void setImageJob(ImageJob imageJob) {
        this.imageJob = imageJob;
    }

    public String getImageFileName(){
        return FilenameUtils.removeExtension(this.imageFile.getName());
    }
}
