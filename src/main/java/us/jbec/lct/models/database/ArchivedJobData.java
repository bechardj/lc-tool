package us.jbec.lct.models.database;

import org.hibernate.annotations.CreationTimestamp;
import us.jbec.lct.models.VersionForUpgrade;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

/**
 * Database entity for archived CloudCaptureData - on save, the previous ImageJob data is saved to an archive table
 */
@Entity
public class ArchivedJobData {

    @Id
    @GeneratedValue
    private long id;

    /**
     * Cloud capture document for which this version was archived
     */
    @ManyToOne
    @JoinColumn(name = "uuid")
    private CloudCaptureDocument sourceDocumentUuid;

    /**
     * Archived serialized ImageJob data
     */
    @Lob
    private String jobData;

    private VersionForUpgrade versionForUpgrade;

    /**
     * When was this archive created
     */
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

    public VersionForUpgrade getVersionForUpgrade() {
        return versionForUpgrade;
    }

    public void setVersionForUpgrade(VersionForUpgrade versionForUpgrade) {
        this.versionForUpgrade = versionForUpgrade;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
