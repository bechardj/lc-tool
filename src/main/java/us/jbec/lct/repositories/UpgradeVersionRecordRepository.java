package us.jbec.lct.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lct.models.database.UpgradeVersionRecord;

/**
 * Repository interface for providing default CRUD operations
 */
public interface UpgradeVersionRecordRepository extends CrudRepository<UpgradeVersionRecord, Integer> {
}