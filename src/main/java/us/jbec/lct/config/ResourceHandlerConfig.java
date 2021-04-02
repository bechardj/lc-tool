package us.jbec.lct.config;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class ResourceHandlerConfig implements WebMvcConfigurer {

    Logger LOG = LoggerFactory.getLogger(ResourceHandlerConfig.class);

    @Value("${lct.tf.modelPath:#{null}}")
    public String localModelPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (localModelPath !=  null) {
            var file = new File(localModelPath);
            var path = file.getAbsolutePath() + File.separator;
            if (!file.exists()) {
                LOG.error("File {} either does not exist or is not resolvable", path);
            }
            if (!file.isDirectory()) {
                LOG.error("File {} is a not recognized as a valid directory", path);
            }
            LOG.info("TensorflowJS model directory {} provided. Binding resource handler to /localModels/", path);
            registry.addResourceHandler("/localModels/**")
                    .setCachePeriod(0)
                    .addResourceLocations(SystemUtils.IS_OS_WINDOWS ? "file:/" + path : "file://" + path);
        } else {
            LOG.info("No TensorflowJS model directory provided. Will fallback to built in model.");
        }
    }
}
