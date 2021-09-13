package us.jbec.lct.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import us.jbec.lct.models.capture.CaptureDataRecordType;
import us.jbec.lct.models.capture.CharacterCaptureData;
import us.jbec.lct.models.capture.DocumentCaptureData;
import us.jbec.lct.models.database.CloudCaptureDocument;
import us.jbec.lct.models.database.User;
import us.jbec.lct.models.geometry.LabeledRectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class CaptureDataStatisticsServiceTest {

    @InjectMocks
    private CaptureDataStatisticsService testee;

    @Mock
    private CloudCaptureDocumentService documentService;

    @BeforeSuite
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeMethod
    public void before() {
        Mockito.reset(documentService);
    }

    @Test
    public void testCalculateAllStatistics() throws JsonProcessingException {

        List<CloudCaptureDocument> input = new ArrayList<>();

        // First User
        User user1 = new User();
        user1.setFirebaseEmail("test@user1.com");

        var doc1 = new CloudCaptureDocument();
        doc1.setUuid("1");
        doc1.setOwner(user1);
        var data1 = buildDocDataFromChars("1", List.of("a", "B", "c", "a", "1", ".", " ", "other"));
        data1.setEdited(true);

        input.add(doc1);

        var doc2 = new CloudCaptureDocument();
        doc2.setUuid("2");
        doc2.setOwner(user1);
        var data2 = buildDocDataFromChars("1", List.of());
        data2.setEdited(false);

        input.add(doc2);

        Mockito.when(documentService.getActiveCloudCaptureDocumentsMetadata()).thenReturn(input);
        Mockito.when(documentService.getDocumentCaptureDataByUuid("1")).thenReturn(data1);
        Mockito.when(documentService.getDocumentCaptureDataByUuid("2")).thenReturn(data2);

        var result = testee.calculateAllStatistics();

        Mockito.verify(documentService, Mockito.times(1)).getActiveCloudCaptureDocumentsMetadata();
        Mockito.verify(documentService, Mockito.times(1)).getDocumentCaptureDataByUuid("1");
        Mockito.verify(documentService, Mockito.times(1)).getDocumentCaptureDataByUuid("2");

        assertNotNull(result);

        assertEquals(result.getLabelFrequency("a"), 2);
        assertEquals(result.getLabelFrequency("B"), 1);

        assertEquals(result.getLowerFrequency().size(), 2);
        assertEquals(result.getUpperFrequency().size(), 1);
        assertEquals(result.getOtherFrequency().size(), 4);
        assertEquals(result.getTotalCaptured().intValue(), 8);

        assertEquals(result.getUserCounts().size(), 1);
        assertEquals(result.getUserCounts().get("test@user1.com").intValue(), 8);

        assertEquals(result.getPagesWithData().intValue(), 1);
        assertEquals(result.getPagesMarkedCompleted().intValue(), 0);
    }

    @Test
    public void testCalculateStatistics_singleUser() throws JsonProcessingException {

        List<CloudCaptureDocument> input = new ArrayList<>();

        // First User
        User user1 = new User();
        user1.setFirebaseEmail("test@user1.com");

        var doc1 = new CloudCaptureDocument();
        doc1.setOwner(user1);
        doc1.setUuid("1");
        var data1 = buildDocDataFromChars("1", List.of("a", "B", "c", "a", "1", ".", " ", "other"));
        data1.setEdited(true);

        input.add(doc1);

        var doc2 = new CloudCaptureDocument();
        doc2.setOwner(user1);
        doc2.setUuid("2");
        var data2 = buildDocDataFromChars("2", List.of());
        data2.setEdited(false);

        input.add(doc2);

        Mockito.when(documentService.getDocumentCaptureDataByUuid("1")).thenReturn(data1);
        Mockito.when(documentService.getDocumentCaptureDataByUuid("2")).thenReturn(data2);

        var result = testee.calculateStatistics(input);

        Mockito.verify(documentService, Mockito.times(1)).getDocumentCaptureDataByUuid("1");
        Mockito.verify(documentService, Mockito.times(1)).getDocumentCaptureDataByUuid("2");

        assertNotNull(result);

        assertEquals(result.getLabelFrequency("a"), 2);
        assertEquals(result.getLabelFrequency("B"), 1);

        assertEquals(result.getLowerFrequency().size(), 2);
        assertEquals(result.getUpperFrequency().size(), 1);
        assertEquals(result.getOtherFrequency().size(), 4);
        assertEquals(result.getTotalCaptured().intValue(), 8);

        assertEquals(result.getUserCounts().size(), 1);
        assertEquals(result.getUserCounts().get("test@user1.com").intValue(), 8);

        assertEquals(result.getPagesWithData().intValue(), 1);
        assertEquals(result.getPagesMarkedCompleted().intValue(), 0);

    }

    @Test
    public void testCalculateStatistics_multipleUsers() throws JsonProcessingException {

        List<CloudCaptureDocument> input = new ArrayList<>();

        // First User
        User user1 = new User();
        user1.setFirebaseEmail("test@user1.com");

        var doc1 = new CloudCaptureDocument();
        doc1.setOwner(user1);
        doc1.setUuid("1");
        var data1 = buildDocDataFromChars("1", List.of("a", "B", "c", "a", "1", ".", " ", "other"));
        data1.setEdited(true);

        input.add(doc1);

        var doc2 = new CloudCaptureDocument();
        doc2.setOwner(user1);
        doc2.setUuid("2");
        var data2 = buildDocDataFromChars("2", List.of("x", "b", "-", "Z"));
        data2.setEdited(true);
        data2.setCompleted(true);

        input.add(doc2);

        // Second User
        User user2 = new User();
        user2.setFirebaseEmail("test@user2.com");

        var doc3 = new CloudCaptureDocument();
        doc3.setOwner(user2);
        doc3.setUuid("3");
        var data3 = buildDocDataFromChars("3", List.of("l", "m", "n", "O"));
        data3.setEdited(true);

        input.add(doc3);

        Mockito.when(documentService.getDocumentCaptureDataByUuid("1")).thenReturn(data1);
        Mockito.when(documentService.getDocumentCaptureDataByUuid("2")).thenReturn(data2);
        Mockito.when(documentService.getDocumentCaptureDataByUuid("3")).thenReturn(data3);

        var result = testee.calculateStatistics(input);

        Mockito.verify(documentService, Mockito.times(1)).getDocumentCaptureDataByUuid("1");
        Mockito.verify(documentService, Mockito.times(1)).getDocumentCaptureDataByUuid("2");
        Mockito.verify(documentService, Mockito.times(1)).getDocumentCaptureDataByUuid("3");

        assertNotNull(result);

        assertEquals(result.getLabelFrequency("a"), 2);
        assertEquals(result.getLabelFrequency("B"), 1);
        assertEquals(result.getLabelFrequency("?"), 0);

        assertEquals(result.getLowerFrequency().size(), 7);
        assertEquals(result.getUpperFrequency().size(), 3);
        assertEquals(result.getOtherFrequency().size(), 5);
        assertEquals(result.getTotalCaptured().intValue(), 16);

        assertEquals(result.getUserCounts().size(), 2);
        assertEquals(result.getUserCounts().get("test@user1.com").intValue(), 12);
        assertEquals(result.getUserCounts().get("test@user2.com").intValue(), 4);

        assertEquals(result.getPagesWithData().intValue(), 3);
        assertEquals(result.getPagesMarkedCompleted().intValue(), 1);

    }

    private DocumentCaptureData buildDocDataFromChars(String uuid, List<String> chars) {
        var documentCaptureData = new DocumentCaptureData(uuid);
        for (var c : chars) {
            var capData = new CharacterCaptureData();
            capData.setUuid(UUID.randomUUID().toString());
            capData.setCaptureDataRecordType(CaptureDataRecordType.CREATE);
            var labeledRectangle = new LabeledRectangle();
            labeledRectangle.setLabel(c);
            capData.setLabeledRectangle(labeledRectangle);
            documentCaptureData.insertCharacterCaptureData(capData);
        }
        return documentCaptureData;
    }
}