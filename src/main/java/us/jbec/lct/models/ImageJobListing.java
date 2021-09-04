package us.jbec.lct.models;

import org.apache.commons.lang3.StringUtils;
import us.jbec.lct.models.database.CloudCaptureDocument;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Convenience model for displaying an ImageJob Listing in the document listing views
 */
public class ImageJobListing {

    private final int MAX_LEN = 50;

    private String fileName;
    private String owner;
    private String dateAdded;
    private String status;
    private String notes;
    private String openUrl;
    private String deleteUrl;

    static DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' hh:mm a");

    public ImageJobListing(CloudCaptureDocument cloudCaptureDocument) throws IOException {
        this.fileName = cloudCaptureDocument.getName();
        this.owner = cloudCaptureDocument.getOwner().getFirebaseEmail();
        if (cloudCaptureDocument.getUpdateTime() != null) {
            this.dateAdded = fmt.format(cloudCaptureDocument.getUpdateTime());
        } else {
            this.dateAdded = "Unknown";
        }
        this.notes = cloudCaptureDocument.getNotesPreview();
        if (StringUtils.length(this.notes) > MAX_LEN) {
            this.notes = StringUtils.left(this.notes, MAX_LEN) + "...";
        }
        this.status = cloudCaptureDocument.getDocumentStatus().getDescription();
        this.openUrl = "/secure/open/document?uuid=" + cloudCaptureDocument.getUuid();
        this.deleteUrl = "/secure/delete/document?uuid=" + cloudCaptureDocument.getUuid();
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

    public String getOpenUrl() {
        return openUrl;
    }

    public void setOpenUrl(String openUrl) {
        this.openUrl = openUrl;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDeleteUrl() {
        return deleteUrl;
    }

    public void setDeleteUrl(String deleteUrl) {
        this.deleteUrl = deleteUrl;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
