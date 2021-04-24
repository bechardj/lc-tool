package us.jbec.lct.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuthException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import us.jbec.lct.io.PrimaryImageIO;
import us.jbec.lct.models.DocumentStatus;
import us.jbec.lct.models.MigrationRequest;
import us.jbec.lct.models.database.CloudCaptureDocument;
import us.jbec.lct.transformers.ImageJobFieldTransformer;

import java.io.IOException;

/**
 * Service for processing migration of legacy image jobs from the standalone application into the cloud application
 */
@Service
public class MigrationService {

    Logger LOG = LoggerFactory.getLogger(MigrationService.class);

    private final UserService userService;
    private final PrimaryImageIO primaryImageIO;
    private final ProjectService projectService;
    private final CloudCaptureDocumentService cloudCaptureDocumentService;
    private final ObjectMapper objectMapper;

    /**
     * Service for processing migration of legacy image jobs from the standalone application into the cloud application
     * @param userService autowired parameter
     * @param primaryImageIO autowired parameter
     * @param projectService autowired parameter
     * @param cloudCaptureDocumentService autowired parameter
     * @param objectMapper autowired parameter
     */
    public MigrationService(UserService userService, PrimaryImageIO primaryImageIO, ProjectService projectService, CloudCaptureDocumentService cloudCaptureDocumentService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.primaryImageIO = primaryImageIO;
        this.projectService = projectService;
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
        this.objectMapper = objectMapper;
    }

    /**
     * Process a migration request, and create a new CloudCaptureDocument for the user sending the migration request
     * If this is the first migration request, delete all other migrated jobs
     *
     * @param migrationRequest migration request to process
     * @param first whether or not this is the first migration request of the migration
     * @throws FirebaseAuthException
     * @throws IOException
     */
    @Transactional
    public void migrate(MigrationRequest migrationRequest, boolean first) throws FirebaseAuthException, IOException {
        var user = userService.getAuthorizedUserByToken(migrationRequest.getFirebaseUuid()).getUser();
        var job = migrationRequest.getImageJob();
        // some migrated jobs might be old and still need this
        ImageJobFieldTransformer.transform(job);
        if (first) {
            var existingDocs = user.getCloudCaptureDocuments();
            if (existingDocs != null) {
                existingDocs.stream()
                        .filter(CloudCaptureDocument::isMigrated)
                        .map(CloudCaptureDocument::getUuid)
                        .forEach(id -> cloudCaptureDocumentService.markDocumentDeleted(id, false));
            }
        }
        LOG.info("Received request to save migrated job...");
        String uuid = cloudCaptureDocumentService.retrieveNewId();
        job.setId(uuid);
        var cloudCaptureDocument = new CloudCaptureDocument();
        cloudCaptureDocument.setName(FilenameUtils.getBaseName(migrationRequest.getOriginalFileName()));
        cloudCaptureDocument.setUuid(uuid);
        cloudCaptureDocument.setOwner(user);
        cloudCaptureDocument.setDocumentStatus(DocumentStatus.MIGRATED);
        cloudCaptureDocument.setMigrated(true);
        cloudCaptureDocument.setJobData(objectMapper.writeValueAsString(job));
        cloudCaptureDocument.setProject(projectService.getDefaultProject());
        cloudCaptureDocument.setMigrated(true);
        var extension = FilenameUtils.getExtension(migrationRequest.getOriginalFileName());
        var filePath = primaryImageIO.persistImage(migrationRequest.getEncodedImage(), uuid, extension);
        cloudCaptureDocument.setFilePath(filePath);
        cloudCaptureDocumentService.saveCloudCaptureDocument(cloudCaptureDocument);
    }
}
