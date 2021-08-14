import { Rectangle, Line } from './geometry.js';

function loadJob(jobId, state) {
    return new Promise((resolve, reject) => {
        $.getJSON("/getJob",
            {
                uuid: jobId
            },
            function (response) {
                let jobInfo = response;
                if (jobInfo.characterRectangles !== null) {
                    state.characterRectangles = Rectangle.convertFromArrayOfPoints(jobInfo.characterRectangles);
                }
                if (jobInfo.characterLabels !== null) {
                    state.characterLabels = jobInfo.characterLabels;
                }
                if (jobInfo.wordLines !== null) {
                    state.wordLines = Line.convertFromArrayOfPoints(jobInfo.wordLines);
                }
                if (jobInfo.lineLines !== null) {
                    state.lineLines = Line.convertFromArrayOfPoints(jobInfo.lineLines);
                }
                if (jobInfo.fields.NOTES !== null && jobInfo.fields.NOTES !== undefined) {
                    state.notes = jobInfo.fields.NOTES;
                }
                state.jobInfo = jobInfo;
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