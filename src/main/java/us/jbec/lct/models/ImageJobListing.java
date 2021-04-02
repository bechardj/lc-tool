package us.jbec.lct.models;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class ImageJobListing {

    private final int MAX_LEN = 50;

    private String fileName;
    private String dateAdded;
    private String status;
    private String notes;
    private String url;

    public ImageJobListing(ImageJobFile imageJobFile) throws IOException {
        this.fileName = FilenameUtils.removeExtension(imageJobFile.getImageFile().getName());
        if (!imageJobFile.getImageJob().getFields().containsKey("timestamp")) {
            this.dateAdded = "Never";
        } else {
            this.dateAdded = (String) imageJobFile.getImageJob().getFields().get("timestamp");
        }
        this.notes = (String) imageJobFile.getImageJob().getFields().get(ImageJobFields.NOTES.name());
        if (StringUtils.length(this.notes) > MAX_LEN) {
            this.notes = StringUtils.left(this.notes, MAX_LEN) + "...";
        }
        this.status = imageJobFile.getImageJob().getStatus();
        this.url = "/open/document?id=" + this.fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
