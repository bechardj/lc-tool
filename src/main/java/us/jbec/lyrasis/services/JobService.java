package us.jbec.lyrasis.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import us.jbec.lyrasis.io.ImageCropsIO;
import us.jbec.lyrasis.io.PrimaryImageIO;
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

    public void processImageJob(ImageJob job) throws IOException {
        Optional <File> optionalImageJobFile = primaryImageIO.getImageJobFiles().stream()
                .filter(imageJobFile -> imageJobFile.getImageJob().getId().equals(job.getId()))
                .map(ImageJobFile::getImageFile)
                .findFirst();
        if (optionalImageJobFile.isPresent()) {
            LOG.info("Found job: {}", job.getId());
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
            job.getFields().put("timestamp", fmt.format(ZonedDateTime.now()));
            LOG.info("Saving JSON...");
            primaryImageIO.saveImageJobJson(job);
            BufferedImage originalImage = ImageIO.read(optionalImageJobFile.get());
            List<LabeledImageCrop> labeledImageCrops = new ArrayList<>();
            List<List<Double>> rectangles = job.getCharacterRectangles();
            List<String> labels = job.getCharacterLabels();
            for(int i = 0; i < job.getCharacterRectangles().size(); i++) {
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
                    LabeledImageCrop labeledImageCrop = new LabeledImageCrop(labels.get(i), optionalImageJobFile.get(), croppedImage);
                    labeledImageCrops.add(labeledImageCrop);
                } catch (Exception e) {
                    LOG.error("Something went wrong when cropping the image.", e);
                    LOG.error("Inspect the saved JSON for character rectangle at coordinate {}{}{}{} with label {}", x, y, w, h, labels.get(i));
                }
            }
            LOG.info("Writing all cropped and labeled images...");
            imageCropsIO.writeLabeledImageCrops(job, labeledImageCrops);
        } else {
            LOG.error("Image Job {} not found in output directory!", job.getId());
            throw new RuntimeException();
        }
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

    public List<ImageJobFile> getAllImageJobsSorted() {
        return primaryImageIO.getImageJobFiles().stream()
                .sorted((file1, file2) -> (int) (file1.getImageFile().getParentFile().lastModified()
                        - file2.getImageFile().getParentFile().lastModified()))
                .collect(Collectors.toList());
    }
}
