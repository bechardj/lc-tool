import { Rectangle, Line} from './geometry.js';

function loadJob(jobId, state) {
    return new Promise((resolve, reject) => {
        $.getJSON("/getDoc",
            {
                uuid: jobId
            },
            function (response) {
                // todo is this already set?
                state.documentUuid = response.uuid;
                state.document = response;

                // TODO: rectify deleted records, ie, object might want to contain deleted records?
                for (const key in response.characterCaptureDataMap) {
                    let rect = new Rectangle();
                    let data = response.characterCaptureDataMap[key][0];
                    Object.assign(rect, data);
                    state.renderableCharacterRectangles.set(data.uuid, rect);
                }

                for (const key in response.wordCaptureDataMap) {
                    let line = new Line();
                    let data = response.wordCaptureDataMap[key][0];
                    Object.assign(line, data);
                    state.renderableWordLines.set(data.uuid, line);
                }

                for (const key in response.lineCaptureDataMap) {
                    let line = new Line();
                    let data = response.lineCaptureDataMap[key][0];
                    Object.assign(line, data);
                    state.renderableLineLines.set(data.uuid, line);
                }

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