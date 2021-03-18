package us.jbec.lct.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class TFModelResourceConfig implements WebMvcConfigurer {

    Logger LOG = LoggerFactory.getLogger(TFModelResourceConfig.class);

    @Value("${lct.tf.modelPath:#{null}}")
    public String localModelPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (localModelPath !=  null) {
            File file = new File(localModelPath);
            String path = file.getAbsolutePath() + File.separator;
            if (!file.exists()) {
                LOG.error("File {} either does not exist or is not resolvable", path);
            }
            if (!file.isDirectory()) {
                LOG.error("File {} is a not recognized as a valid directory", path);
            }
            LOG.info("TensorflowJS model directory {} provided. Binding resource handler to /localModels/", path);
            registry.addResourceHandler("/localModels/**")
                    .addResourceLocations("file:/" + path);
        } else {
            LOG.info("No TensorflowJS model directory provided. Will fallback to built in model.");
        }
    }
}
