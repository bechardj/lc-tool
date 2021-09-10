package us.jbec.lct.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import us.jbec.lct.models.DocumentStatus;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.capture.CaptureDataPayload;
import us.jbec.lct.models.capture.DocumentCaptureData;
import us.jbec.lct.models.database.CloudCaptureDocument;
import us.jbec.lct.models.database.User;
import us.jbec.lct.repositories.ArchivedJobDataRepository;
import us.jbec.lct.repositories.CloudCaptureDocumentRepository;
import us.jbec.lct.transformers.DocumentCaptureDataTransformer;
import us.jbec.lct.transformers.ImageJobTransformer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for interacting with cloud capture documents
 */
@Service
public class CloudCaptureDocumentService {

    Logger LOG = LoggerFactory.getLogger(CloudCaptureDocumentService.class);

    private int MAX_ATTEMPTS = 3;

    private final CloudCaptureDocumentRepository cloudCaptureDocumentRepository;
    private final ArchivedJobDataRepository archivedJobDataRepository;
    private final CaptureDataMergeService captureDataMergeService;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Service for interacting with cloud capture documents
     * @param cloudCaptureDocumentRepository autowired parameter
     * @param archivedJobDataRepository autowired parameter
     * @param objectMapper autowired parameter
     * @param userService autowired parameter
     */
    public CloudCaptureDocumentService(CloudCaptureDocumentRepository cloudCaptureDocumentRepository,
                                       ArchivedJobDataRepository archivedJobDataRepository,
                                       CaptureDataMergeService captureDataMergeService, ObjectMapper objectMapper,
                                       UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.cloudCaptureDocumentRepository = cloudCaptureDocumentRepository;
        this.archivedJobDataRepository = archivedJobDataRepository;
        this.captureDataMergeService = captureDataMergeService;
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
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
    public void directlySaveCloudCaptureDocument(CloudCaptureDocument cloudCaptureDocument) {
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
    public void saveUploadedCaptureData(String userToken, MultipartFile imageJobAsFile, String uuid) throws IOException {
        DocumentCaptureData documentCaptureData = objectMapper.readValue(imageJobAsFile.getBytes(), DocumentCaptureData.class);

        if (documentCaptureData.getUuid() == null) {
            LOG.warn("User updated what is suspected to be legacy file. Will transform format.");
            ImageJob imageJob = objectMapper.readValue(imageJobAsFile.getBytes(), ImageJob.class);
            documentCaptureData = ImageJobTransformer.apply(imageJob);
        }

        documentCaptureData.setUuid(uuid);
        saveDocumentCaptureData(documentCaptureData, userToken, true);
    }

    @Transactional
    public int saveDocumentCaptureData(DocumentCaptureData newData, String userToken, boolean archive) throws JsonProcessingException {

        // check permissions
        var cloudCaptureDocument = cloudCaptureDocumentRepository.selectDocumentForUpdate(newData.getUuid());

        if (!userCanSaveDocument(userToken, cloudCaptureDocument.getUuid())) {
            throw new LCToolException("User not authorized to save job!");
        }

        var existingData = cloudCaptureDocument.getDocumentCaptureData();
        if (archive) {
            archivedJobDataRepository.createCaptureDataArchive(cloudCaptureDocument.getUuid());
        }

        int mergeCount = captureDataMergeService.mergeCaptureData(existingData, newData);

        assignOverallStatus(cloudCaptureDocument, newData);

        existingData.setNotes(newData.getNotes());

        var notesPreview = newData.getNotes();
        if (StringUtils.length(notesPreview) > 50) {
            notesPreview = StringUtils.left(notesPreview,50) + "...";
        }

        cloudCaptureDocument.setNotesPreview(notesPreview);

        cloudCaptureDocument.setDocumentCaptureData(existingData);
        cloudCaptureDocumentRepository.save(cloudCaptureDocument);

        return mergeCount;
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
        return optionalUser.isPresent() && optionalDocument.isPresent()
                && optionalUser.get().getFirebaseIdentifier().equals(optionalDocument.get().getOwner().getFirebaseIdentifier());
    }

    public boolean userCanSaveDocument(String userUuid, String documentId) {
        return BooleanUtils.toBoolean(cloudCaptureDocumentRepository.canSaveCloudCaptureDocument(userUuid, documentId));
    }

    /**
     * Return a map of all cloud capture documents and the corresponding image jobs for a given user
     * @param userToken user token to lookup the user with
     * @return Map of all cloud capture documents and the corresponding image jobs
     */
    @Transactional(propagation = Propagation.NEVER)
    public List<CloudCaptureDocument> getCloudCaptureDocumentsByUserIdentifier(String userToken) {
        Optional<User> optionalUser = userService.getUserByFirebaseIdentifier(userToken);
        if (optionalUser.isPresent()) {
            var uuid = optionalUser.get().getFirebaseIdentifier();
            return cloudCaptureDocumentRepository.selectUserDocumentCaptureDataInfoOnly(uuid).stream()
                    .filter(doc -> doc.getDocumentStatus() != DocumentStatus.DELETED)
                    .toList();
        } else {
            throw new LCToolException("Could not find user");
        }
    }


    @Transactional(propagation = Propagation.NEVER)
    public List<CloudCaptureDocument> getEditableByUserIdentifier(String userToken) {
        Optional<User> optionalUser = userService.getUserByFirebaseIdentifier(userToken);
        if (optionalUser.isPresent()) {
            var uuid = optionalUser.get().getFirebaseIdentifier();
            return cloudCaptureDocumentRepository.selectUserEditableDocumentCaptureDataInfoOnly(uuid).stream()
                    .filter(doc -> doc.getDocumentStatus() != DocumentStatus.DELETED)
                    .toList();
        } else {
            throw new LCToolException("Could not find user");
        }
    }

    /**
     * Return a map of all active cloud capture documents and the corresponding image job
     * @return Map of all active cloud capture documents and the corresponding image job
     */
    @Transactional(propagation = Propagation.NEVER)
    public Map<CloudCaptureDocument, ImageJob> getActiveCloudCaptureDocumentsDataMap() {
        var result = new HashMap<CloudCaptureDocument, ImageJob>();

        for (var document : cloudCaptureDocumentRepository.findAll()) {
            if (DocumentStatus.DELETED != document.getDocumentStatus()) {
                result.put(document, getImageJobFromDocument(document));
            }
        }

        return result;
    }

    @Transactional(propagation = Propagation.NEVER)
    public List<CloudCaptureDocument> getActiveCloudCaptureDocumentsMetadata() {
        var result = new ArrayList<CloudCaptureDocument>();

        for (var document : cloudCaptureDocumentRepository.selectAllDocumentCaptureDataInfoOnly()) {
            if (DocumentStatus.DELETED != document.getDocumentStatus()) {
                result.add(document);
            }
        }
        return result;
    }


    /**
     * Get image job from cloud capture document based on the UUID
     * @param uuid document UUID to retrieve Image Job from
     * @return corresponding image job
     */
    public DocumentCaptureData getDocumentCaptureDataByUuid(String uuid) {
        var optionalDocument = cloudCaptureDocumentRepository.findById(uuid);
        if (optionalDocument.isPresent()) {
            return optionalDocument.get().getDocumentCaptureData();
        } else {
            throw new LCToolException("Could not find image job");
        }
    }

    /**
     * Deserialize image job from LOB column on CloudCaptureDocument
     * @param cloudCaptureDocument CloudCaptureDocument to retrieve image job from
     * @return deserialized image job
     */
    @Deprecated
    public ImageJob getImageJobFromDocument(CloudCaptureDocument cloudCaptureDocument) {
        return DocumentCaptureDataTransformer.apply(cloudCaptureDocument.getDocumentCaptureData());
    }

    @Transactional
    public void saveCaptureData(CaptureDataPayload payload, String docUuid) {
        long start = System.currentTimeMillis();
        var cloudCaptureDocument = cloudCaptureDocumentRepository.selectDocumentForUpdate(docUuid);
        var documentCaptureData = cloudCaptureDocument.getDocumentCaptureData();

        captureDataMergeService.mergePayloadIntoDocument(documentCaptureData, payload);

        cloudCaptureDocument.setDocumentCaptureData(documentCaptureData);
        cloudCaptureDocumentRepository.save(cloudCaptureDocument);
        long end = System.currentTimeMillis();
        LOG.debug("Processing took {}", end-start);
    }

    public List<CaptureDataPayload> buildPayloadsToSyncClient(DocumentCaptureData clientData) {
        var cloudCaptureDocument = cloudCaptureDocumentRepository.selectDocumentForUpdate(clientData.getUuid());
        if (cloudCaptureDocument != null) {
            return captureDataMergeService.createPayloadsForSync(cloudCaptureDocument.getDocumentCaptureData(), clientData);
        } else {
            throw new LCToolException("Could not find document");
        }
    }

    public void requestClientSync(String docUuid, String originator) {
        var payload = new CaptureDataPayload();
        payload.setOriginator(originator);
        payload.setRequestCompleteSync(true);
        messagingTemplate.convertAndSend("/topic/document/" + docUuid, payload);
    }

    private void assignOverallStatus(CloudCaptureDocument cloudCaptureDocument, DocumentCaptureData documentCaptureData) {
        if (documentCaptureData.isEdited()) {
            cloudCaptureDocument.setDocumentStatus(DocumentStatus.EDITED);
        }
        if (documentCaptureData.isCompleted()) {
            cloudCaptureDocument.setDocumentStatus(DocumentStatus.COMPLETED);
        }
        if (cloudCaptureDocument.getDocumentStatus() == null) {
            cloudCaptureDocument.setDocumentStatus(DocumentStatus.INGESTED);
        }
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

    @Transactional
    public void toggleProjectLevelEditing(String uuid, boolean toggle) {
        var optionalDocument = cloudCaptureDocumentRepository.findById(uuid);
        if (optionalDocument.isPresent()) {
            var doc = optionalDocument.get();
            doc.setProjectLevelEditing(toggle);
        } else {
            throw new LCToolException("Document not found when attempting to toggle project level editability/");
        }
    }
}
