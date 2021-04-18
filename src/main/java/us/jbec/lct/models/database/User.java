package us.jbec.lct.models.database;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.firebase.auth.FirebaseToken;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
public class User {


    @Id
    private String firebaseIdentifier;
    private String firebaseName;
    private String firebaseEmail;
    @ManyToMany
    @JsonManagedReference
    private Set<Project> project;

    @OneToMany
    private Set<Role> roles;

    @OneToMany
    private Set<RemotelySubmittedJob> remotelySubmittedJobs;

    public User (FirebaseToken firebaseToken) {
        this.firebaseIdentifier = firebaseToken.getUid();
        this.firebaseName = firebaseToken.getName();
        this.firebaseEmail = firebaseToken.getEmail();
    }

    public User() {

    }

    public String getFirebaseIdentifier() {
        return firebaseIdentifier;
    }

    public void setFirebaseIdentifier(String firebaseIdentifier) {
        this.firebaseIdentifier = firebaseIdentifier;
    }

    public String getFirebaseName() {
        return firebaseName;
    }

    public void setFirebaseName(String firebaseName) {
        this.firebaseName = firebaseName;
    }

    public String getFirebaseEmail() {
        return firebaseEmail;
    }

    public void setFirebaseEmail(String firebaseEmail) {
        this.firebaseEmail = firebaseEmail;
    }

    public Set<Project> getProject() {
        return project;
    }

    public void setProject(Set<Project> project) {
        this.project = project;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<RemotelySubmittedJob> getRemotelySubmittedJobs() {
        return remotelySubmittedJobs;
    }

    public void setRemotelySubmittedJobs(Set<RemotelySubmittedJob> remotelySubmittedJobs) {
        this.remotelySubmittedJobs = remotelySubmittedJobs;
    }
}
