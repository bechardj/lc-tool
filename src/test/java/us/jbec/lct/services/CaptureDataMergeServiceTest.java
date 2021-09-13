package us.jbec.lct.services;

import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.Test;
import us.jbec.lct.models.capture.CaptureData;
import us.jbec.lct.models.capture.CaptureDataPayload;
import us.jbec.lct.models.capture.CaptureDataRecordType;
import us.jbec.lct.models.capture.CharacterCaptureData;
import us.jbec.lct.models.capture.DocumentCaptureData;
import us.jbec.lct.models.capture.LineCaptureData;
import us.jbec.lct.models.capture.WordCaptureData;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class CaptureDataMergeServiceTest {

    CaptureDataMergeService testee = new CaptureDataMergeService();

    private final static String PRIVATE_SHOULD_INTEGRATE = "shouldIntegrateCaptureData";

    @Test
    public void testShouldIntegrateCaptureData_Null() {
        var dataToIntegrate = new CharacterCaptureData();
        dataToIntegrate.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        dataToIntegrate.setUuid("1");

        Boolean result = ReflectionTestUtils.invokeMethod(testee,
                PRIVATE_SHOULD_INTEGRATE,
                null,
                dataToIntegrate);
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    public void testShouldIntegrateCaptureData_Empty() {
        List<LineCaptureData> existingData = new ArrayList<>();
        var dataToIntegrate = new LineCaptureData();
        dataToIntegrate.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        dataToIntegrate.setUuid("1");

        Boolean result = ReflectionTestUtils.invokeMethod(testee,
                PRIVATE_SHOULD_INTEGRATE,
                existingData,
                dataToIntegrate);
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    public void testShouldIntegrateCaptureData_DeleteExists() {
        List<LineCaptureData> existingData = new ArrayList<>();

        var existing = new LineCaptureData();
        existing.setCaptureDataRecordType(CaptureDataRecordType.DELETE);
        existing.setUuid("1");
        existingData.add(existing);

        var dataToIntegrate = new LineCaptureData();
        dataToIntegrate.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        dataToIntegrate.setUuid("1");

        Boolean result = ReflectionTestUtils.invokeMethod(testee,
                PRIVATE_SHOULD_INTEGRATE,
                existingData,
                dataToIntegrate);
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    public void testShouldIntegrateCaptureData_CreateExistsIntegrateDelete() {
        List<LineCaptureData> existingData = new ArrayList<>();

        var existing = new LineCaptureData();
        existing.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        existing.setUuid("1");
        existingData.add(existing);

        var dataToIntegrate = new LineCaptureData();
        dataToIntegrate.setCaptureDataRecordType(CaptureDataRecordType.DELETE);
        dataToIntegrate.setUuid("1");

        Boolean result = ReflectionTestUtils.invokeMethod(testee,
                PRIVATE_SHOULD_INTEGRATE,
                existingData,
                dataToIntegrate);
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    public void testShouldNotIntegrateCaptureData_CreateExistsIntegrateCreate() {
        List<LineCaptureData> existingData = new ArrayList<>();

        var existing = new LineCaptureData();
        existing.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        existing.setUuid("1");
        existingData.add(existing);

        var dataToIntegrate = new LineCaptureData();
        dataToIntegrate.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        dataToIntegrate.setUuid("1");

        Boolean result = ReflectionTestUtils.invokeMethod(testee,
                PRIVATE_SHOULD_INTEGRATE,
                existingData,
                dataToIntegrate);
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void testShouldNotIntegrateCaptureData_DeleteExistsIntegrateDelete() {
        List<LineCaptureData> existingData = new ArrayList<>();

        var existing = new LineCaptureData();
        existing.setCaptureDataRecordType(CaptureDataRecordType.DELETE);
        existing.setUuid("1");
        existingData.add(existing);

        var dataToIntegrate = new LineCaptureData();
        dataToIntegrate.setCaptureDataRecordType(CaptureDataRecordType.DELETE);
        dataToIntegrate.setUuid("1");

        Boolean result = ReflectionTestUtils.invokeMethod(testee,
                PRIVATE_SHOULD_INTEGRATE,
                existingData,
                dataToIntegrate);
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void testShouldNotIntegrateCaptureData_TwoRecordsExist() {
        List<LineCaptureData> existingData = new ArrayList<>();

        var existing1 = new LineCaptureData();
        existing1.setCaptureDataRecordType(CaptureDataRecordType.DELETE);
        existing1.setUuid("1");
        existingData.add(existing1);

        var existing2 = new LineCaptureData();
        existing2.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        existing2.setUuid("1");
        existingData.add(existing2);

        var dataToIntegrate = new LineCaptureData();
        dataToIntegrate.setCaptureDataRecordType(CaptureDataRecordType.DELETE);
        dataToIntegrate.setUuid("1");

        Boolean result = ReflectionTestUtils.invokeMethod(testee,
                PRIVATE_SHOULD_INTEGRATE,
                existingData,
                dataToIntegrate);
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void testMergeCaptureData_New() {
        var server = new DocumentCaptureData("1");
        var client = new DocumentCaptureData("2");

        var serverData1 = new CharacterCaptureData();
        serverData1.setUuid("1");
        serverData1.setCaptureDataRecordType(CaptureDataRecordType.CREATE);

        server.insertCharacterCaptureData(serverData1);

        var clientData1 = new CharacterCaptureData();
        clientData1.setUuid("1");
        clientData1.setCaptureDataRecordType(CaptureDataRecordType.CREATE);

        var clientData2 = new CharacterCaptureData();
        clientData2.setUuid("2");
        clientData2.setCaptureDataRecordType(CaptureDataRecordType.CREATE);

        client.insertCharacterCaptureData(clientData1);
        client.insertCharacterCaptureData(clientData2);

        int result = testee.mergeCaptureData(server, client);

        assertEquals(result, 1);
        assertEquals(server.getCharacterCaptureDataMap().size(), 2);
        assertEquals(server.getCharacterCaptureDataMap().get("1").size(), 1);
        assertEquals(server.getCharacterCaptureDataMap().get("1").get(0), serverData1);
        assertEquals(server.getCharacterCaptureDataMap().get("2").get(0), clientData2);
    }

    @Test
    public void testMergeCaptureData_DeleteExisting() {
        var server = new DocumentCaptureData("1");
        var client = new DocumentCaptureData("2");

        var serverData1 = new LineCaptureData();
        serverData1.setUuid("1");
        serverData1.setCaptureDataRecordType(CaptureDataRecordType.CREATE);

        server.insertLineCaptureData(serverData1);

        var clientData1 = new LineCaptureData();
        clientData1.setUuid("1");
        clientData1.setCaptureDataRecordType(CaptureDataRecordType.CREATE);

        var clientData2 = new LineCaptureData();
        clientData2.setUuid("1");
        clientData2.setCaptureDataRecordType(CaptureDataRecordType.DELETE);

        client.insertLineCaptureData(clientData1);
        client.insertLineCaptureData(clientData2);

        int result = testee.mergeCaptureData(server, client);

        assertEquals(result, 1);
        assertEquals(server.getLineCaptureDataMap().size(), 1);
        assertEquals(server.getLineCaptureDataMap().get("1").size(), 2);
        assertTrue(server.getLineCaptureDataMap().get("1").contains(serverData1));
        assertTrue(server.getLineCaptureDataMap().get("1").contains(clientData2));
    }

    @Test
    public void testMergeCaptureData_DeleteNonExisting() {
        var server = new DocumentCaptureData("1");
        var client = new DocumentCaptureData("2");

        var serverData1 = new WordCaptureData();
        serverData1.setUuid("1");
        serverData1.setCaptureDataRecordType(CaptureDataRecordType.DELETE);

        server.insertWordCaptureData(serverData1);

        var clientData1 = new WordCaptureData();
        clientData1.setUuid("1");
        clientData1.setCaptureDataRecordType(CaptureDataRecordType.CREATE);

        var clientData2 = new WordCaptureData();
        clientData2.setUuid("2");
        clientData2.setCaptureDataRecordType(CaptureDataRecordType.DELETE);

        client.insertWordCaptureData(clientData1);
        client.insertWordCaptureData(clientData2);

        int result = testee.mergeCaptureData(server, client);

        assertEquals(result, 2);
        assertEquals(server.getWordCaptureDataMap().size(), 2);
        assertEquals(server.getWordCaptureDataMap().get("1").size(), 2);
        assertTrue(server.getWordCaptureDataMap().get("1").contains(serverData1));
        assertTrue(server.getWordCaptureDataMap().get("1").contains(clientData1));
        assertTrue(server.getWordCaptureDataMap().get("2").contains(clientData2));
    }

    @Test
    public void testMergePayloadIntoDocumentChar_Merge() {
        var server = new DocumentCaptureData("1");
        CaptureDataPayload payload = new CaptureDataPayload();
        var charData = new CharacterCaptureData();
        charData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        charData.setUuid("1");
        payload.setCharacterCaptureData(charData);

        testee.mergePayloadIntoDocument(server, payload);

        assertNotNull(server.getCharacterCaptureDataMap());
        assertTrue(server.getCharacterCaptureDataMap().get("1").contains(charData));
    }

    @Test
    public void testMergePayloadIntoDocumentChar_NoMerge() {
        var server = new DocumentCaptureData("1");
        CaptureDataPayload payload = new CaptureDataPayload();
        var charData = new CharacterCaptureData();
        charData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        charData.setUuid("1");
        payload.setCharacterCaptureData(charData);

        var serverCharData = new CharacterCaptureData();
        serverCharData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        serverCharData.setUuid("1");
        server.insertCharacterCaptureData(serverCharData);

        testee.mergePayloadIntoDocument(server, payload);

        assertNotNull(server.getCharacterCaptureDataMap());
        assertTrue(server.getCharacterCaptureDataMap().get("1").contains(serverCharData));
        assertFalse(server.getCharacterCaptureDataMap().get("1").contains(charData));
    }

    @Test
    public void testMergePayloadIntoDocumentWord_Merge() {
        var server = new DocumentCaptureData("1");
        CaptureDataPayload payload = new CaptureDataPayload();
        var wordData = new WordCaptureData();
        wordData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        wordData.setUuid("1");
        payload.setWordCaptureData(wordData);

        testee.mergePayloadIntoDocument(server, payload);

        assertNotNull(server.getWordCaptureDataMap());
        assertTrue(server.getWordCaptureDataMap().get("1").contains(wordData));
    }

    @Test
    public void testMergePayloadIntoDocumentWord_NoMerge() {
        var server = new DocumentCaptureData("1");
        CaptureDataPayload payload = new CaptureDataPayload();
        var wordData = new WordCaptureData();
        wordData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        wordData.setUuid("1");
        payload.setWordCaptureData(wordData);

        var wordCharData = new WordCaptureData();
        wordCharData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        wordCharData.setUuid("1");
        server.insertWordCaptureData(wordCharData);

        testee.mergePayloadIntoDocument(server, payload);

        assertNotNull(server.getWordCaptureDataMap());
        assertTrue(server.getWordCaptureDataMap().get("1").contains(wordCharData));
        assertFalse(server.getWordCaptureDataMap().get("1").contains(wordData));
    }

    @Test
    public void testMergePayloadIntoDocumentLine_Merge() {
        var server = new DocumentCaptureData("1");
        CaptureDataPayload payload = new CaptureDataPayload();
        var lineData = new LineCaptureData();
        lineData.setCaptureDataRecordType(CaptureDataRecordType.DELETE);
        lineData.setUuid("1");
        payload.setLineCaptureData(lineData);

        testee.mergePayloadIntoDocument(server, payload);

        assertNotNull(server.getLineCaptureDataMap());
        assertTrue(server.getLineCaptureDataMap().get("1").contains(lineData));
    }

    @Test
    public void testMergePayloadIntoDocumentLine_NoMerge() {
        var server = new DocumentCaptureData("1");
        CaptureDataPayload payload = new CaptureDataPayload();
        var lineData = new LineCaptureData();
        lineData.setCaptureDataRecordType(CaptureDataRecordType.DELETE);
        lineData.setUuid("1");
        payload.setLineCaptureData(lineData);

        var serverLineData = new LineCaptureData();
        serverLineData.setCaptureDataRecordType(CaptureDataRecordType.DELETE);
        serverLineData.setUuid("1");
        server.insertLineCaptureData(serverLineData);

        testee.mergePayloadIntoDocument(server, payload);

        assertNotNull(server.getLineCaptureDataMap());
        assertEquals(server.getLineCaptureDataMap().size(), 1);
        assertTrue(server.getLineCaptureDataMap().get("1").contains(serverLineData));
        assertFalse(server.getLineCaptureDataMap().get("1").contains(lineData));
    }

    @Test
    public void testMergePayloadIntoDocumentLine_MergeJustOneOfThree() {
        var server = new DocumentCaptureData("1");
        CaptureDataPayload payload = new CaptureDataPayload();

        var charData = new CharacterCaptureData();
        charData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        charData.setUuid("1");
        payload.setCharacterCaptureData(charData);

        var wordData = new WordCaptureData();
        wordData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        wordData.setUuid("2");
        payload.setWordCaptureData(wordData);

        var lineData = new LineCaptureData();
        lineData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        lineData.setUuid("3");
        payload.setLineCaptureData(lineData);


        testee.mergePayloadIntoDocument(server, payload);

        assertNotNull(server.getCharacterCaptureDataMap());
        assertTrue(server.getCharacterCaptureDataMap().get("1").contains(charData));
        assertEquals(server.getCharacterCaptureDataMap().size(), 1);
        assertNull(server.getWordCaptureDataMap().get("2"));
        assertNull(server.getLineCaptureDataMap().get("3"));
    }

    @Test
    public void testMergePayloadIntoDocumentLine_MergeJustOneOfTwo() {
        var server = new DocumentCaptureData("1");
        CaptureDataPayload payload = new CaptureDataPayload();


        var wordData = new WordCaptureData();
        wordData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        wordData.setUuid("2");
        payload.setWordCaptureData(wordData);

        var lineData = new LineCaptureData();
        lineData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
        lineData.setUuid("3");
        payload.setLineCaptureData(lineData);


        testee.mergePayloadIntoDocument(server, payload);

        assertNotNull(server.getWordCaptureDataMap());
        assertTrue(server.getWordCaptureDataMap().get("2").contains(wordData));
        assertEquals(server.getWordCaptureDataMap().size(), 1);
        assertNull(server.getCharacterCaptureDataMap().get("2"));
        assertNull(server.getLineCaptureDataMap().get("3"));
    }

    @Test
    public void testCreatePayloadsForSyncNoPayloads() {
        var server = new DocumentCaptureData("1");
        var client = new DocumentCaptureData("1");

        var result = testee.createPayloadsForSync(server, client);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCreatePayloadsForSyncMixedCreate() {
        var server = new DocumentCaptureData("1");
        var client = new DocumentCaptureData("1");

        // Create Record on Server, Not Client
        server.insertCharacterCaptureData(setupCaptureData(new CharacterCaptureData(), CaptureDataRecordType.CREATE, "1a"));
        server.insertWordCaptureData(setupCaptureData(new WordCaptureData(), CaptureDataRecordType.CREATE, "1b"));
        server.insertLineCaptureData(setupCaptureData(new LineCaptureData(), CaptureDataRecordType.CREATE, "1c"));

        var result = testee.createPayloadsForSync(server, client);

        assertNotNull(result);

        var charDataPayloads = result.stream()
                .filter(payload -> payload.getCharacterCaptureData() != null)
                .toList();
        assertEquals(charDataPayloads.size(), 1);
        assertEquals(charDataPayloads.get(0).getCharacterCaptureData().getUuid(), "1a");

        var wordDataPayloads = result.stream()
                .filter(payload -> payload.getWordCaptureData() != null)
                .toList();
        assertEquals(wordDataPayloads.size(), 1);
        assertEquals(wordDataPayloads.get(0).getWordCaptureData().getUuid(), "1b");

        var lineDataPayloads = result.stream()
                .filter(payload -> payload.getLineCaptureData() != null)
                .toList();
        assertEquals(lineDataPayloads.size(), 1);
        assertEquals(lineDataPayloads.get(0).getLineCaptureData().getUuid(), "1c");
    }

    @Test
    public void testCreatePayloadsForSyncMixedNoCreate() {
        var server = new DocumentCaptureData("1");
        var client = new DocumentCaptureData("1");

        // Create Record on Server, And Client
        server.insertCharacterCaptureData(setupCaptureData(new CharacterCaptureData(), CaptureDataRecordType.CREATE, "1a"));
        client.insertCharacterCaptureData(setupCaptureData(new CharacterCaptureData(), CaptureDataRecordType.CREATE, "1a"));
        server.insertWordCaptureData(setupCaptureData(new WordCaptureData(), CaptureDataRecordType.CREATE, "1b"));
        client.insertWordCaptureData(setupCaptureData(new WordCaptureData(), CaptureDataRecordType.CREATE, "1b"));
        server.insertLineCaptureData(setupCaptureData(new LineCaptureData(), CaptureDataRecordType.CREATE, "1c"));
        server.insertLineCaptureData(setupCaptureData(new LineCaptureData(), CaptureDataRecordType.DELETE, "1c"));
        client.insertLineCaptureData(setupCaptureData(new LineCaptureData(), CaptureDataRecordType.CREATE, "1c"));
        client.insertLineCaptureData(setupCaptureData(new LineCaptureData(), CaptureDataRecordType.DELETE, "1c"));

        var result = testee.createPayloadsForSync(server, client);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCreatePayloadsForSyncMixedDelete() {
        var server = new DocumentCaptureData("1");
        var client = new DocumentCaptureData("1");

        // Delete Record only on Server (no create), Not Client
        server.insertCharacterCaptureData(setupCaptureData(new CharacterCaptureData(), CaptureDataRecordType.CREATE, "1"));
        server.insertCharacterCaptureData(setupCaptureData(new CharacterCaptureData(), CaptureDataRecordType.DELETE, "1"));

        // Delete Record on Server, Not Client but Client Has Create Record
        server.insertWordCaptureData(setupCaptureData(new WordCaptureData(), CaptureDataRecordType.CREATE, "2"));
        client.insertWordCaptureData(setupCaptureData(new WordCaptureData(), CaptureDataRecordType.CREATE, "2"));
        server.insertWordCaptureData(setupCaptureData(new WordCaptureData(), CaptureDataRecordType.DELETE, "2"));

        // Delete Record on Server, Not Client
        server.insertLineCaptureData(setupCaptureData(new LineCaptureData(), CaptureDataRecordType.DELETE, "3"));

        var result = testee.createPayloadsForSync(server, client);

        assertNotNull(result);

        var charDataPayloads = result.stream()
                .filter(payload -> payload.getCharacterCaptureData() != null)
                .toList();
        assertEquals(charDataPayloads.size(), 1);
        assertEquals(charDataPayloads.get(0).getCharacterCaptureData().getUuid(), "1");
        assertEquals(charDataPayloads.get(0).getCharacterCaptureData().getCaptureDataRecordType(), CaptureDataRecordType.DELETE);

        var wordDataPayloads = result.stream()
                .filter(payload -> payload.getWordCaptureData() != null)
                .toList();
        assertEquals(wordDataPayloads.size(), 1);
        assertEquals(wordDataPayloads.get(0).getWordCaptureData().getUuid(), "2");
        assertEquals(wordDataPayloads.get(0).getWordCaptureData().getCaptureDataRecordType(), CaptureDataRecordType.DELETE);

        var lineDataPayloads = result.stream()
                .filter(payload -> payload.getLineCaptureData() != null)
                .toList();
        assertEquals(lineDataPayloads.size(), 1);
        assertEquals(lineDataPayloads.get(0).getLineCaptureData().getUuid(), "3");
        assertEquals(lineDataPayloads.get(0).getLineCaptureData().getCaptureDataRecordType(),CaptureDataRecordType.DELETE);
    }


    private <T extends CaptureData> T setupCaptureData(T captureData, CaptureDataRecordType recordType, String uuid) {
        captureData.setCaptureDataRecordType(recordType);
        captureData.setUuid(uuid);
        return captureData;
    }

}