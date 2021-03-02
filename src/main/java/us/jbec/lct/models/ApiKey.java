package us.jbec.lct.models;

import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
public class ApiKey implements Serializable {
    @Id
    private String id;

    private LocalDateTime created;

    @UpdateTimestamp
    private LocalDateTime updated;

    private Boolean disabled;

    public String getId() {
        return id;
    }

    public void setId(String key) {
        this.id = key;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }
}
