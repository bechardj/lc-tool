console.log("Hello!");

var canvas = document.getElementById("mainCanvas");
var ctx = canvas.getContext("2d");

var drawing = false;
var background;

var characterRectangles = [];
var characterLabels = [];
var redoQueueCharacterRectangles = [];
var redoQueueCharacterLabels = [];

var wordLines = [];
var redoQueueWordLines = [];

var lineLines = [];
var redoQueueLineLines = [];

var jobId;
var jobInfo;

const CaptureModes = {
    LETTER: 'Letter',
    WORD: 'Word',
    LINE: 'Line',
}

var lastIsLabeled = function() {
    return characterLabels.length === 0 || characterLabels[characterLabels.length-1] !== undefined;
}

// Click Functions

var clickDown = function(event) {
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
    }
}

var clickUp = function(event) {
    drawing = false;
    clean();
    draw();
}

var dragHandler = function(event) {
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
        }
    }
}

// Draw Functions

var draw = function(){
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.drawImage(background, 0,0);
    for (var i = 0; i < characterRectangles.length; i++) {
        r = characterRectangles[i];
        label = characterLabels[i];
        ctx.strokeStyle = "#FF0000";
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.rect(r[0], r[1], r[2], r[3]);
        ctx.stroke();
        if (label !== undefined) {
            ctx.font = "30px Comic Sans MS";
            ctx.fillStyle = "red";
            ctx.textAlign = "center";
            ctx.fillText(label, r[0] + r[2]/2, r[1] + r[3]/2);
        }
    }
    for (var j = 0; j < wordLines.length; j++) {
        var w = wordLines[j];
        ctx.beginPath();
        ctx.moveTo(w[0], w[1]);
        ctx.lineTo(w[2], w[3]);
        ctx.strokeStyle = "#00FF00";
        ctx.lineWidth = 3;
        ctx.stroke();
    }
    for (var k = 0; k < lineLines.length; k++) {
        var l = lineLines[k];
        ctx.beginPath();
        ctx.moveTo(l[0], l[1]);
        ctx.lineTo(l[2], l[3]);
        ctx.strokeStyle = "#0000FF";
        ctx.lineWidth = 5;
        ctx.stroke();
    }
}

var clean = function(){
    for (var i = 0; i < characterRectangles.length; i++) {
        var r = characterRectangles[i];
        label = characterLabels[i];
        if (Math.abs(r[2]) < 10 || Math.abs(r[3]) < 10) {
            console.log("Removing bad rectangle");
            characterRectangles.splice(i, 1);
            characterLabels.splice(i, 1);
        }
    }
    // TODO: cleanup repeated code here
    for (var j = 0; j < wordLines.length; j++) {
        var w = wordLines[j];
        var word_distance = Math.sqrt(Math.pow(w[0]-w[2],2) + Math.pow(w[1]-w[3], 2));
        if (word_distance < 10) {
            console.log("Removing bad word lines");
            wordLines.splice(j, 1);
        }
    }
    for (var k = 0; k < lineLines.length; k++) {
        var l = lineLines[k];
        var line_distance = Math.sqrt(Math.pow(l[0]-l[2],2) + Math.pow(l[1]-l[3], 2));
        if (line_distance < 10) {
            console.log("Removing bad word lines");
            lineLines.splice(j, 1);
        }
    }
}

// Button Functions

var setCaptureMode = function(mode) {
    if(!drawing && (mode !== CaptureModes.WORD || lastIsLabeled())) {
        $('#captureMode')[0].innerText = 'Current Mode: ' + mode;
        captureMode = mode;
    }
}

var undo = function() {
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

var redo = function() {
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

var keyHandler = function(e) {
    e.preventDefault();
    var modifier = e.metaKey || e.ctrlKey;
    var code = e.code;
    var key = e.key;
    var shift  = e.shiftKey;
    console.log(e);
    if (!drawing) {
        if((modifier && code === "KeyZ" && !shift) || code === "BracketLeft")
        {
            undo();
        } else if ((modifier && shift && code === "KeyZ") || code === "BracketRight")
        {
            redo();
        } else {
            clean();
            if(captureMode === CaptureModes.LETTER && characterLabels.length > 0 && !drawing)
            {
                label = characterLabels.pop();
                if (!drawing) {
                    label = key
                }
                characterLabels.push(label);
            }
        }
        draw();
    }
}

var initEventHandlersAndListeners = function() {
    canvas.addEventListener("mousedown", function(e) { clickDown(e)});
    canvas.addEventListener("mouseup", function(e) { clickUp(e)});
    canvas.addEventListener("mousemove", function(e) { dragHandler(e)});
    canvas.addEventListener("touchstart", function(e) { clickDown(e)});
    canvas.addEventListener("touchend", function(e) { clickUp(e)});
    canvas.addEventListener("touchmove", function(e) { dragHandler(e)});
    document.addEventListener('keypress', keyHandler);

    var saveButton = $('#save')[0];
    saveButton.addEventListener("click", save);

    var downloadButton = $('#download')[0];
    downloadButton.addEventListener("click", saveJsonLocally);

    var closeButton = $('#close')[0];
    closeButton.addEventListener("click", function () {window.location.href = '/'})

    var undoButton = $('#undo')[0];
    undoButton.addEventListener("click", undo);

    var redoButton = $('#redo')[0];
    redoButton.addEventListener("click", redo);

    var letterCapButton = $('#letterCap')[0];
    letterCapButton.addEventListener("click", function() {setCaptureMode(CaptureModes.LETTER)});

    var wordCapButton = $('#wordCap')[0];
    wordCapButton.addEventListener("click", function() {setCaptureMode(CaptureModes.WORD)});

    var lineCapButton = $('#lineCap')[0];
    lineCapButton.addEventListener("click", function() {setCaptureMode(CaptureModes.LINE)});

}

var updateJobInfo = function() {
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
    var json = JSON.stringify(jobInfo);
    const a = document.createElement('a');
    a.setAttribute('href', 'data:text/plain;charset=utf-8,'+encodeURIComponent(json));
    a.setAttribute('download', jobId + '_imageJob.json');
    a.click()
}

var save = function () {
    var saveText = $('#saveText');
    var saveErrorMessage = function(message) {
        saveText[0].innerText = message;
        saveText.css('color', 'red');
        saveText.show();
    }
    var saveSuccessMessage = function(message) {
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
var init = function() {
    captureMode = CaptureModes.LETTER;
    setCaptureMode(captureMode);
    background = new Image();
    jobId = $('#imageId')[0].textContent;
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
            if (completed !== null) $('#completed')[0].checked = jobInfo.completed;
            console.log(JSON.stringify(jobInfo));
            var request = $.get( "/getImage", { id: jobId})
            request.done(function(data) {
                background.src = 'data:image/png;base64,' + data;
                console.log("success");
            });

            request.fail(function(XMLHttpRequest, textStatus, errorThrown) {
                        alert(errorThrown);
            });
        });

    // Add event listners
    initEventHandlersAndListeners();

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


