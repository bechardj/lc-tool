package us.jbec.lct.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import us.jbec.lct.models.DocumentStatus;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.capture.CaptureData;
import us.jbec.lct.models.capture.CaptureDataPayload;
import us.jbec.lct.models.capture.CaptureDataRecordType;
import us.jbec.lct.models.capture.DocumentCaptureData;
import us.jbec.lct.models.database.ArchivedJobData;
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
        DocumentCaptureData documentCaptureData = objectMapper.readValue(imageJobAsFile.getBytes(), DocumentCaptureData.class);

        if (documentCaptureData.getUuid() == null) {
            LOG.warn("User updated what is suspected to be legacy file. Will transform format.");
            ImageJob imageJob = objectMapper.readValue(imageJobAsFile.getBytes(), ImageJob.class);
            documentCaptureData = ImageJobTransformer.apply(imageJob);
        }

        documentCaptureData.setUuid(uuid);
        saveCloudCaptureDocument(userToken, documentCaptureData);
    }

    /**
     * Save a cloud capture document using an image job
     * @param userToken token of user owning the document
     * @param imageJob image job to save
     * @throws JsonProcessingException
     */
    @Transactional
    public void saveCloudCaptureDocument(String userToken, DocumentCaptureData documentCaptureData) throws JsonProcessingException {
        var optionalDocument = cloudCaptureDocumentRepository.findById(documentCaptureData.getUuid());
        if (optionalDocument.isPresent()) {
            var document = optionalDocument.get();
            if (!userOwnsDocument(userToken, document)) {
                throw new LCToolException("User not authorized to save job!");
            }
            // todo: set status from object instead of edited
            document.setDocumentStatus(DocumentStatus.EDITED);
            if (document.getArchivedJobDataList().isEmpty()) {
                document.setArchivedJobDataList(new ArrayList<>());
            }
            var archivedData = new ArchivedJobData();
            archivedData.setJobData(document.getJobData());
            archivedData.setSourceDocumentUuid(document);
            document.getArchivedJobDataList().add(archivedData);
            document.setJobData(objectMapper.writeValueAsString(documentCaptureData));
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
        var imageJob = objectMapper.readValue(cloudCaptureDocument.getJobData(), ImageJob.class);
        if (imageJob.getId() == null) {
            try {
                imageJob = DocumentCaptureDataTransformer.apply(objectMapper.readValue(cloudCaptureDocument.getJobData(), DocumentCaptureData.class));
            } catch (CloneNotSupportedException e) {
                throw new LCToolException("Image job conversion failed!");
            }
        }
        return imageJob;
    }

    @Transactional
    @Async
    public void integrateChangesIntoDocument(CaptureDataPayload payload, String uuid) throws JsonProcessingException {
        long start = System.currentTimeMillis();
        var cloudCaptureDocument = cloudCaptureDocumentRepository.selectDocumentForUpdate(uuid);
        var documentCaptureData = getDocumentCaptureDataFromDocument(cloudCaptureDocument);

        if (payload.getCharacterCaptureData() != null) {
            var characterCaptureData = payload.getCharacterCaptureData();
            var targetList = documentCaptureData.getCharacterCaptureDataMap().get(characterCaptureData.getUuid());
            if (shouldIntegrateCaptureData(targetList, characterCaptureData)) {
                documentCaptureData.insertCharacterCaptureData(characterCaptureData);
            } else {
                LOG.error("Try to integrate data twice");
            }
        }
        if (payload.getWordCaptureData() != null) {
            var wordCaptureData = payload.getWordCaptureData();
            var targetList = documentCaptureData.getWordCaptureDataMap().get(wordCaptureData.getUuid());
            if (shouldIntegrateCaptureData(targetList, wordCaptureData)) {
                documentCaptureData.insertWordCaptureData(wordCaptureData);
            } else {
                LOG.error("Try to integrate data twice");
            }
        }
        if (payload.getLineCaptureData() != null) {
            var lineCaptureData = payload.getLineCaptureData();
            var targetList = documentCaptureData.getLineCaptureDataMap().get(lineCaptureData.getUuid());
            if (shouldIntegrateCaptureData(targetList, lineCaptureData)) {
                documentCaptureData.insertLineCaptureData(lineCaptureData);
            } else {
                LOG.error("Try to integrate data twice");
            }
        }

        // todo proper authenticated save
        cloudCaptureDocument.setJobData(objectMapper.writeValueAsString(documentCaptureData));
        cloudCaptureDocumentRepository.save(cloudCaptureDocument);
        long end = System.currentTimeMillis();
        LOG.info("Processing took {}", end-start);
    }

    private <T extends CaptureData> boolean shouldIntegrateCaptureData(List<T> targetList, T dataToIntegrate) {
        if (targetList == null || targetList.isEmpty()) {
            return true;
        }
        if (targetList.size() == 1
                && targetList.get(0).getCaptureDataRecordType() == CaptureDataRecordType.CREATE
                && dataToIntegrate.getCaptureDataRecordType() != CaptureDataRecordType.DELETE) {
            return false;
        }
        return targetList.size() <= 1;
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
