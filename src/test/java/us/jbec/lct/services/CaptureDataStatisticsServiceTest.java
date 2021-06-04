package us.jbec.lct.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.database.CloudCaptureDocument;
import us.jbec.lct.models.database.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class CaptureDataStatisticsServiceTest {

    @InjectMocks
    private CaptureDataStatisticsService testee;

    @Mock
    private CloudCaptureDocumentService documentService;

    @BeforeSuite
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCalculateAllStatistics() throws JsonProcessingException {

        Map<CloudCaptureDocument, ImageJob> input = new HashMap<>();

        List<ImageJob> imageJobs = new ArrayList<>();

        // First User
        User user1 = new User();
        user1.setFirebaseEmail("test@user1.com");

        var doc1 = new CloudCaptureDocument();
        doc1.setOwner(user1);
        var job1 = new ImageJob();
        job1.setEdited(true);
        job1.setCharacterLabels(List.of("a", "B", "c", "a", "1", ".", " ", "other"));

        input.put(doc1, job1);

        var doc2 = new CloudCaptureDocument();
        doc2.setOwner(user1);
        var job2 = new ImageJob();
        job2.setEdited(false);
        job2.setCharacterLabels(new ArrayList<>());

        input.put(doc2, job2);

        Mockito.when(documentService.getActiveCloudCaptureDocuments()).thenReturn(input);

        var result = testee.calculateAllStatistics();

        Mockito.verify(documentService, Mockito.times(1)).getActiveCloudCaptureDocuments();

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
    public void testCalculateStatistics_singleUser() {

        Map<CloudCaptureDocument, ImageJob> input = new HashMap<>();

        List<ImageJob> imageJobs = new ArrayList<>();

        // First User
        User user1 = new User();
        user1.setFirebaseEmail("test@user1.com");

        var doc1 = new CloudCaptureDocument();
        doc1.setOwner(user1);
        var job1 = new ImageJob();
        job1.setEdited(true);
        job1.setCharacterLabels(List.of("a", "B", "c", "a", "1", ".", " ", "other"));

        input.put(doc1, job1);

        var doc2 = new CloudCaptureDocument();
        doc2.setOwner(user1);
        var job2 = new ImageJob();
        job2.setEdited(false);
        job2.setCharacterLabels(new ArrayList<>());

        input.put(doc2, job2);

        var result = testee.calculateStatistics(input);

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
    public void testCalculateStatistics_multipleUsers() {

        Map<CloudCaptureDocument, ImageJob> input = new HashMap<>();

        List<ImageJob> imageJobs = new ArrayList<>();

        // First User
        User user1 = new User();
        user1.setFirebaseEmail("test@user1.com");

        var doc1 = new CloudCaptureDocument();
        doc1.setOwner(user1);
        var job1 = new ImageJob();
        job1.setEdited(true);
        job1.setCharacterLabels(List.of("a", "B", "c", "a", "1", ".", " ", "other"));

        input.put(doc1, job1);

        var doc2 = new CloudCaptureDocument();
        doc2.setOwner(user1);
        var job2 = new ImageJob();
        job2.setEdited(true);
        job2.setCompleted(true);
        job2.setCharacterLabels(List.of("x", "b", "-", "Z"));

        input.put(doc2, job2);

        // Second User
        User user2 = new User();
        user2.setFirebaseEmail("test@user2.com");

        var doc3 = new CloudCaptureDocument();
        doc3.setOwner(user2);
        var job3 = new ImageJob();
        job3.setEdited(true);
        job3.setCharacterLabels(List.of("l", "m", "n", "O"));

        input.put(doc3, job3);

        var result = testee.calculateStatistics(input);

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
}