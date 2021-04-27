import {Line, Rectangle} from "./geometry.js";

const CaptureModes = {
    LETTER: 'Letter',
    WORD: 'Word',
    LINE: 'Line',
    ERASER: 'Eraser',
    DISABLED: 'Disabled'
}

class CaptureState {
    constructor() {
        this.drawing = false;

        this.characterRectangles = [];
        this.characterLabels = [];
        this.redoQueueCharacterRectangles = [];
        this.redoQueueCharacterLabels = [];

        this.wordLines = [];
        this.redoQueueWordLines = [];

        this.lineLines = [];
        this.redoQueueLineLines = [];

        this.erasedCharacterLabels = [];
        this.erasedCharacterRectangles = [];
        this.erasedWordLines = [];
        this.erasedLineLines = [];

        this.notes = "";

        this.captureMode = CaptureModes.WORD;
        this.previousCaptureMode = undefined;

        this.jobInfo = undefined;
    }

    lastIsLabeled() {
        return this.characterLabels.length === 0 || this.characterLabels[this.characterLabels.length - 1] !== undefined;
    }

    clearEraserQueues() {
        this.erasedCharacterLabels = [];
        this.erasedCharacterRectangles = [];
        this.erasedWordLines = [];
        this.erasedLineLines = [];
    }

    undo(callback) {
        if (!this.drawing) {
            if (this.captureMode === CaptureModes.LETTER && this.characterRectangles.length > 0) {
                this.redoQueueCharacterRectangles.push(this.characterRectangles.pop());
                this.redoQueueCharacterLabels.push(this.characterLabels.pop());
            } else if (this.captureMode === CaptureModes.WORD && this.wordLines.length > 0) {
                this.redoQueueWordLines.push(this.wordLines.pop());
            } else if (this.captureMode === CaptureModes.LINE && this.lineLines.length > 0) {
                this.redoQueueLineLines.push(this.lineLines.pop());
            } else if (this.captureMode === CaptureModes.ERASER) {
                this.characterLabels = this.characterLabels.concat(this.erasedCharacterLabels);
                this.characterRectangles = this.characterRectangles.concat(this.erasedCharacterRectangles);
                this.wordLines = this.wordLines.concat(this.erasedWordLines);
                this.lineLines = this.lineLines.concat(this.erasedLineLines);
                this.clearEraserQueues();
            }
            callback.call();
        }
    }

    redo(callback) {
        if (!this.drawing) {
            if (this.captureMode === CaptureModes.LETTER && this.redoQueueCharacterRectangles.length > 0) {
                this.characterRectangles.push(this.redoQueueCharacterRectangles.pop());
                this.characterLabels.push(this.redoQueueCharacterLabels.pop());
            } else if (this.captureMode === CaptureModes.WORD && this.redoQueueWordLines.length > 0) {
                this.wordLines.push(this.redoQueueWordLines.pop());
            } else if (this.captureMode === CaptureModes.LINE && this.redoQueueLineLines.length > 0) {
                this.lineLines.push(this.redoQueueLineLines.pop());
            }
            callback.call();
        }
    }

    clean() {
        let removedBadRectangle = false;
        for (let i = 0; i < this.characterRectangles.length; i++) {
            let r = this.characterRectangles[i];
            if (Math.abs(r.width) < 10 || Math.abs(r.height) < 10) {
                console.log("Removing bad rectangle");
                this.characterRectangles.splice(i, 1);
                this.characterLabels.splice(i, 1);
                removedBadRectangle = true;
            }

        }
        // consider lines less than 10 px as erroneous
        for(const lines of Array.of(this.wordLines, this.lineLines)) {
            for (let j = 0; j < lines.length; j++) {
                let line = lines[j];
                if (line.length() < 10) {
                    console.log("Removing bad line");
                    lines.splice(j, 1);
                }
            }
        }
        return removedBadRectangle;
    }
    
    startDrawing(eventX, eventY) {
        if (this.captureMode === CaptureModes.LETTER && this.lastIsLabeled()) {
            this.drawing = true;
            this.characterRectangles.push(new Rectangle([eventX, eventY, 0, 0]));
            this.characterLabels.push(undefined);
            this.redoQueueCharacterLabels = [];
            this.redoQueueCharacterRectangles = [];
        }
        if (this.captureMode === CaptureModes.WORD) {
            this.drawing = true;
            this.wordLines.push(new Line([eventX, eventY, eventX, eventY]));
            this.redoQueueWordLines = [];
        }
        if (this.captureMode === CaptureModes.LINE) {
            this.drawing = true;
            this.lineLines.push(new Line([eventX, eventY, eventX, eventY]));
            this.redoQueueLineLines = [];
        }
        if (this.captureMode === CaptureModes.ERASER) {
            this.drawing = true;
        }
    }

    stopDrawing() {
        this.drawing = false;
    }
    
    updateJobInfo() {
        this.jobInfo.characterRectangles = Rectangle.convertFromArrayOfRectangles(this.characterRectangles);
        this.jobInfo.characterLabels = this.characterLabels;
        this.jobInfo.wordLines = Line.convertFromArrayOfLines(this.wordLines);
        this.jobInfo.lineLines = Line.convertFromArrayOfLines(this.lineLines);
        this.jobInfo.completed = $('#completed')[0].checked;
        this.jobInfo.edited = true;
        this.jobInfo.fields.NOTES = $('#notes')[0].value;
        if (this.jobInfo.completed) {
            this.jobInfo.status = "Completed";
        } else {
            this.jobInfo.status = "Edited";
        }
    }

    generateJobInfoJson() {
        this.updateJobInfo();
        return JSON.stringify(this.jobInfo);
    }

    eligibleForPredictions(index, rectangle) {
        return !this.drawing && this.characterRectangles.length - 1 === index
            && this.characterRectangles[index] === rectangle
            && !this.lastIsLabeled();
    }
}

export {CaptureModes, CaptureState};