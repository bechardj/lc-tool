package us.jbec.lct.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import us.jbec.lct.models.capture.CaptureData;
import us.jbec.lct.models.capture.CaptureDataPayload;
import us.jbec.lct.models.capture.CaptureDataRecordType;
import us.jbec.lct.models.capture.DocumentCaptureData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Merges CaptureData into existing DocumentCaptureData
 */
@Service
public class CaptureDataMergeService {

    Logger LOG = LoggerFactory.getLogger(CaptureDataMergeService.class);

    /**
     * Determine whether or not CaptureData should be added to a list of CaptureData.
     * Currently checks that the list is either null or empty, or that the change to integrate
     * is a delete record and the existing data is a create record
     *
     * @param targetList list to check if CaptureData should be integrated into
     * @param dataToIntegrate data to integrate
     * @param <T> CaptureData type
     * @return should the capture data be integrated
     */
    private <T extends CaptureData> boolean shouldIntegrateCaptureData(List<T> targetList, T dataToIntegrate) {
        if (targetList == null || targetList.isEmpty()) {
            return true;
        }
        if (targetList.size() == 1
                && targetList.get(0).getCaptureDataRecordType() == CaptureDataRecordType.CREATE
                && dataToIntegrate.getCaptureDataRecordType() != CaptureDataRecordType.DELETE) {
            return false;
        }
        return targetList.size() <= 1;
    }

    /**
     * For a given CaptureData type, insert records from the CaptureData map corresponding to incoming CaptureData
     * when the data should be integrated.
     * @param existingDataMap existing CaptureData Map
     * @param newDataMap new CaptureData Map with potential changes we need to merge into the existing CaptureData
     * @param insert consumer that accepts the CaptureData we are inserting
     * @param <T> CaptureData type
     * @return count of CaptureData inserted
     */
    private <T extends CaptureData> int insertForSave(Map<String, List<T>> existingDataMap, Map<String, List<T>> newDataMap, Consumer<T> insert) {
        int insertCount = 0;
        if (newDataMap != null) {
            for (var entry : newDataMap.entrySet()) {
                for(var dataRecord : entry.getValue()) {
                    var targetList = existingDataMap.get(entry.getKey());
                    if (shouldIntegrateCaptureData(targetList, dataRecord)) {
                        insertCount++;
                        insert.accept(dataRecord);
                    }
                }
            }
        }
        return insertCount;
    }

    /**
     * Merge incoming capture data into existing capture data
     * @param existingData existing backend capture data
     * @param newData incoming capture data to merge into existing capture data
     * @return count of CaptureData changes merged in
     */
    @Transactional(propagation = Propagation.REQUIRED)
    protected int mergeCaptureData(DocumentCaptureData existingData, DocumentCaptureData newData) {
        int mergeCount = 0;
        mergeCount += insertForSave(existingData.getCharacterCaptureDataMap(), newData.getCharacterCaptureDataMap(), existingData::insertCharacterCaptureData);
        mergeCount += insertForSave(existingData.getLineCaptureDataMap(), newData.getLineCaptureDataMap(), existingData::insertLineCaptureData);
        mergeCount += insertForSave(existingData.getWordCaptureDataMap(), newData.getWordCaptureDataMap(), existingData::insertWordCaptureData);
        return mergeCount;
    }

    /**
     * Merge a single payload into existing backend CaptureData if it should be merged
     * @param existingData existing CaptureData
     * @param payload payload to merge
     */
    @Transactional(propagation = Propagation.REQUIRED)
    protected void mergePayloadIntoDocument(DocumentCaptureData existingData, CaptureDataPayload payload) {
        if (payload.getCharacterCaptureData() != null) {
            var characterCaptureData = payload.getCharacterCaptureData();
            var targetList = existingData.getCharacterCaptureDataMap().get(characterCaptureData.getUuid());
            if (shouldIntegrateCaptureData(targetList, characterCaptureData)) {
                existingData.insertCharacterCaptureData(characterCaptureData);
            } else {
                LOG.error("Try to integrate data twice");
            }
        }
        if (payload.getWordCaptureData() != null) {
            var wordCaptureData = payload.getWordCaptureData();
            var targetList = existingData.getWordCaptureDataMap().get(wordCaptureData.getUuid());
            if (shouldIntegrateCaptureData(targetList, wordCaptureData)) {
                existingData.insertWordCaptureData(wordCaptureData);
            } else {
                LOG.error("Try to integrate data twice");
            }
        }
        if (payload.getLineCaptureData() != null) {
            var lineCaptureData = payload.getLineCaptureData();
            var targetList = existingData.getLineCaptureDataMap().get(lineCaptureData.getUuid());
            if (shouldIntegrateCaptureData(targetList, lineCaptureData)) {
                existingData.insertLineCaptureData(lineCaptureData);
            } else {
                LOG.error("Try to integrate data twice");
            }
        }
    }


    /**
     * Creates list of CaptureDataPayload objects to send to client to synchronize their state with the backend after a disconnect
     * @param serverData existing server data
     * @param clientData client data requiring sync
     * @return list of CaptureDataPayload objects to send to client to synchronize their state
     */
    public List<CaptureDataPayload> createPayloadsForSync(DocumentCaptureData serverData, DocumentCaptureData clientData) {
        var flattenedServerData = DocumentCaptureData.flatten(serverData);
        var flattenedClientData = DocumentCaptureData.flatten(clientData);
        List<CaptureDataPayload> payloads = new ArrayList<>();

        payloads.addAll(buildCreationSyncPayloads(flattenedServerData, flattenedClientData, DocumentCaptureData::getCharacterCaptureDataMap));
        payloads.addAll(buildCreationSyncPayloads(flattenedServerData, flattenedClientData, DocumentCaptureData::getLineCaptureDataMap));
        payloads.addAll(buildCreationSyncPayloads(flattenedServerData, flattenedClientData, DocumentCaptureData::getWordCaptureDataMap));

        payloads.addAll(buildDeletionSyncPayloads(serverData, flattenedServerData, flattenedClientData, DocumentCaptureData::getCharacterCaptureDataMap));
        payloads.addAll(buildDeletionSyncPayloads(serverData, flattenedServerData, flattenedClientData, DocumentCaptureData::getLineCaptureDataMap));
        payloads.addAll(buildDeletionSyncPayloads(serverData, flattenedServerData, flattenedClientData, DocumentCaptureData::getWordCaptureDataMap));

        return payloads;
    }

    /**
     * Identify server side CaptureData that needs to be sent to the client for creating new records
     * @param flattenedServerData flattened server side DocumentCaptureData
     * @param flattenedClientData flattened client side DocumentCaptureData
     * @param mapExtractor function to extract CaptureData Map of type T from DocumentCaptureData
     * @param <T> CaptureData Type
     * @return list of CaptureData payloads containing creation records to send to client
     */
    protected  <T extends CaptureData> List<CaptureDataPayload> buildCreationSyncPayloads(DocumentCaptureData flattenedServerData,
                                                                                          DocumentCaptureData flattenedClientData,
                                                                                          Function<DocumentCaptureData, Map<String, List<T>>> mapExtractor) {
        var flattenedServerMap = mapExtractor.apply(flattenedServerData);
        var flattenedClientMap = mapExtractor.apply(flattenedClientData);
        List<CaptureDataPayload> captureDataPayloads = new ArrayList<>();
        for (var uuid : flattenedServerMap.keySet()) {
            if (!flattenedClientMap.containsKey(uuid)) {
                flattenedServerMap.get(uuid).stream()
                        .filter(record -> record.getCaptureDataRecordType() == CaptureDataRecordType.CREATE)
                        .map(record -> new CaptureDataPayload(record, "BACKEND"))
                        .findFirst()
                        .ifPresent(captureDataPayloads::add);
            }
        }
        return captureDataPayloads;
    }

    /**
     * Identify server side CaptureData that needs to be sent to the client for deleting client records
     * @param serverData unflattened server side DocumentCaptureData
     * @param flattenedServerData flattened server side DocumentCaptureData
     * @param flattenedClientData flattened client side DocumentCaptureData
     * @param mapExtractor function to extract CaptureData Map of type T from DocumentCaptureData
     * @param <T> CaptureData Type
     * @return list of CaptureData payloads containing deletion records to send to client
     */
    protected <T extends CaptureData> List<CaptureDataPayload> buildDeletionSyncPayloads(DocumentCaptureData serverData,
                                                                                         DocumentCaptureData flattenedServerData,
                                                                                         DocumentCaptureData flattenedClientData,
                                                                                         Function<DocumentCaptureData, Map<String, List<T>>> mapExtractor) {
        var serverMap = mapExtractor.apply(serverData);
        var flattenedServerMap = mapExtractor.apply(flattenedServerData);
        var flattenedClientMap = mapExtractor.apply(flattenedClientData);
        List<CaptureDataPayload> captureDataPayloads = new ArrayList<>();
        for (var uuid : flattenedClientMap.keySet()) {
            if (!flattenedServerMap.containsKey(uuid)) {
                serverMap.get(uuid).stream()
                        .filter(record -> record.getCaptureDataRecordType() == CaptureDataRecordType.DELETE)
                        .map(record -> new CaptureDataPayload(record, "BACKEND"))
                        .findFirst()
                        .ifPresent(captureDataPayloads::add);
            }
        }
        return captureDataPayloads;
    }
}
