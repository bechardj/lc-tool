package us.jbec.lct.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import us.jbec.lct.models.database.CloudCaptureDocument;

import java.util.List;

/**
 * Repository interface for providing default CRUD operations
 */
public interface CloudCaptureDocumentRepository extends CrudRepository<CloudCaptureDocument, String> {

    @Query(value = "select * from cloud_capture_document where uuid= :uuid for update",
            nativeQuery = true)
    CloudCaptureDocument selectDocumentForUpdate(@Param("uuid") String uuid);

    @Query(value = "select job_data from cloud_capture_document where uuid= :uuid",
            nativeQuery = true)
    String selectRawDocumentCaptureData(@Param("uuid") String uuid);

    @Query(value = "CALL SELECT_ALL_CAP_DOC_DATA_INFO_ONLY",
            nativeQuery = true)
    List<CloudCaptureDocument> selectAllDocumentCaptureDataInfoOnly();

}