CREATE OR REPLACE PROCEDURE SELECT_ALL_CAP_DOC_DATA_INFO_ONLY ()
BEGIN
    select uuid, create_time, document_status, file_path, NULL as job_data, migrated, name, update_time, firebase_identifier, project, file_checksum, notes_preview, project_level_editing from cloud_capture_document;
END;
#
CREATE OR REPLACE PROCEDURE UPDATE_CAP_DOC_DATA_RAW (IN DOC_UUID VARCHAR(255), IN CAP_DOC_DATA LONGBLOB)
BEGIN
    UPDATE cloud_capture_document
    SET job_data = CAP_DOC_DATA
    WHERE uuid = DOC_UUID;
END;
#
CREATE OR REPLACE FUNCTION CAN_EDIT (USER_UUID VARCHAR(255), DOC_UUID VARCHAR(255))
RETURNS INT
BEGIN
    CASE
        WHEN EXISTS (SELECT 1
                     FROM cloud_capture_document A
                     WHERE A.uuid = DOC_UUID
                       AND A.firebase_identifier = USER_UUID)
            THEN RETURN 1;
        WHEN EXISTS (SELECT 1
                     FROM cloud_capture_document A
                     WHERE A.uuid = DOC_UUID
                       and A.project_level_editing = true
                       AND EXISTS (SELECT 1
                                   FROM user_project B
                                   WHERE B.user_firebase_identifier = USER_UUID
                                     AND B.project_project = A.project))
            THEN RETURN 1;
        ELSE RETURN 0;
        END CASE;
END;
#
CREATE OR REPLACE PROCEDURE CAN_SAVE_CAP_DOC (IN USER_UUID VARCHAR(255), IN DOC_UUID VARCHAR(255))
BEGIN
    SELECT CAN_EDIT(USER_UUID, DOC_UUID) AS CAN_SAVE;
END;
#
CREATE OR REPLACE PROCEDURE SELECT_CAP_DOC_DATA_OWNED_BY_INFO_ONLY (IN USER_UUID VARCHAR(255))
BEGIN
    select uuid, create_time, document_status, file_path, NULL as job_data, migrated, name, update_time, firebase_identifier, project, file_checksum, notes_preview, project_level_editing
    FROM cloud_capture_document
    WHERE firebase_identifier = USER_UUID;
END;
#
CREATE OR REPLACE PROCEDURE SELECT_CAP_DOC_DATA_EDITABLE_BY_INFO_ONLY (IN USER_UUID VARCHAR(255))
BEGIN
    select uuid, create_time, document_status, file_path, NULL as job_data, migrated, name, update_time, firebase_identifier, project, file_checksum, notes_preview, project_level_editing
    FROM cloud_capture_document A
    WHERE TRUE = (SELECT CAN_EDIT(USER_UUID, A.uuid));
END;
#
CREATE OR REPLACE PROCEDURE CLEANUP_ARCHIVE_DATA (IN DOC_UUID VARCHAR(255), IN KEEP_COUNT int)
BEGIN
    DECLARE DELETE_COUNT INT;
    SET DELETE_COUNT = (SELECT COUNT(*) FROM archived_job_data A2
                        WHERE A2.version_for_upgrade IS NULL
                          AND A2.uuid=DOC_UUID) - KEEP_COUNT;
    CASE
        WHEN DELETE_COUNT > 0
            THEN DELETE FROM archived_job_data
                 WHERE version_for_upgrade IS NULL
                   AND uuid = DOC_UUID
                 ORDER BY create_time
                 LIMIT DELETE_COUNT;
        ELSE
            BEGIN END;
        END CASE;
END;
#
CREATE OR REPLACE PROCEDURE CREATE_ARCHIVE_DATA(IN DOC_UUID VARCHAR(255))
BEGIN
    INSERT INTO archived_job_data (id, create_time, job_data, uuid, version_for_upgrade)
    SELECT COALESCE((SELECT MAX(ID) FROM archived_job_data), 0) + 1 AS ID,
           CURRENT_TIME AS CREATE_TIME,
           job_data,
           uuid,
           NULL
    FROM cloud_capture_document where uuid = DOC_UUID;
    CALL CLEANUP_ARCHIVE_DATA(DOC_UUID, 20);
END;
#