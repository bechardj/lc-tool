package us.jbec.lct.models;

/**
 * Model representing the status of a CloudCaptureDocument
 */
public enum DocumentStatus {
    INGESTED("Ingested"),
    DELETED("Deleted"),
    EDITED("Edited"),
    MIGRATED("Migrated");

    private String description;

    DocumentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
