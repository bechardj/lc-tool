package us.jbec.lct.io;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.jbec.lct.models.CropsType;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.LabeledImageCrop;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * IO Class for writing image crops
 */
@Component
public class ImageCropsIO {

    Logger LOG = LoggerFactory.getLogger(ImageCropsIO.class);

    @Value("${lct.path.image.bulk.output}")
    private String bulkOutputPath;

    /**
     * Write labels to the autowired output directory
     * @param job image job to use for naming and metadata
     * @param labeledImageCrops image crops to write
     * @param cropsType crops type to generate
     * @throws IOException
     */
    public void writeLabeledImageCrops(ImageJob job, List<LabeledImageCrop> labeledImageCrops,
                                       CropsType cropsType) throws IOException {
        int nameCounter = 0;
        if (labeledImageCrops != null && labeledImageCrops.size() > 0) {
            deleteBulkCropsByImageJob(job, cropsType);
            for (LabeledImageCrop labeledImageCrop : labeledImageCrops) {
                nameCounter++;
                var name = job.getId() + "_" + nameCounter + ".png";

                var directory = buildOutputDirectory(labeledImageCrop, cropsType);
                if (directory == null) {
                    throw new LCToolException("Unable to open output directory");
                }
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                var fullPath = directory.getAbsolutePath() + File.separator + name;
                var outputFile = new File(fullPath);
                ImageIO.write(labeledImageCrop.getImage(), "png", outputFile);
            }
            LOG.debug("Wrote labeled image crops for {}.", job.getId());
        }
    }

    /**
     * Build output directory file path
     * @param labeledImageCrop image crop to write
     * @param destination destination type
     * @param cropsType crops type
     * @return file containing the directory to use (and to create if needed)
     */
    private File buildOutputDirectory(LabeledImageCrop labeledImageCrop, CropsType cropsType) {
        var label = labeledImageCrop.getLabel();
        return new File(bulkOutputPath
                + File.separator
                + cropsType.getDirectoryName()
                + File.separator
                + label
                + File.separator);

    }

    /**
     * Delete crops directory for single file output
     * @param source image job source file
     * @param cropsType crops type
     * @throws IOException
     */
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

    /**
     * Perform upgrade of subdirectory structure
     * Originally, before word/line crops were saved, letter crops were stored directly in the output directory
     * This cleans up those files.
     * @param subDirectory subdirectory to upgrade
     * @throws IOException
     */
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
        LOG.debug("Clearing bulk image crops for job id {}....", imageJob.getId());
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
            // TODO: probably can be removed now, whole dir gets cleared and triggers this error
            LOG.debug("Could not open bulk directory");
        }
    }
}
