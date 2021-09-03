package us.jbec.lct.models.database;

import us.jbec.lct.models.VersionForUpgrade;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UpgradeVersionRecord {

    @Id
    private int id;
    private VersionForUpgrade versionForUpgrade;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public VersionForUpgrade getVersionForUpgrade() {
        return versionForUpgrade;
    }

    public void setVersionForUpgrade(VersionForUpgrade versionForUpgrade) {
        this.versionForUpgrade = versionForUpgrade;
    }

    public static int getDefaultKey() {
        return 1;
    }
}
