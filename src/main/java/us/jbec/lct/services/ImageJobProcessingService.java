package us.jbec.lct.services;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import us.jbec.lct.io.ImageCropsIO;
import us.jbec.lct.io.PrimaryImageIO;
import us.jbec.lct.models.CropsType;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.ImageJobFile;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.LabeledImageCrop;
import us.jbec.lct.models.geometry.LabeledRectangle;
import us.jbec.lct.models.geometry.OffsetRectangle;
import us.jbec.lct.transformers.DocumentCaptureDataTransformer;
import us.jbec.lct.util.geometry.GeometricCollectionUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
     * Process all image jobs for all active CloudCaptureDocuments, generating character, line, and word crops
     * @throws IOException
     */
    public void processAllImageJobCrops() throws IOException {
        var uuids = cloudCaptureDocumentService.getActiveCloudCaptureDocumentUuids();

        for(var uuid : uuids) {
            try {
                ImageJobFile imageJobFile = buildImageJobFile(uuid);
                writeCharacterCrops(imageJobFile);
                writeLineCrops(imageJobFile, CropsType.LINES);
                writeLineCrops(imageJobFile, CropsType.WORDS);
            } catch (Exception e) {
                LOG.error("Error occurred while processing job: {} - Will attempt to continue with no cleanup", uuid);
            }
        }
    }


    /**
     * Generate and write character crops to disk for a given ImageJobFile
     * @param imageJobFile image job file to generate character crops for
     * @throws IOException
     */
    private void writeCharacterCrops(ImageJobFile imageJobFile) throws IOException {
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
        LOG.debug("Writing all cropped and labeled character images for job {}...", job.getId());
        imageCropsIO.writeLabeledImageCrops(job, labeledImageCrops, CropsType.LETTERS);
    }

    /**
     * Generate and write line crops to disk for a given ImageJobFile
     * @param imageJobFile image job file to generate line crops for
     * @param cropsType crops type (line or word)
     * @throws IOException
     */
    private void writeLineCrops(ImageJobFile imageJobFile, CropsType cropsType) throws IOException {

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
        LOG.debug("Writing all cropped and labeled line/word images for job {}...", job.getId());
        imageCropsIO.writeLabeledImageCrops(job, labeledImageCrops, cropsType);
    }

    /**
     * Build ImageJobFile objects from active cloud capture documents
     * @return List of ImageJobFiles
     */
    private ImageJobFile buildImageJobFile(String uuid) {
        var captureData = cloudCaptureDocumentService.getDocumentCaptureDataByUuid(uuid);
        ImageJob imageJob = DocumentCaptureDataTransformer.apply(captureData);
        Optional<File> optionalFile = primaryImageIO.getImageByUuid(uuid);
        if (optionalFile.isEmpty()) {
            throw new LCToolException("Image file missing!");
        }
        return new ImageJobFile(optionalFile.get(), imageJob);
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
            LOG.info("Starting ZIP Generation, clearing bulk output directory & re-generating.");
            FileUtils.deleteDirectory(outputDirectory);
            var created = outputDirectory.mkdir();
            if (!created) {
                LOG.error("Could not create output directory");
            }
            processAllImageJobCrops();
            zipOutputService.updateZipOutput();
            zipOutputService.cleanupZipDirectory();
        } else {
            LOG.info("Skipping export because it is disabled.");
        }
    }
}
