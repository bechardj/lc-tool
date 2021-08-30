package us.jbec.lct.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import us.jbec.lct.models.database.CloudCaptureDocument;

/**
 * Repository interface for providing default CRUD operations
 */
public interface CloudCaptureDocumentRepository extends CrudRepository<CloudCaptureDocument, String> {

    @Query(value = "select * from cloud_capture_document where uuid= :uuid for update",
            nativeQuery = true)
    CloudCaptureDocument selectDocumentForUpdate(@Param("uuid") String uuid);

}