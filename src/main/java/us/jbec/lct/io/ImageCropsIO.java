package us.jbec.lct.io;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.jbec.lct.models.CropsDestination;
import us.jbec.lct.models.CropsType;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.LabeledImageCrop;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ImageCropsIO {

    Logger LOG = LoggerFactory.getLogger(ImageCropsIO.class);

    @Value("${image.bulk.output.path}")
    private String bulkOutputPath;

    public void writeLabeledImageCrops(ImageJob job, List<LabeledImageCrop> labeledImageCrops,
                                       CropsDestination destination, CropsType cropsType) throws IOException {
        int nameCounter = 0;
        if (labeledImageCrops != null && labeledImageCrops.size() > 0) {
            if (CropsDestination.BULK.equals(destination)){
                deleteBulkCropsByImageJob(job, cropsType);
            } else {
                deleteCropsDirectoryBySourceFile(labeledImageCrops.get(0).getSource(), cropsType);
            }
            for (LabeledImageCrop labeledImageCrop : labeledImageCrops) {
                nameCounter++;
                var name = job.getId() + "_" + nameCounter + ".png";

                var directory = buildOutputDirectory(labeledImageCrop, destination, cropsType);
                if (directory == null) {
                    throw new RuntimeException("Unable to open output directory");
                }
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                var fullPath = directory.getAbsolutePath() + File.separator + name;
                var outputFile = new File(fullPath);
                ImageIO.write(labeledImageCrop.getImage(), "png", outputFile);
            }
            LOG.info("Wrote labeled image crops.");
        }
    }

    private File buildOutputDirectory(LabeledImageCrop labeledImageCrop, CropsDestination destination, CropsType cropsType) {
        var label = labeledImageCrop.getLabel();
        if (CropsDestination.PAGE.equals(destination)) {
            var imageDirectory = labeledImageCrop.getSource().getParentFile();
            return new File(imageDirectory.getAbsolutePath()
                    + File.separator
                    + "crops"
                    + File.separator
                    + cropsType.getDirectoryName()
                    + File.separator
                    + label
                    + File.separator);
        } else if (CropsDestination.BULK.equals(destination)) {
            return new File(bulkOutputPath
                    + File.separator
                    + cropsType.getDirectoryName()
                    +File.separator
                    + label
                    + File.separator);
        }
        return null;
    }

    private void deleteCropsDirectoryBySourceFile(File source, CropsType cropsType) throws IOException {
        LOG.info("Clearing existing crops directory....");
        var cropsDirectory = new File(source.getParentFile().getAbsolutePath()
                + File.separator +
                "crops" +
                File.separator +
                cropsType.getDirectoryName() +
                File.separator);
        performCropsDirectoryUpgrade(cropsDirectory);
        FileUtils.deleteDirectory(cropsDirectory);
        LOG.info("Cleared!");
        if (!cropsDirectory.exists()) {
            cropsDirectory.mkdirs();
            LOG.info("Created new crop directory.");
        }
    }

    private void performCropsDirectoryUpgrade(File subDirectory) throws IOException {
        var parentDirectory = subDirectory.getParentFile();
        if (parentDirectory.exists()) {
            List<String> dirNames = Arrays.stream(CropsType.values())
                    .map(CropsType::getDirectoryName)
                    .collect(Collectors.toList());
            for(File file : parentDirectory.listFiles()) {
                if (file.isDirectory() && dirNames.contains(file.getName())) {
                    return;
                }
            }
            FileUtils.deleteDirectory(parentDirectory);
        }
        parentDirectory.mkdirs();
    }

    private void deleteBulkCropsByImageJob(ImageJob imageJob, CropsType cropsType) throws IOException {
        LOG.info("Clearing bulk image crops for job id {}....", imageJob.getId());
        var bulkDirectory = new File(bulkOutputPath + File.separator
                + cropsType.getDirectoryName() + File.separator);
        performCropsDirectoryUpgrade(bulkDirectory);
        var labeledDirectories = bulkDirectory.listFiles();
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
