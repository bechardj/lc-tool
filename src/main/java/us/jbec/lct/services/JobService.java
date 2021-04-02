package us.jbec.lct.services;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import us.jbec.lct.io.ImageCropsIO;
import us.jbec.lct.io.PrimaryImageIO;
import us.jbec.lct.models.CropsDestination;
import us.jbec.lct.models.CropsType;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.ImageJobFile;
import us.jbec.lct.models.LabeledImageCrop;
import us.jbec.lct.models.geometry.LineSegment;
import us.jbec.lct.models.geometry.OffsetRectangle;
import us.jbec.lct.models.geometry.Point;

import javax.imageio.ImageIO;
import java.awt.*;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JobService {


    private final ImageCropsIO imageCropsIO;
    private final PrimaryImageIO primaryImageIO;

    Logger LOG = LoggerFactory.getLogger(JobService.class);

    private static final Set ILLEGAL_CHARACTERS = new HashSet<>(Arrays.asList("/", "\n", "\r", "\t", "\0", "\f", "`", "?", "*", "\\", "<", ">", "|", "\"", ":"));


    public JobService(ImageCropsIO imageCropsIO, PrimaryImageIO primaryImageIO) {
        this.imageCropsIO = imageCropsIO;
        this.primaryImageIO = primaryImageIO;
    }

    public void processAllImageJobCrops(CropsDestination cropsDestination) throws IOException {
        List<ImageJobFile> imageJobFiles = primaryImageIO.getImageJobFiles();
        for(ImageJobFile imageJobFile : imageJobFiles) {
            writeCharacterCrops(imageJobFile, cropsDestination);
            writeLineCrops(imageJobFile, cropsDestination, CropsType.LINES);
            writeLineCrops(imageJobFile, cropsDestination, CropsType.WORDS);
        }
    }


    public void processImageJobWithFile(ImageJob job, CropsDestination cropsDestination) throws IOException {
        Optional <ImageJobFile> optionalImageJobFile = primaryImageIO.getImageJobFiles().stream()
                .filter(imageJobFile -> imageJobFile.getImageJob().getId().equals(job.getId()))
                .findFirst();
        if (optionalImageJobFile.isPresent()) {
            LOG.info("Found job: {}", job.getId());
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
            job.getFields().put("timestamp", fmt.format(ZonedDateTime.now()));
            job.setVersion("0.3");
            if (CropsDestination.PAGE.equals(cropsDestination)) {
                LOG.info("Saving JSON...");
                primaryImageIO.saveImageJobJson(job);
            }
            ImageJobFile jobFileToProcess = new ImageJobFile(optionalImageJobFile.get().getImageFile(),
                    job);
            writeCharacterCrops(jobFileToProcess, cropsDestination);
            writeLineCrops(jobFileToProcess, cropsDestination, CropsType.LINES);
            writeLineCrops(jobFileToProcess, cropsDestination, CropsType.WORDS);
        } else {
            LOG.error("Image Job {} not found in output directory!", job.getId());
            throw new RuntimeException();
        }
    }

    private void writeCharacterCrops(ImageJobFile imageJobFile, CropsDestination destination) throws IOException {
        ImageJob job = imageJobFile.getImageJob();
        File imageFile = imageJobFile.getImageFile();

        BufferedImage originalImage = ImageIO.read(imageFile);
        List<LabeledImageCrop> labeledImageCrops = new ArrayList<>();
        List<List<Double>> rectangles = job.getCharacterRectangles();
        List<String> labels = job.getCharacterLabels();
        int cropCount = job.getCharacterRectangles() == null ? 0 : job.getCharacterRectangles().size();
        for(int i = 0; i < cropCount; i++) {
            int x = rectangles.get(i).get(0).intValue();
            int y = rectangles.get(i).get(1).intValue();
            int w = rectangles.get(i).get(2).intValue();
            int h = rectangles.get(i).get(3).intValue();
            if (w < 0) {
                x = x + w;
                w *= -1;
            }
            if (h < 0) {
                y = y + h;
                h *= -1;
            }
            try {
                BufferedImage croppedImage = originalImage.getSubimage(x, y, w, h);
                LabeledImageCrop labeledImageCrop = new LabeledImageCrop(labels.get(i), imageFile, croppedImage);
                labeledImageCrops.add(labeledImageCrop);
            } catch (Exception e) {
                LOG.error("Something went wrong when cropping the image.", e);
                LOG.error("Inspect the saved JSON for character rectangle at coordinate {}{}{}{} with label {}", x, y, w, h, labels.get(i));
            }
        }
        LOG.info("Writing all cropped and labeled images...");
        imageCropsIO.writeLabeledImageCrops(job, labeledImageCrops, destination, CropsType.LETTERS);
    }

    private void writeLineCrops(ImageJobFile imageJobFile, CropsDestination destination, CropsType cropsType) throws IOException {
        ImageJob job = imageJobFile.getImageJob();
        File imageFile = imageJobFile.getImageFile();

        BufferedImage originalImage = ImageIO.read(imageFile);
        List<LabeledImageCrop> labeledImageCrops = new ArrayList<>();
        List<OffsetRectangle> rectangles = new ArrayList<>();
        Map<OffsetRectangle, String> mappedLabels = new HashMap<>();
        if (job.getCharacterRectangles() != null){
            for (int i = 0; i < job.getCharacterRectangles().size(); i++) {
                OffsetRectangle offsetRectangle = new OffsetRectangle(job.getCharacterRectangles().get(i));
                rectangles.add(offsetRectangle);
                mappedLabels.put(offsetRectangle, job.getCharacterLabels().get(i));
            }
        }

        List<List<Double>> lines = cropsType.equals(CropsType.LINES) ? job.getLineLines() : job.getWordLines();

        List<LineSegment> lineSegments = lines == null ? new ArrayList<>() :
                lines.stream().map(LineSegment::new).collect(Collectors.toList());

        List<Set<OffsetRectangle>> unGroupedRectangles = new ArrayList<>();
        for (LineSegment lineSegment : lineSegments) {
            Set<OffsetRectangle> thisGroupsRectangles = rectangles.stream()
                    .filter(lineSegment::interceptsRectangle)
                    .collect(Collectors.toSet());
            unGroupedRectangles.add(thisGroupsRectangles);
        }
        List<Set<OffsetRectangle>> groupedRectangles = new ArrayList<>();

        for (Set<OffsetRectangle> offsetRectangleSet : unGroupedRectangles) {
            boolean grouped = false;
            for (OffsetRectangle offsetRectangle : offsetRectangleSet) {
                Optional<Set<OffsetRectangle>> matchingSet = groupedRectangles.stream().filter(set -> set.contains(offsetRectangle)).findFirst();
                if (matchingSet.isPresent()) {
                    grouped = true;
                    matchingSet.get().addAll(offsetRectangleSet);
                    break;
                }
            }
            if (!grouped) {
                groupedRectangles.add(offsetRectangleSet);
            }
        }

        for (Set<OffsetRectangle> rectangleGroup : groupedRectangles.stream().filter(set -> set.size() != 0).collect(Collectors.toList())) {
            Point upperLeft = upperLeftPoint(rectangleGroup);
            Point lowerRight = lowerRightPoint(rectangleGroup);
            int x = (int) upperLeft.getX();
            int y = (int) upperLeft.getY();
            int w = (int) (lowerRight.getX() - upperLeft.getX());
            int h = (int) (lowerRight.getY() - upperLeft.getY());
            try {
                String label;
                if (cropsType == CropsType.WORDS) {
                    label = rectangleGroup.stream()
                                .sorted(Comparator.comparing(OffsetRectangle::getX1))
                                .map(mappedLabels::get)
                                .filter(mappedLabel -> !ILLEGAL_CHARACTERS.contains(mappedLabel))
                                .collect(Collectors.joining(""));
                    label = StringUtils.isEmpty(label) ? job.getId() : label;
                } else {
                    label = job.getId();
                }
                BufferedImage croppedImage = originalImage.getSubimage(x, y, w, h);
                LabeledImageCrop labeledImageCrop = new LabeledImageCrop(label, imageFile, croppedImage);
                labeledImageCrops.add(labeledImageCrop);
            } catch (Exception e) {
                LOG.error("Something went wrong when cropping the image.", e);
//                    LOG.error("Inspect the saved JSON for character rectangle at coordinate {}{}{}{} with label {}", x, y, w, h, labels.get(i));
            }
        }
        LOG.info("Writing all cropped and labeled images...");
        imageCropsIO.writeLabeledImageCrops(job, labeledImageCrops, destination, cropsType);
    }

    public boolean lineInterceptsRectangle(LineSegment lineSegment, OffsetRectangle rectangle) {

        return lineSegment.intersection(rectangle.getLeftEdge()) != null
                || lineSegment.intersection(rectangle.getRightEdge()) != null;

    }

    public Point upperLeftPoint(Set<OffsetRectangle> rectangles) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;

        for (OffsetRectangle rectangle : rectangles) {
            if (rectangle.getX1() < minX) {
                minX = rectangle.getX1();
            }
            if (rectangle.getY1() < minY) {
                minY = rectangle.getY1();
            }
        }
        return new Point(minX, minY);
    }

    public Point lowerRightPoint(Set<OffsetRectangle> rectangles) {
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (OffsetRectangle rectangle : rectangles) {
            if (rectangle.getX2() > maxX) {
                maxX = rectangle.getX2();
            }
            if (rectangle.getY2() > maxY) {
                maxY = rectangle.getY2();
            }
        }
        return new Point(maxX, maxY);
    }

    public ImageJob getImageJob(String id){
        List<ImageJobFile> imageJobFiles = primaryImageIO.getImageJobFiles();
        for (ImageJobFile imageJobFile : imageJobFiles) {
            if (id.equals(imageJobFile.getImageFileName())){
                return imageJobFile.getImageJob();
            }
        }
        LOG.error("Image job with id: {} not found in output directory", id);
        return null;
    }

    public List<ImageJobFile> getAllImageJobFilesSorted() {
        return primaryImageIO.getImageJobFiles().stream()
                .sorted(Comparator.comparing(imageJobFile -> imageJobFile.getImageJob().getId()))
                .collect(Collectors.toList());
    }
}
