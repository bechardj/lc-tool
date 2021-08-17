package us.jbec.lct.models.database;

import us.jbec.lct.models.DynamicTextType;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.time.LocalDateTime;

/**
 * Entity for storing dynamic text that can be manipulated on the database
 */
@Entity
public class DynamicText {
    @Id
    private String id;

    private DynamicTextType dynamicTextType;

    private int sortOrder;

    private LocalDateTime effectiveDate;

    private LocalDateTime expiryDate;

    @Lob
    private String text;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DynamicTextType getDynamicTextType() {
        return dynamicTextType;
    }

    public void setDynamicTextType(DynamicTextType dynamicTextType) {
        this.dynamicTextType = dynamicTextType;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDateTime effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
}
