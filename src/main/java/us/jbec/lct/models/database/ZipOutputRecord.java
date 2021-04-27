package us.jbec.lct.models.database;

import org.hibernate.annotations.CreationTimestamp;
import us.jbec.lct.models.ZipType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Database entity storing the location of a Zip File, as well as other metadata
 */
@Entity
public class ZipOutputRecord implements Serializable {
    @Id
    @GeneratedValue
    private long id;

    @CreationTimestamp
    private LocalDateTime createDate;

    private String fileUri;
    private String filePath;
    private String source;
    private ZipType zipType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ZipType getZipType() {
        return zipType;
    }

    public void setZipType(ZipType zipType) {
        this.zipType = zipType;
    }
}
