package us.jbec.lct.controllers.stomp;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import us.jbec.lct.models.capture.CaptureDataPayload;
import us.jbec.lct.services.CloudCaptureDocumentService;

import javax.validation.Valid;

@Controller
public class DocumentStompController {

    private CloudCaptureDocumentService cloudCaptureDocumentService;

    public DocumentStompController(CloudCaptureDocumentService cloudCaptureDocumentService) {
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
    }

    @MessageMapping("/document/{uuid}")
    @SendTo("/topic/document/{uuid}")
    public CaptureDataPayload getMessages(Message<?> message,
                                          @DestinationVariable String uuid,
                                          @Valid CaptureDataPayload captureDataPayload) throws JsonProcessingException {
        cloudCaptureDocumentService.integrateChangesIntoDocument(captureDataPayload, uuid);
        return captureDataPayload;
    }

}
