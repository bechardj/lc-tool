package us.jbec.lct.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.capture.DocumentCaptureData;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class DocumentCaptureDataConverter implements AttributeConverter<DocumentCaptureData, String> {

    private static ObjectMapper objectMapper;

    public DocumentCaptureDataConverter() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public String convertToDatabaseColumn(DocumentCaptureData attribute) {
        if (attribute == null) {
            return null;
        } else {
            try {
                return objectMapper.writeValueAsString(attribute);
            } catch (JsonProcessingException e) {
                throw new LCToolException("Failure in converter - unable to serialize!");
            }
        }
    }

    @Override
    public DocumentCaptureData convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        } else {
            try {
                return objectMapper.readValue(dbData, DocumentCaptureData.class);
            } catch (JsonProcessingException e) {
                throw new LCToolException("Failure in converter - unable to serialize!");
            }
        }
    }
}
