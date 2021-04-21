package us.jbec.lct.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
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
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class PrimaryImageIO {

    @Value("${image.persistence.path}")
    private String imagePersistencePath;

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

    public String persistImage(String encodedImage, String uuid, String extension) throws IOException {
        var bytes = Base64.getDecoder().decode(encodedImage);
        String path = new File(imagePersistencePath).getAbsolutePath();
        File target = new File(path + File.separator + uuid + "." + extension);
        FileUtils.writeByteArrayToFile(target, bytes);
        return target.getAbsolutePath();
    }


}
