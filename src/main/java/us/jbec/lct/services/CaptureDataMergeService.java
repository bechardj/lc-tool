package us.jbec.lct.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import us.jbec.lct.models.capture.CaptureData;
import us.jbec.lct.models.capture.CaptureDataPayload;
import us.jbec.lct.models.capture.CaptureDataRecordType;
import us.jbec.lct.models.capture.DocumentCaptureData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class CaptureDataMergeService {

    Logger LOG = LoggerFactory.getLogger(CaptureDataMergeService.class);

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

    private <T extends CaptureData> void insertForSave(Map<String, List<T>> existingDataMap, Map<String, List<T>> newDataMap, Consumer<T> insert) {
        if (newDataMap != null) {
            for (var entry : newDataMap.entrySet()) {
                for(var dataRecord : entry.getValue()) {
                    var targetList = existingDataMap.get(entry.getKey());
                    if (shouldIntegrateCaptureData(targetList, dataRecord)) {
                        insert.accept(dataRecord);
                    }
                }
            }
        }
    }

    protected void mergeCaptureData(DocumentCaptureData existingData, DocumentCaptureData newData) {
        insertForSave(newData.getCharacterCaptureDataMap(), existingData.getCharacterCaptureDataMap(), existingData::insertCharacterCaptureData);
        insertForSave(newData.getLineCaptureDataMap(), existingData.getLineCaptureDataMap(), existingData::insertLineCaptureData);
        insertForSave(newData.getWordCaptureDataMap(), existingData.getWordCaptureDataMap(), existingData::insertWordCaptureData);

    }

    protected void mergePayloadIntoDocument(CaptureDataPayload payload, DocumentCaptureData documentCaptureData) {
        if (payload.getCharacterCaptureData() != null) {
            var characterCaptureData = payload.getCharacterCaptureData();
            var targetList = documentCaptureData.getCharacterCaptureDataMap().get(characterCaptureData.getUuid());
            if (shouldIntegrateCaptureData(targetList, characterCaptureData)) {
                documentCaptureData.insertCharacterCaptureData(characterCaptureData);
            } else {
                LOG.error("Try to integrate data twice");
            }
        }
        if (payload.getWordCaptureData() != null) {
            var wordCaptureData = payload.getWordCaptureData();
            var targetList = documentCaptureData.getWordCaptureDataMap().get(wordCaptureData.getUuid());
            if (shouldIntegrateCaptureData(targetList, wordCaptureData)) {
                documentCaptureData.insertWordCaptureData(wordCaptureData);
            } else {
                LOG.error("Try to integrate data twice");
            }
        }
        if (payload.getLineCaptureData() != null) {
            var lineCaptureData = payload.getLineCaptureData();
            var targetList = documentCaptureData.getLineCaptureDataMap().get(lineCaptureData.getUuid());
            if (shouldIntegrateCaptureData(targetList, lineCaptureData)) {
                documentCaptureData.insertLineCaptureData(lineCaptureData);
            } else {
                LOG.error("Try to integrate data twice");
            }
        }
    }

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
}
