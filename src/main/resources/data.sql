CREATE OR REPLACE PROCEDURE SELECT_ALL_CAP_DOC_DATA_INFO_ONLY ()
BEGIN
    select uuid, create_time, document_status, file_path, NULL as job_data, migrated, name, update_time, firebase_identifier, project, file_checksum, notes_preview from cloud_capture_document;
END
#