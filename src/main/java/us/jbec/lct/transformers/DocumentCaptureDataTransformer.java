package us.jbec.lct.transformers;

import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.ImageJobFields;
import us.jbec.lct.models.capture.CharacterCaptureData;
import us.jbec.lct.models.capture.DocumentCaptureData;
import us.jbec.lct.models.capture.LineCaptureData;
import us.jbec.lct.models.capture.WordCaptureData;
import us.jbec.lct.models.geometry.LineSegment;

import java.util.HashMap;
import java.util.Map;

public class DocumentCaptureDataTransformer {

    public ImageJob apply(DocumentCaptureData documentCaptureData) {
        DocumentCaptureData source = DocumentCaptureData.flatten(documentCaptureData, documentCaptureData.getUuid());
        ImageJob target = new ImageJob();

        target.setEdited(source.isEdited());
        target.setCompleted(source.isCompleted());
        target.setId(source.getUuid());

        Map<String, String> fields = new HashMap<>();
        fields.put(ImageJobFields.NOTES.name(), source.getNotes());

        target.setFields(fields);

        source.getCharacterCaptureDataList().stream()
                .map(CharacterCaptureData::getLabeledRectangle)
                .forEach(labeledRectangle -> {
                    target.getCharacterRectangles().add(labeledRectangle.generateCoordinatesAsList());
                    target.getCharacterLabels().add(labeledRectangle.getLabel());
                });

        target.setWordLines(source.getWordCaptureDataList().stream()
                .map(WordCaptureData::getLineSegment)
                .map(LineSegment::getCoordinatesAsList)
                .toList());

        target.setLineLines(source.getLineCaptureDataList().stream()
                .map(LineCaptureData::getLineSegment)
                .map(LineSegment::getCoordinatesAsList)
                .toList());

        return target;
    }
}
