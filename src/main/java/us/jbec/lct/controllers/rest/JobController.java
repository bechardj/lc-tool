package us.jbec.lct.controllers.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import us.jbec.lct.models.ImageJob;
import us.jbec.lct.models.LCToolException;
import us.jbec.lct.models.LCToolResponse;
import us.jbec.lct.models.capture.CaptureDataPayload;
import us.jbec.lct.models.capture.DocumentCaptureData;
import us.jbec.lct.services.CloudCaptureDocumentService;
import us.jbec.lct.transformers.DocumentCaptureDataTransformer;
import us.jbec.lct.transformers.ImageJobTransformer;
import us.jbec.lct.util.LCToolUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller for interacting with image jobs
 */
@RestController
public class JobController {

    Logger LOG = LoggerFactory.getLogger(JobController.class);

    private final CloudCaptureDocumentService cloudCaptureDocumentService;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Controller for interacting with image jobs
     * @param cloudCaptureDocumentService autowired parameter
     */
    public JobController(CloudCaptureDocumentService cloudCaptureDocumentService) {
        this.cloudCaptureDocumentService = cloudCaptureDocumentService;
    }

    private Bucket syncAfterDisconnectBucket() {
        return Bucket4j.builder()
                .addLimit(Bandwidth.simple(10, Duration.ofSeconds(30)))
                .build();
    }

    /**
     * Retrieves legacy ImageJob by uuid
     * @param uuid uuid of DocumentCaptureData to retrieve
     * @return retrieved DocumentCaptureData
     */
    @GetMapping(value = "/getJob")
    public @ResponseBody ImageJob getJob(@RequestParam String uuid) throws JsonProcessingException {
        LOG.info("Received request for image job: {}, conversion required", uuid);
        return DocumentCaptureDataTransformer.apply(cloudCaptureDocumentService.getDocumentCaptureDataByUuidRaw(uuid));
    }

    /**
     * Retrieves DocumentCaptureData by uuid
     * @param uuid uuid of DocumentCaptureData to retrieve
     * @return retrieved DocumentCaptureData
     */
    @GetMapping(value = "/getDoc")
    public @ResponseBody DocumentCaptureData getDoc(@RequestParam String uuid) throws JsonProcessingException {
        LOG.info("Received request for job: {}", uuid);
        try {
            return DocumentCaptureData.flatten(cloudCaptureDocumentService.getDocumentCaptureDataByUuidRaw(uuid));
        } catch (Exception e) {
            LOG.error("An error occurred while getting image job!", e);
            throw e;
        }
    }

    /**
     * Saves DocumentCaptureData
     * @param authentication auth object
     * @param documentCaptureData DocumentCaptureData to save
     * @return LCToolResponse object
     * @throws IOException
     */
    @PostMapping(value = "/sec/api/saveDoc", consumes= { "application/json" })
    public LCToolResponse saveDoc(Authentication authentication, @RequestBody DocumentCaptureData documentCaptureData) throws IOException {
        var user = LCToolUtils.getUserFromAuthentication(authentication);
        cloudCaptureDocumentService.saveDocumentCaptureData(documentCaptureData, user.getFirebaseIdentifier(), true);
        return new LCToolResponse(false, "Saved!");
    }

    /**
     * Syncs DocumentCaptureData state between backend and client, broadcasting newly integrated changes
     * to other users when present. Rate-limited by user to 10 sync requests per 30 seconds
     * @param authentication authentication object
     * @param documentCaptureData DocumentCaptureData from client to sync
     * @param originSession client session-id (used to prevent client from trying to re-sync again)
     * @return List of CaptureDataPayloads containing CaptureData the client should integrate to be in sync
     */
    @PostMapping(value="/sec/api/captureData/sync")
    public List<CaptureDataPayload> syncAfterDisconnect(Authentication authentication,
                                                        @RequestBody DocumentCaptureData documentCaptureData,
                                                        @RequestParam String originSession) {
        var user = LCToolUtils.getUserFromAuthentication(authentication);
        Bucket userBucket = this.buckets.computeIfAbsent(user.getFirebaseIdentifier(), (uuid) -> syncAfterDisconnectBucket());
        if (userBucket.tryConsume(1)) {
            int mergeCount = cloudCaptureDocumentService.saveDocumentCaptureData(documentCaptureData, user.getFirebaseIdentifier(), true);
            var payloads = cloudCaptureDocumentService.buildPayloadsToSyncClient(documentCaptureData);
            if (mergeCount > 0) {
                // TODO: ultimately, we should do this re-sync using payloads probably. This method at least guarantees eventual consistency
                //  at the risk of out running the rate limiter.
                LOG.info("Client changes were merged in. Requesting all clients sync.");
                cloudCaptureDocumentService.requestClientSync(documentCaptureData.getUuid(), originSession);
            }
            return payloads;
        } else {
            throw new LCToolException("Rate limit exceeded!");
        }
    }
}
