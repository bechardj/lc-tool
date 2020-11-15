package us.jbec.lyrasis.models;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class ImageJobFile {
    private File imageFile;
    private ImageJob imageJob;

    /**
     * Object representing an ImageJob File in the output directory
     *
     * @param imageFile The file for the corresponding information
     * @param imageJob The corresponding deserialized JSON
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
