package us.jbec.lct.upgrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.VersionForUpgrade;
import us.jbec.lct.models.database.UpgradeVersionRecord;
import us.jbec.lct.repositories.UpgradeVersionRecordRepository;
import us.jbec.lct.upgrade.v2_0_0.UpgradeExecutor_2_0_0;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Service for performing Upgrades at the Database Level on ContextRefreshedEvent
 */
@Service
public class UpgradeService implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(UpgradeService.class);

    private final UpgradeVersionRecordRepository upgradeVersionRecordRepository;

    private final static List<UpgradeExecutor> upgradeExecutors = new ArrayList<>();

    public UpgradeService(UpgradeVersionRecordRepository upgradeVersionRecordRepository,
                          UpgradeExecutor_2_0_0 upgradeExecutor_2_0_0) {
        this.upgradeVersionRecordRepository = upgradeVersionRecordRepository;
        upgradeExecutors.add(upgradeExecutor_2_0_0);
    }

    /**
     * Transactionally check which upgrades need to be done, and execute them one by one.
     * After each upgrade, the database version is bumped. If any upgrade fails along the way,
     * changes should be rolled back
     */
    @Transactional
    public void upgrade() {
        LOG.info("Checking if any upgrades need to be performed.");
        int upgradesPerformed = 0;
        for(var upgradeExecutor : upgradeExecutors) {
            var versionForUpgrade = getVersionForUpgrade();
            if (upgradeExecutor.shouldPerform(versionForUpgrade)) {
                LOG.info("Upgrading from DB version {} to {}", versionForUpgrade.getDescription(), upgradeExecutor.upgradedTo().getDescription());
                upgradeExecutor.execute();
                setVersionForUpgrade(upgradeExecutor.upgradedTo());
                upgradesPerformed++;
            }
        }
        LOG.info("{} upgrades performed", upgradesPerformed);
    }

    private void setVersionForUpgrade(VersionForUpgrade versionForUpgrade) {
        var record = upgradeVersionRecordRepository.findById(UpgradeVersionRecord.getDefaultKey());
        if (record.isEmpty()) {
            throw new LCToolException("Version Upgrade Record Missing");
        }
        record.get().setVersionForUpgrade(versionForUpgrade);
        upgradeVersionRecordRepository.save(record.get());
    }

    private VersionForUpgrade getVersionForUpgrade() {
        var optionalRecord = upgradeVersionRecordRepository.findById(UpgradeVersionRecord.getDefaultKey());
        if (optionalRecord.isPresent()) {
            return optionalRecord.get().getVersionForUpgrade();
        } else {
            var record = new UpgradeVersionRecord();
            record.setId(UpgradeVersionRecord.getDefaultKey());
            var versionForUpgrade = Arrays.stream(VersionForUpgrade.values()).max(Comparator.comparing(Enum::ordinal));
            record.setVersionForUpgrade(versionForUpgrade.get());
            upgradeVersionRecordRepository.save(record);
            return versionForUpgrade.get();
        }
    }

    /**
     * Listen for ContextRefreshedEvent. Note that this needs to be done as opposed to executing from a
     * PostConstruct method to make sure Hibernate Transactions can be done
     * @param event ContextRefreshedEvent
     */
    @Transactional
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        upgrade();
    }
}
