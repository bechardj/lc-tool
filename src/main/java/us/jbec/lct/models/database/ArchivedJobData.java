package us.jbec.lct.models.database;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class ArchivedJobData {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    @JoinColumn(name = "uuid")
    private CloudCaptureDocument sourceDocumentUuid;

    @Lob
    private String jobData;

    @CreationTimestamp
    private LocalDateTime createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CloudCaptureDocument getSourceDocumentUuid() {
        return sourceDocumentUuid;
    }

    public void setSourceDocumentUuid(CloudCaptureDocument sourceDocumentUuid) {
        this.sourceDocumentUuid = sourceDocumentUuid;
    }

    public String getJobData() {
        return jobData;
    }

    public void setJobData(String jobData) {
        this.jobData = jobData;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
