/**
 * Do I think it was a good idea to put everything in a monolithic JS file? No.
 * For the sake of readability, there's a lot of project restructuring & organization
 * that could be done to enhance this code. There is also bad-practice here as well.
 *
 * The intent was to develop a prototype ASAP. Feel free to contribute, just make sure you
 * don't break existing behavior/functionality.
 *
 */

function captureCanvasInit (trained_model, trained_model_labels) {

    const canvas = document.getElementById("mainCanvas");
    const drawingCanvas = document.getElementById("drawingCanvas");
    const cropCanvas = document.getElementById("cropCanvas");

    const ctx = canvas.getContext("2d");
    const drawingCtx = drawingCanvas.getContext("2d");
    const cropCtx = cropCanvas.getContext("2d");

    let drawing;
    let background;

    let characterRectangles = [];
    let characterLabels = [];
    let redoQueueCharacterRectangles = [];
    let redoQueueCharacterLabels = [];

    let wordLines = [];
    let redoQueueWordLines = [];

    let lineLines = [];
    let redoQueueLineLines = [];

    let erasedCharacterLabels = [];
    let erasedCharacterRectangles = [];
    let erasedWordLines = [];
    let erasedLineLines = [];

    let jobId;
    let jobInfo;

    let captureMode;
    let fontSize;
    let transparency;
    let textFieldEdit;

    let hideCapture;

    let predictionAutofill;

    const CaptureModes = {
        LETTER: 'Letter',
        WORD: 'Word',
        LINE: 'Line',
        ERASER: 'Eraser',
        DISABLED: 'Disabled'
    }

    function lastIsLabeled() {
        return characterLabels.length === 0 || characterLabels[characterLabels.length - 1] !== undefined;
    }

    // Click Functions

    function clickDown(event) {
        if (!drawing) {
            const x = event.pageX - canvas.offsetLeft;
            const y = event.pageY - canvas.offsetTop;
            if (captureMode === CaptureModes.LETTER && lastIsLabeled()) {
                drawing = true;
                characterRectangles.push([x, y, 0, 0]);
                characterLabels.push(undefined);
                redoQueueCharacterLabels = [];
                redoQueueCharacterRectangles = [];
            }
            if (captureMode === CaptureModes.WORD) {
                drawing = true;
                wordLines.push([x, y, x, y]);
                redoQueueWordLines = [];
            }
            if (captureMode === CaptureModes.LINE) {
                drawing = true;
                lineLines.push([x, y, x, y]);
                redoQueueLineLines = [];
            }
            if (captureMode === CaptureModes.ERASER) {
                drawing = true;
            }
        }
    }

    function clickUp(event) {
        let index = characterRectangles.length-1
        let rectangle = characterRectangles[index];
        drawing = false;
        let removedBadRectangle = clean();
        draw();
        if (!removedBadRectangle && event.type !== 'mouseout' && index !== -1 && predictionAutofill) {
            generateCropAndPredict(index, rectangle);
        }
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
                let mouseX = event.pageX - canvas.offsetLeft
                let mouseY = event.pageY - canvas.offsetTop
                for (let i = 0; i < characterRectangles.length; i++) {
                    let r = characterRectangles[i];
                    if ((r[0] < mouseX && mouseX < r[0] + r[2])
                        && (r[1] < mouseY && mouseY < r[1] + r[3])) {
                        erasedCharacterRectangles.push(r);
                        erasedCharacterLabels.push(characterLabels[i]);
                        // console.log(r, event.pageX - canvas.offsetLeft, event.pageY - canvas.offsetTop);
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
                    let line_slope = Math.abs(slope(w[0], w[1], w[2], w[3]));
                    let slope_start = Math.abs(slope(w[0], w[1], mouseX, mouseY));
                    let slope_end = Math.abs(slope(w[2], w[3], mouseX, mouseY));
                    if ((Math.abs(line_slope - slope_start) < eps) &&
                        (Math.abs(line_slope - slope_end) < eps) &&
                        (Math.min(w[0], w[2]) <= mouseX && mouseX <= Math.max(w[0], w[2])) &&
                        (Math.min(w[1], w[3]) <= mouseY && mouseY <= Math.max(w[1], w[3]))) {
                        erasedWordLines.push(w);
                        wordLines.splice(j, 1);
                    }
                }
                for (let l = 0; l < lineLines.length; l++) {
                    let k = lineLines[l];
                    let eps = 0.5;
                    let line_slope = Math.abs(slope(k[0], k[1], k[2], k[3]));
                    let slope_start = Math.abs(slope(k[0], k[1], mouseX, mouseY));
                    let slope_end = Math.abs(slope(k[2], k[3], mouseX, mouseY));
                    if ((Math.abs(line_slope - slope_start) < eps) &&
                        (Math.abs(line_slope - slope_end) < eps) &&
                        (Math.min(k[0], k[2]) <= mouseX && mouseX <= Math.max(k[0], k[2])) &&
                        (Math.min(k[1], k[3]) <= mouseY && mouseY <= Math.max(k[1], k[3]))) {
                        erasedLineLines.push(k)
                        lineLines.splice(k, 1);
                    }
                }

            }
        }
    }

    function clearMainCanvas() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.drawImage(background, 0, 0);
    }

    /**
     * Work could be done here to improve efficiency for large canvas size
     */
    // Draw Functions

    function drawRectangle(index, canvas, currentContext) {
        let r = characterRectangles[index];
        let label = characterLabels[index];
        currentContext.strokeStyle = "#d9345a";
        currentContext.fillStyle = '#ffffff';
        currentContext.lineWidth = 2;
        currentContext.beginPath();
        currentContext.rect(r[0], r[1], r[2], r[3]);
        currentContext.stroke();
        currentContext.beginPath();
        if (transparency) {
            currentContext.globalAlpha = 0.4;
            currentContext.fillRect(r[0], r[1], r[2], r[3]);
            currentContext.globalAlpha = 1.0
            currentContext.stroke();
        }
        if (label !== undefined) {
            currentContext.font = "bold " + fontSize + "px Exo";
            currentContext.fillStyle = "#d9345a";
            currentContext.strokeStyle = "white";
            currentContext.lineWidth = 1;
            currentContext.textAlign = "center";
            currentContext.fillText(label, r[0] + r[2] / 2, r[1] + (r[3] / 2) + fontSize / 4);
            currentContext.strokeText(label, r[0] + r[2] / 2, r[1] + (r[3] / 2) + fontSize / 4);
        }
    }

    function draw() {
        if (drawing && CaptureModes.LETTER === captureMode) {
            drawingCtx.clearRect(0, 0, drawingCanvas.width, drawingCanvas.height);
            drawRectangle(characterRectangles.length-1, drawingCanvas, drawingCtx);
            drawingCtx.stroke();
        } else {
            drawingCtx.clearRect(0, 0, drawingCanvas.width, drawingCanvas.height);
            drawComplete();
        }
    }

    function drawComplete() {
        clearMainCanvas();
        for (let i = 0; i < characterRectangles.length; i++) {
            drawRectangle(i, canvas, ctx);
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
        let removedBadRectangle = false;
        for (let i = 0; i < characterRectangles.length; i++) {
            let r = characterRectangles[i];
            if (Math.abs(r[2]) < 10 || Math.abs(r[3]) < 10) {
                console.log("Removing bad rectangle");
                characterRectangles.splice(i, 1);
                characterLabels.splice(i, 1);
                removedBadRectangle = true;
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
            let word_distance = Math.sqrt(Math.pow(w[0] - w[2], 2) + Math.pow(w[1] - w[3], 2));
            if (word_distance < 10) {
                console.log("Removing bad word lines");
                wordLines.splice(j, 1);
            }
        }
        for (let k = 0; k < lineLines.length; k++) {
            let l = lineLines[k];
            let line_distance = Math.sqrt(Math.pow(l[0] - l[2], 2) + Math.pow(l[1] - l[3], 2));
            if (line_distance < 10) {
                console.log("Removing bad line lines");
                lineLines.splice(j, 1);
            }
        }
        return removedBadRectangle;
    }

    function clearEraserQueues() {
        erasedCharacterLabels = [];
        erasedCharacterRectangles = [];
        erasedWordLines = [];
        erasedLineLines = [];
    }

    // Button Functions

    function setCaptureMode(mode) {
        const captureModeIndicator = $('#captureMode')[0];
        if (!drawing && (captureMode !== CaptureModes.LETTER || lastIsLabeled())) {
            captureModeIndicator.innerText = 'Current Mode: ' + mode;
            captureMode = mode;
            clearEraserQueues();
        } else {
            alert("Can't change modes while drawing or while rectangle is unlabeled.");
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
            } else if (captureMode === CaptureModes.ERASER) {
                characterLabels = characterLabels.concat(erasedCharacterLabels);
                characterRectangles = characterRectangles.concat(erasedCharacterRectangles);
                wordLines = wordLines.concat(erasedWordLines);
                lineLines = lineLines.concat(erasedLineLines);
                clearEraserQueues();
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

    // default key handler
    function keyHandler(e) {
        if (!textFieldEdit) {
            e.preventDefault();
            let modifier = e.metaKey || e.ctrlKey;
            let code = e.code;
            let key = e.key;
            if (!drawing) {
                // TODO: these bracket shortcuts should be deprecated
                if (code === "BracketLeft" || code === "Backspace" || code === "Enter") {
                    undo();
                } else if (code === "BracketRight") {
                    redo();
                } else if (modifier && code === "KeyE") {
                    setCaptureMode(CaptureModes.ERASER);
                } else if (modifier && code === "KeyL") {
                    setCaptureMode(CaptureModes.LETTER);
                } else {
                    clean();
                    if (captureMode === CaptureModes.LETTER && characterLabels.length > 0 && !drawing && !modifier) {
                        // correct the most recent label
                        characterLabels.pop();
                        characterLabels.push(key);
                    }
                }
                draw();
                return false;
            }
        }
    }

    // advanced hot key shortcuts

    hotkeys('ctrl+s, command+s', function () {
        if (!drawing) save();
        return false;
    });

    hotkeys('ctrl+z, command+z', function () {
        if (!drawing) undo();
        return false;
    });

    hotkeys('ctrl+y, command+y', function () {
        if (!drawing) redo();
        return false;
    });

    hotkeys('ctrl+y, command+y, ctrl+shift+z, command+shift+z', function () {
        if (!drawing) redo();
        return false;
    });

    hotkeys('ctrl+e, command+e', function () {
        if (!drawing) setCaptureMode(CaptureModes.ERASER);
        return false;
    });

    hotkeys('ctrl+l, command+l', function () {
        if (!drawing) setCaptureMode(CaptureModes.LETTER);
        return false;
    });

    hotkeys('ctrl+shift+., command+shift+.', function () {
        if (!drawing) {
            hideCapture = !hideCapture;
            if (hideCapture) {
                clearMainCanvas();
            } else {
                draw();
            }
        }
        return false;
    });

    function initEventHandlersAndListeners() {
        drawingCanvas.addEventListener("mousedown", function (e) {
            clickDown(e);
        });
        drawingCanvas.addEventListener("mouseup", function (e) {
            clickUp(e)
        });
        drawingCanvas.addEventListener("mouseout", function (e) {
            clickUp(e);
        });
        drawingCanvas.addEventListener("mousemove", function (e) {
            dragHandler(e);
        });
        drawingCanvas.addEventListener("touchstart", function (e) {
            if (e.touches.length === 1) {
                document.documentElement.style.overflow = 'hidden';
                clickDown(e);
            }
        });
        drawingCanvas.addEventListener("touchend", function (e) {
            document.documentElement.style.overflow = 'auto';
            clickUp(e);
            $('.hidden-mobile-input').blur().focus();
        });
        drawingCanvas.addEventListener("touchmove", function (e) {
            if (e.touches.length === 1) {
                dragHandler(e);
                e.preventDefault();
                return false;
            }

        });

        document.addEventListener('keypress', keyHandler);

        const saveButton = $('#save')[0];
        saveButton.addEventListener("click", save);

        const downloadButton = $('#download')[0];
        downloadButton.addEventListener("click", saveJsonLocally);

        const closeButton = $('#close')[0];
        closeButton.addEventListener("click", function () {
            window.location.href = '/'
        })

        const undoButton = $('#undo')[0];
        undoButton.addEventListener("click", undo);

        const redoButton = $('#redo')[0];
        redoButton.addEventListener("click", redo);

        const letterCapButton = $('#letterCap')[0];
        letterCapButton.addEventListener("click", function () {
            setCaptureMode(CaptureModes.LETTER)
        });

        const wordCapButton = $('#wordCap')[0];
        wordCapButton.addEventListener("click", function () {
            setCaptureMode(CaptureModes.WORD)
        });

        const lineCapButton = $('#lineCap')[0];
        lineCapButton.addEventListener("click", function () {
            setCaptureMode(CaptureModes.LINE)
        });

        const eraseButton = $('#eraser')[0];
        eraseButton.addEventListener("click", function () {
            setCaptureMode(CaptureModes.ERASER)
        });

        const fontSlider = $('#fontSlider')[0];
        fontSlider.addEventListener("change", function () {
                fontSize = fontSlider.value;
                draw();
            }
        );

        const textFieldSelector = $('.text-entry');

        textFieldSelector.focusin(() => textFieldEdit = true);
        textFieldSelector.focusout(() => textFieldEdit = false);

        const enableTransparency = $('#enableTransparency')[0];
        enableTransparency.checked = transparency;
        enableTransparency.addEventListener("change", function () {
                transparency = enableTransparency.checked;
                draw();
            }
        );

        const enablePredictions = $('#enablePredictions')[0];
        enablePredictions.checked = predictionAutofill;
        enablePredictions.addEventListener("change", function () {
            predictionAutofill = enablePredictions.checked;
            if (!predictionAutofill) {
                $('.alert-prediction').hide();
            }
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
        jobInfo.fields.NOTES = $('#notes')[0].value;
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
        a.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(json));
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

        if (!lastIsLabeled()) {
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
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    saveErrorMessage("Save Failed! Make a copy of the JSON with the Download Button.");
                }
            });
        }
    }

    function init() {
        drawing = false;
        hideCapture = false;
        setCaptureMode(CaptureModes.LETTER);
        background = new Image();
        jobId = $('#imageId')[0].textContent;
        fontSize = $('#fontSlider')[0].value;
        transparency = true;
        predictionAutofill = true;
        textFieldEdit = false;
        document.fonts.load(fontSize+  "px Exo");
        $.getJSON("/getJob",
            {
                id: jobId
            },
            function (response) {
                jobInfo = response;
                if (jobInfo.characterRectangles !== null) characterRectangles = jobInfo.characterRectangles;
                if (jobInfo.characterLabels !== null) characterLabels = jobInfo.characterLabels;
                if (jobInfo.wordLines !== null) wordLines = jobInfo.wordLines;
                if (jobInfo.lineLines !== null) lineLines = jobInfo.lineLines;
                if (jobInfo.completed !== null) $('#completed')[0].checked = jobInfo.completed;
                if (jobInfo.fields.NOTES !== null) $('#notes')[0].value = jobInfo.fields.NOTES;
                let request = $.get("/getImage", {id: jobId})
                request.done(function (data) {
                    // there is likely a better way to do this, this was just the quickest
                    background.src = 'data:image/png;base64,' + data;
                    console.log("Retrieved Image From Server");
                });

                request.fail(function (XMLHttpRequest, textStatus, errorThrown) {
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
        background.onload = function () {
            canvas.width = background.width;
            canvas.height = background.height;
            drawingCanvas.width = background.width;
            drawingCanvas.height = background.height;
            ctx.drawImage(background, 0, 0);
            document.fonts.load(fontSize+  "px Exo").then(() => {
                draw();
            })
            $('#full-screen-load').delay(500).fadeOut();
            $('#main-content-container').delay(750).fadeIn();
        }
    }

    function generateCropAndPredict(index, rectangle) {
        $('.alert-prediction').show();
        $('#prediction').text("Predicting...");
        // let index =
        let r  = rectangle;
        cropCanvas.width = 64;
        cropCanvas.height = 64;
        cropCtx.clearRect(0, 0, cropCanvas.width, cropCanvas.height);
        cropCtx.drawImage(background, r[0], r[1], r[2], r[3], 0, 0, 64, 64);
        let imgSelector = $('#renderedCrop');
        let img = imgSelector[0];
        img.width = 64;
        img.height = 64;
        img.src = cropCanvas.toDataURL("image/png");
        imgSelector.on('load', (ev => tf.tidy( () => {tensorFlowPrediction(img, index, rectangle)})));
    }

    // used for testing concurrency issues

    function sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    function tensorFlowPrediction(img, index, rectangle) {
        let input = tf.browser.fromPixels(img).mean(2)
            .toFloat()
            .expandDims(0)
            .expandDims(-1)
            .mul(0.003921569);
        let prediction = trained_model.predict(input);
        // await sleep(10000);
        prediction.data().then(data => {
            let index_label = data.indexOf(Math.max(...data));
            let char_label = trained_model_labels[index_label];

            $('#prediction').text("Prediction: " + trained_model_labels[index_label]);
            if (predictionAutofill && !drawing && char_label !== undefined
                && characterRectangles.length - 1 === index
                && characterRectangles[index] === rectangle
                && !lastIsLabeled()) {
                characterLabels[index] = trained_model_labels[index_label];
                draw();
            }
        });
    }

    init();
    $(window).bind('beforeunload', function () {
        return 'Before closing, make sure you saved your changes.';
    });
}



$(window).on('load', function() {

    function validateLabels(labels) {
        for (let i = 0; i < labels.length; i++) {
            if(labels[i].length !== 1) {
                console.error("Encountered label longer than length 1 at index ", i);
                return false;
            }
        }
        return true;
    }

    function retrieveIncludedLabels(model) {
        $.getJSON("/ml/labels.json",
            function (response) {
                captureCanvasInit(model, response.predictionLabels);
            },)
            .done()
            .fail(function( jqxhr, textStatus, error ) {
                let err = textStatus + ", " + error;
                console.log( "Request Failed: " + err );
            });
    }

    function retrieveLocalLabels(model) {
        $.getJSON("/localModels/labels.json",
            function (response) {
                if (validateLabels(response.predictionLabels)) {
                    captureCanvasInit(model, response.predictionLabels);
                } else {
                    console.log("Falling back to included labels (prediction result max index corresponds to 26 lower characters a-z)")
                    retrieveIncludedLabels(model);
                }
            })
            .fail(function( jqxhr, textStatus, error ) {
                let err = textStatus + ", " + error;
                console.warn("WARNING: You are using a local model without providing a labels.json file \n"
                    + "To see an example, look at http://" + window.location.host + "/ml/labels.json");
                console.log("Falling back to included labels (prediction result max index corresponds to 26 lower characters a-z)")
                retrieveIncludedLabels(model);
            });
    }

    setupTfBackend().then( () => {
            tf.loadLayersModel('/localModels/model.json')
                .then(model => {
                    console.log("Loaded local model.");
                    retrieveLocalLabels(model);
                })
                .catch(err => {
                    console.log("Falling back to included model...");
                    tf.loadLayersModel('/ml/model.json')
                        .then(model => retrieveIncludedLabels(model));
                });
        }
    )
})

async function setupTfBackend() {
    await tf.ready();
}



