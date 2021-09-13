package us.jbec.lct.transformers;

import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.ImageJobFields;
import us.jbec.lct.models.capture.CaptureDataRecordType;
import us.jbec.lct.models.capture.CharacterCaptureData;
import us.jbec.lct.models.capture.DocumentCaptureData;
import us.jbec.lct.models.capture.LineCaptureData;
import us.jbec.lct.models.capture.WordCaptureData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Convert ImageJob up to DocumentCaptureData
 */
public class ImageJobTransformer {

    /**
     * Convert ImageJob up to DocumentCaptureData
     * @param imageJob ImageJob to convert
     * @return converted DocumentCaptureData
     */
    public static DocumentCaptureData apply(ImageJob imageJob) {

        DocumentCaptureData documentCaptureData = new DocumentCaptureData(imageJob.getId());

        documentCaptureData.setUuid(imageJob.getId());
        documentCaptureData.setEdited(imageJob.isEdited());
        documentCaptureData.setCompleted(imageJob.isCompleted());
        String notes = imageJob.getFields().get(ImageJobFields.NOTES.name());

        Map<String, List<CharacterCaptureData>> characterCaptureDataMap = new HashMap<>();
        for (var labeledRectangle : imageJob.getLabeledRectangles()) {
            var characterCaptureData = new CharacterCaptureData();
            characterCaptureData.setUuid(UUID.randomUUID().toString());
            characterCaptureData.setLabeledRectangle(labeledRectangle);
            characterCaptureData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
            characterCaptureDataMap.put(characterCaptureData.getUuid(), List.of(characterCaptureData));
        }
        documentCaptureData.setCharacterCaptureDataMap(characterCaptureDataMap);

        Map<String, List<WordCaptureData>> wordCaptureDataMap = new HashMap<>();
        for (var wordLineSegment : imageJob.getWordLineSegments()) {
            var wordCaptureData = new WordCaptureData();
            wordCaptureData.setUuid(UUID.randomUUID().toString());
            wordCaptureData.setLineSegment(wordLineSegment);
            wordCaptureData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
            wordCaptureDataMap.put(wordCaptureData.getUuid(), List.of(wordCaptureData));
        }
        documentCaptureData.setWordCaptureDataMap(wordCaptureDataMap);

        Map<String, List<LineCaptureData>> lineCaptureDataMap = new HashMap<>();
        for (var lineLineSegment : imageJob.getLineLineSegments()) {
            var lineCaptureData = new LineCaptureData();
            lineCaptureData.setUuid(UUID.randomUUID().toString());
            lineCaptureData.setLineSegment(lineLineSegment);
            lineCaptureData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
            lineCaptureDataMap.put(lineCaptureData.getUuid(), List.of(lineCaptureData));
        }

        documentCaptureData.setLineCaptureDataMap(lineCaptureDataMap);

        if (notes != null) {
            documentCaptureData.setNotes(notes);
        }
        return documentCaptureData;
    }
}
