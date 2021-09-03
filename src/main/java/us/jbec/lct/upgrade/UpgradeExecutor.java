package us.jbec.lct.upgrade;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.VersionForUpgrade;

import java.util.ArrayList;
import java.util.List;

public abstract class UpgradeExecutor {

    private final List<Upgrade> upgrades = new ArrayList<>();

    public abstract boolean shouldPerform(VersionForUpgrade versionForUpgrade);
    public boolean optional() {
        return upgrades.stream().allMatch(Upgrade::optional);
    };

    public abstract VersionForUpgrade upgradedTo();

    @Transactional
    public void execute() throws RuntimeException {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new LCToolException("Upgrade not performed because Transaction Not Active!");
        }
        upgrades.stream().forEach(Upgrade::execute);
    }

    public List<Upgrade> getUpgrades() {
        return upgrades;
    }
}
