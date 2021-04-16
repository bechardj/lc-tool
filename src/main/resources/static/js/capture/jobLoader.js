import { Rectangle, Line } from './geometry.js';

function loadJob(jobId, state, callback) {
    $.getJSON("/getJob",
        {
            id: jobId
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
            state.jobInfo = jobInfo;
            callback.call();
        });
}

function loadImage(jobId, image) {
    let request = $.get("/getImage", {id: jobId})
    request.done(function (data) {
        // there is likely a better way to do this, this was just the quickest
        image.src = 'data:image/png;base64,' + data;
        console.log("Retrieved Image From Server");
    });

    request.fail(function (XMLHttpRequest, textStatus, errorThrown) {
        alert(errorThrown);
    });
}

export {loadImage, loadJob};