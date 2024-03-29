package us.jbec.lct.controllers.stomp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import us.jbec.lct.models.DocumentSaveEvent;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.capture.CaptureDataPayload;
import us.jbec.lct.services.CloudCaptureDocumentService;
import us.jbec.lct.util.LCToolUtils;

import javax.validation.Valid;

/**
 * Controller for handling STOMP websockets for client-side sync state
 */
@Controller
public class DocumentStompController {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentStompController.class);

    private final CloudCaptureDocumentService cloudCaptureDocumentService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public DocumentStompController(CloudCaptureDocumentService cloudCaptureDocumentService,
                                   ApplicationEventPublisher applicationEventPublisher) {
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Consumes new CaptureDataPayloads from clients and broadcasts to other client sessions
     * @param message message containing auth info
     * @param docUuid uuid of document for which the payload is directed
     * @param captureDataPayload payload data to integrate
     * @return Payload to broadcast to clients
     */
    @MessageMapping("/document/{docUuid}")
    @SendTo("/topic/document/{docUuid}")
    public CaptureDataPayload getMessages(Message<?> message,
                                          @DestinationVariable String docUuid,
                                          @Valid CaptureDataPayload captureDataPayload) {
        var user = LCToolUtils.getUserFromMessage(message);
        if (user == null) {
            throw new LCToolException("User was null!");
        }
        LOG.info("Request to process payload for document {} by user {}", docUuid, user.getFirebaseEmail());
        cloudCaptureDocumentService.saveCaptureDataPayload(captureDataPayload, docUuid, user.getFirebaseIdentifier());
        applicationEventPublisher.publishEvent(new DocumentSaveEvent(docUuid));
        return captureDataPayload;
    }

}
