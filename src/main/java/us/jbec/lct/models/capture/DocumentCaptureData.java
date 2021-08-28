package us.jbec.lct.models.capture;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DocumentCaptureData {
    private String uuid;
    private String notes;
    private boolean completed;
    private boolean edited;

    private List<CharacterCaptureData> characterCaptureDataList;
    private List<WordCaptureData> wordCaptureDataList;
    private List<LineCaptureData> lineCaptureDataList;

    public DocumentCaptureData(@JsonProperty("uuid") String uuid) {
        this.uuid = uuid;
        completed = false;
        edited = false;
        notes = "";
        characterCaptureDataList = new ArrayList<>();
        wordCaptureDataList = new ArrayList<>();
        lineCaptureDataList = new ArrayList<>();
    }

    public static DocumentCaptureData flatten(DocumentCaptureData source, String uuid) {
        DocumentCaptureData target = new DocumentCaptureData(uuid);
        target.setCompleted(source.isCompleted());
        target.setEdited(source.isEdited());
        target.setNotes(source.getNotes());

        Set<String> deletedUuids = Stream.of(source.getCharacterCaptureDataList(),
                        source.getLineCaptureDataList(),
                        source.getWordCaptureDataList())
                .flatMap(Collection::stream)
                .filter(captureData -> CaptureDataRecordType.DELETE.equals(captureData.getCaptureDataRecordType()))
                .map(CaptureData::getUuid)
                .collect(Collectors.toSet());

        target.setCharacterCaptureDataList(source.getCharacterCaptureDataList()
                .stream().filter(data -> !deletedUuids.contains(data.getUuid())).map(CharacterCaptureData::new).toList());

        target.setWordCaptureDataList(source.getWordCaptureDataList()
                .stream().filter(data -> !deletedUuids.contains(data.getUuid())).map(WordCaptureData::new).toList());

        target.setLineCaptureDataList(source.getLineCaptureDataList()
                .stream().filter(data -> !deletedUuids.contains(data.getUuid())).map(LineCaptureData::new).toList());

        return target;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNotes() {
        return Jsoup.clean(notes, Safelist.basic());
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public List<CharacterCaptureData> getCharacterCaptureDataList() {
        if (characterCaptureDataList == null) {
            characterCaptureDataList = new ArrayList<>();
        }
        return characterCaptureDataList;
    }

    public void setCharacterCaptureDataList(List<CharacterCaptureData> characterCaptureDataList) {
        this.characterCaptureDataList = characterCaptureDataList;
    }

    public List<WordCaptureData> getWordCaptureDataList() {
        if (wordCaptureDataList == null) {
            wordCaptureDataList = new ArrayList<>();
        }
        return wordCaptureDataList;
    }

    public void setWordCaptureDataList(List<WordCaptureData> wordCaptureDataList) {
        this.wordCaptureDataList = wordCaptureDataList;
    }

    public List<LineCaptureData> getLineCaptureDataList() {
        if (lineCaptureDataList == null) {
            lineCaptureDataList = new ArrayList<>();
        }
        return lineCaptureDataList;
    }

    public void setLineCaptureDataList(List<LineCaptureData> lineCaptureDataList) {
        this.lineCaptureDataList = lineCaptureDataList;
    }
}
