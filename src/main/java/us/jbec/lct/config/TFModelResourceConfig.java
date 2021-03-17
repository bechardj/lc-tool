package us.jbec.lct.config;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TFModelResourceConfig implements WebMvcConfigurer {

    Logger LOG = LoggerFactory.getLogger(TFModelResourceConfig.class);

    @Value("${lct.tf.modelPath:#{null}}")
    public String localModelPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (localModelPath !=  null) {
            String path = FilenameUtils.normalizeNoEndSeparator(localModelPath);
            path = FilenameUtils.separatorsToSystem(path);
            LOG.info("Registering {} to /localModels/", path);
            registry.addResourceHandler("/localModels/**")
                    .addResourceLocations("file://" + path + "/");
        }
    }
}
