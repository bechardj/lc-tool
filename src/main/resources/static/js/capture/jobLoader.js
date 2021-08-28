import { Rectangle, Line } from './geometry.js';

function loadJob(jobId, state) {
    return new Promise((resolve, reject) => {
        $.getJSON("/getJob",
            {
                uuid: jobId
            },
            function (response) {
                // todo is this already set?
                state.documentUuid = response.uuid;
                state.document = response;
                let deletedUuids = new Set();
                [... response.characterCaptureDataList, ...response.lineCaptureDataList, ...response.wordCaptureDataList].forEach(data => {
                    if (data.captureDataRecordType === "DELETE") {
                        deletedUuids.add(data.uuid);
                    }
                })

                // TODO: rectify deleted records, ie, object might want to contain deleted records?
                response.characterCaptureDataList.forEach(data => {
                    if (!deletedUuids.has(data.uuid)) {
                        let rect = new Rectangle();
                        Object.assign(rect, data)
                        state.renderableCharacterRectangles.set(data.uuid, rect);
                    }
                });

                response.wordCaptureDataList.forEach(data => {
                    if (!deletedUuids.has(data.uuid)) {
                        let line = new Line();
                        Object.assign(line, data);
                        state.renderableWordLines.set(data.uuid, line);
                    }
                });

                response.lineCaptureDataList.forEach(data => {
                    if (!deletedUuids.has(data.uuid)) {
                        let line = new Line();
                        Object.assign(line, data);
                        state.renderableLineLines.set(data.uuid, line);
                    }
                });

                // if (jobInfo.characterRectangles !== null) {
                //     state.characterRectangles = Rectangle.convertFromArrayOfPoints(jobInfo.characterRectangles);
                // }
                // if (jobInfo.characterLabels !== null) {
                //     state.characterLabels = jobInfo.characterLabels;
                // }
                // if (jobInfo.wordLines !== null) {
                //     state.wordLines = Line.convertFromArrayOfPoints(jobInfo.wordLines);
                // }
                // if (jobInfo.lineLines !== null) {
                //     state.lineLines = Line.convertFromArrayOfPoints(jobInfo.lineLines);
                // }
                // if (jobInfo.fields.NOTES !== null && jobInfo.fields.NOTES !== undefined) {
                //     state.notes = jobInfo.fields.NOTES;
                // }
                // state.jobInfo = jobInfo;
                resolve();
            });
    });
}

function loadImage(jobId) {
    return new Promise((resolve, reject) => {
        let image = new Image();
        image.src = '/image?uuid='  + jobId;
        image.onload = () => resolve(image)
        image.onerror = () => {
            reject(new Error("Failed to retrieve image"));
        }
    })
}

export {loadImage, loadJob};