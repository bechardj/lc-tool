package us.jbec.lct.upgrade.v2_0_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.VersionForUpgrade;
import us.jbec.lct.models.capture.DocumentCaptureData;
import us.jbec.lct.models.database.ArchivedJobData;
import us.jbec.lct.models.database.CloudCaptureDocument;
import us.jbec.lct.repositories.ArchivedJobDataRepository;
import us.jbec.lct.repositories.CloudCaptureDocumentRepository;
import us.jbec.lct.transformers.ImageJobTransformer;
import us.jbec.lct.upgrade.Upgrade;

import java.util.ArrayList;

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
        for (CloudCaptureDocument cloudCaptureDocument : cloudCaptureDocumentRepository.findAll()) {
            try {
                LOG.info("Start upgrade of {}", cloudCaptureDocument.getUuid());
                ImageJob imageJob = objectMapper.readValue(cloudCaptureDocument.getDocumentCaptureData(), ImageJob.class);

                if (cloudCaptureDocument.getArchivedJobDataList().isEmpty()) {
                    cloudCaptureDocument.setArchivedJobDataList(new ArrayList<>());
                }

                var archivedData = new ArchivedJobData();
                archivedData.setJobData(cloudCaptureDocument.getDocumentCaptureData());
                archivedData.setSourceDocumentUuid(cloudCaptureDocument);
                archivedData.setVersionForUpgrade(VersionForUpgrade.PRE_2_0_0);
                cloudCaptureDocument.getArchivedJobDataList().add(archivedData);
                archivedJobDataRepository.save(archivedData);

                LOG.info("Archive record saved for {}", cloudCaptureDocument.getUuid());

                DocumentCaptureData documentCaptureData = ImageJobTransformer.apply(imageJob);
                cloudCaptureDocument.setDocumentCaptureData(objectMapper.writeValueAsString(documentCaptureData));

                cloudCaptureDocumentRepository.save(cloudCaptureDocument);

                LOG.info("ImageJob converted to DocumentCaptureData for {}", cloudCaptureDocument.getUuid());
            } catch (JsonProcessingException e) {
                throw new LCToolException("Failed to parse Image Job");
            }
        }
    }
}
