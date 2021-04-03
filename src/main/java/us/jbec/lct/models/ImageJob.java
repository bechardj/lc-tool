package us.jbec.lct.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import us.jbec.lct.models.geometry.LabeledRectangle;
import us.jbec.lct.models.geometry.LineSegment;
import us.jbec.lct.models.geometry.OffsetRectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
        if (characterLabels == null) {
            characterRectangles = new ArrayList<>();
        }
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
        if (characterLabels == null) {
            characterRectangles = new ArrayList<>();
        }
        return characterRectangles;

    }

    @JsonIgnore
    public List<LabeledRectangle> getLabeledRectangles() {
        var characterRectangles = getCharacterRectangles();
        var characterLabels = getCharacterLabels();
        List<LabeledRectangle> labeledRectangles = new ArrayList<>();

        for(int i = 0; i < getCharacterRectangles().size(); i++) {
            labeledRectangles.add(new LabeledRectangle(characterRectangles.get(i), characterLabels.get(i)));

        }
        return labeledRectangles;
    }

    public void setCharacterRectangles(List<List<Double>> characterRectangles) {
        this.characterRectangles = characterRectangles;
    }


    public List<List<Double>> getWordLines() {
        if (wordLines == null) {
            wordLines = new ArrayList<>();
        }
        return wordLines;
    }

    @JsonIgnore
    public List<LineSegment> getWordLineSegments() {
        return getWordLines().stream()
                .map(LineSegment::new)
                .toList();
    }

    public void setWordLines(List<List<Double>> wordLines) {
        this.wordLines = wordLines;
    }

    public List<List<Double>> getLineLines() {
        if (lineLines == null) {
            lineLines = new ArrayList<>();
        }
        return lineLines;
    }

    @JsonIgnore
    public List<LineSegment> getLineLineSegments() {
        return getLineLines().stream()
                .map(LineSegment::new)
                .toList();
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
