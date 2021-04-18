package us.jbec.lct.models.database;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Set;

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
}
