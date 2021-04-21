package us.jbec.lct.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lct.models.database.ArchivedJobData;
import us.jbec.lct.models.database.CloudCaptureDocument;

public interface ArchivedJobDataRepository extends CrudRepository<ArchivedJobData, String> {
}