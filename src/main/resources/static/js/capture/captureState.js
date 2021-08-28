import {Line, Rectangle} from "./geometry.js";

const CaptureModes = {
    LETTER: 'Letter',
    WORD: 'Word',
    LINE: 'Line',
    ERASER: 'Eraser',
    DISABLED: 'Disabled'
}

class CaptureState {
    document;
    constructor() {
        this.drawing = false;

        this.deletedUuids = new Set();

        this.renderableCharacterRectangles = new Map();
        this.deletedCharacterRectangles = new Map();
        this.characterCaptureRectangleQueue = [];
        this.characterCaptureRectangleRedoQueue = [];
        this.characterCaptureRectangleErasedQueue = [];

        this.renderableWordLines = new Map();
        this.deletedWordLines = new Map();
        this.wordLinesQueue = [];
        this.wordLinesRedoQueue = [];
        this.wordLinesErasedQueue = [];


        this.renderableLineLines = new Map();
        this.deletedLineLines = new Map();
        this.lineLinesQueue = [];
        this.lineLinesRedoQueue = [];
        this.lineLinesErasedQueue = [];

        this.characterRectangles = [];
        this.characterLabels = [];

        this.wordLines = [];
        this.redoQueueWordLines = [];

        this.lineLines = [];
        this.redoQueueLineLines = [];

        this.erasedCharacterRectangles = [];
        this.erasedWordLines = [];
        this.erasedLineLines = [];

        this.notes = "";

        this.captureMode = CaptureModes.WORD;
        this.previousCaptureMode = undefined;

        this.jobInfo = undefined;
    }

    lastIsLabeled() {
        if (this.characterCaptureRectangleQueue.length === 0) {
            return true;
        } else {
            const uuid = this.characterCaptureRectangleQueue[this.characterCaptureRectangleQueue.length - 1];
            const r = this.renderableCharacterRectangles.get(uuid);
            return r === undefined || r.labeledRectangle.label !== undefined;
        }
    }

    clearEraserQueues() {
        this.characterCaptureRectangleErasedQueue = [];
        this.erasedWordLines = [];
        this.erasedLineLines = [];
    }

    undo(callback) {
        if (!this.drawing) {
            if (this.captureMode === CaptureModes.LETTER && this.characterCaptureRectangleQueue.length > 0) {
                let undoneUuid = this.characterCaptureRectangleQueue[this.characterCaptureRectangleQueue.length-1];
                let rect = this.renderableCharacterRectangles.get(undoneUuid);
                this.deleteRectangle(rect);
                this.characterCaptureRectangleRedoQueue.push(undoneUuid);
            } else if (this.captureMode === CaptureModes.WORD && this.wordLinesQueue.length > 0) {
                let undoneUuid = this.wordLinesQueue[this.wordLinesQueue.length-1];
                let line = this.renderableWordLines.get(undoneUuid);
                this.deleteWordLine(line);
                this.wordLinesRedoQueue.push(undoneUuid);
            } else if (this.captureMode === CaptureModes.LINE && this.lineLines.length > 0) {
                let undoneUuid = this.lineLines[this.lineLines.length-1];
                let line = this.renderableLineLines.get(undoneUuid);
                this.deleteLineLine(line);
                this.lineLinesRedoQueue.push(undoneUuid);
            } else if (this.captureMode === CaptureModes.ERASER) {
                this.characterCaptureRectangleErasedQueue.forEach(uuid => {
                    this.reviveRect(uuid);
                })
                this.wordLines = this.wordLines.concat(this.erasedWordLines);
                this.lineLines = this.lineLines.concat(this.erasedLineLines);
                this.clearEraserQueues();
            }
            callback.call();
        }
    }

    reviveRect(uuid) {
        let revivedRect = this.deletedCharacterRectangles.get(uuid);
        if (revivedRect !== undefined) {
            let rect = new Rectangle();
            rect.initFromExisting(revivedRect);
            this.commitRectangle(rect);
        } else {
            console.error("Error state: revived rect did not exist");
        }
    }

    reviveWordLine(uuid) {
        let revivedLine = this.deletedWordLines.get(uuid);
        if (revivedLine !== undefined) {
            let line = new Line();
            line.initFromExisting(revivedLine);
            this.commitWordLine(line);
        } else {
            console.error("Error state: revived word line did not exist");
        }
    }

    reviveLineLine(uuid) {
        let revivedLine = this.deletedLineLines.get(uuid);
        if (revivedLine !== undefined) {
            let line = new Line();
            line.initFromExisting(revivedLine);
            this.commitLineLine(line);
        } else {
            console.error("Error state: revived line line did not exist");
        }
    }

    redo(callback) {
        if (!this.drawing) {
            if (this.captureMode === CaptureModes.LETTER && this.characterCaptureRectangleRedoQueue.length > 0) {
                let redoneUuid = this.characterCaptureRectangleRedoQueue.pop();
                this.reviveRect(redoneUuid);
            } else if (this.captureMode === CaptureModes.WORD && this.wordLinesRedoQueue.length > 0) {
                let redoneUuid = this.wordLinesRedoQueue.pop();
                this.reviveWordLine(redoneUuid);
            } else if (this.captureMode === CaptureModes.LINE && this.lineLinesRedoQueue.length > 0) {
                let redoneUuid = this.lineLinesRedoQueue.pop();
                this.reviveLineLine(redoneUuid);
            }
            callback.call();
        }
    }

    clean() {
        let removedBadRectangle = false;
        // for (let i = 0; i < this.characterRectangles.length; i++) {
        //     let r = this.characterRectangles[i];
        //     if (Math.abs(r.width) < 10 || Math.abs(r.height) < 10) {
        //         console.log("Removing bad rectangle");
        //         this.characterRectangles.splice(i, 1);
        //         this.characterLabels.splice(i, 1);
        //         removedBadRectangle = true;
        //     }
        // }
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
            this.stagedCharacterRectangle = new Rectangle([eventX, eventY, 0, 0]);
            this.characterCaptureRectangleRedoQueue = [];
        }
        if (this.captureMode === CaptureModes.WORD) {
            this.drawing = true;
            this.stagedWordLine = new Line([eventX, eventY, eventX, eventY]);
            this.wordLinesRedoQueue = [];
        }
        if (this.captureMode === CaptureModes.LINE) {
            this.drawing = true;
            this.stagedLineLine = new Line([eventX, eventY, eventX, eventY]);
            this.lineLinesRedoQueue = [];
        }
        if (this.captureMode === CaptureModes.ERASER) {
            this.drawing = true;
        }
    }

    stopDrawing() {
        this.drawing = false;
        let wasCommitted = false;
        if(this.stagedCharacterRectangle !== undefined) {
            if (Math.abs(this.stagedCharacterRectangle.labeledRectangle.width) > 10
                && Math.abs(this.stagedCharacterRectangle.labeledRectangle.height) > 10) {
                this.commitRectangle(this.stagedCharacterRectangle);
                wasCommitted = true;
            } else {
                console.warn("Not Committing Rectangle because it is too small.");
            }
        }
        if(this.stagedWordLine !== undefined) {
            if (this.stagedWordLine.length() > 10) {
                this.commitWordLine(this.stagedWordLine);
                wasCommitted = true;
            } else {
                console.warn("Not Committing Word Line because it is too small.");
            }
        }
        if(this.stagedLineLine !== undefined) {
            if (this.stagedLineLine.length() > 10) {
                this.commitLineLine(this.stagedLineLine);
                wasCommitted = true;
            } else {
                console.warn("Not Committing Word Line because it is too small.");
            }
        }

        this.stagedCharacterRectangle = undefined;
        this.stagedLineLine = undefined;
        this.stagedWordLine = undefined;
        return wasCommitted;
    }
    
    updateDocumentInfo() {
        this.document.completed = $('#completed')[0].checked;
        this.document.edited = true;
        this.document.notes = $('#notes')[0].value;
    }

    generateJobInfoJson() {
        this.updateDocumentInfo();
        return JSON.stringify(this.document);
    }

    commitRectangle(rect) {
        this.renderableCharacterRectangles.set(rect.uuid, rect);
        this.characterCaptureRectangleQueue.push(rect.uuid);

        let createRecord = new Rectangle(rect.asArray);
        createRecord.uuid = rect.uuid;

        this.document.characterCaptureDataList.push(createRecord);
    }

    commitWordLine(line) {
        this.renderableWordLines.set(line.uuid, line);
        this.wordLinesQueue.push(line.uuid);

        let createRecord = new Line(line.asArray);
        createRecord.uuid = line.uuid;

        this.document.wordCaptureDataList.push(createRecord);
    }

    commitLineLine(line) {
        this.renderableLineLines.set(line.uuid, line);
        this.lineLinesQueue.push(line.uuid);

        let createRecord = new Line(line.asArray);
        createRecord.uuid = line.uuid;

        this.document.lineCaptureDataList.push(createRecord);
    }

    reverseSearchArrayForUuid(array, uuid) {
        console.log(array, uuid);
        let i;
        for(i = array.length - 1; i>= 0; i--) {
            if(array[i] === uuid) {
                break;
            }
        }
        // needed if the object was not found because it wasn't created by the user & was loaded on init
        if (array[i] === uuid) {
            array.splice(i, 1);
        }
    }

    deleteRectangle(rect) {
        this.renderableCharacterRectangles.delete(rect.uuid);
        this.deletedCharacterRectangles.set(rect.uuid, rect);
        this.reverseSearchArrayForUuid(this.characterCaptureRectangleQueue, rect.uuid);
        let deletedRectangle = new Rectangle(rect.asArray);
        deletedRectangle.uuid = rect.uuid;
        deletedRectangle.captureDataRecordType = "DELETE";

        let deleteRecord = new Rectangle();
        deleteRecord.uuid = rect.uuid;
        deleteRecord.captureDataRecordType = "DELETE";

        this.document.characterCaptureDataList.push(deleteRecord);
    }

    eraseRectangle(rect) {
        this.characterCaptureRectangleErasedQueue.push(rect.uuid);
        this.deleteRectangle(rect);
    }

    deleteLineLine(line) {
        this.renderableLineLines.delete(line.uuid);
        this.deletedLineLines.set(line.uuid, line);
        this.reverseSearchArrayForUuid(this.lineLinesQueue, line.uuid);
        let deletedLine = new Line(line.asArray);
        deletedLine.uuid = line.uuid;
        deletedLine.captureDataRecordType = "DELETE";

        let deleteLine = new Line();
        deleteLine.uuid = line.uuid;
        deleteLine.captureDataRecordType = "DELETE";

        this.document.lineCaptureDataList.push(deleteLine);
    }

    eraseWordLine(line) {
        this.wordLinesErasedQueue.push(line.uuid);
        this.deleteWordLine(line);
    }


    deleteWordLine(line) {
        this.renderableWordLines.delete(line.uuid);
        this.deletedWordLines.set(line.uuid, line);
        this.reverseSearchArrayForUuid(this.wordLinesQueue, line.uuid);
        let deletedLine = new Line(line.asArray);
        deletedLine.uuid = line.uuid;
        deletedLine.captureDataRecordType = "DELETE";

        let deleteLine = new Line();
        deleteLine.uuid = line.uuid;
        deleteLine.captureDataRecordType = "DELETE";

        this.document.wordCaptureDataList.push(deleteLine);
    }

    eraseLineLine(line) {
        this.lineLinesErasedQueue.push(line.uuid);
        this.deleteLineLine(line);
    }

    updateDrawnRectangleText(uuid, text) {
        let rect = this.renderableCharacterRectangles.get(uuid);
        this.deleteRectangle(rect);
        let redraw = new Rectangle(rect.asArray);
        redraw.labeledRectangle.label = text;
        this.commitRectangle(redraw);
    }

    eligibleForPredictions(rectangle) {
        let existingRect = this.renderableCharacterRectangles.get(rectangle.uuid);
        return !this.drawing && existingRect !== undefined && existingRect.label === undefined
            && !this.lastIsLabeled();
    }
}

export {CaptureModes, CaptureState};