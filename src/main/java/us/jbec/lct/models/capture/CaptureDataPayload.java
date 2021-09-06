package us.jbec.lct.models.capture;

import com.fasterxml.jackson.annotation.JsonInclude;
import us.jbec.lct.models.LCToolException;
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
    private Boolean requestCompleteSync;

    public CaptureDataPayload () {

    }

    public CaptureDataPayload (CaptureData captureData, String originator) {
        this.originator = originator;
        if (captureData instanceof CharacterCaptureData) {
            this.characterCaptureData = new CharacterCaptureData((CharacterCaptureData) captureData);
        } else if (captureData instanceof LineCaptureData) {
            this.lineCaptureData = new LineCaptureData((LineCaptureData) captureData);
        } else if (captureData instanceof WordCaptureData) {
            this.wordCaptureData = new WordCaptureData((WordCaptureData) captureData);
        } else {
            throw new LCToolException("Invalid Argument!");
        }
    }

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

    public Boolean getRequestCompleteSync() {
        return requestCompleteSync;
    }

    public void setRequestCompleteSync(Boolean requestCompleteSync) {
        this.requestCompleteSync = requestCompleteSync;
    }
}
