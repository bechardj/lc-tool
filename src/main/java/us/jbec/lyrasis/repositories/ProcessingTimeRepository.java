package us.jbec.lyrasis.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lyrasis.models.ProcessingTimeRecord;

public interface ProcessingTimeRepository extends CrudRepository<ProcessingTimeRecord, String> {
}