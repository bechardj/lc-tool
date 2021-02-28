package us.jbec.lyrasis.models;

import java.util.HashMap;
import java.util.List;

public class ImageJob {
    private String version;
    private String id;
    private String status;
    private List<List<Double>> characterRectangles;
    private List<String> characterLabels;
    private List<List<Double>> wordLines;
    private List<List<Double>> lineLines;
    private boolean completed;
    private boolean edited;
    private HashMap<String, Object> fields;

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getCharacterLabels() {
        return characterLabels;
    }

    public void setCharacterLabels(List<String> characterLabels) {
        this.characterLabels = characterLabels;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<List<Double>> getCharacterRectangles() {
        return characterRectangles;
    }

    public void setCharacterRectangles(List<List<Double>> characterRectangles) {
        this.characterRectangles = characterRectangles;
    }


    public List<List<Double>> getWordLines() {
        return wordLines;
    }

    public void setWordLines(List<List<Double>> wordLines) {
        this.wordLines = wordLines;
    }

    public List<List<Double>> getLineLines() {
        return lineLines;
    }

    public void setLineLines(List<List<Double>> lineLines) {
        this.lineLines = lineLines;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public HashMap<String, Object> getFields() {
        if (null == fields) {
            fields = new HashMap<String, Object>();
        }
        return fields;
    }

    public void setFields(HashMap<String, Object> fields) {
        this.fields = fields;
    }
}
