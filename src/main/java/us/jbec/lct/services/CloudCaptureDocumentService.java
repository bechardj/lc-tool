package us.jbec.lct.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import us.jbec.lct.io.PrimaryImageIO;
import us.jbec.lct.models.DocumentStatus;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.ImageJobFile;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.database.ArchivedJobData;
import us.jbec.lct.models.database.CloudCaptureDocument;
import us.jbec.lct.models.database.User;
import us.jbec.lct.repositories.ArchivedJobDataRepository;
import us.jbec.lct.repositories.CloudCaptureDocumentRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CloudCaptureDocumentService {

    private int MAX_ATTEMPTS = 3;

    private final CloudCaptureDocumentRepository cloudCaptureDocumentRepository;
    private final ArchivedJobDataRepository archivedJobDataRepository;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    public CloudCaptureDocumentService(CloudCaptureDocumentRepository cloudCaptureDocumentRepository, ArchivedJobDataRepository archivedJobDataRepository, ObjectMapper objectMapper, UserService userService) {
        this.cloudCaptureDocumentRepository = cloudCaptureDocumentRepository;
        this.archivedJobDataRepository = archivedJobDataRepository;
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    public String retrieveNewId() {
        for(int i = 0; i < MAX_ATTEMPTS; i++) {
            String uuid = UUID.randomUUID().toString();
            if (cloudCaptureDocumentRepository.findById(uuid).isEmpty()) {
                return uuid;
            }
        }
        throw new LCToolException("Could not generate ID!");
    }

    @Transactional
    public void saveCloudCaptureDocument(String userToken, MultipartFile imageJobAsFile, String id) throws IOException {
        ImageJob imageJob = objectMapper.readValue(imageJobAsFile.getBytes(), ImageJob.class);
        imageJob.setId(id);
        saveCloudCaptureDocument(userToken, imageJob);
    }

    @Transactional
    public void saveCloudCaptureDocument(String userToken, ImageJob imageJob) throws JsonProcessingException {
        var optionalDocument = cloudCaptureDocumentRepository.findById(imageJob.getId());
        if (optionalDocument.isPresent()) {
            var document = optionalDocument.get();
            if (!userOwnsDocument(userToken, document)) {
                throw new LCToolException("User not authorized to save job!");
            }
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

    public boolean userOwnsDocument(String userToken, CloudCaptureDocument document) {
        var optionalUser = userService.getUserByFirebaseIdentifier(userToken);
        return optionalUser.isPresent() && optionalUser.get().getFirebaseIdentifier().equals(document.getOwner().getFirebaseIdentifier());
    }

    public boolean userOwnsDocument(String userToken, String documentId) {
        var optionalUser = userService.getUserByFirebaseIdentifier(userToken);
        var optionalDocument = cloudCaptureDocumentRepository.findById(documentId);
        return optionalUser.isPresent() && optionalDocument.isPresent()
                && optionalUser.get().getFirebaseIdentifier().equals(optionalDocument.get().getOwner().getFirebaseIdentifier());
    }

    public void saveCloudCaptureDocument(CloudCaptureDocument cloudCaptureDocument) {
        cloudCaptureDocumentRepository.save(cloudCaptureDocument);
    }

    public Map<CloudCaptureDocument, ImageJob> getCloudCaptureDocumentsByUserIdentifier(String identifier) throws JsonProcessingException {
        var result = new HashMap<CloudCaptureDocument, ImageJob>();
        Optional<User> optionalUser = userService.getUserByFirebaseIdentifier(identifier);
        if (optionalUser.isPresent()) {
            for (var document : optionalUser.get().getCloudCaptureDocuments()) {
                result.put(document, objectMapper.readValue(document.getJobData(), ImageJob.class));
            }
            return result;
        } else {
            throw new LCToolException("Could not find user");
        }
    }

    public Map<CloudCaptureDocument, ImageJob> getActiveCloudCaptureDocuments() throws JsonProcessingException {
        var result = new HashMap<CloudCaptureDocument, ImageJob>();

        for (var document : cloudCaptureDocumentRepository.findAll()) {
            result.put(document, objectMapper.readValue(document.getJobData(), ImageJob.class));
        }

        return result;
    }

    public ImageJob getImageJobByUuid(String uuid) throws JsonProcessingException {
        var optionalDocument = cloudCaptureDocumentRepository.findById(uuid);
        if (optionalDocument.isPresent()) {
            return getImageJobFromDocument(optionalDocument.get());
        } else {
            throw new LCToolException("Could not find image job");
        }
    }

    public ImageJob getImageJobFromDocument(CloudCaptureDocument cloudCaptureDocument) throws JsonProcessingException {
        return objectMapper.readValue(cloudCaptureDocument.getJobData(), ImageJob.class);
    }

    public boolean markDocumentDeleted(String id) {
        var optionalDocument = cloudCaptureDocumentRepository.findById(id);
        if (optionalDocument.isPresent()) {
            var doc = optionalDocument.get();
            doc.setDocumentStatus(DocumentStatus.DELETED);
            cloudCaptureDocumentRepository.save(doc);
            return true;
        }
        return false;
    }

    public List<CloudCaptureDocument> getAllCloudCaptureDocuments() {
        List<CloudCaptureDocument> docs = new ArrayList<>();
        cloudCaptureDocumentRepository.findAll().forEach(docs::add);
        return docs;
    }
}
