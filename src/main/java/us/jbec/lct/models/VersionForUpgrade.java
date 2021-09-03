package us.jbec.lct.models;

public enum VersionForUpgrade {
    PRE_2_0_0("Pre-2.0.0"),
    VERSION_2_0_0("2.0.0");

    private String description;

    VersionForUpgrade(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
