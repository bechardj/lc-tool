package us.jbec.lct.repositories;

import org.springframework.data.jpa.repository.Modifying;
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

    @Query(value = "select uuid from cloud_capture_document",
            nativeQuery = true)
    List<String> selectAllDocumentUuids();

    @Query(value = "select job_data from cloud_capture_document where uuid= :uuid",
            nativeQuery = true)
    String selectRawDocumentCaptureData(@Param("uuid") String uuid);

    @Query(value = "CALL UPDATE_CAP_DOC_DATA_RAW(:doc_uuid, :raw_data)",
            nativeQuery = true)
    @Modifying
    void updateRawDocumentCaptureData(@Param("doc_uuid") String docId, @Param("raw_data") String rawData);

    @Query(value = "CALL SELECT_ALL_CAP_DOC_DATA_INFO_ONLY",
            nativeQuery = true)
    List<CloudCaptureDocument> selectAllDocumentCaptureDataInfoOnly();

    @Query(value = "CALL CAN_SAVE_CAP_DOC(:user_uuid, :doc_uuid)",
            nativeQuery = true)
    int canSaveCloudCaptureDocument(@Param("user_uuid") String userUuid, @Param("doc_uuid") String docUuid);

    @Query(value = "CALL SELECT_CAP_DOC_DATA_OWNED_BY_INFO_ONLY(:user_uuid)",
            nativeQuery = true)
    List<CloudCaptureDocument> selectUserDocumentCaptureDataInfoOnly(@Param("user_uuid") String userUuid);

    @Query(value = "CALL SELECT_CAP_DOC_DATA_EDITABLE_BY_INFO_ONLY(:user_uuid)",
            nativeQuery = true)
    List<CloudCaptureDocument> selectUserEditableDocumentCaptureDataInfoOnly(@Param("user_uuid") String userUuid);

}