package us.jbec.lct.models.capture;

import org.locationtech.jts.geom.Coordinate;
import org.testng.annotations.Test;
import us.jbec.lct.models.geometry.LabeledRectangle;
import us.jbec.lct.models.geometry.LineSegment;

import java.util.Arrays;

import static org.testng.Assert.*;

public class DocumentCaptureDataTest {

    @Test
    public void testFlattenNoCaptureData() throws CloneNotSupportedException {
        DocumentCaptureData data = new DocumentCaptureData("1234");
        data.setEdited(true);
        data.setCompleted(false);
        data.setNotes("Working on it...");
        var result = DocumentCaptureData.flatten(data, "1234");

        assertNotNull(result);
        assertTrue(result.isEdited());
        assertFalse(result.isCompleted());
        assertEquals(result.getNotes(), "Working on it...");
        assertTrue(result.getCharacterCaptureDataMap().isEmpty());
        assertTrue(result.getWordCaptureDataMap().isEmpty());
        assertTrue(result.getLineCaptureDataMap().isEmpty());
    }

    @Test
    public void testFlattenWordCaptureData() throws CloneNotSupportedException {

        var data = new DocumentCaptureData("1234");

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
        wordCaptureData3.setUuid("wc1");

        Arrays.asList(wordCaptureData1, wordCaptureData2, wordCaptureData3)
                .forEach(data::insertWordCaptureData);

        var result = DocumentCaptureData.flatten(data, "1234");

        assertNotNull(result);
        assertEquals(result.getWordCaptureDataMap().size(), 1);
        assertNotNull(result.getWordCaptureDataMap().get(wordCaptureData2.getUuid()));
        assertEquals(result.getWordCaptureDataMap().get(wordCaptureData2.getUuid()).size(), 1);
        var wordCaptureData2Flattened = result.getWordCaptureDataMap()
                .get(wordCaptureData2.getUuid()).get(0);
        assertEquals(wordCaptureData2Flattened.getUuid(), "wc2");
        assertEquals(wordCaptureData2Flattened.getLineSegment(), new LineSegment(2,3,3,4));
    }

    @Test
    public void testFlattenLineCaptureData() throws CloneNotSupportedException {

        var data = new DocumentCaptureData("1234");

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

        Arrays.asList(lineCaptureData1, lineCaptureData2, lineCaptureData3)
                .forEach(data::insertLineCaptureData);

        var result = DocumentCaptureData.flatten(data, "1234");

        assertNotNull(result);
        assertEquals(result.getLineCaptureDataMap().size(), 1);
        assertNotNull(result.getLineCaptureDataMap().get(lineCaptureData1.getUuid()));
        assertEquals(result.getLineCaptureDataMap().get(lineCaptureData1.getUuid()).size(), 1);

        var lineCaptureData1Flattened = result.getLineCaptureDataMap().get(lineCaptureData1.getUuid()).get(0);
        assertEquals(lineCaptureData1Flattened.getUuid(), "lc1");
        assertEquals(lineCaptureData1Flattened.getLineSegment(), new LineSegment(1,2,3,4));
    }

    @Test
    public void testFlattenCharacterCaptureData() throws CloneNotSupportedException {

        var data = new DocumentCaptureData("1234");

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

        Arrays.asList(characterCaptureData3, characterCaptureData2, characterCaptureData1, characterCaptureData3, characterCaptureData1)
                .forEach(data::insertCharacterCaptureData);

        var result = DocumentCaptureData.flatten(data, "1234");

        assertNotNull(result);
        assertEquals(result.getCharacterCaptureDataMap().size(), 1);
        assertNotNull(result.getCharacterCaptureDataMap().get(characterCaptureData2.getUuid()));
        assertEquals(result.getCharacterCaptureDataMap().get(characterCaptureData2.getUuid()).size(), 1);

        var characterCaptureData2Flattened = result.getCharacterCaptureDataMap().get(characterCaptureData2.getUuid()).get(0);
        assertEquals(characterCaptureData2Flattened.getUuid(), "cc2");
        assertEquals(characterCaptureData2Flattened.getLabeledRectangle(), new LabeledRectangle(Arrays.asList(1.0,2.1,3.1,4.1), "B"));
    }

    @Test
    public void testManipulateAfterFlatten() throws CloneNotSupportedException {

        var data = new DocumentCaptureData("1234");

        WordCaptureData wordCaptureData1 = new WordCaptureData();
        wordCaptureData1.setLineSegment(new LineSegment(1,2,3,4));
        wordCaptureData1.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        wordCaptureData1.setUuid("wc1");

        LineCaptureData lineCaptureData1 = new LineCaptureData();
        lineCaptureData1.setLineSegment(new LineSegment(2,1,3,4));
        lineCaptureData1.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        lineCaptureData1.setUuid("lc1");

        CharacterCaptureData characterCaptureData1 = new CharacterCaptureData();
        characterCaptureData1.setUuid("cc1");
        characterCaptureData1.setLabeledRectangle(new LabeledRectangle(Arrays.asList(1.0,2.0,3.0,4.0), "a"));
        characterCaptureData1.setCaptureDataRecordType(CaptureDataRecordType.CREATE);

        data.insertWordCaptureData(wordCaptureData1);
        data.insertLineCaptureData(lineCaptureData1);
        data.insertCharacterCaptureData(characterCaptureData1);

        var result = DocumentCaptureData.flatten(data, "1234");

        assertNotNull(result);

        assertEquals(result.getWordCaptureDataMap().size(), 1);
        assertNotNull(result.getWordCaptureDataMap().get(wordCaptureData1.getUuid()));
        assertEquals(result.getWordCaptureDataMap().get(wordCaptureData1.getUuid()).size(), 1);

        assertEquals(result.getLineCaptureDataMap().size(), 1);
        assertNotNull(result.getLineCaptureDataMap().get(lineCaptureData1.getUuid()));
        assertEquals(result.getLineCaptureDataMap().get(lineCaptureData1.getUuid()).size(), 1);

        assertEquals(result.getCharacterCaptureDataMap().size(), 1);
        assertNotNull(result.getCharacterCaptureDataMap().get(characterCaptureData1.getUuid()));
        assertEquals(result.getCharacterCaptureDataMap().get(characterCaptureData1.getUuid()).size(), 1);

        var wordCaptureData1Flattened = result.getWordCaptureDataMap().get(wordCaptureData1.getUuid()).get(0);
        var lineCaptureData1Flattened = result.getLineCaptureDataMap().get(lineCaptureData1.getUuid()).get(0);
        var characterCaptureData1Flattened = result.getCharacterCaptureDataMap().get(characterCaptureData1.getUuid()).get(0);

        wordCaptureData1Flattened.setUuid("9");
        wordCaptureData1Flattened.setCaptureDataRecordType(CaptureDataRecordType.DELETE);
        wordCaptureData1Flattened.getLineSegment().setCoordinates(new Coordinate(99,99), new Coordinate(0,0));

        assertEquals(wordCaptureData1.getUuid(), "wc1");
        assertEquals(wordCaptureData1.getLineSegment(), new LineSegment(1,2,3,4));
        assertEquals(wordCaptureData1.getCaptureDataRecordType(), CaptureDataRecordType.CREATE);

        lineCaptureData1Flattened.setUuid("8");
        lineCaptureData1Flattened.setCaptureDataRecordType(CaptureDataRecordType.DELETE);
        lineCaptureData1Flattened.getLineSegment().setCoordinates(new Coordinate(10,12), new Coordinate(14,15));

        assertEquals(lineCaptureData1.getUuid(), "lc1");
        assertEquals(lineCaptureData1.getLineSegment(), new LineSegment(2,1,3,4));
        assertEquals(lineCaptureData1.getCaptureDataRecordType(), CaptureDataRecordType.CREATE);

        characterCaptureData1Flattened.setUuid("7");
        var flattenedLabeledRectangle = characterCaptureData1Flattened.getLabeledRectangle();
        flattenedLabeledRectangle.setLabel("?");
        flattenedLabeledRectangle.setX1(100);
        flattenedLabeledRectangle.setY1(200);
        flattenedLabeledRectangle.setHeight(300);
        flattenedLabeledRectangle.setWidth(400);
        characterCaptureData1Flattened.setCaptureDataRecordType(CaptureDataRecordType.DELETE);

        assertEquals(characterCaptureData1.getUuid(), "cc1");
        assertEquals(characterCaptureData1.getLabeledRectangle(), new LabeledRectangle(Arrays.asList(1.0,2.0,3.0,4.0), "a"));
        assertEquals(characterCaptureData1.getCaptureDataRecordType(), CaptureDataRecordType.CREATE);

    }

}