package us.jbec.lct.io;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.jbec.lct.models.CropsDestination;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.LabeledImageCrop;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class ImageCropsIO {

    Logger LOG = LoggerFactory.getLogger(ImageCropsIO.class);

    @Value("${image.bulk.output.path}")
    private String bulkOutputPath;

    public void writeLabeledImageCrops(ImageJob job, List<LabeledImageCrop> labeledImageCrops,
                                       CropsDestination destination) throws IOException {
        int nameCounter = 0;
        if (labeledImageCrops != null && labeledImageCrops.size() > 0) {
            if (CropsDestination.BULK.equals(destination)){
                deleteBulkCropsByImageJob(job);
            } else {
                deleteCropsDirectoryBySourceFile(labeledImageCrops.get(0).getSource());
            }
            for (LabeledImageCrop labeledImageCrop : labeledImageCrops) {
                nameCounter++;
                String name = job.getId() + "_" + nameCounter + ".png";
                String label = labeledImageCrop.getLabel();

                File directory = buildOutputDirectory(labeledImageCrop, destination);
                if (directory == null) {
                    throw new RuntimeException("Unable to open output directory");
                }
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

    private File buildOutputDirectory(LabeledImageCrop labeledImageCrop, CropsDestination destination) {
        String label = labeledImageCrop.getLabel();
        if (CropsDestination.PAGE.equals(destination)) {
            File imageDirectory = labeledImageCrop.getSource().getParentFile();
            return new File(imageDirectory.getAbsolutePath()
                    + File.separator
                    + "crops"
                    + File.separator
                    + label
                    + File.separator);
        } else if (CropsDestination.BULK.equals(destination)) {
            return new File(bulkOutputPath
                    + File.separator
                    + label
                    + File.separator);
        }
        return null;
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

    private void deleteBulkCropsByImageJob(ImageJob imageJob) {
        LOG.info("Clearing bulk image crops for job id {}....", imageJob.getId());
        File bulkDirectory = new File(bulkOutputPath);
        File[] labeledDirectories = bulkDirectory.listFiles();
        if (labeledDirectories != null) {
            for (File labeledDirectory : labeledDirectories) {
                if (labeledDirectory.isDirectory()) {
                    File[] crops = labeledDirectory.listFiles();
                    for (File crop : crops) {
                        if (crop.isFile() && StringUtils.contains(crop.getName(), imageJob.getId())) {
                            crop.delete();
                        }
                    }
                }
            }
        } else {
            LOG.error("Could not open bulk directory");
        }
    }
}
