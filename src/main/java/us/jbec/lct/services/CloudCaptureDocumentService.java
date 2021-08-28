package us.jbec.lct.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import us.jbec.lct.models.DocumentStatus;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.capture.DocumentCaptureData;
import us.jbec.lct.models.database.ArchivedJobData;
import us.jbec.lct.models.database.CloudCaptureDocument;
import us.jbec.lct.models.database.User;
import us.jbec.lct.repositories.ArchivedJobDataRepository;
import us.jbec.lct.repositories.CloudCaptureDocumentRepository;
import us.jbec.lct.transformers.ImageJobTransformer;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Service for interacting with cloud capture documents
 */
@Service
public class CloudCaptureDocumentService {

    Logger LOG = LoggerFactory.getLogger(CloudCaptureDocumentService.class);

    private int MAX_ATTEMPTS = 3;

    private final CloudCaptureDocumentRepository cloudCaptureDocumentRepository;
    private final ArchivedJobDataRepository archivedJobDataRepository;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    /**
     * Service for interacting with cloud capture documents
     * @param cloudCaptureDocumentRepository autowired parameter
     * @param archivedJobDataRepository autowired parameter
     * @param objectMapper autowired parameter
     * @param userService autowired parameter
     */
    public CloudCaptureDocumentService(CloudCaptureDocumentRepository cloudCaptureDocumentRepository,
                                       ArchivedJobDataRepository archivedJobDataRepository,
                                       ObjectMapper objectMapper,
                                       UserService userService) {
        this.cloudCaptureDocumentRepository = cloudCaptureDocumentRepository;
        this.archivedJobDataRepository = archivedJobDataRepository;
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    /**
     * Retrieve a new, unique UUID for cloud capture documents
     * @return unique UUID
     */
    public String retrieveNewId() {
        for(int i = 0; i < MAX_ATTEMPTS; i++) {
            String uuid = UUID.randomUUID().toString();
            if (cloudCaptureDocumentRepository.findById(uuid).isEmpty()) {
                return uuid;
            }
        }
        throw new LCToolException("Could not generate ID!");
    }

    /**
     * Persist a cloud capture document to the database
     * @param cloudCaptureDocument cloud capture document to save
     */
    public void saveCloudCaptureDocument(CloudCaptureDocument cloudCaptureDocument) {
        cloudCaptureDocumentRepository.save(cloudCaptureDocument);
    }

    /**
     * Save a cloud capture document, using an uploaded image job file as the source
     * @param userToken token of user owning the document
     * @param imageJobAsFile image job file to save
     * @param uuid UUID of document to save
     * @throws IOException
     */
    @Transactional
    public void saveCloudCaptureDocument(String userToken, MultipartFile imageJobAsFile, String uuid) throws IOException {
        ImageJob imageJob = objectMapper.readValue(imageJobAsFile.getBytes(), ImageJob.class);
        imageJob.setId(uuid);
        saveCloudCaptureDocument(userToken, imageJob);
    }

    /**
     * Save a cloud capture document using an image job
     * @param userToken token of user owning the document
     * @param imageJob image job to save
     * @throws JsonProcessingException
     */
    @Transactional
    public void saveCloudCaptureDocument(String userToken, ImageJob imageJob) throws JsonProcessingException {
        var optionalDocument = cloudCaptureDocumentRepository.findById(imageJob.getId());
        if (optionalDocument.isPresent()) {
            var document = optionalDocument.get();
            if (!userOwnsDocument(userToken, document)) {
                throw new LCToolException("User not authorized to save job!");
            }
            document.setDocumentStatus(DocumentStatus.EDITED);
            if (document.getArchivedJobDataList().isEmpty()) {
                document.setArchivedJobDataList(new ArrayList<>());
            }
            var archivedData = new ArchivedJobData();
            archivedData.setJobData(document.getJobData());
            archivedData.setSourceDocumentUuid(document);
            document.getArchivedJobDataList().add(archivedData);
            document.setJobData(objectMapper.writeValueAsString(imageJob));
            archivedJobDataRepository.save(archivedData);
            cloudCaptureDocumentRepository.save(document);
        } else {
            throw new LCToolException("Could not find image job");
        }
    }

    /**
     * Given a user token, determine if a document is owned by the user
     * @param userToken user token to use for ownership check
     * @param document document to verify ownership of
     * @return whether or not the user owns the document
     */
    public boolean userOwnsDocument(String userToken, CloudCaptureDocument document) {
        var optionalUser = userService.getUserByFirebaseIdentifier(userToken);
        return optionalUser.isPresent() && optionalUser.get().getFirebaseIdentifier().equals(document.getOwner().getFirebaseIdentifier());
    }

    /**
     * Given a user token, determine if a documentId is owned by the user
     * @param userToken user token to use for ownership check
     * @param documentId documentId to verify ownership of
     * @return whether or not the user owns the document
     */
    public boolean userOwnsDocument(String userToken, String documentId) {
        var optionalUser = userService.getUserByFirebaseIdentifier(userToken);
        var optionalDocument = cloudCaptureDocumentRepository.findById(documentId);
        // todo remove
        return true || optionalUser.isPresent() && optionalDocument.isPresent()
                && optionalUser.get().getFirebaseIdentifier().equals(optionalDocument.get().getOwner().getFirebaseIdentifier());
    }

    /**
     * Return a map of all cloud capture documents and the corresponding image jobs for a given user
     * @param userToken user token to lookup the user with
     * @return Map of all cloud capture documents and the corresponding image jobs
     * @throws JsonProcessingException
     */
    public Map<CloudCaptureDocument, ImageJob> getCloudCaptureDocumentsByUserIdentifier(String userToken) throws JsonProcessingException {
        var result = new HashMap<CloudCaptureDocument, ImageJob>();
        Optional<User> optionalUser = userService.getUserByFirebaseIdentifier(userToken);
        if (optionalUser.isPresent()) {
            for (var document : optionalUser.get().getCloudCaptureDocuments()) {
                if (DocumentStatus.DELETED != document.getDocumentStatus()) {
                    result.put(document, getImageJobFromDocument(document));
                }
            }
            return result;
        } else {
            throw new LCToolException("Could not find user");
        }
    }

    /**
     * Return a map of all active cloud capture documents and the corresponding image job
     * @return Map of all active cloud capture documents and the corresponding image job
     * @throws JsonProcessingException
     */
    public Map<CloudCaptureDocument, ImageJob> getActiveCloudCaptureDocuments() throws JsonProcessingException {
        var result = new HashMap<CloudCaptureDocument, ImageJob>();

        for (var document : cloudCaptureDocumentRepository.findAll()) {
            if (DocumentStatus.DELETED != document.getDocumentStatus()) {
                result.put(document, getImageJobFromDocument(document));
            }
        }

        return result;
    }

    /**
     * Get image job from cloud capture document based on the UUID
     * @param uuid document UUID to retrieve Image Job from
     * @return corresponding image job
     * @throws JsonProcessingException
     */
    public ImageJob getImageJobByUuid(String uuid) throws JsonProcessingException {
        var optionalDocument = cloudCaptureDocumentRepository.findById(uuid);
        if (optionalDocument.isPresent()) {
            return getImageJobFromDocument(optionalDocument.get());
        } else {
            throw new LCToolException("Could not find image job");
        }
    }

    /**
     * Get image job from cloud capture document based on the UUID
     * @param uuid document UUID to retrieve Image Job from
     * @return corresponding image job
     * @throws JsonProcessingException
     */
    public DocumentCaptureData getDocumentCaptureDataByUuid(String uuid) throws JsonProcessingException {
        var optionalDocument = cloudCaptureDocumentRepository.findById(uuid);
        if (optionalDocument.isPresent()) {
            return getDocumentCaptureDataFromDocument(optionalDocument.get());
        } else {
            throw new LCToolException("Could not find image job");
        }
    }

    /**
     * Deserialize image job from LOB column on CloudCaptureDocument
     * @param cloudCaptureDocument CloudCaptureDocument to retrieve image job from
     * @return deserialized image job
     * @throws JsonProcessingException
     */
    public ImageJob getImageJobFromDocument(CloudCaptureDocument cloudCaptureDocument) throws JsonProcessingException {
        return objectMapper.readValue(cloudCaptureDocument.getJobData(), ImageJob.class);
    }

    public DocumentCaptureData getDocumentCaptureDataFromDocument(CloudCaptureDocument cloudCaptureDocument) throws JsonProcessingException {
        DocumentCaptureData data;
        data = objectMapper.readValue(cloudCaptureDocument.getJobData(), DocumentCaptureData.class);
        if (data.getUuid() == null) {
            LOG.info("performing upgrade...");
            var convertedData = ImageJobTransformer.apply(objectMapper.readValue(cloudCaptureDocument.getJobData(), ImageJob.class));
            cloudCaptureDocument.setJobData(objectMapper.writeValueAsString(convertedData));
            cloudCaptureDocumentRepository.save(cloudCaptureDocument);
            return convertedData;
        }
        return data;
    }

    /**
     * Mark document with corresponding uuid as deleted, optionally preserving the corresponding image
     * @param uuid uuid of document to mark as deleted
     * @param keepImage whether the corresponding persisted image should be saved
     * @return whether or not the document was successfully deleted
     */
    public boolean markDocumentDeleted(String uuid, boolean keepImage) {
        var optionalDocument = cloudCaptureDocumentRepository.findById(uuid);
        if (optionalDocument.isPresent()) {
            var doc = optionalDocument.get();
            doc.setDocumentStatus(DocumentStatus.DELETED);
            if (!keepImage) {
                var file = new File(doc.getFilePath());
                if(!file.delete()) {
                    LOG.error("Failed to Delete Image {}", uuid);
                }
            }
            cloudCaptureDocumentRepository.save(doc);
            return true;
        }
        return false;
    }
}
