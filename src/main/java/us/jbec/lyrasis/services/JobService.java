package us.jbec.lyrasis.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import us.jbec.lyrasis.io.ImageCropsIO;
import us.jbec.lyrasis.io.PrimaryImageIO;
import us.jbec.lyrasis.models.CropsDestination;
import us.jbec.lyrasis.models.ImageJob;
import us.jbec.lyrasis.models.ImageJobFile;
import us.jbec.lyrasis.models.LabeledImageCrop;

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
            writeCrops(imageJobFile, cropsDestination);
        }
    }


    public void processImageJob(ImageJob job, CropsDestination cropsDestination) throws IOException {
        Optional <ImageJobFile> optionalImageJobFile = primaryImageIO.getImageJobFiles().stream()
                .filter(imageJobFile -> imageJobFile.getImageJob().getId().equals(job.getId()))
                .findFirst();
        if (optionalImageJobFile.isPresent()) {
            processImageFile(optionalImageJobFile.get(), cropsDestination);
        } else {
            LOG.error("Image Job {} not found in output directory!", job.getId());
            throw new RuntimeException();
        }
    }

    public void processImageFile(ImageJobFile imageJobFile, CropsDestination cropsDestination) throws IOException {
        ImageJob job = imageJobFile.getImageJob();
        LOG.info("Found job: {}", job.getId());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
        job.getFields().put("timestamp", fmt.format(ZonedDateTime.now()));
        job.setVersion("0.3");
        if (CropsDestination.PAGE.equals(cropsDestination)) {
            LOG.info("Saving JSON...");
            primaryImageIO.saveImageJobJson(job);
        }
        writeCrops(imageJobFile, cropsDestination);
    }

    private void writeCrops(ImageJobFile imageJobFile, CropsDestination destination) throws IOException {
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
        imageCropsIO.writeLabeledImageCrops(job, labeledImageCrops, destination);
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
