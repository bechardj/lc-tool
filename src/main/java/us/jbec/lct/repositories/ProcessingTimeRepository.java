package us.jbec.lct.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lct.models.ProcessingTimeRecord;

public interface ProcessingTimeRepository extends CrudRepository<ProcessingTimeRecord, String> {
}