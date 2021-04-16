package us.jbec.lct.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lct.models.ProcessingTimeRecord;
import us.jbec.lct.models.ZipOutputRecord;

public interface ZipOutputRepository extends CrudRepository<ZipOutputRecord, Long> {
}