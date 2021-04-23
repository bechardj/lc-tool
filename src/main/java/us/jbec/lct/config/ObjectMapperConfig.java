package us.jbec.lct.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;

/**
 * Configuration class for Object Mapper used for (de) serializing objects
 */
public class ObjectMapperConfig {

    /**
     * Object mapper for (de) serializing objects
     * @return object mapper for serialization
     */
    @Bean
    public ObjectMapper getObjectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        return objectMapper;
    }
}
