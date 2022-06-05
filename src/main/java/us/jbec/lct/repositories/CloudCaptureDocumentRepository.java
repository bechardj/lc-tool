package us.jbec.lct.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import us.jbec.lct.models.database.CloudCaptureDocument;

import java.util.List;

/**
 * Repository interface for providing default CRUD operations
 */
public interface CloudCaptureDocumentRepository extends CrudRepository<CloudCaptureDocument, String> {

    /**
     * Retrieves & Locks CloudCaptureDocument until transaction is completed
     * @param uuid document UUID
     * @return locked CloudCaptureDocument
     */
    @Query(value = "select * from cloud_capture_document where uuid= :uuid for update",
            nativeQuery = true)
    CloudCaptureDocument selectDocumentForUpdate(@Param("uuid") String uuid);

    /**
     * Select all cloud capture document UUID's
     * @return all cloud capture document UUID's
     */
    @Query(value = "select uuid from cloud_capture_document",
            nativeQuery = true)
    List<String> selectAllDocumentUuids();

    /**
     * Select all active cloud capture document UUID's
     * @return all active cloud capture document UUID's
     */
    @Query(value = "select uuid from CloudCaptureDocument " +
            "where documentStatus <> us.jbec.lct.models.DocumentStatus.DELETED and " +
            "documentStatus <> us.jbec.lct.models.DocumentStatus.IGNORED")
    List<String> selectAllActiveDocumentUuids();

    /**
     * Fetch the raw underlying LOB data for DocumentCaptureData
     * @param uuid document UUID
     * @return raw DocumentCaptureData LOB data
     */
    @Query(value = "select job_data from cloud_capture_document where uuid= :uuid",
            nativeQuery = true)
    String selectRawDocumentCaptureData(@Param("uuid") String uuid);

    /**
     * Call proc to update raw capture data at the LOB level, bypassing converter
     * @param docId document UUID
     * @param rawData raw data to replace
     */
    @Query(value = "CALL UPDATE_CAP_DOC_DATA_RAW(:doc_uuid, :raw_data)",
            nativeQuery = true)
    @Modifying
    void updateRawDocumentCaptureData(@Param("doc_uuid") String docId, @Param("raw_data") String rawData);

    /**
     * Call proc to select metadata only for cloud capture document.
     * Should NOT be called from a transaction.
     * @return List of CloudCaptureDocument containing only metadata
     */
    @Transactional(propagation = Propagation.NEVER)
    @Query(value = "CALL SELECT_ALL_CAP_DOC_DATA_INFO_ONLY",
            nativeQuery = true)
    List<CloudCaptureDocument> selectAllDocumentCaptureDataInfoOnly();

    /**
     * Call proc to determine if user is authorized to save a capture document
     * User can save if they own the doc, or project-level editing is on and they belong to the same project
     * @param userUuid user uuid
     * @param docUuid document uuid
     * @return 1 if the user is authorized to save, 0 otherwise
     */
    @Query(value = "CALL CAN_SAVE_CAP_DOC(:user_uuid, :doc_uuid)",
            nativeQuery = true)
    int canSaveCloudCaptureDocument(@Param("user_uuid") String userUuid, @Param("doc_uuid") String docUuid);

    /**
     * Call proc to select metadata only for cloud capture document belonging to a user.
     * Should NOT be called from a transaction.
     * @param userUuid user uuid
     * @return List of CloudCaptureDocument containing only metadata
     */
    @Transactional(propagation = Propagation.NEVER)
    @Query(value = "CALL SELECT_CAP_DOC_DATA_OWNED_BY_INFO_ONLY(:user_uuid)",
            nativeQuery = true)
    List<CloudCaptureDocument> selectUserDocumentCaptureDataInfoOnly(@Param("user_uuid") String userUuid);

    /**
     * Call proc to select metadata only for cloud capture document editable a user.
     * Should NOT be called from a transaction.
     * @param userUuid user uuid
     * @return List of CloudCaptureDocument containing only metadata
     */
    @Transactional(propagation = Propagation.NEVER)
    @Query(value = "CALL SELECT_CAP_DOC_DATA_EDITABLE_BY_INFO_ONLY(:user_uuid)",
            nativeQuery = true)
    List<CloudCaptureDocument> selectUserEditableDocumentCaptureDataInfoOnly(@Param("user_uuid") String userUuid);

}