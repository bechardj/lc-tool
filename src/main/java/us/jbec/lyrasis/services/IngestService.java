package us.jbec.lyrasis.services;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import us.jbec.lyrasis.io.PrimaryImageIO;
import us.jbec.lyrasis.models.ImageJobFile;

import java.io.File;
import java.util.List;

@Component
public class IngestService {

    Logger LOG = LoggerFactory.getLogger(IngestService.class);

    private final PrimaryImageIO primaryImageIO;

    public IngestService(PrimaryImageIO primaryImageIO){
        this.primaryImageIO = primaryImageIO;
    }

    public void ingest() {
        LOG.info("Beginning ingest process.");
        List<File> imageFiles = primaryImageIO.getFilesFromIngestDirectory();
        if (imageFiles != null) {
            LOG.info("Successfully opened ingest directory");
            File outputDirectory = primaryImageIO.getOutputDirectory();
            for(File imageFile : imageFiles) {
                String path = outputDirectory.getAbsolutePath()
                        + File.separator
                        + FilenameUtils.removeExtension(imageFile.getName());
                boolean directoryCreated = primaryImageIO.createDirectory(path);
                if (directoryCreated) {
                    primaryImageIO.initializeDirectory(imageFile, path);
                    LOG.info("Created directory for image: {}", path);
                } else {
                    LOG.error("Failed to create output directory for image {}", imageFile.getName());
                }
            }
            LOG.info("Ingest complete.");
        } else {
            LOG.error("Failed to open ingest directory.");
        }
    }
}
