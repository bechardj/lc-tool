package us.jbec.lct.upgrade.v2_0_0;

import org.springframework.stereotype.Component;
import us.jbec.lct.models.VersionForUpgrade;
import us.jbec.lct.upgrade.UpgradeExecutor;

/**
 * Execute upgrades from versions prior to 2.0.0 to 2.0.0
 */
@Component
public class UpgradeExecutor_2_0_0 extends UpgradeExecutor {

    public UpgradeExecutor_2_0_0 (ImageJobUpgrader imageJobUpgrader, DefaultProjectAssigner defaultProjectAssigner) {
        super();
        this.getUpgrades().add(imageJobUpgrader);
        this.getUpgrades().add(defaultProjectAssigner);
    }


    @Override
    public boolean shouldPerform(VersionForUpgrade versionForUpgrade) {
        return versionForUpgrade.equals(VersionForUpgrade.PRE_2_0_0);
    }

    @Override
    public VersionForUpgrade upgradedTo() {
        return VersionForUpgrade.VERSION_2_0_0;
    }
}
