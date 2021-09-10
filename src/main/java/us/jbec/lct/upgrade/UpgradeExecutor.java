package us.jbec.lct.upgrade;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.VersionForUpgrade;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes upgrades
 */
public abstract class UpgradeExecutor {

    private final List<Upgrade> upgrades = new ArrayList<>();

    /**
     * Given the current version, should this upgrade be executed
     * @param versionForUpgrade current database version
     * @return should this upgrade be executed
     */
    public abstract boolean shouldPerform(VersionForUpgrade versionForUpgrade);

    /**
     * Are all underlying upgrades optional
     * @return are all underlying upgrades optional
     */
    public boolean optional() {
        return upgrades.stream().allMatch(Upgrade::optional);
    };

    /**
     * What database version did this executor upgrade to
     * @return version upgraded to
     */
    public abstract VersionForUpgrade upgradedTo();

    /**
     * Execute the upgrade. Requires a transaction
     * @throws RuntimeException
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void execute() throws RuntimeException {
        // sanity check
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new LCToolException("Upgrade not performed because Transaction Not Active!");
        }
        upgrades.stream().forEach(Upgrade::execute);
    }

    /**
     * Get underlying upgrades
     * @return underlying upgrades
     */
    public List<Upgrade> getUpgrades() {
        return upgrades;
    }
}
