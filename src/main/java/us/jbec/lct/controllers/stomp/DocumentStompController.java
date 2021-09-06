package us.jbec.lct.controllers.stomp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.capture.CaptureDataPayload;
import us.jbec.lct.services.CloudCaptureDocumentService;
import us.jbec.lct.util.LCToolUtils;

import javax.validation.Valid;

@Controller
public class DocumentStompController {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentStompController.class);

    private CloudCaptureDocumentService cloudCaptureDocumentService;

    public DocumentStompController(CloudCaptureDocumentService cloudCaptureDocumentService) {
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
    }

    @MessageMapping("/document/{docUuid}")
    @SendTo("/topic/document/{docUuid}")
    public CaptureDataPayload getMessages(Message<?> message,
                                          @DestinationVariable String docUuid,
                                          @Valid CaptureDataPayload captureDataPayload) {
        var user = LCToolUtils.getUserFromMessage(message);
        if (user != null && cloudCaptureDocumentService.userCanSaveDocument(user.getFirebaseIdentifier(), docUuid)) {
            cloudCaptureDocumentService.saveCaptureData(captureDataPayload, docUuid);
            return captureDataPayload;
        } else {
            if (user == null) {
                LOG.error("User was null");
            } else {
                LOG.error("User {} not authorized to save document {}", user.getFirebaseIdentifier(), docUuid);
            }
            throw new LCToolException("Origin User Not Authorized to Submit Changes");
        }
    }

}
