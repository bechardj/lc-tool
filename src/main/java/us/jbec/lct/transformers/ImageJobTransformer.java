package us.jbec.lct.transformers;

import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.ImageJobFields;
import us.jbec.lct.models.capture.CaptureDataRecordType;
import us.jbec.lct.models.capture.CharacterCaptureData;
import us.jbec.lct.models.capture.DocumentCaptureData;
import us.jbec.lct.models.capture.LineCaptureData;
import us.jbec.lct.models.capture.WordCaptureData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImageJobTransformer {

    public static DocumentCaptureData apply(ImageJob imageJob) {

        DocumentCaptureData documentCaptureData = new DocumentCaptureData(imageJob.getId());

        documentCaptureData.setUuid(imageJob.getId());
        documentCaptureData.setEdited(imageJob.isEdited());
        documentCaptureData.setCompleted(imageJob.isCompleted());
        String notes = imageJob.getFields().get(ImageJobFields.NOTES.name());

        List<CharacterCaptureData> characterCaptureDataList = new ArrayList<>();
        for (var labeledRectangle : imageJob.getLabeledRectangles()) {
            var characterCaptureData = new CharacterCaptureData();
            characterCaptureData.setUuid(UUID.randomUUID().toString());
            characterCaptureData.setLabeledRectangle(labeledRectangle);
            characterCaptureData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
            characterCaptureDataList.add(characterCaptureData);
        }
        documentCaptureData.setCharacterCaptureDataList(characterCaptureDataList);

        List<WordCaptureData> wordCaptureDataList = new ArrayList<>();
        for (var wordLineSegment : imageJob.getWordLineSegments()) {
            var wordCaptureData = new WordCaptureData();
            wordCaptureData.setUuid(UUID.randomUUID().toString());
            wordCaptureData.setLineSegment(wordLineSegment);
            wordCaptureData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
            wordCaptureDataList.add(wordCaptureData);
        }
        documentCaptureData.setWordCaptureDataList(wordCaptureDataList);

        List<LineCaptureData> lineCaptureDataList = new ArrayList<>();
        for (var lineLineSegment : imageJob.getLineLineSegments()) {
            var lineCaptureData = new LineCaptureData();
            lineCaptureData.setUuid(UUID.randomUUID().toString());
            lineCaptureData.setLineSegment(lineLineSegment);
            lineCaptureData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
            lineCaptureDataList.add(lineCaptureData);
        }

        documentCaptureData.setLineCaptureDataList(lineCaptureDataList);

        if (notes != null) {
            documentCaptureData.setNotes(notes);
        }
        return documentCaptureData;
    }
}
