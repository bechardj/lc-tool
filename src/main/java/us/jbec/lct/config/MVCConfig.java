package us.jbec.lct.config;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Configuration class for configuring custom MVC settings, including serving custom resources
 */
@Configuration
public class MVCConfig implements WebMvcConfigurer {

    Logger LOG = LoggerFactory.getLogger(MVCConfig.class);

    @Value("${lct.tf.modelPath:#{null}}")
    public String localModelPath;

    @Value("${lct.path.zip.output:#{null}}")
    private String zipResourcePath;

    Environment environment;

    public MVCConfig(Environment environment) {
        super();
        this.environment = environment;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (localModelPath !=  null) {
            addResourceByFileSystemPath(registry, localModelPath, "/localModels/**");
        } else {
            LOG.info("No TensorflowJS model directory provided. Will fallback to built in model.");
        }
        if (zipResourcePath !=  null) {
            addResourceByFileSystemPath(registry, zipResourcePath, "/zipOutput/**");
        } else {
            LOG.info("No TensorflowJS model directory provided. Will fallback to built in model.");
        }
    }

    private void addResourceByFileSystemPath(ResourceHandlerRegistry registry, String inputPath, String targetUri) {
        var file = new File(inputPath);
        var path = file.getAbsolutePath() + File.separator;
        if (!file.exists()) {
            LOG.error("File {} either does not exist or is not resolvable", path);
        }
        if (!file.isDirectory()) {
            LOG.error("File {} is a not recognized as a valid directory", path);
        }
        LOG.info("Resource directory {} provided. Binding resource handler to {}", path, targetUri);
        registry.addResourceHandler(targetUri)
                .setCachePeriod(0)
                .addResourceLocations(SystemUtils.IS_OS_WINDOWS ? "file:/" + path : "file://" + path);
    }

}
