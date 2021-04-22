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

@Service
public class MigrationService {

    Logger LOG = LoggerFactory.getLogger(MigrationService.class);

    private final UserService userService;
    private final PrimaryImageIO primaryImageIO;
    private final ProjectService projectService;
    private final CloudCaptureDocumentService cloudCaptureDocumentService;
    private final ObjectMapper objectMapper;

    public MigrationService(UserService userService, PrimaryImageIO primaryImageIO, ProjectService projectService, CloudCaptureDocumentService cloudCaptureDocumentService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.primaryImageIO = primaryImageIO;
        this.projectService = projectService;
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void migrate(MigrationRequest migrationRequest, boolean first) throws FirebaseAuthException, IOException {
        var user = userService.getAuthorizedUserByToken(migrationRequest.getFirebaseUuid()).getUser();
        var job = migrationRequest.getImageJob();
        // some migrated jobs might be old and still need this
        ImageJobFieldTransformer.transform(job);
        if (first) {
            var existingDocs = user.getCloudCaptureDocuments();
            existingDocs.stream()
                    .filter(CloudCaptureDocument::isMigrated)
                    .map(CloudCaptureDocument::getUuid)
                    .forEach(cloudCaptureDocumentService::markDocumentDeleted);
        }
        LOG.info("Received request to save migrated job...");
        String uuid = cloudCaptureDocumentService.retrieveNewId();
        var cloudCaptureDocument = new CloudCaptureDocument();
        cloudCaptureDocument.setName(FilenameUtils.getBaseName(job.getId()));
        cloudCaptureDocument.setUuid(uuid);
        cloudCaptureDocument.setOwner(user);
        cloudCaptureDocument.setDocumentStatus(DocumentStatus.INGESTED);
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