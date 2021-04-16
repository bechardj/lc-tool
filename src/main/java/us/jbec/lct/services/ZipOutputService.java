package us.jbec.lct.services;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import us.jbec.lct.models.ZipOutputRecord;
import us.jbec.lct.repositories.ZipOutputRepository;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Profile("remote")
public class ZipOutputService {

    Logger LOG = LoggerFactory.getLogger(ZipOutputService.class);

    @Value("${image.zip.resource.path:#{null}}")
    private String zipResourcePath;

    @Value("${image.bulk.output.path}")
    private String bulkOutputPath;

    @Value("${lct.remote.source}")
    private String source;

    private final ZipOutputRepository zipOutputRepository;

    public ZipOutputService(ZipOutputRepository zipOutputRepository) {
        this.zipOutputRepository = zipOutputRepository;
    }

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

    @Transactional
    public void updateZipOutput() throws ZipException {
        if (zipResourcePath != null) {
            LOG.info("Updating Zip Output directory using {}...", bulkOutputPath);
            String fileName = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond() + ".zip";
            String zipOutputPath = zipResourcePath + File.separator + fileName;
            String zipUri = "/zipOutput/" + fileName;
            var zipOutputRecord = new ZipOutputRecord();
            zipOutputRecord.setFilePath(zipOutputPath);
            zipOutputRecord.setFileUri(zipUri);
            zipOutputRecord.setSource(source);
            new ZipFile(zipOutputPath).addFolder(new File(bulkOutputPath));
            zipOutputRepository.save(zipOutputRecord);
            LOG.info("Zip Output directory updated");
        }
    }

    @Transactional
    public void cleanupZipDirectory() {
        LOG.info("Cleaning up zip directory...");
        List<ZipOutputRecord> allZips = new ArrayList<>();
        zipOutputRepository.findAll().forEach(allZips::add);
        List<ZipOutputRecord> sortedZips = allZips.stream()
                .filter(record -> source.equals(record.getSource()))
                .sorted(Comparator.comparing(ZipOutputRecord::getCreateDate))
                .toList();
        for (int i = 0; i < sortedZips.size() - 2; i++) {
            var zipRecord = sortedZips.get(i);
            File zipFile = new File(zipRecord.getFilePath());
            var deleted = zipFile.delete();
            if (deleted) {
                zipOutputRepository.delete(zipRecord);
            } else {
                LOG.warn("Filed to delete zip {}", zipFile.getAbsolutePath());
            }
        }
        LOG.info("Cleaned up zip directory...");
    }
}
