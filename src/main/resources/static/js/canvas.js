/**
 * Do I think it was a good idea to put everything in a monolithic JS file? No.
 * For the sake of readability, there's a lot of project restructuring & organization
 * that could be done to enhance this code. There is also bad-practice here as well.
 *
 * The intent was to develop a prototype ASAP. Feel free to contribute, just make sure you
 * don't break existing behavior/functionality.
 *
 */

console.log("Hello!");

const canvas = document.getElementById("mainCanvas");
const ctx = canvas.getContext("2d");

let drawing = false;
let background;

let characterRectangles = [];
let characterLabels = [];
let redoQueueCharacterRectangles = [];
let redoQueueCharacterLabels = [];

let wordLines = [];
let redoQueueWordLines = [];

let lineLines = [];
let redoQueueLineLines = [];

let jobId;
let jobInfo;

let captureMode;
let fontSize;

const CaptureModes = {
    LETTER: 'Letter',
    WORD: 'Word',
    LINE: 'Line',
    ERASER: 'Eraser'
}

function lastIsLabeled () {
    return characterLabels.length === 0 || characterLabels[characterLabels.length-1] !== undefined;
}

// Click Functions

function clickDown(event) {
    if (!drawing) {
        const x = event.pageX - canvas.offsetLeft;
        const y = event.pageY - canvas.offsetTop;
        if(captureMode === CaptureModes.LETTER && lastIsLabeled()) {
            drawing = true;
            characterRectangles.push([x, y, 0, 0]);
            characterLabels.push(undefined);
            redoQueueCharacterLabels = [];
            redoQueueCharacterRectangles = [];
        }
        if(captureMode === CaptureModes.WORD) {
            drawing = true;
            wordLines.push([x, y, x, y]);
            redoQueueWordLines = [];
        }
        if(captureMode === CaptureModes.LINE) {
            drawing = true;
            lineLines.push([x, y, x, y]);
            redoQueueLineLines = [];
        }
        if(captureMode === CaptureModes.ERASER) {
            drawing = true;
        }
    }
}

function clickUp(event) {
    drawing = false;
    clean();
    draw();
}

function dragHandler(event) {
    if (drawing) {
        if (captureMode === CaptureModes.LETTER && characterRectangles.length > 0 && drawing) {
            const lastDrawn = characterRectangles.pop();
            const lastLabel = characterLabels.pop();
            lastDrawn[2] = event.pageX - canvas.offsetLeft - lastDrawn[0];
            lastDrawn[3] = event.pageY - canvas.offsetTop - lastDrawn[1];
            characterRectangles.push(lastDrawn);
            characterLabels.push(lastLabel);
            draw();
        } else if (captureMode === CaptureModes.WORD && wordLines.length > 0) {
            const lastDrawn = wordLines.pop();
            lastDrawn[2] = event.pageX - canvas.offsetLeft;
            lastDrawn[3] = event.pageY - canvas.offsetTop;
            wordLines.push(lastDrawn);
            draw();
        } else if (captureMode === CaptureModes.LINE && lineLines.length > 0) {
            const lastDrawn = lineLines.pop();
            lastDrawn[2] = event.pageX - canvas.offsetLeft;
            lastDrawn[3] = event.pageY - canvas.offsetTop;
            lineLines.push(lastDrawn);
            draw();
        } else if (captureMode === CaptureModes.ERASER) {
            let mouseX = event.pageX- canvas.offsetLeft
            let mouseY = event.pageY - canvas.offsetTop
            for (let i = 0; i < characterRectangles.length; i++) {
                let r = characterRectangles[i];
                if ((r[0] < mouseX && mouseX < r[0] + r[2])
                    && (r[1] < mouseY &&mouseY < r[1] + r[3])) {
                    console.log(r, event.pageX- canvas.offsetLeft, event.pageY - canvas.offsetTop);
                    characterRectangles.splice(i, 1);
                    characterLabels.splice(i, 1);
                    draw();
                }
            }
            const slope = function (x1, y1, x2, y2) {
                return (y2 - y1) / ((x2 - x1) !== 0 ? (x2 - x1) : 0.000001);
            };
            // TODO: cleanup duplicated code here, also this approach does not work well for vertical lines
            for (let j = 0; j < wordLines.length; j++) {
                let w = wordLines[j];
                let eps = 0.5;
                let line_slope = Math.abs(slope(w[0],w[1],w[2],w[3]));
                let slope_start = Math.abs(slope(w[0], w[1], mouseX, mouseY));
                let slope_end = Math.abs(slope(w[2], w[3], mouseX, mouseY));
                if ((Math.abs(line_slope - slope_start) < eps) &&
                    (Math.abs(line_slope - slope_end) < eps) &&
                    (Math.min(w[0], w[2]) <= mouseX && mouseX <= Math.max(w[0], w[2])) &&
                    (Math.min(w[1], w[3]) <= mouseY && mouseY <= Math.max(w[1], w[3])))
                    {
                    wordLines.splice(j, 1);
                }
            }
            for (let l = 0; l < lineLines.length; l++) {
                let k = lineLines[l];
                let eps = 0.5;
                let line_slope = Math.abs(slope(k[0],k[1],k[2],k[3]));
                let slope_start = Math.abs(slope(k[0], k[1], mouseX, mouseY));
                let slope_end = Math.abs(slope(k[2], k[3], mouseX, mouseY));
                if ((Math.abs(line_slope - slope_start) < eps) &&
                    (Math.abs(line_slope - slope_end) < eps) &&
                    (Math.min(k[0], k[2]) <= mouseX && mouseX <= Math.max(k[0], k[2])) &&
                    (Math.min(k[1], k[3]) <= mouseY && mouseY <= Math.max(k[1], k[3])))
                {
                    lineLines.splice(k, 1);
                }
            }

        }
    }
}

/**
 * Work could be done here to improve efficiency for large canvas size
 */
// Draw Functions
function draw(){
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.drawImage(background, 0,0);
    for (let i = 0; i < characterRectangles.length; i++) {
        let r = characterRectangles[i];
        let label = characterLabels[i];
        ctx.strokeStyle = "#d9345a";
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.rect(r[0], r[1], r[2], r[3]);
        ctx.stroke();
        if (label !== undefined) {
            ctx.font = "bold " + fontSize + "px Comic Sans MS";
            ctx.fillStyle = "#d9345a";
            ctx.strokeStyle = "white";
            ctx.lineWidth = 1;
            ctx.textAlign = "center";
            ctx.fillText(label, r[0] + r[2]/2, r[1] + (r[3]/2)+fontSize/4);
            ctx.strokeText(label, r[0] + r[2]/2, r[1] + (r[3]/2)+fontSize/4);
        }
    }
    for (let j = 0; j < wordLines.length; j++) {
        let w = wordLines[j];
        ctx.beginPath();
        ctx.moveTo(w[0], w[1]);
        ctx.lineTo(w[2], w[3]);
        ctx.strokeStyle = "#00FF00";
        ctx.lineWidth = 3;
        ctx.stroke();
    }
    for (let k = 0; k < lineLines.length; k++) {
        let l = lineLines[k];
        ctx.beginPath();
        ctx.moveTo(l[0], l[1]);
        ctx.lineTo(l[2], l[3]);
        ctx.strokeStyle = "#0000FF";
        ctx.lineWidth = 5;
        ctx.stroke();
    }
}

function clean() {
    /**
     * There is not efficient, but functional
     */
    for (let i = 0; i < characterRectangles.length; i++) {
        let r = characterRectangles[i];
        if (Math.abs(r[2]) < 10 || Math.abs(r[3]) < 10) {
            console.log("Removing bad rectangle");
            characterRectangles.splice(i, 1);
            characterLabels.splice(i, 1);
            continue;
        }

        /**
         * The backend also handles flipping, but later on it was needed here for the
         * current approach to erasing
         */

        if (r[2] < 0) {
            r[0] = r[0] + r[2];
            r[2] *= -1;
            console.log("flipping");
        }
        if (r[3] < 0) {
            r[1] = r[1] + r[3];
            r[3] *= -1;
            console.log("flipping");
        }
    }
    // TODO: cleanup repeated code here
    // consider lines less than 10 px as erroneous
    for (let j = 0; j < wordLines.length; j++) {
        let w = wordLines[j];
        let word_distance = Math.sqrt(Math.pow(w[0]-w[2],2) + Math.pow(w[1]-w[3], 2));
        if (word_distance < 10) {
            console.log("Removing bad word lines");
            wordLines.splice(j, 1);
        }
    }
    for (let k = 0; k < lineLines.length; k++) {
        let l = lineLines[k];
        let line_distance = Math.sqrt(Math.pow(l[0]-l[2],2) + Math.pow(l[1]-l[3], 2));
        if (line_distance < 10) {
            console.log("Removing bad line lines");
            lineLines.splice(j, 1);
        }
    }
}

// Button Functions

function setCaptureMode(mode) {
    if(!drawing && (mode !== CaptureModes.WORD || lastIsLabeled())) {
        $('#captureMode')[0].innerText = 'Current Mode: ' + mode;
        captureMode = mode;
    }
}

function undo() {
    if (!drawing) {
        if (captureMode === CaptureModes.LETTER && characterRectangles.length > 0) {
            redoQueueCharacterRectangles.push(characterRectangles.pop());
            redoQueueCharacterLabels.push(characterLabels.pop());
            draw();
        } else if (captureMode === CaptureModes.WORD && wordLines.length > 0) {
            redoQueueWordLines.push(wordLines.pop());
            draw();
        } else if (captureMode === CaptureModes.LINE && lineLines.length > 0) {
            redoQueueLineLines.push(lineLines.pop());
            draw();
        }
    }
}

function redo() {
    if (!drawing) {
        if (captureMode === CaptureModes.LETTER && redoQueueCharacterRectangles.length > 0) {
            characterRectangles.push(redoQueueCharacterRectangles.pop());
            characterLabels.push(redoQueueCharacterLabels.pop());
            draw();
        } else if (captureMode === CaptureModes.WORD && redoQueueWordLines.length > 0) {
            wordLines.push(redoQueueWordLines.pop());
            draw();
        } else if (captureMode === CaptureModes.LINE && redoQueueLineLines.length > 0) {
            lineLines.push(redoQueueLineLines.pop());
            draw();
        }
    }
}

function keyHandler(e) {
    e.preventDefault();
    let modifier = e.metaKey || e.ctrlKey;
    let code = e.code;
    let key = e.key;
    let shift  = e.shiftKey;
    console.log(e);
    if (!drawing) {
        if((modifier && code === "KeyZ" && !shift) || code === "BracketLeft" || code === "Backspace") {
            undo();
        } else if ((modifier && shift && code === "KeyZ") || code === "BracketRight") {
            redo();
        } else if (modifier && code === "KeyS") {
            save();
        } else {
            clean();
            if(captureMode === CaptureModes.LETTER && characterLabels.length > 0 && !drawing)
            {
                // correct the most recent label
                characterLabels.pop();
                characterLabels.push(key);
            }
        }
        draw();
    }
}

function initEventHandlersAndListeners() {
    canvas.addEventListener("mousedown", function(e) { clickDown(e)});
    canvas.addEventListener("mouseup", function(e) { clickUp(e)});
    canvas.addEventListener("mousemove", function(e) { dragHandler(e)});
    canvas.addEventListener("touchstart", function(e) { clickDown(e)});
    canvas.addEventListener("touchend", function(e) { clickUp(e)});
    canvas.addEventListener("touchmove", function(e) { dragHandler(e)});
    document.addEventListener('keypress', keyHandler);

    const saveButton = $('#save')[0];
    saveButton.addEventListener("click", save);

    const downloadButton = $('#download')[0];
    downloadButton.addEventListener("click", saveJsonLocally);

    const closeButton = $('#close')[0];
    closeButton.addEventListener("click", function () {window.location.href = '/'})

    const undoButton = $('#undo')[0];
    undoButton.addEventListener("click", undo);

    const redoButton = $('#redo')[0];
    redoButton.addEventListener("click", redo);

    const letterCapButton = $('#letterCap')[0];
    letterCapButton.addEventListener("click", function() {setCaptureMode(CaptureModes.LETTER)});

    const wordCapButton = $('#wordCap')[0];
    wordCapButton.addEventListener("click", function() {setCaptureMode(CaptureModes.WORD)});

    const lineCapButton = $('#lineCap')[0];
    lineCapButton.addEventListener("click", function() {setCaptureMode(CaptureModes.LINE)});

    const eraseButton = $('#eraser')[0];
    eraseButton.addEventListener("click", function() {setCaptureMode(CaptureModes.ERASER)});

    const fontSlider = $('#fontSlider')[0];
    fontSlider.addEventListener("change", function()
        {
            fontSize = fontSlider.value;
            draw();
        }
    );

}

function updateJobInfo() {
    jobInfo.characterRectangles = characterRectangles;
    jobInfo.characterLabels = characterLabels;
    jobInfo.wordLines = wordLines;
    jobInfo.lineLines = lineLines;
    jobInfo.completed = $('#completed')[0].checked;
    jobInfo.edited = true;
    if (jobInfo.completed) {
        jobInfo.status = "Completed";
    } else {
        jobInfo.status = "Edited";
    }
}

function saveJsonLocally() {
    updateJobInfo();
    let json = JSON.stringify(jobInfo);
    const a = document.createElement('a');
    a.setAttribute('href', 'data:text/plain;charset=utf-8,'+encodeURIComponent(json));
    a.setAttribute('download', jobId + '_imageJob.json');
    a.click()
}

function save() {
    const saveText = $('#saveText');
    function saveErrorMessage(message) {
        saveText[0].innerText = message;
        saveText.css('color', 'red');
        saveText.show();
    }
    function saveSuccessMessage(message) {
        saveText[0].innerText = message;
        saveText.css('color', 'green');
        saveText.show(0).delay(3000).hide(0);
    }
    if(!lastIsLabeled()) {
        saveErrorMessage("You must label all letters before saving!");
    } else {
        updateJobInfo();
        $.ajax({
            type: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            url: '/saveJob',
            data: JSON.stringify(jobInfo),
            success: function (data) {
                console.log("submission success");
                saveSuccessMessage("Saved Successfully!");
            },
            error: function(XMLHttpRequest, textStatus, errorThrown) {
                saveErrorMessage("Save Failed! Make a copy of the JSON with the Download Button.");
            }
        });
    }
}

function init() {
    captureMode = CaptureModes.LETTER;
    setCaptureMode(captureMode);
    background = new Image();
    jobId = $('#imageId')[0].textContent;
    fontSize = $('#fontSlider')[0].value;
    $.getJSON("/getJob",
        {
            id: jobId
        },
        function(response) {
            jobInfo = response;
            if (jobInfo.characterRectangles !== null) characterRectangles = jobInfo.characterRectangles;
            if (jobInfo.characterLabels !== null) characterLabels = jobInfo.characterLabels;
            if (jobInfo.wordLines !== null) wordLines = jobInfo.wordLines;
            if (jobInfo.lineLines !== null) lineLines = jobInfo.lineLines;
            if (jobInfo.completed !== null) $('#completed')[0].checked = jobInfo.completed;
            console.log(JSON.stringify(jobInfo));
            let request = $.get( "/getImage", { id: jobId})
            request.done(function(data) {
                // there is likely a better way to do this, this was just the quickest
                background.src = 'data:image/png;base64,' + data;
                console.log("success");
            });

            request.fail(function(XMLHttpRequest, textStatus, errorThrown) {
                        alert(errorThrown);
            });
        });

    // Add event listeners
    initEventHandlersAndListeners();


    /**
     * Ideally, we might want to do some kind of scaling here to fit the browser window.
     * However, most images we are currently using are small enough that this isn't worth
     * the risk of doing some bad math and causing misalignment between the captured data
     * and what actually gets cropped on the backend
     */
    // Wait for image to load
    background.onload = function() {
        canvas.width = background.width;
        canvas.height = background.height;
        console.log(background.width, background.height);
        ctx.drawImage(background, 0,0);
        draw();
    }

}

$(window).on('load', function() { init(); console.log("init");})


