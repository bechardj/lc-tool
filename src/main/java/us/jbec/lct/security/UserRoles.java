package us.jbec.lct.security;

/**
 * Model representing user roles for authorization purposes
 */
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
