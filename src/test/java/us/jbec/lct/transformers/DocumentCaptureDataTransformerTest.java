package us.jbec.lct.transformers;

import org.testng.annotations.Test;
import us.jbec.lct.models.capture.CaptureDataRecordType;
import us.jbec.lct.models.capture.CharacterCaptureData;
import us.jbec.lct.models.capture.DocumentCaptureData;
import us.jbec.lct.models.capture.LineCaptureData;
import us.jbec.lct.models.capture.WordCaptureData;
import us.jbec.lct.models.geometry.LabeledRectangle;
import us.jbec.lct.models.geometry.LineSegment;

import java.util.Arrays;

import static org.testng.Assert.*;

public class DocumentCaptureDataTransformerTest {

    private DocumentCaptureDataTransformer testee = new DocumentCaptureDataTransformer();

    @Test
    public void testTransformSimpleFields() throws CloneNotSupportedException {
        DocumentCaptureData data = new DocumentCaptureData("1234");
        data.setEdited(true);
        data.setCompleted(true);
        data.setNotes("Working on it...");

        var result = testee.apply(data);

        assertNotNull(result);
        assertEquals(result.getId(), "1234");
        assertTrue(result.isEdited());
        assertTrue(result.isCompleted());

        assertEquals(result.getFields().get("NOTES"), "Working on it...");
        assertTrue(result.getCharacterLabels().isEmpty());
        assertTrue(result.getCharacterRectangles().isEmpty());
        assertTrue(result.getLineLines().isEmpty());
        assertTrue(result.getWordLines().isEmpty());
    }

    @Test
    public void testTransformCharacterRectangles() throws CloneNotSupportedException {
        DocumentCaptureData data = new DocumentCaptureData("1234");

        CharacterCaptureData characterCaptureData1 = new CharacterCaptureData();
        characterCaptureData1.setUuid("cc1");
        characterCaptureData1.setLabeledRectangle(new LabeledRectangle(Arrays.asList(1.0,2.0,3.0,4.0), "a"));
        characterCaptureData1.setCaptureDataRecordType(CaptureDataRecordType.CREATE);

        CharacterCaptureData characterCaptureData2 = new CharacterCaptureData();
        characterCaptureData2.setUuid("cc2");
        characterCaptureData2.setLabeledRectangle(new LabeledRectangle(Arrays.asList(1.0,2.1,3.1,4.1), "B"));
        characterCaptureData2.setCaptureDataRecordType(CaptureDataRecordType.CREATE);

        CharacterCaptureData characterCaptureData3 = new CharacterCaptureData();
        characterCaptureData3.setUuid("cc1");
        characterCaptureData3.setCaptureDataRecordType(CaptureDataRecordType.DELETE);

        CharacterCaptureData characterCaptureData4 = new CharacterCaptureData();
        characterCaptureData4.setUuid("cc4");
        characterCaptureData4.setLabeledRectangle(new LabeledRectangle(Arrays.asList(1.4,2.5,3.9,4.2), "X"));
        characterCaptureData4.setCaptureDataRecordType(CaptureDataRecordType.CREATE);

        Arrays.asList(characterCaptureData1, characterCaptureData2, characterCaptureData3, characterCaptureData4)
                .forEach(data::insertCharacterCaptureData);

        var result = testee.apply(data);

        assertNotNull(result);
        assertEquals(result.getLabeledRectangles().size(), 2);
        assertTrue(result.getLabeledRectangles().contains(new LabeledRectangle(Arrays.asList(1.0,2.1,3.1,4.1), "B")));
        assertTrue(result.getLabeledRectangles().contains(new LabeledRectangle(Arrays.asList(1.4,2.5,3.9,4.2), "X")));
    }

    @Test
    public void testTransformLineLines() throws CloneNotSupportedException {
        DocumentCaptureData data = new DocumentCaptureData("1234");

        LineCaptureData lineCaptureData1 = new LineCaptureData();
        lineCaptureData1.setLineSegment(new LineSegment(1,2,3,4));
        lineCaptureData1.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        lineCaptureData1.setUuid("lc1");

        LineCaptureData lineCaptureData2 = new LineCaptureData();
        lineCaptureData2.setLineSegment(new LineSegment(2,3,3,4));
        lineCaptureData2.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        lineCaptureData2.setUuid("lc2");

        LineCaptureData lineCaptureData3 = new LineCaptureData();
        lineCaptureData3.setCaptureDataRecordType(CaptureDataRecordType.DELETE);
        lineCaptureData3.setUuid("lc2");

        LineCaptureData lineCaptureData4 = new LineCaptureData();
        lineCaptureData4.setLineSegment(new LineSegment(1.2,3.9,4.3,9));
        lineCaptureData4.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        lineCaptureData4.setUuid("lc4");

        Arrays.asList(lineCaptureData1, lineCaptureData2, lineCaptureData3, lineCaptureData4)
                .forEach(data::insertLineCaptureData);

        var result = testee.apply(data);

        assertNotNull(result);
        assertEquals(result.getLineLineSegments().size(), 2);
        assertTrue(result.getLineLineSegments().contains(new LineSegment(1,2,3,4)));
        assertTrue(result.getLineLineSegments().contains(new LineSegment(1.2,3.9,4.3,9)));
    }

    @Test
    public void testTransformWordLines() throws CloneNotSupportedException {
        DocumentCaptureData data = new DocumentCaptureData("1234");

        WordCaptureData wordCaptureData1 = new WordCaptureData();
        wordCaptureData1.setLineSegment(new LineSegment(1,2,3,4));
        wordCaptureData1.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        wordCaptureData1.setUuid("wc1");

        WordCaptureData wordCaptureData2 = new WordCaptureData();
        wordCaptureData2.setLineSegment(new LineSegment(2,3,3,4));
        wordCaptureData2.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        wordCaptureData2.setUuid("wc2");

        WordCaptureData wordCaptureData3 = new WordCaptureData();
        wordCaptureData3.setCaptureDataRecordType(CaptureDataRecordType.DELETE);
        wordCaptureData3.setUuid("wc2");

        WordCaptureData wordCaptureData4 = new WordCaptureData();
        wordCaptureData4.setLineSegment(new LineSegment(1.2,3.9,4.3,9));
        wordCaptureData4.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        wordCaptureData4.setUuid("wc4");

        Arrays.asList(wordCaptureData1, wordCaptureData2, wordCaptureData3, wordCaptureData4)
                .forEach(data::insertWordCaptureData);
        var result = testee.apply(data);

        assertNotNull(result);
        assertEquals(result.getWordLineSegments().size(), 2);
        assertTrue(result.getWordLineSegments().contains(new LineSegment(1,2,3,4)));
        assertTrue(result.getWordLineSegments().contains(new LineSegment(1.2,3.9,4.3,9)));
    }
}