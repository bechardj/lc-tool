package us.jbec.lct.models;

public enum UserRoles {
    USER("USER"),
    ADMIN("ADMIN");

    private String description;

    UserRoles(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
