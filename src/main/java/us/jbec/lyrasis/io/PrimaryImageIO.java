package us.jbec.lyrasis.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.jbec.lyrasis.models.ImageJob;
import us.jbec.lyrasis.models.ImageJobFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PrimaryImageIO {

    @Value("${image.ingest.path}")
    private String ingestPath;

    @Value("${image.output.path}")
    private String outputPath;

    @Value("${image.output.json.name}")
    private String jsonName;

    Logger LOG = LoggerFactory.getLogger(PrimaryImageIO.class);

    private final ObjectMapper objectMapper;

    private final List<String> extensions = new ArrayList<>(
            Arrays.asList("jpeg", "jpg", "png")
    );

    /**
     * Data Access Object for images
     * Handles FileIO
     *
     * @param objectMapper The JSON Object Mapper
     */
    public PrimaryImageIO(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    public File getOutputDirectory(){
        return new File(outputPath);
    }

    /**
     * Reads images from the ingest directory
     * @return list of files ending in the specified extensions
     */
    public List<File> getFilesFromIngestDirectory() {
        File directory = new File(ingestPath);
        File[] files = directory.listFiles();
        List<File> imageFiles = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
                if (!file.isDirectory() && extensions.contains(extension)) {
                    imageFiles.add(file);
                }
            }
        } else {
            LOG.info("Read no images in ingest directory");
        }
        return imageFiles;
    }

    /**
     * Creates local directory for file
     * @param path the directory to create
     * @return whether the directory was successfully created or not
     */
    public boolean createDirectory(String path){
        File directory = new File(path);
        if (directory.exists()){
            LOG.error("A file with path {} has already been ingested. You should change " +
                    "the name of this file in the ingest directory.", path);
            return false;
        } else {
            boolean success = directory.mkdir();
            if (success) {
                LOG.info("Created directory: {}", directory.getAbsolutePath());
                return true;
            } else {
                LOG.error("Failed to create folder {}, check output directory and permissions.", path);
                return false;
            }
        }
    }

    /**
     * Moves an image file into an already created destination directory, removing
     * it from the ingest directory. A JSON file containing the image job information
     * will also be created. This will allow for easy interchange of information between
     * Java Script and Python, etc.
     *
     * @param source The image file to copy
     * @param destinationDirectory The directory to copy into (w/o file separator)
     */
    public void initializeDirectory(File source, String destinationDirectory) {
        String sourcePath = source.getAbsolutePath();
        String destinationFilePath = destinationDirectory + File.separator + source.getName();
        String jsonPath = destinationDirectory + File.separator + jsonName;
        String jsonArchiveDirectory = destinationDirectory + File.separator + "archive";
        String cropsDirectory = destinationDirectory + File.separator + "crops";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
        try {
            Files.move(Paths.get(sourcePath), Paths.get(destinationFilePath));
            ImageJob imageJob = new ImageJob();
            imageJob.setId(FilenameUtils.getBaseName(sourcePath));
            imageJob.setVersion("0.1");
            imageJob.setCompleted(false);
            imageJob.setEdited(false);
            imageJob.setStatus("Ingested");
            imageJob.getFields().put("timestamp", fmt.format(ZonedDateTime.now()));
            objectMapper.writeValue(new File(jsonPath), imageJob);
            createDirectory(jsonArchiveDirectory);
            createDirectory(cropsDirectory);
            LOG.info("Initialized directory {}", destinationFilePath);
        } catch (IOException e) {
            LOG.error("Failed to initialize file {} in {}", sourcePath, destinationFilePath);
        }
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


}
