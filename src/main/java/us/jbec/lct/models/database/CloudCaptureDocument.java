package us.jbec.lct.models.database;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import us.jbec.lct.converters.DocumentCaptureDataConverter;
import us.jbec.lct.models.DocumentStatus;
import us.jbec.lct.models.capture.DocumentCaptureData;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Database Entity for keeping track of ImageJobs, their owner, associated image, and other metadata
 */
@Entity
public class CloudCaptureDocument implements Serializable {

    /**
     * Document UUID
     */
    @Id
    private String uuid;

    /**
     * Document name, taken from image name on upload
     */
    private String name;

    /**
     * Owner of the document
     */
    @ManyToOne
    @JoinColumn(name = "firebase_identifier")
    private User owner;

    /**
     * Associated project
     */
    @ManyToOne
    @JoinColumn(name = "project")
    private Project project;

    /**
     * List of archived saved job data, backing up prior versions of ImageJob data on every save
     */
    @OneToMany(mappedBy = "sourceDocumentUuid")
    List<ArchivedJobData> archivedJobDataList;

    /**
     * Creation time
     */
    @CreationTimestamp
    private LocalDateTime createTime;

    /**
     * Last update of the document
     */
    @UpdateTimestamp
    private LocalDateTime updateTime;

    /**
     * DocumentCaptureData data serialized and stored in a Lob.
     * Column name specified for legacy support
     */
    @Lob
    @Column(name = "job_data")
    @Convert(converter = DocumentCaptureDataConverter.class)
    private DocumentCaptureData documentCaptureData;

    /**
     * File path to associated image
     */
    private String filePath;

    /**
     * Checksum of associated image
     */
    private String fileChecksum;

    /**
     * Document Status
     */
    private DocumentStatus documentStatus;

    /**
     * Was the document created via MigrationService?
     */
    private boolean migrated;

    private String notesPreview;

    private Boolean projectLevelEditing;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<ArchivedJobData> getArchivedJobDataList() {
        return archivedJobDataList;
    }

    public void setArchivedJobDataList(List<ArchivedJobData> archivedJobDataList) {
        this.archivedJobDataList = archivedJobDataList;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public DocumentCaptureData getDocumentCaptureData() {
        return documentCaptureData;
    }

    public void setDocumentCaptureData(DocumentCaptureData jobData) {
        this.documentCaptureData = jobData;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public DocumentStatus getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(DocumentStatus documentStatus) {
        this.documentStatus = documentStatus;
    }

    public boolean isMigrated() {
        return migrated;
    }

    public void setMigrated(boolean migrated) {
        this.migrated = migrated;
    }

    public String getFileChecksum() {
        return fileChecksum;
    }

    public void setFileChecksum(String fileChecksum) {
        this.fileChecksum = fileChecksum;
    }

    public String getNotesPreview() {
        return notesPreview;
    }

    public void setNotesPreview(String notesPreview) {
        this.notesPreview = notesPreview;
    }

    public Boolean getProjectLevelEditing() {
        return projectLevelEditing;
    }

    public void setProjectLevelEditing(Boolean projectLevelEditing) {
        this.projectLevelEditing = projectLevelEditing;
    }
}
