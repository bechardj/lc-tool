package us.jbec.lct.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import us.jbec.lct.io.PrimaryImageIO;
import us.jbec.lct.models.DocumentStatus;
import us.jbec.lct.models.capture.DocumentCaptureData;
import us.jbec.lct.models.database.CloudCaptureDocument;
import us.jbec.lct.models.database.User;

import java.io.IOException;

/**
 * Service for ingesting uploaded images
 */
@Component
public class IngestService {

    Logger LOG = LoggerFactory.getLogger(IngestService.class);

    private final PrimaryImageIO primaryImageIO;
    private final ObjectMapper objectMapper;
    private final CloudCaptureDocumentService cloudCaptureDocumentService;
    private final ProjectService projectService;
    private final ImageJobProcessingService imageJobProcessingService;

    /**
     * Service for ingesting uploaded images
     * @param primaryImageIO autowired parameter
     * @param objectMapper autowired parameter
     * @param cloudCaptureDocumentService autowired parameter
     * @param projectService autowired parameter
     * @param imageJobProcessingService autowired parameter
     */
    public IngestService(PrimaryImageIO primaryImageIO, ObjectMapper objectMapper, CloudCaptureDocumentService cloudCaptureDocumentService, ProjectService projectService, ImageJobProcessingService imageJobProcessingService) {
        this.primaryImageIO = primaryImageIO;
        this.objectMapper = objectMapper;
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
        this.projectService = projectService;
        this.imageJobProcessingService = imageJobProcessingService;
    }

    /**
     * Given a user and uploaded image, create a new CloudCaptureDocument for the uploaded image
     * @param user User to assign ownership of the uploaded image to
     * @param uploadedFile uploaded image file to ingest and create a CloudCaptureDocument from
     * @throws IOException
     */
    @Transactional
    public void ingest(User user, MultipartFile uploadedFile) throws IOException {
        LOG.info("Received request to save image...");
        String uuid = cloudCaptureDocumentService.retrieveNewId();
        var cloudCaptureDocument = new CloudCaptureDocument();
        cloudCaptureDocument.setName(FilenameUtils.getBaseName(uploadedFile.getOriginalFilename()));
        cloudCaptureDocument.setUuid(uuid);
        cloudCaptureDocument.setOwner(user);
        cloudCaptureDocument.setDocumentStatus(DocumentStatus.INGESTED);
        cloudCaptureDocument.setMigrated(false);
        cloudCaptureDocument.setDocumentCaptureData(new DocumentCaptureData(uuid));
        cloudCaptureDocument.setProject(projectService.getDefaultProject());
        cloudCaptureDocument.setMigrated(false);
        var imageFile = primaryImageIO.persistImage(uploadedFile, uuid);
        var checksum = DigestUtils.sha256Hex((FileUtils.readFileToByteArray(imageFile)));
        cloudCaptureDocument.setFileChecksum(checksum);
        cloudCaptureDocument.setFilePath(imageFile.getAbsolutePath());
        cloudCaptureDocumentService.directlySaveCloudCaptureDocument(cloudCaptureDocument);
    }

}
