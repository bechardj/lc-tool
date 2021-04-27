package us.jbec.lct.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lct.models.database.CloudCaptureDocument;

/**
 * Repository interface for providing default CRUD operations
 */
public interface CloudCaptureDocumentRepository extends CrudRepository<CloudCaptureDocument, String> {
}