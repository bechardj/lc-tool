package us.jbec.lct.models;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * Represents a User Invitation to the application
 */
public class UserInvitation {

    Long id;
    @Email
    @NotBlank
    String email;
    String requestedBy;
    String requestedRole;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestedRole() {
        return requestedRole;
    }

    public void setRequestedRole(String requestedRole) {
        this.requestedRole = requestedRole;
    }
}
