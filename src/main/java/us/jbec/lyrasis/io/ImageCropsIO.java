package us.jbec.lyrasis.io;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import us.jbec.lyrasis.controllers.JobController;
import us.jbec.lyrasis.models.ImageJob;
import us.jbec.lyrasis.models.LabeledImageCrop;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class ImageCropsIO {

    Logger LOG = LoggerFactory.getLogger(ImageCropsIO.class);

    public void writeLabeledImageCrops(ImageJob job, List<LabeledImageCrop> labeledImageCrops) throws IOException {
        int nameCounter = 0;
        if (labeledImageCrops != null && labeledImageCrops.size() > 0) {
            deleteCropsDirectoryBySourceFile(labeledImageCrops.get(0).getSource());
            for (LabeledImageCrop labeledImageCrop : labeledImageCrops) {
                nameCounter++;
                String name = nameCounter + ".png";
                String label = labeledImageCrop.getLabel();

                File imageDirectory = labeledImageCrop.getSource().getParentFile();
                File directory = new File(imageDirectory.getAbsolutePath()
                        + File.separator
                        + "crops"
                        + File.separator
                        + label
                        + File.separator);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                String fullPath = directory.getAbsolutePath() + File.separator + name;
                File outputFile = new File(fullPath);
                ImageIO.write(labeledImageCrop.getImage(), "png", outputFile);
            }
            LOG.info("Wrote labeled image crops.");
        }
    }

    private void deleteCropsDirectoryBySourceFile(File source) throws IOException {
        LOG.info("Clearing existing crops directory....");
        File cropsDirectory = new File(source.getParentFile().getAbsolutePath()
                + File.separator +
                "crops");
        FileUtils.deleteDirectory(cropsDirectory);
        LOG.info("Cleared!");
        if (!cropsDirectory.exists()) {
            cropsDirectory.mkdirs();
            LOG.info("Created new crop directory.");
        }
    }
}
