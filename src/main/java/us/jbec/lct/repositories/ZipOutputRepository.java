package us.jbec.lct.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lct.models.database.ZipOutputRecord;

/**
 * Repository interface for providing default CRUD operations native queries
 */
public interface ZipOutputRepository extends CrudRepository<ZipOutputRecord, Long> {
}