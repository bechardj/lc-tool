package us.jbec.lct.models;

public enum DocumentStatus {
    INGESTED("Ingested"),
    DELETED("Deleted");

    private String description;

    DocumentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
