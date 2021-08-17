package us.jbec.lct.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import us.jbec.lct.io.ImageCropsIO;
import us.jbec.lct.io.PrimaryImageIO;
import us.jbec.lct.models.CropsDestination;
import us.jbec.lct.models.CropsType;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.ImageJobFile;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.LabeledImageCrop;
import us.jbec.lct.models.geometry.LabeledRectangle;
import us.jbec.lct.models.geometry.OffsetRectangle;
import us.jbec.lct.util.geometry.GeometricCollectionUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for processing image jobs, writing character, line, and word crops
 */
@Service
public class ImageJobProcessingService {

    @Value("${lct.path.image.bulk.output}")
    private String bulkOutputPath;

    @Value("${lct.remote.export.enabled}")
    private boolean exportEnabled;

    private final ImageCropsIO imageCropsIO;
    private final PrimaryImageIO primaryImageIO;
    private final GeometricCollectionUtils geometricCollectionUtils;
    private final CloudCaptureDocumentService cloudCaptureDocumentService;
    private final ZipOutputService zipOutputService;

    Logger LOG = LoggerFactory.getLogger(ImageJobProcessingService.class);


    private static final Set<String> ILLEGAL_CHARACTERS = new HashSet<>(Arrays.asList("/", "\n", "\r", "\t", "\0", "\f", "`", "?", "*", "\\", "<", ">", "|", "\"", ":"));

    /**
     * Service for processing image jobs, writing character, line, and word crops
     * @param imageCropsIO autowired parameter
     * @param primaryImageIO autowired parameter
     * @param geometricCollectionUtils autowired parameter
     * @param cloudCaptureDocumentService autowired parameter
     * @param zipOutputService autowired parameter
     */
    public ImageJobProcessingService(ImageCropsIO imageCropsIO, PrimaryImageIO primaryImageIO, GeometricCollectionUtils geometricCollectionUtils, CloudCaptureDocumentService cloudCaptureDocumentService, ZipOutputService zipOutputService) {
        this.imageCropsIO = imageCropsIO;
        this.primaryImageIO = primaryImageIO;
        this.geometricCollectionUtils = geometricCollectionUtils;
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
        this.zipOutputService = zipOutputService;
    }

    /**
     * Initialize a new image job with the provided UUID corresponding to the CloudCaptureDocument
     * @param uuid UUID of the corresponding CloudCaptureDocument
     * @return initialized ImageJob
     */
    public ImageJob initializeImageJob(String uuid) {
        ImageJob imageJob = new ImageJob();
        imageJob.setId(uuid);
        imageJob.setVersion("0.5");
        imageJob.setCompleted(false);
        imageJob.setEdited(false);
        imageJob.setStatus("Ingested");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
        imageJob.getFields().put("timestamp", fmt.format(ZonedDateTime.now()));
        return imageJob;
    }

    /**
     * Process all image jobs for all active CloudCaptureDocuments, generating character, line, and word crops
     * @param cropsDestination processed job output destination
     * @throws IOException
     */
    public void processAllImageJobCrops(CropsDestination cropsDestination) throws IOException {
        var imageJobFiles = buildImageJobFiles();
        for(var imageJobFile : imageJobFiles) {
            writeCharacterCrops(imageJobFile, cropsDestination);
            writeLineCrops(imageJobFile, cropsDestination, CropsType.LINES);
            writeLineCrops(imageJobFile, cropsDestination, CropsType.WORDS);
        }
    }


    /**
     * Generate and write character crops to disk for a given ImageJobFile
     * @param imageJobFile image job file to generate character crops for
     * @param destination processed job output destination
     * @throws IOException
     */
    private void writeCharacterCrops(ImageJobFile imageJobFile, CropsDestination destination) throws IOException {
        var job = imageJobFile.getImageJob();
        var imageFile = imageJobFile.getImageFile();

        BufferedImage originalImage = ImageIO.read(imageFile);
        List<LabeledImageCrop> labeledImageCrops = new ArrayList<>();
        var rectangles = job.getLabeledRectangles();
        for(var rectangle : rectangles) {
            int x = (int) rectangle.getX1();
            int y = (int) rectangle.getY1();
            int w = (int) rectangle.getWidth();
            int h = (int) rectangle.getHeight();
            try {
                var croppedImage = originalImage.getSubimage(x, y, w, h);
                var labeledImageCrop = new LabeledImageCrop(rectangle.getLabel(), imageFile, croppedImage);
                labeledImageCrops.add(labeledImageCrop);
            } catch (Exception e) {
                LOG.error("Something went wrong when cropping the image.", e);
                LOG.error("Inspect the saved JSON for character rectangle at coordinate {}{}{}{} with label {}", x, y, w, h, rectangle.getLabel());
            }
        }
        LOG.info("Writing all cropped and labeled images...");
        imageCropsIO.writeLabeledImageCrops(job, labeledImageCrops, destination, CropsType.LETTERS);
    }

    /**
     * Generate and write line crops to disk for a given ImageJobFile
     * @param imageJobFile image job file to generate line crops for
     * @param destination processed job output destination
     * @param cropsType crops type (line or word)
     * @throws IOException
     */
    private void writeLineCrops(ImageJobFile imageJobFile, CropsDestination destination, CropsType cropsType) throws IOException {

        if(cropsType == CropsType.LETTERS || cropsType == null) {
            throw new LCToolException("Invalid Crops Type for writing line crops");
        }

        ImageJob job = imageJobFile.getImageJob();
        File imageFile = imageJobFile.getImageFile();

        BufferedImage originalImage = ImageIO.read(imageFile);
        List<LabeledImageCrop> labeledImageCrops = new ArrayList<>();
        var rectangles = job.getLabeledRectangles();

        var lineSegments = cropsType.equals(CropsType.LINES) ? job.getLineLineSegments() : job.getWordLineSegments();

        var unGroupedRectangles = geometricCollectionUtils
                .groupLabeledRectanglesByLineSegment(rectangles, lineSegments);

        List<Set<LabeledRectangle>> groupedRectangles = geometricCollectionUtils
                .mergeLabeledRectangleSets(unGroupedRectangles);

        for (var rectangleGroup : groupedRectangles) {
            var upperLeft = geometricCollectionUtils.uppermostLeftPoint(rectangleGroup);
            var lowerRight = geometricCollectionUtils.lowermostRightPoint(rectangleGroup);
            int x = (int) upperLeft.getX();
            int y = (int) upperLeft.getY();
            int w = (int) (lowerRight.getX() - upperLeft.getX());
            int h = (int) (lowerRight.getY() - upperLeft.getY());
            try {
                String label;
                if (cropsType == CropsType.WORDS) {
                    label = rectangleGroup.stream()
                                .sorted(Comparator.comparing(OffsetRectangle::getX1))
                                .map(LabeledRectangle::getLabel)
                                .filter(mappedLabel -> !ILLEGAL_CHARACTERS.contains(mappedLabel))
                                .collect(Collectors.joining(""));
                    label = StringUtils.isEmpty(label) ? job.getId() : label;
                } else {
                    label = job.getId();
                }
                var croppedImage = originalImage.getSubimage(x, y, w, h);
                var labeledImageCrop = new LabeledImageCrop(label, imageFile, croppedImage);
                labeledImageCrops.add(labeledImageCrop);
            } catch (Exception e) {
                LOG.error("Something went wrong when cropping the image.", e);
            }
        }
        LOG.info("Writing all cropped and labeled images...");
        imageCropsIO.writeLabeledImageCrops(job, labeledImageCrops, destination, cropsType);
    }

    /**
     * Build ImageJobFile objects from active cloud capture documents
     * @return List of ImageJobFiles
     * @throws JsonProcessingException
     */
    private List<ImageJobFile> buildImageJobFiles() throws JsonProcessingException {
        var cloudDocs = cloudCaptureDocumentService.getActiveCloudCaptureDocuments();
        HashMap<String, File> images = new HashMap<>();
        primaryImageIO.getPersistedImages().forEach(image -> images.put(FilenameUtils.getBaseName(image.getName()), image));
        List<ImageJobFile> imageJobFiles = new ArrayList<>();
        for(var entry : cloudDocs.entrySet()) {
            var doc = entry.getKey();
            var image = images.get(doc.getUuid());
            if (image != null) {
                var imageFile = new ImageJobFile(image, entry.getValue());
                imageJobFiles.add(imageFile);
            }
        }
        return imageJobFiles;
    }

    /**
     * Method for performing scheduled bulk processing of image jobs
     * @throws IOException
     */
    // TODO: should be queue driven and ideally separated into a separate module that does this work as a batch job
    // the current approach works ok given the delay is sufficiently log, but limits scalability
    @Scheduled(fixedDelayString = "${lct.remote.export.frequency}")
    public void exportCurrentImageJobs() throws IOException {
        if (exportEnabled) {
            File outputDirectory = new File(bulkOutputPath);
            LOG.info("Clearing bulk output directory.");
            FileUtils.deleteDirectory(outputDirectory);
            var created = outputDirectory.mkdir();
            if (!created) {
                LOG.error("Could not create output directory");
            }
            processAllImageJobCrops(CropsDestination.BULK);
            zipOutputService.updateZipOutput();
            zipOutputService.cleanupZipDirectory();
        } else {
            LOG.info("Skipping export because it is disabled.");
        }
    }
}
