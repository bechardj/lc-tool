package us.jbec.lct.transformers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.geometry.LabeledRectangle;
import us.jbec.lct.models.geometry.LineSegment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.*;

public class ImageJobTransformerTest {

    List<ImageJob> imageJobs;

    private final DocumentCaptureDataTransformer documentCaptureDataTransformer = new DocumentCaptureDataTransformer();

    @BeforeSuite
    private void loadTestData() throws IOException {
        imageJobs = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        for(int i = 1; i <= 10; i++) {
            var fileName = String.format("/ImageJobTransformerTest/job%d.json", i);
            var job = ResourceUtils.getFile(this.getClass().getResource(fileName));
            imageJobs.add(objectMapper.readValue(job, ImageJob.class));
        }
    }

    @Test
    public void testBasicFields() {
        ImageJob imageJob = new ImageJob();
        imageJob.setId("id");
        imageJob.setCompleted(true);
        imageJob.setEdited(true);
        Map<String, String> fields = new HashMap<>();
        fields.put("NOTES", "note");
        imageJob.setFields(fields);

        var result = ImageJobTransformer.apply(imageJob);

        assertNotNull(result);
        assertEquals(result.getUuid(), "id");
        assertTrue(result.isCompleted());
        assertTrue(result.isEdited());
        assertEquals(result.getNotes(), "note");

        assertTrue(result.getCharacterCaptureDataMap().isEmpty());
        assertTrue(result.getLineCaptureDataMap().isEmpty());
        assertTrue(result.getWordCaptureDataMap().isEmpty());
    }

    @Test
    public void testCharacterCaptureRectangles() {
        ImageJob imageJob = new ImageJob();
        imageJob.setId("id");

        List<List<Double>> captureRectangles = Arrays.asList(
                Arrays.asList(1.1,2.1,3.1,4.1),
                Arrays.asList(1.2,2.2,3.2,4.2),
                Arrays.asList(1.3,2.3,3.3,4.3)
        );

        List<String> labels = Arrays.asList("a", "b", "c");

        imageJob.setCharacterRectangles(captureRectangles);
        imageJob.setCharacterLabels(labels);

        var result = ImageJobTransformer.apply(imageJob);
        assertNotNull(result);

        assertEquals(result.getCharacterCaptureDataMap().size(), 3);
        Set<String> uuids = result.getCharacterCaptureDataMap().keySet();
        assertEquals(uuids.size(), 3);

        result.getCharacterCaptureDataMap().values().stream()
                .map(entryList -> entryList.get(0).getLabeledRectangle())
                .forEach(labeledRectangle -> {
                    assertTrue(imageJob.getLabeledRectangles().contains(labeledRectangle));
                });
    }

    @Test
    public void testWordLines() {
        ImageJob imageJob = new ImageJob();
        imageJob.setId("id");

        List<List<Double>> wordLines = Arrays.asList(
                Arrays.asList(1.1,2.1,3.1,4.1),
                Arrays.asList(1.2,2.2,3.2,4.2),
                Arrays.asList(1.3,2.3,3.3,4.3)
        );


        imageJob.setWordLines(wordLines);

        var result = ImageJobTransformer.apply(imageJob);
        assertNotNull(result);

        assertEquals(result.getWordCaptureDataMap().size(), 3);
        Set<String> uuids = result.getWordCaptureDataMap().keySet();
        assertEquals(uuids.size(), 3);

        result.getWordCaptureDataMap().values().stream()
                .map(entryList -> entryList.get(0).getLineSegment())
                .forEach(lineSegment -> {
                    assertTrue(imageJob.getWordLineSegments().contains(lineSegment));
                });
    }

    @Test
    public void testLineLines() {
        ImageJob imageJob = new ImageJob();
        imageJob.setId("id");

        List<List<Double>> lineLines = Arrays.asList(
                Arrays.asList(1.1,2.1,3.1,4.1),
                Arrays.asList(1.2,2.2,3.2,4.2),
                Arrays.asList(1.3,2.3,3.3,4.3)
        );


        imageJob.setLineLines(lineLines);

        var result = ImageJobTransformer.apply(imageJob);
        assertNotNull(result);

        assertEquals(result.getLineCaptureDataMap().size(), 3);
        Set<String> uuids = result.getLineCaptureDataMap().keySet();
        assertEquals(uuids.size(), 3);

        result.getLineCaptureDataMap().values()
                .stream()
                .map(entryList -> entryList.get(0).getLineSegment())
                .forEach(lineSegment -> {
                    assertTrue(imageJob.getLineLineSegments().contains(lineSegment));
                });
    }


    @Test
    public void testRoundTrip() throws CloneNotSupportedException {
        assertEquals(imageJobs.size(), 10);
        for(ImageJob imageJob : imageJobs) {

            var documentCaptureData = ImageJobTransformer.apply(imageJob);
            var convertedBack = documentCaptureDataTransformer.apply(documentCaptureData);

            assertEquals(imageJob.getId(), convertedBack.getId());
            assertEquals(imageJob.isCompleted(), convertedBack.isCompleted());
            assertEquals(imageJob.isEdited(), convertedBack.isEdited());

            if (imageJob.getFields().containsKey("NOTES")) {
                assertEquals(imageJob.getFields().get("NOTES"), convertedBack.getFields().get("NOTES"));
            }

            assertEquals(imageJob.getCharacterRectangles().size(), convertedBack.getCharacterRectangles().size());
            assertEquals(imageJob.getCharacterLabels().size(), convertedBack.getCharacterLabels().size());
            assertEquals(imageJob.getWordLines().size(), convertedBack.getWordLines().size());
            assertEquals(imageJob.getLineLines().size(), convertedBack.getLineLines().size());

            for(LabeledRectangle labeledRectangle : imageJob.getLabeledRectangles()) {
                assertTrue(convertedBack.getLabeledRectangles().contains(labeledRectangle));
            }

            for(LineSegment lineSegment : imageJob.getLineLineSegments()) {
                assertTrue(convertedBack.getLineLineSegments().contains(lineSegment));
            }

            for(LineSegment lineSegment : imageJob.getWordLineSegments()) {
                assertTrue(convertedBack.getWordLineSegments().contains(lineSegment));
            }
        }
    }
}