CREATE OR REPLACE PROCEDURE SELECT_ALL_CAP_DOC_DATA_INFO_ONLY ()
BEGIN
    select uuid, create_time, document_status, file_path, NULL as job_data, migrated, name, update_time, firebase_identifier, project, file_checksum, notes_preview, project_level_editing from cloud_capture_document;
END
#
CREATE OR REPLACE PROCEDURE CAN_SAVE_CAP_DOC (IN USER_UUID VARCHAR(255), IN DOC_UUID VARCHAR(255))
BEGIN
    CASE
        WHEN EXISTS (SELECT 1
                     FROM cloud_capture_document A
                     WHERE A.uuid = DOC_UUID
                       AND A.firebase_identifier = USER_UUID)
            THEN SELECT 1;
        WHEN EXISTS (SELECT 1
                     FROM cloud_capture_document A
                     WHERE A.uuid = DOC_UUID
                       and a.project_level_editing = true
                       AND EXISTS (SELECT 1
                                   FROM user_project B
                                   WHERE B.user_firebase_identifier = USER_UUID
                                     AND B.project_project = A.project))
            THEN SELECT 1;
        ELSE SELECT 0;
        END CASE;
END
#