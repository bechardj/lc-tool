package us.jbec.lct.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.capture.DocumentCaptureData;
import us.jbec.lct.models.database.CloudCaptureDocument;
import us.jbec.lct.repositories.ArchivedJobDataRepository;
import us.jbec.lct.repositories.CloudCaptureDocumentRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class CloudCaptureDocumentServiceTest {

    @Mock
    private final CloudCaptureDocumentRepository cloudCaptureDocumentRepository = Mockito.mock(CloudCaptureDocumentRepository.class);
    private final ArchivedJobDataRepository archivedJobDataRepository = Mockito.mock(ArchivedJobDataRepository.class);
    private final CaptureDataMergeService captureDataMergeService = Mockito.mock(CaptureDataMergeService.class);
    private final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
    private final UserService userService = Mockito.mock(UserService.class);
    private final SimpMessagingTemplate messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);

    private final CloudCaptureDocumentService testee = new CloudCaptureDocumentService(cloudCaptureDocumentRepository,
            archivedJobDataRepository,
            captureDataMergeService,
            objectMapper,
            userService,
            messagingTemplate);

    @BeforeSuite
    public void setup() {
        MockitoAnnotations.openMocks(testee);
    }

    @BeforeMethod
    public void reset() {
        Mockito.reset(cloudCaptureDocumentRepository, archivedJobDataRepository, captureDataMergeService, objectMapper, userService, messagingTemplate);
    }

    @Test
    public void testSaveFailsOnAuthFailure() {
        var data = new DocumentCaptureData("doc");

        var document = new CloudCaptureDocument();
        document.setUuid("doc");
        document.setDocumentCaptureData(data);

        when(cloudCaptureDocumentRepository.selectDocumentForUpdate("doc"))
                .thenReturn(document);
        when(cloudCaptureDocumentRepository.canSaveCloudCaptureDocument(any(), any()))
                .thenReturn(0);


        try {
            testee.saveDocumentCaptureData(data, "user", true);
            fail();
        } catch (LCToolException e) {

            verify(cloudCaptureDocumentRepository, Mockito.times(1))
                    .canSaveCloudCaptureDocument("user", "doc");
            verify(captureDataMergeService, Mockito.times(0))
                    .mergeCaptureData(any(), any());
        }

    }

}