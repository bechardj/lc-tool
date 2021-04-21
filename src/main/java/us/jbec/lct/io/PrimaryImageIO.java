package us.jbec.lct.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.ImageJobFile;
import us.jbec.lct.transformers.ImageJobFieldTransformer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class PrimaryImageIO {

    @Value("${image.ingest.path}")
    private String ingestPath;

    @Value("${image.persistence.path}")
    private String imagePersistencePath;

    @Value("${image.output.path}")
    private String outputPath;

    @Value("${image.output.json.name}")
    private String jsonName;

    Logger LOG = LoggerFactory.getLogger(PrimaryImageIO.class);

    private final ObjectMapper objectMapper;

    public static final Set<String> extensions = Set.of("jpeg", "jpg", "png");

    /**
     * Data Access Object for images
     * Handles FileIO
     *
     * @param objectMapper The JSON Object Mapper
     */
    public PrimaryImageIO(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    /**
     * Persist an uploaded file to the file system
     * @param file file to persist
     * @return String containing the absolute path to the file
     * @throws IOException
     */
    public String persistImage(MultipartFile file, String uuid) throws IOException {
        var extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!extensions.contains(extension)) {
            throw new RuntimeException("Unsupported file type");
        }
        String path = new File(imagePersistencePath).getAbsolutePath();
        File target = new File(path + File.separator + uuid + "." + extension);
        file.transferTo(target);
        return target.getAbsolutePath();
    }

    public Optional<File> getImageByUuid(String uuid) {
        var images = getPersistedImages();
        for (var image : images) {
            var name = FilenameUtils.getBaseName(image.getName());
            if (name.equals(uuid)) {
                return Optional.of(image);
            }
        }
        return Optional.empty();
    }


    /**
     * Read images from the persistence directory
     * @return List of images from the persistence directory
     */
    public List<File> getPersistedImages(){
        var  imageJobFiles = new ArrayList<File>();
        var directory = new File(imagePersistencePath);
        File[] images = directory.listFiles();
        if (images != null){
            for (File image : images) {
                if (extensions.contains(FilenameUtils.getExtension(image.getName()))) {
                    imageJobFiles.add(image);
                }
            }
        } else {
            LOG.error("Could not open image persistence directory.");
        }
        LOG.info("Successfully read images in.");
        return imageJobFiles;
    }

    /**
     * Read image job files from the output directory
     * @return List of image job files from the output directory
     */
    public List<ImageJobFile> getImageJobFiles(){
        List<ImageJobFile> imageJobFiles = new ArrayList<>();
        File directory = new File(outputPath);
        File[] outputFolders = directory.listFiles();
        if (outputFolders != null){
            for (File outputFolder : outputFolders) {
                if (outputFolder.isDirectory()){
                    File imageFile = getImageFileFromDirectory(outputFolder);
                    ImageJob imageJob = readJsonFileFromDirectory(outputFolder);
                    if (imageFile == null) {
                        LOG.error("Could not find image file in directory {}", outputFolder.getAbsolutePath());
                        continue;
                    }
                    if (imageJob == null) {
                        LOG.error("Could not read json file in directory {}", outputFolder.getAbsolutePath());
                        continue;
                    }
                    ImageJobFieldTransformer.transform(imageJob);
                    imageJobFiles.add(new ImageJobFile(imageFile, imageJob));
                }
            }
        } else {
            LOG.error("Could not open output directory.");
        }
        LOG.info("Successfully read image job files in.");
        return imageJobFiles;
    }

    /**
     * Read image file from output directory
     * @param directory
     * @return null if unable to read
     */
    private File getImageFileFromDirectory(File directory) {
        String fileName = directory.getName();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory() && file.getName().contains(fileName) &&
                        extensions.contains(FilenameUtils.getExtension(file.getName())))
                    return file;
            }
        }
        return null;
    }

    /**
     * Read image file from output directory
     * @param directory
     * @return ImageJob deserialized from JSON. null if unable to read
     */
    private ImageJob readJsonFileFromDirectory(File directory) {
        File jsonFile = null;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory() && file.getName().equals(jsonName)) {
                    jsonFile = file;
                    break;
                }
            }
        }
        if (jsonFile != null) {
            try {
                return objectMapper.readValue(jsonFile, ImageJob.class);
            } catch (Exception e) {
                LOG.error("Error reading JSON in file {}", jsonFile.getAbsolutePath(), e);
                return null;
            }
        } else {
            return null;
        }
    }

    public void saveImageJobJson(ImageJob imageJob) throws IOException {
        if (!backupJsonById(imageJob.getId())){
            LOG.error("Archiving JSON Failed.");
            throw new RuntimeException();
        };
        File outputDirectory = findOutputDirectoryByID(imageJob.getId());
        if (outputDirectory == null) {
            LOG.error("Opening output directory for image {} failed.", imageJob.getId());
            throw new RuntimeException();
        }
        String jsonPath = outputDirectory.getAbsolutePath() + File.separator + jsonName;
        objectMapper.writeValue(new File(jsonPath), imageJob);
        LOG.info("Wrote JSON for image {}.", imageJob.getId());
    }

    private boolean backupJsonById(String id) throws IOException {
        File outputDirectory = findOutputDirectoryByID(id);
        if (outputDirectory != null) {
            File jsonFile = null;
            File[] files = outputDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.isDirectory() && file.getName().equals(jsonName)) {
                        jsonFile = file;
                        break;
                    }
                }
            }
            if (jsonFile != null) {
                String sourcePath = jsonFile.getAbsolutePath();
                String destinationFilePath = outputDirectory.getAbsolutePath()
                        + File.separator
                        + "archive"
                        + File.separator
                        + LocalDateTime.now().toString().replaceAll(":", "-")
                        + ".json";
                Files.move(Paths.get(sourcePath), Paths.get(destinationFilePath));
                return true;
            } else {
                LOG.error("Failed to open jsonFile.");
                return false;
            }
        }
        LOG.error("Failed to open output directory.");
        return false;
    }

    private File findOutputDirectoryByID(String id){
        File directory = new File(outputPath);
        File[] outputFolders = directory.listFiles();
        if (outputFolders != null){
            for (File outputFolder : outputFolders) {
                if (outputFolder.isDirectory() && id.contains(FilenameUtils.getBaseName(outputFolder.getName()))){
                    return outputFolder;
                }
            }
        }
        return null;
    }

    private void saveFileInOutputDirectory(File file) {

    }


}
