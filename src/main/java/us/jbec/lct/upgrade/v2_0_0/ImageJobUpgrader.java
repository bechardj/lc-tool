package us.jbec.lct.upgrade.v2_0_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.VersionForUpgrade;
import us.jbec.lct.models.capture.DocumentCaptureData;
import us.jbec.lct.models.database.ArchivedJobData;
import us.jbec.lct.repositories.ArchivedJobDataRepository;
import us.jbec.lct.repositories.CloudCaptureDocumentRepository;
import us.jbec.lct.transformers.ImageJobTransformer;
import us.jbec.lct.upgrade.Upgrade;

import java.util.HashMap;
import java.util.Map;

@Component
public class ImageJobUpgrader implements Upgrade {

    private static final Logger LOG = LoggerFactory.getLogger(ImageJobUpgrader.class);

    private final CloudCaptureDocumentRepository cloudCaptureDocumentRepository;
    private final ArchivedJobDataRepository archivedJobDataRepository;
    private final ObjectMapper objectMapper;

    public ImageJobUpgrader(CloudCaptureDocumentRepository cloudCaptureDocumentRepository,
                            ArchivedJobDataRepository archivedJobDataRepository, ObjectMapper objectMapper) {
        this.cloudCaptureDocumentRepository = cloudCaptureDocumentRepository;
        this.archivedJobDataRepository = archivedJobDataRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean optional() {
        return false;
    }

    @Override
    @Transactional
    public void execute() throws RuntimeException {
        LOG.info("Start upgrade of ImageJob data from pre-2.0.0");
        Map<String, String> archiveDataMap = new HashMap<>();
        for (String docUuid : cloudCaptureDocumentRepository.selectAllDocumentUuids()) {
            try {
                LOG.info("Start upgrade of {}", docUuid);
                String rawCaptureData = cloudCaptureDocumentRepository.selectRawDocumentCaptureData(docUuid);
                archiveDataMap.put(docUuid, rawCaptureData);
                ImageJob imageJob = objectMapper.readValue(rawCaptureData, ImageJob.class);
                imageJob.setId(docUuid);
                DocumentCaptureData documentCaptureData = ImageJobTransformer.apply(imageJob);
                cloudCaptureDocumentRepository.updateRawDocumentCaptureData(docUuid, objectMapper.writeValueAsString(documentCaptureData));
                LOG.info("ImageJob converted to DocumentCaptureData for {}", docUuid);
            } catch (JsonProcessingException e) {
                throw new LCToolException("Failed to parse Image Job");
            }
        }
        LOG.info("Completed initial transformation of all ImageJob data.");
        // After doing low-level conversion, perform other upgrades
        for (var cloudCaptureDocument : cloudCaptureDocumentRepository.findAll()) {
            var imageData = archiveDataMap.get(cloudCaptureDocument.getUuid());
            if (imageData != null) {
                var archive = new ArchivedJobData();
                archive.setJobData(imageData);
                archive.setSourceDocumentUuid(cloudCaptureDocument);
                archive.setVersionForUpgrade(VersionForUpgrade.PRE_2_0_0);
                cloudCaptureDocument.getArchivedJobDataList().add(archive);
                archivedJobDataRepository.save(archive);
                LOG.info("Archived original information for job {}", cloudCaptureDocument.getUuid());
            } else {
                LOG.error("Encountered document with no archive data for job {}", cloudCaptureDocument.getUuid());
                throw new LCToolException("Encountered document with no archive data");
            }

            String notes = cloudCaptureDocument.getDocumentCaptureData().getNotes();
            if (StringUtils.length(notes) > 50) {
                notes = StringUtils.left(notes,50) + "...";
            } else if (notes == null) {
                notes = "";
            }
            cloudCaptureDocument.setNotesPreview(notes);
            LOG.info("Completed upgrade post-processing for document {}", cloudCaptureDocument.getUuid());
        }
    }
}
