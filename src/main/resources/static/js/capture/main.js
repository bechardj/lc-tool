import { Rectangle, Line } from './geometry.js';
import {CaptureModes, CaptureState} from "./captureState.js";
import {loadJob, loadImage} from "./jobLoader.js";
import { PredictionEngine } from "./predictionEngine.js";

function captureCanvasInit (predictionEngine) {

    const state = new CaptureState();

    const mainCanvas = document.getElementById("mainCanvas");
    const drawingCanvas = document.getElementById("drawingCanvas");
    const cropCanvas = document.getElementById("cropCanvas");

    const mainCtx = mainCanvas.getContext("2d");
    const drawingCtx = drawingCanvas.getContext("2d");
    const cropCtx = cropCanvas.getContext("2d");

    let background;
    let jobId;

    let fontSize;
    let transparency;
    let textFieldEdit;

    let hideCapture;

    let predictionAutofill;

    const Colors = {
        PINK: "#d9345a",
        BLUE: "#0000FF",
        GREEN: "#00FF00",
        WHITE: "#FFFFFF"
    }

    // Click Functions

    function clickDown(event) {
        if (!state.drawing) {
            const x = event.pageX - mainCanvas.offsetLeft;
            const y = event.pageY - mainCanvas.offsetTop;
            state.startDrawing(x, y)
        }
    }

    function clickUp(event) {
        let index = state.characterRectangles.length-1
        let rectangle = state.characterRectangles[index];
        state.stopDrawing();
        let removedBadRectangle = state.clean();
        draw();
        if (!removedBadRectangle && event.type !== 'mouseout' && index !== -1 && predictionAutofill
            && state.captureMode === CaptureModes.LETTER) {
            generateCropAndPredict(index, rectangle);
        }
    }

    function dragHandler(event) {
        if (state.drawing) {
            let mouseX = event.pageX - mainCanvas.offsetLeft
            let mouseY = event.pageY - mainCanvas.offsetTop
            const captureMode = state.captureMode;
            if (captureMode === CaptureModes.LETTER && state.characterRectangles.length > 0 && state.drawing) {
                const lastDrawn = state.characterRectangles.pop();
                const lastLabel = state.characterLabels.pop();
                lastDrawn.width = mouseX - lastDrawn.x1;
                lastDrawn.height = mouseY - lastDrawn.y1;
                state.characterRectangles.push(lastDrawn);
                state.characterLabels.push(lastLabel);
            } else if (captureMode === CaptureModes.WORD && state.wordLines.length > 0) {
                const lastDrawn = state.wordLines.pop();
                lastDrawn.x2 = mouseX;
                lastDrawn.y2 = mouseY;
                state.wordLines.push(lastDrawn);
            } else if (captureMode === CaptureModes.LINE && state.lineLines.length > 0) {
                const lastDrawn = state.lineLines.pop();
                lastDrawn.x2 = mouseX;
                lastDrawn.y2 = mouseY;
                state.lineLines.push(lastDrawn);
            } else if (captureMode === CaptureModes.ERASER) {
                eraser([mouseX, mouseY]);
            }
            draw();
        }
    }

    function eraser(point) {
        const previousCaptureMode = state.previousCaptureMode;
        if (previousCaptureMode === CaptureModes.LETTER) {
            for (let i = 0; i < state.characterRectangles.length; i++) {
                let r = state.characterRectangles[i];
                if (r.containsPoint(point)) {
                    state.erasedCharacterRectangles.push(r);
                    state.erasedCharacterLabels.push(state.characterLabels[i]);
                    state.characterRectangles.splice(i, 1);
                    state.characterLabels.splice(i, 1);
                    draw();
                }
            }
        }
        if (previousCaptureMode === CaptureModes.WORD || previousCaptureMode === CaptureModes.LINE) {
            let lines = CaptureModes.WORD === previousCaptureMode ? state.wordLines : state.lineLines;
            let erasedLines = CaptureModes.WORD === previousCaptureMode ? state.erasedWordLines : state.erasedLineLines;

            for (let j = 0; j < lines.length; j++) {
                if (lines[j].pointNearLine(point, 1)) {
                    erasedLines.push(lines[j]);
                    lines.splice(j, 1);
                }
            }
        }
    }

    function clearMainCanvas() {
        mainCtx.clearRect(0, 0, mainCanvas.width, mainCanvas.height);
        mainCtx.drawImage(background, 0, 0);
    }


    function draw() {
        const captureMode = state.captureMode;
        if (state.drawing && captureMode !== CaptureModes.ERASER) {
            drawingCtx.clearRect(0, 0, drawingCanvas.width, drawingCanvas.height);
            if (captureMode === CaptureModes.LETTER) {
                let rectangle = state.characterRectangles[state.characterRectangles.length-1];
                rectangle.draw(drawingCtx, Colors.PINK, 2, transparency, Colors.WHITE);
            }
            if (captureMode === CaptureModes.WORD) {
                window.requestAnimationFrame(() => drawComplete());
            }
            if (captureMode === CaptureModes.LINE) {
                window.requestAnimationFrame(() => drawComplete());
            }
            drawingCtx.stroke();
        } else {
            drawingCtx.clearRect(0, 0, drawingCanvas.width, drawingCanvas.height);
            drawComplete();
        }
    }

    function drawComplete() {
        clearMainCanvas();
        const captureMode = state.captureMode;
        const previousCaptureMode = state.previousCaptureMode;
        for (let i = 0; i < state.characterRectangles.length; i++) {
            let color = Colors.PINK;
            let rectangle = state.characterRectangles[i];
            if (captureMode !== CaptureModes.LETTER && captureMode !== CaptureModes.ERASER) {
                let lines = captureMode === CaptureModes.WORD ? state.wordLines : state.lineLines
                if (lines !== undefined && lines.length !== 0) {
                    let line = lines[lines.length - 1];
                    if (line.intersectsRectangle(rectangle)) {
                        color = captureMode === CaptureModes.LINE ? Colors.BLUE : Colors.GREEN;
                    }
                }
            }
            rectangle.draw(mainCtx, color, 2, transparency, Colors.WHITE);
            rectangle.drawLabel(mainCtx, state.characterLabels[i], Colors.PINK, 1, Colors.WHITE, "Exo", fontSize);
        }

        if (captureMode === CaptureModes.WORD
            || captureMode === CaptureModes.ERASER && previousCaptureMode === CaptureModes.WORD) {
            for (let j = 0; j < state.wordLines.length; j++) {
                state.wordLines[j].draw(mainCtx, Colors.GREEN, 3);
            }
        }
        if (captureMode === CaptureModes.LINE
            || captureMode === CaptureModes.ERASER && previousCaptureMode === CaptureModes.LINE) {
            for (let k = 0; k < state.lineLines.length; k++) {
                state.lineLines[k].draw(mainCtx, Colors.BLUE, 3)
            }
        }
    }

    // Button Functions

    function setCaptureMode(mode) {
        if (mode === state.captureMode) {
            return;
        }
        state.previousCaptureMode = state.captureMode;
        const captureModeIndicator = $('#captureMode')[0];
        if (!state.drawing && (state.captureMode !== CaptureModes.LETTER || state.lastIsLabeled())) {
            captureModeIndicator.innerText = 'Current Mode: '
                + (mode === CaptureModes.ERASER ?  state.previousCaptureMode + " " : "")
                + mode;
            state.captureMode = mode;
            if (mode !== CaptureModes.LETTER) {
                $('.alert-prediction').hide();
            }
            state.clearEraserQueues();
        } else {
            alert("Can't change modes while drawing or while rectangle is unlabeled.");
        }
        draw();
    }

    function undo() {
        state.undo(() => draw());
    }

    function redo() {
        state.redo(() => draw());
    }

    // default key handler
    function keyHandler(e) {
        if (!textFieldEdit) {
            e.preventDefault();
            let modifier = e.metaKey || e.ctrlKey;
            let code = e.code;
            let key = e.key;
            if (!state.drawing) {
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
                    state.clean();
                    if (state.captureMode === CaptureModes.LETTER && state.characterLabels.length > 0
                        && !state.drawing && !modifier) {
                        // correct the most recent label
                        state.characterLabels.pop();
                        state.characterLabels.push(key);
                    }
                }
                draw();
                return false;
            }
        }
    }

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

        hotkeys('ctrl+s, command+s', function () {
            if (!state.drawing) save();
            return false;
        });

        hotkeys('ctrl+z, command+z', function () {
            if (!state.drawing) undo();
            return false;
        });

        hotkeys('ctrl+y, command+y', function () {
            if (!state.drawing) redo();
            return false;
        });

        hotkeys('ctrl+y, command+y, ctrl+shift+z, command+shift+z', function () {
            if (!state.drawing) redo();
            return false;
        });

        hotkeys('ctrl+e, command+e', function () {
            if (!state.drawing) setCaptureMode(CaptureModes.ERASER);
            return false;
        });

        hotkeys('ctrl+l, command+l', function () {
            if (!state.drawing) setCaptureMode(CaptureModes.LETTER);
            return false;
        });

        hotkeys('ctrl+shift+., command+shift+.', function () {
            if (!state.drawing) {
                hideCapture = !hideCapture;
                if (hideCapture) {
                    clearMainCanvas();
                } else {
                    draw();
                }
            }
            return false;
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

        const enablePredictionsSelector = $('#enablePredictions');
        const enablePredictions = enablePredictionsSelector[0];
        enablePredictions.checked = predictionAutofill;
        enablePredictions.addEventListener("change", function () {
            predictionAutofill = enablePredictions.checked;
            if (!predictionAutofill) {
                $('.alert-prediction').hide();
            }
        });
        if (predictionEngine === undefined) {
            enablePredictions.disabled = true;
            enablePredictionsSelector.parent().hide();
        }
    }

    function saveJsonLocally() {
        let json = state.generateJobInfoJson();
        const a = document.createElement('a');
        a.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(json));
        a.setAttribute('download', jobId + '_imageJob.json');
        a.click()
    }

    function save() {

        if (!state.lastIsLabeled()) {
            notify("You must label all letters before saving!", 3000);
        } else {
            getBearerTokenWithPrompt().then(token => {
            $.ajax({
                type: "POST",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + token);
                },
                headers: {
                    'Content-Type': 'application/json'
                },
                url: '/secure_api/saveJob',
                data: state.generateJobInfoJson(),
                success: function (data) {
                    console.log("submission success");
                    notify("Saved Successfully!", 3000);
                },
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    notify("Save Failed! Make a copy of the JSON with the Download Button.", 3000);
                }
            });
        });
        }
    }

    function init() {
        state.drawing = false;
        hideCapture = false;
        background = new Image();
        jobId = $('#imageId')[0].textContent;
        fontSize = $('#fontSlider')[0].value;
        transparency = true;
        predictionAutofill = predictionEngine !== undefined;
        textFieldEdit = false;
        loadJob(jobId, state, () => loadImage(jobId, background));

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
            mainCanvas.width = background.width;
            mainCanvas.height = background.height;
            drawingCanvas.width = background.width;
            drawingCanvas.height = background.height;
            mainCtx.drawImage(background, 0, 0);
            document.fonts.load(fontSize+  "px Exo").then(() => {
                draw();
            })
            setCaptureMode(CaptureModes.LETTER);
            $('#full-screen-load').delay(500).fadeOut();
            $('#main-content-container').delay(750).fadeIn();
        }
    }

    function generateCropAndPredict(index, rectangle) {
        $('.alert-prediction').show();
        $('#prediction').text("Predicting...");
        let r  = rectangle;
        cropCanvas.width = 64;
        cropCanvas.height = 64;
        cropCtx.clearRect(0, 0, cropCanvas.width, cropCanvas.height);
        cropCtx.drawImage(background, r.x1, r.y1, r.width, r.height, 0, 0, 64, 64);
        let imgSelector = $('#renderedCrop');
        let img = imgSelector[0];
        img.width = 64;
        img.height = 64;
        img.src = cropCanvas.toDataURL("image/png");
        imgSelector.off().on('load', (ev => tf.tidy( () => {callPredictionEngine(img, index, rectangle)})));

    }


    function callPredictionEngine(img, index, rectangle) {
        predictionEngine.tensorFlowPrediction(img).then((predictionResult) => {
            if (predictionAutofill && predictionResult !== undefined && state.eligibleForPredictions(index, rectangle)) {
                state.characterLabels[index] = predictionResult;
                $('#prediction').text("Prediction: " + predictionResult);
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
    const predictionEngine = new PredictionEngine();
    predictionEngine.init().then(r => captureCanvasInit(r  ? predictionEngine : undefined));
});