package us.jbec.lct.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import us.jbec.lct.models.database.ArchivedJobData;

/**
 * Repository interface for providing default CRUD operations
 */
public interface ArchivedJobDataRepository extends CrudRepository<ArchivedJobData, String> {

    /**
     * Create archive data for capture data. Old records will be purged at the proc-level
     * @param docUuid
     */
    @Query(value = "CALL CREATE_ARCHIVE_DATA(:doc_uuid)",
            nativeQuery = true)
    @Modifying
    void createCaptureDataArchive(@Param("doc_uuid") String docUuid);

}