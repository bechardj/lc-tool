package us.jbec.lyrasis.models;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ImageJobListing {

    private String fileName;
    private String dateAdded;
    private String status;
    private String url;

    public ImageJobListing(ImageJobFile imageJobFile) throws IOException {
        this.fileName = FilenameUtils.removeExtension(imageJobFile.getImageFile().getName());
        if (!imageJobFile.getImageJob().getFields().containsKey("timestamp")) {
            this.dateAdded = "Never";
        } else {
            this.dateAdded = (String) imageJobFile.getImageJob().getFields().get("timestamp");
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
}
