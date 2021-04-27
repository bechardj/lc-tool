package us.jbec.lct.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lct.models.database.ArchivedJobData;

/**
 * Repository interface for providing default CRUD operations
 */
public interface ArchivedJobDataRepository extends CrudRepository<ArchivedJobData, String> {
}