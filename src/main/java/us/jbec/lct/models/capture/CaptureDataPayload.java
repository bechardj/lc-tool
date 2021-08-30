package us.jbec.lct.models.capture;

import com.fasterxml.jackson.annotation.JsonInclude;
import us.jbec.lct.validators.CaptureDataPayloadConstraint;

import javax.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
@CaptureDataPayloadConstraint
public class CaptureDataPayload {

    @NotBlank
    private String originator;
    private CharacterCaptureData characterCaptureData;
    private LineCaptureData lineCaptureData;
    private WordCaptureData wordCaptureData;

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public CharacterCaptureData getCharacterCaptureData() {
        return characterCaptureData;
    }

    public void setCharacterCaptureData(CharacterCaptureData characterCaptureData) {
        this.characterCaptureData = characterCaptureData;
    }

    public LineCaptureData getLineCaptureData() {
        return lineCaptureData;
    }

    public void setLineCaptureData(LineCaptureData lineCaptureData) {
        this.lineCaptureData = lineCaptureData;
    }

    public WordCaptureData getWordCaptureData() {
        return wordCaptureData;
    }

    public void setWordCaptureData(WordCaptureData wordCaptureData) {
        this.wordCaptureData = wordCaptureData;
    }
}
