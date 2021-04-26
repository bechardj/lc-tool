package us.jbec.lct.models.database;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Set;

/**
 * Database entity representing a Project
 */
@Entity
public class Project {

    @Id
    @GeneratedValue
    private Long project;

    private String name;

    @ManyToMany
    @JsonBackReference
    private Set<User> users;

    @ManyToMany
    @JsonBackReference
    private Set<User> admins;

    @OneToMany(mappedBy = "project")
    private List<CloudCaptureDocument> cloudCaptureDocuments;


    public Long getProject() {
        return project;
    }

    public void setProject(Long project) {
        this.project = project;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<User> getAdmins() {
        return admins;
    }

    public void setAdmins(Set<User> admins) {
        this.admins = admins;
    }

    public List<CloudCaptureDocument> getCloudCaptureDocuments() {
        return cloudCaptureDocuments;
    }

    public void setCloudCaptureDocuments(List<CloudCaptureDocument> cloudCaptureDocuments) {
        this.cloudCaptureDocuments = cloudCaptureDocuments;
    }
}
