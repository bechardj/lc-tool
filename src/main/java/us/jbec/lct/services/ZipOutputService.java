package us.jbec.lct.services;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import us.jbec.lct.models.ZipType;
import us.jbec.lct.models.database.ZipOutputRecord;
import us.jbec.lct.repositories.ZipOutputRepository;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing the Zip archive output
 */
@Service
public class ZipOutputService {

    Logger LOG = LoggerFactory.getLogger(ZipOutputService.class);

    private final int ZIP_ARCHIVE_COUNT = 2;

    @Value("${lct.path.zip.output:#{null}}")
    private String zipResourcePath;

    @Value("${lct.path.image.bulk.output}")
    private String bulkOutputPath;

    @Value("${lct.remote.source}")
    private String source;

    private final ZipOutputRepository zipOutputRepository;

    /**
     * Service for managing the Zip archive output
     * @param zipOutputRepository autowired parameter
     */
    public ZipOutputService(ZipOutputRepository zipOutputRepository) {
        this.zipOutputRepository = zipOutputRepository;
    }

    /**
     * Get the URI of the most recently generated Zip archive
     * @return optionally return the URI of the latest Zip archive, provided one exists
     */
    public Optional<String> getLatestZipUri() {
        List<ZipOutputRecord> allZips = new ArrayList<>();
        zipOutputRepository.findAll().forEach(allZips::add);
        Optional<ZipOutputRecord> optionalMostRecentRecord = allZips.stream()
                .filter(record -> source.equals(record.getSource()))
                .max(Comparator.comparing(ZipOutputRecord::getCreateDate));
        if (optionalMostRecentRecord.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(optionalMostRecentRecord.get().getFileUri());
        }
    }

    /**
     * Generate a new Zip archive from the content of the bulk output directory,
     * and persist the output path to the database
     * @throws ZipException
     */
    @Transactional
    public void updateZipOutput() throws ZipException {
        if (zipResourcePath != null) {
            LOG.info("Updating Zip Output directory using {}...", bulkOutputPath);
            String fileName = UUID.randomUUID() + ".zip";
            String zipOutputPath = zipResourcePath + File.separator + fileName;
            String zipUri = "/zipOutput/" + fileName;
            var zipOutputRecord = new ZipOutputRecord();
            zipOutputRecord.setFilePath(zipOutputPath);
            zipOutputRecord.setFileUri(zipUri);
            zipOutputRecord.setSource(source);
            zipOutputRecord.setZipType(ZipType.BULK);
            new ZipFile(zipOutputPath).addFolder(new File(bulkOutputPath));
            zipOutputRepository.save(zipOutputRecord);
            LOG.info("Zip Output directory updated");
        }
    }

    /**
     * Cleanup the zip directory for this project source, keeping only the ZIP_ARCHIVE_COUNT
     * most recent archives
     */
    @Transactional
    public void cleanupZipDirectory() {
        LOG.info("Cleaning up zip directory...");
        List<ZipOutputRecord> allZips = new ArrayList<>();
        zipOutputRepository.findAll().forEach(allZips::add);
        List<ZipOutputRecord> sortedZips = allZips.stream()
                .filter(record -> source.equals(record.getSource()))
                .sorted(Comparator.comparing(ZipOutputRecord::getCreateDate))
                .toList();
        for (int i = 0; i < sortedZips.size() - ZIP_ARCHIVE_COUNT; i++) {
            var zipRecord = sortedZips.get(i);
            File zipFile = new File(zipRecord.getFilePath());
            try {
                var deleted = zipFile.delete();
                if (deleted) {
                    zipOutputRepository.delete(zipRecord);
                } else {
                    LOG.warn("Failed to delete zip {}", zipFile.getAbsolutePath());
                }
            } catch (Exception e) {
                LOG.warn("Failed to delete zip {}", zipFile.getAbsolutePath());
            }
        }
        LOG.info("Cleaned up zip directory...");
    }
}
