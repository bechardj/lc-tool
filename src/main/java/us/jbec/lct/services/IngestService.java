package us.jbec.lct.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import us.jbec.lct.io.PrimaryImageIO;
import us.jbec.lct.models.DocumentStatus;
import us.jbec.lct.models.database.CloudCaptureDocument;
import us.jbec.lct.models.database.User;

import java.io.IOException;

@Component
public class IngestService {

    Logger LOG = LoggerFactory.getLogger(IngestService.class);

    private final PrimaryImageIO primaryImageIO;
    private final ObjectMapper objectMapper;
    private final CloudCaptureDocumentService cloudCaptureDocumentService;
    private final ProjectService projectService;
    private final JobProcessingService jobProcessingService;

    public IngestService(PrimaryImageIO primaryImageIO, ObjectMapper objectMapper, CloudCaptureDocumentService cloudCaptureDocumentService, ProjectService projectService, JobProcessingService jobProcessingService) {
        this.primaryImageIO = primaryImageIO;
        this.objectMapper = objectMapper;
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
        this.projectService = projectService;
        this.jobProcessingService = jobProcessingService;
    }

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
        cloudCaptureDocument.setJobData(objectMapper.writeValueAsString(jobProcessingService.initializeImageJob(uuid)));
        cloudCaptureDocument.setProject(projectService.getDefaultProject());
        cloudCaptureDocument.setMigrated(false);
        var filePath = primaryImageIO.persistImage(uploadedFile, uuid);
        cloudCaptureDocument.setFilePath(filePath);
        cloudCaptureDocumentService.saveCloudCaptureDocument(cloudCaptureDocument);
    }

}
