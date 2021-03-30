package us.jbec.lct.services;

import org.locationtech.jts.geom.LineSegment;
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
import us.jbec.lct.models.geometry.Point;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JobService {


    private final ImageCropsIO imageCropsIO;
    private final PrimaryImageIO primaryImageIO;

    Logger LOG = LoggerFactory.getLogger(JobService.class);


    public JobService(ImageCropsIO imageCropsIO, PrimaryImageIO primaryImageIO) {
        this.imageCropsIO = imageCropsIO;
        this.primaryImageIO = primaryImageIO;
    }

    public void processAllImageJobCrops(CropsDestination cropsDestination) throws IOException {
        List<ImageJobFile> imageJobFiles = primaryImageIO.getImageJobFiles();
        for(ImageJobFile imageJobFile : imageJobFiles) {
            writeCharacterCrops(imageJobFile, cropsDestination);
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
        List<List<Double>> rectangles = job.getCharacterRectangles();
        List<List<Double>> lines = cropsType.equals(CropsType.LINES) ? job.getLineLines() : job.getWordLines();

        if (lines != null && rectangles != null) {
            for (List<Double> line : lines) {
                List<List<Double>> containedRectangles = rectangles.stream()
                        .filter(rectangle -> lineInterceptsRectangle(line, rectangle))
                        .collect(Collectors.toList());
                Point upperLeft = upperLeftPoint(containedRectangles);
                Point lowerRight = lowerRightPoint(containedRectangles);
                int x = (int) upperLeft.getX();
                int y = (int) upperLeft.getY();
                int w = (int) (lowerRight.getX() - upperLeft.getX());
                int h = (int) (lowerRight.getY() - upperLeft.getY());
                try {
                    BufferedImage croppedImage = originalImage.getSubimage(x, y, w, h);
                    LabeledImageCrop labeledImageCrop = new LabeledImageCrop(job.getId(), imageFile, croppedImage);
                    labeledImageCrops.add(labeledImageCrop);
                } catch (Exception e) {
                    LOG.error("Something went wrong when cropping the image.", e);
//                    LOG.error("Inspect the saved JSON for character rectangle at coordinate {}{}{}{} with label {}", x, y, w, h, labels.get(i));
                }
            }
        }
        LOG.info("Writing all cropped and labeled images...");
        imageCropsIO.writeLabeledImageCrops(job, labeledImageCrops, destination, cropsType);
    }

    public boolean lineInterceptsRectangle(List<Double> linePoints, List<Double> rectangle) {
        LineSegment line = new LineSegment(linePoints.get(0), linePoints.get(1), linePoints.get(2), linePoints.get(3));


        double leftX = rectangle.get(0);
        double rightX = rectangle.get(0) + rectangle.get(2);
        double upperY = rectangle.get(1);
        double lowerY = rectangle.get(1) + rectangle.get(3);

        LineSegment leftRectEdge = new LineSegment(leftX, upperY, leftX, lowerY);
        LineSegment rightRectEdge = new LineSegment(rightX, upperY, rightX, lowerY);

        return leftRectEdge.intersection(line) != null || rightRectEdge.intersection(line) != null;

    }

    public Point upperLeftPoint(List<List<Double>> rectangles) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;

        for (List<Double> rectangle : rectangles) {
            if (rectangle.get(0) < minX) {
                minX = rectangle.get(0);
            }
            if (rectangle.get(1) < minY) {
                minY = rectangle.get(1);
            }
        }
        return new Point(minX, minY);
    }

    public Point lowerRightPoint(List<List<Double>> rectangles) {
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (List<Double> rectangle : rectangles) {
            if (rectangle.get(0) + rectangle.get(2) > maxX) {
                maxX = rectangle.get(0) + rectangle.get(2);
            }
            if (rectangle.get(1) + rectangle.get(3) > maxY) {
                maxY = rectangle.get(1) + rectangle.get(3);
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
