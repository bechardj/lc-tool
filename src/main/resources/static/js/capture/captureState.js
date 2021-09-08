import {Line, Rectangle} from "./geometry.js";

const CaptureModes = {
    LETTER: 'Letter',
    WORD: 'Word',
    LINE: 'Line',
    ERASER: 'Eraser',
    DISABLED: 'Disabled'
}

const DataType = {
    LETTER: 'Letter',
    WORD: 'Word',
    LINE: 'Line'
}

class CaptureDataPayload {
    originator;
    characterCaptureData;
    lineCaptureData;
    wordCaptureData;

    constructor() {
    }
}

class CaptureState {

    document;
    drawCallback;

    constructor() {
        this.drawing = false;
        this.session = UUID.genV4().hexString;

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

        this.captureMode = CaptureModes.WORD;
        this.previousCaptureMode = undefined;

        this.connected = false;

        // this is setup in the capture.html. If not editable, don't bother sending changes to the backend
        this.sync = backendInfo.editable && backendInfo.syncOptIn;
    }

    getNotes() {
        return this.document.notes;
    }

    setNotes(notes) {
        if (notes !== undefined) {
            this.document.notes = notes;
        }
    }

    publishData(data, dataType) {
        if (this.sync) {
            let payload = new CaptureDataPayload();
            payload.originator = this.session;
            if (dataType === DataType.LETTER) {
                payload.characterCaptureData = data;
            } else if (dataType === DataType.WORD) {
                payload.wordCaptureData = data;
            } else if (dataType === DataType.LINE) {
                payload.lineCaptureData = data;
            }
            this.client.send('/app/document/' + this.document.uuid, {}, JSON.stringify(payload));
        }
    }

    processPayload(payload) {
        if (payload.originator !== undefined && payload.originator !== this.session) {
            if (payload.characterCaptureData !== undefined) {
                let rect = new Rectangle();
                Object.assign(rect, payload.characterCaptureData);
                if (payload.characterCaptureData.captureDataRecordType === "CREATE" && !this.renderableWordLines.has(payload.characterCaptureData.uuid)) {
                    rect.labeledRectangle.label = rect.labeledRectangle.label === null ? undefined : rect.labeledRectangle.label;
                    if (this.document.characterCaptureDataMap[payload.characterCaptureData.uuid] === undefined) {
                        this.renderableCharacterRectangles.set(rect.uuid, rect);
                    }
                    let createRecord = new Rectangle(rect.asArray);
                    createRecord.uuid = rect.uuid;
                    this.safeAddToMap(this.document.characterCaptureDataMap, createRecord);
                } else {
                    this.renderableCharacterRectangles.delete(rect.uuid);

                    let deleteRecord = new Rectangle();
                    deleteRecord.uuid = rect.uuid;
                    deleteRecord.captureDataRecordType = "DELETE";

                    this.safeAddToMap(this.document.characterCaptureDataMap, deleteRecord);
                }
            } else if (payload.wordCaptureData !== undefined) {
                let line = new Line();
                Object.assign(line, payload.wordCaptureData);
                if (payload.wordCaptureData.captureDataRecordType === "CREATE" && !this.renderableWordLines.has(payload.wordCaptureData.uuid)) {
                    if (this.document.wordCaptureDataMap[payload.wordCaptureData.uuid] === undefined) {
                        this.renderableWordLines.set(line.uuid, line);
                    }
                    let createRecord = new Line(line.asArray);
                    createRecord.uuid = line.uuid;
                    this.safeAddToMap(this.document.wordCaptureDataMap, createRecord);
                } else {
                    this.renderableWordLines.delete(line.uuid);

                    let deleteLine = new Line();
                    deleteLine.uuid = line.uuid;
                    deleteLine.captureDataRecordType = "DELETE";

                    this.safeAddToMap(this.document.wordCaptureDataMap, deleteLine);
                }
            } else if (payload.lineCaptureData !== undefined) {
                let line = new Line();
                Object.assign(line, payload.lineCaptureData);
                if (payload.lineCaptureData.captureDataRecordType === "CREATE" && !this.renderableLineLines.has(payload.lineCaptureData.uuid)) {
                    if (this.document.lineCaptureDataMap[payload.lineCaptureData.uuid] === undefined) {
                        this.renderableLineLines.set(line.uuid, line);
                    }
                    let createRecord = new Line(line.asArray);
                    createRecord.uuid = line.uuid;
                    this.safeAddToMap(this.document.lineCaptureDataMap, createRecord);
                } else {
                    this.renderableLineLines.delete(line.uuid);

                    let deleteLine = new Line();
                    deleteLine.uuid = line.uuid;
                    deleteLine.captureDataRecordType = "DELETE";
                    this.safeAddToMap(this.document.lineCaptureDataMap, deleteLine);
                }
            } else if (payload.requestCompleteSync) {
                console.log("Doing complete sync");
                this.doCompleteSync();
            }
            this.drawCallback();
        }
    }

    reconnectState() {
        if (this.connected !== true)
        {
            setTimeout(() => {
                this.reconnectStateDelegate();
            }, 1000)
        }
    }

    doCompleteSync() {
        firebaseModal().then((token) => {
            fetch('/sec/api/captureData/sync?originSession=' + this.session, {
                method: 'post',
                headers: {
                    'Accept': 'application/json, text/plain, */*',
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify(this.document)
            }).then(response => response.json())
                .then(payloads => {
                    payloads.forEach(payload => this.processPayload(payload));
                });
        });
    }

    async reconnectStateDelegate() {
        if (this.client !== undefined) {
            this.client.disconnect();
        }
        let token = await firebaseModal();
        await this.connectState(this.document.uuid, token);
        this.doCompleteSync();
    }

    connectState(jobId, token) {
        return new Promise((resolve, reject) => {
            if (!backendInfo.syncOptIn) {
                console.log("Synchronized Editing Disabled.")
                resolve();
                return;
            }
            let sock = new SockJS("/stomp");
            this.client = Stomp.over(sock);
            this.client.debug = f => f;
            const headers = {'token': token};
            const callback = () => {
                this.client.subscribe(('/topic/document/' + jobId),
                    (data) => {
                        this.processPayload(JSON.parse(data.body));
                    });
                this.connected = true;
                $("#connection-status-container").hide();
                this.client.ws.onclose = () => {
                    console.warn('on close called');
                    $("#connection-status-container").show();
                    this.connected = false;
                    this.reconnectState();
                };
                resolve();
            }
            this.client.connect(
                headers,
                callback,
                () => {console.error("Error opening websocket"); this.reconnectState();}
            );
        });
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
        this.wordLinesErasedQueue = [];
        this.lineLinesErasedQueue = [];
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
            } else if (this.captureMode === CaptureModes.LINE && this.lineLinesQueue.length > 0) {
                let undoneUuid = this.lineLinesQueue[this.lineLinesQueue.length-1];
                let line = this.renderableLineLines.get(undoneUuid);
                this.deleteLineLine(line);
                this.lineLinesRedoQueue.push(undoneUuid);
            } else if (this.captureMode === CaptureModes.ERASER) {
                this.characterCaptureRectangleErasedQueue.forEach(uuid => {
                    this.reviveRect(uuid);
                })
                this.wordLinesErasedQueue.forEach(uuid => {
                    this.reviveWordLine(uuid);
                })
                this.lineLinesErasedQueue.forEach(uuid => {
                    this.reviveLineLine(uuid);
                })
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
        createRecord.labeledRectangle.label = rect.labeledRectangle.label;
        createRecord.uuid = rect.uuid;

        this.safeAddToMap(this.document.characterCaptureDataMap, createRecord);
        this.publishData(createRecord, DataType.LETTER);
    }

    commitWordLine(line) {
        this.renderableWordLines.set(line.uuid, line);
        this.wordLinesQueue.push(line.uuid);

        let createRecord = new Line(line.asArray);
        createRecord.uuid = line.uuid;

        this.safeAddToMap(this.document.wordCaptureDataMap, createRecord);
        this.publishData(createRecord, DataType.WORD);
    }

    commitLineLine(line) {
        this.renderableLineLines.set(line.uuid, line);
        this.lineLinesQueue.push(line.uuid);

        let createRecord = new Line(line.asArray);
        createRecord.uuid = line.uuid;

        this.safeAddToMap(this.document.lineCaptureDataMap, createRecord);
        this.publishData(createRecord, DataType.LINE);
    }

    reverseSearchArrayForUuid(array, uuid) {
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

        this.safeAddToMap(this.document.characterCaptureDataMap, deleteRecord);
        this.publishData(deleteRecord, DataType.LETTER);
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

        this.safeAddToMap(this.document.lineCaptureDataMap, deletedLine);
        this.publishData(deletedLine, DataType.LINE);
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

        this.safeAddToMap(this.document.wordCaptureDataMap, deletedLine);
        this.publishData(deletedLine, DataType.WORD)
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

    safeAddToMap(map, element) {
        if (element.uuid === undefined) {
            throw "Try to add element to map with undefined uuid";
        }
        if (map[element.uuid] === undefined) {
            map[element.uuid] = [];
        }
        map[element.uuid].push(element);
    }
}

export {CaptureModes, CaptureState};