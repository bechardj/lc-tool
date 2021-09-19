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

    const PanState = {
        panning: false,
        panX: 0,
        panY: 0,
        startScreenX: 0,
        startScreenY: 0
    }

    const Colors = {
        PRIMARY: primaryColor,
        BLUE: "#0000FF",
        GREEN: "#00FF00",
        WHITE: "#FFFFFF",
        GREY: "#afafaf"
    }

    // Click Functions

    function clickDown(event) {
        if (!PanState.panning && (event.ctrlKey || event.shiftKey)) {
            PanState.panning = true;
            PanState.panX = event.screenX;
            PanState.panY = event.screenY;
            PanState.startScreenX = window.scrollX;
            PanState.startScreenY = window.scrollY;
        } else if (!state.drawing) {
            $('.alert-prediction').hide();
            const x = event.pageX - mainCanvas.offsetLeft;
            const y = event.pageY - mainCanvas.offsetTop;
            state.startDrawing(x, y)
        }
    }

    function clickUp(event) {
        if (state.drawing) {
            let rectangle = state.stagedCharacterRectangle;
            let rectangleCommitted = state.stopDrawing();
            draw();
            if (rectangleCommitted && event.type !== 'mouseout' && predictionAutofill
                && state.captureMode === CaptureModes.LETTER) {
                generateCropAndPredict(rectangle);
            }
        } else if (PanState.panning) {
            PanState.panning = false;
        }
    }

    function dragHandler(event) {
        if (state.drawing) {
            let mouseX = event.pageX - mainCanvas.offsetLeft;
            let mouseY = event.pageY - mainCanvas.offsetTop;
            const captureMode = state.captureMode;
            if (captureMode === CaptureModes.LETTER && state.stagedCharacterRectangle !== undefined && state.drawing) {
                const lastDrawn = state.stagedCharacterRectangle.labeledRectangle;
                lastDrawn.width = mouseX - lastDrawn.x1;
                lastDrawn.height = mouseY - lastDrawn.y1;
            } else if (captureMode === CaptureModes.WORD && state.stagedWordLine !== undefined && state.drawing) {
                const lastDrawn = state.stagedWordLine.lineSegment;
                lastDrawn.x2 = mouseX;
                lastDrawn.y2 = mouseY;
            } else if (captureMode === CaptureModes.LINE && state.stagedLineLine !== undefined && state.drawing) {
                const lastDrawn = state.stagedLineLine.lineSegment;
                lastDrawn.x2 = mouseX;
                lastDrawn.y2 = mouseY;
            } else if (captureMode === CaptureModes.ERASER) {
                eraser([mouseX, mouseY]);
            }
            draw();
        } else if (PanState.panning) {
            if (event.ctrlKey || event.shiftKey) {
                let offsetY = PanState.panY - event.screenY;
                let offsetX = PanState.panX - event.screenX;
                window.scroll({
                    top: PanState.startScreenY + offsetY,
                    left: PanState.startScreenX + offsetX,
                });
            }
            else {
                PanState.panning = false;
            }
        }
    }

    function eraser(point) {
        const previousCaptureMode = state.previousCaptureMode;
        if (previousCaptureMode === CaptureModes.LETTER) {
            state.renderableCharacterRectangles.forEach((r) => {
                if (r.containsPoint(point)) {
                    state.eraseRectangle(r);
                    r.draw(drawingCtx, Colors.GREY, 2, false, Colors.WHITE);
                    r.drawLabel(drawingCtx, Colors.GREY, 1, Colors.WHITE, "Verdana", fontSize);
                }
            });
        }
        if (previousCaptureMode === CaptureModes.WORD || previousCaptureMode === CaptureModes.LINE) {
            let lines = CaptureModes.WORD === previousCaptureMode ? state.renderableWordLines : state.renderableLineLines;
            lines.forEach(line => {
                if (line.pointNearLine(point, 1)) {
                    if (previousCaptureMode === CaptureModes.WORD) {
                        state.eraseWordLine(line);
                    } else {
                        state.eraseLineLine(line);
                    }
                }
            });
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
                let rectangle = state.stagedCharacterRectangle;
                rectangle.draw(drawingCtx, Colors.PRIMARY, 2, transparency, Colors.WHITE);
            }
            if (captureMode === CaptureModes.WORD) {
                let wordLine = state.stagedWordLine;
                wordLine.draw(drawingCtx, Colors.GREEN, 3);
                state.renderableCharacterRectangles.forEach((r) => {
                    if (wordLine.intersectsRectangle(r)) {
                        r.draw(drawingCtx, Colors.GREEN, 2, false, Colors.WHITE);
                    }
                })
            }
            if (captureMode === CaptureModes.LINE) {
                let lineLine = state.stagedLineLine;
                lineLine.draw(drawingCtx, Colors.BLUE, 3);
                state.renderableCharacterRectangles.forEach((r) => {
                    if (lineLine.intersectsRectangle(r)) {
                        r.draw(drawingCtx, Colors.BLUE, 2, false, Colors.WHITE);
                    }
                })
            }
            drawingCtx.stroke();
        } else if (!state.drawing) {
            drawingCtx.clearRect(0, 0, drawingCanvas.width, drawingCanvas.height);
            drawComplete();
        }
    }

    function drawComplete() {
        clearMainCanvas();
        const captureMode = state.captureMode;
        const previousCaptureMode = state.previousCaptureMode;
        state.renderableCharacterRectangles.forEach(rectangle => {
            let color = Colors.PRIMARY;
            if (state.captureMode === CaptureModes.LINE && state.stagedLineLine !== undefined
                && state.stagedLineLine.intersectsRectangle(rectangle)) {
                color = Colors.BLUE;
            } else if (state.captureMode === CaptureModes.WORD && state.stagedWordLine !== undefined
                && state.stagedWordLine.intersectsRectangle(rectangle)) {
                color = Colors.GREEN;
            }
            rectangle.draw(mainCtx, color, 2, transparency, Colors.WHITE);
            rectangle.drawLabel(mainCtx, Colors.PRIMARY, 1, Colors.WHITE, "Verdana", fontSize);
        })

        if (captureMode === CaptureModes.WORD
            || captureMode === CaptureModes.ERASER && previousCaptureMode === CaptureModes.WORD) {
            state.renderableWordLines.forEach(line => {
                line.draw(mainCtx, Colors.GREEN, 3);
            });
        }
        if (captureMode === CaptureModes.LINE
            || captureMode === CaptureModes.ERASER && previousCaptureMode === CaptureModes.LINE) {
            state.renderableLineLines.forEach(line => {
                line.draw(mainCtx, Colors.BLUE, 3);
            });
        }
    }

    // Button Functions

    function setCaptureMode(mode) {
        if (mode === state.captureMode) {
            if (mode === CaptureModes.ERASER) {
                setCaptureMode(state.previousCaptureMode);
            }
            return;
        }
        state.previousCaptureMode = state.captureMode;
        // this indicator text is currently not visible so probably can be removed unless there are plans to re-implement
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
            notify("Can't change modes while drawing or while rectangle is unlabeled.", 3000);
        }
        $('.capture-mode').addClass('nbs-button-link-disabled');
        if(state.captureMode === CaptureModes.ERASER) {
            $('#eraser').removeClass('nbs-button-link-disabled');
            $('#' + state.previousCaptureMode.toLowerCase() + 'Cap').removeClass('nbs-button-link-disabled');
        } else {
            $('#' + state.captureMode.toLowerCase() + 'Cap').removeClass('nbs-button-link-disabled');
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
                    if (state.captureMode === CaptureModes.LETTER && state.characterCaptureRectangleQueue.length > 0
                        && !state.drawing && !modifier) {
                        // correct the most recent label
                        const uuid = state.characterCaptureRectangleQueue[state.characterCaptureRectangleQueue.length - 1];
                        state.updateDrawnRectangleText(uuid, key);
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

        function toggleHideCaptureData() {
            if (!state.drawing) {
                hideCapture = !hideCapture;
                if (hideCapture) {
                    clearMainCanvas();
                } else {
                    draw();
                }
            }
            return false;
        }

        hotkeys('ctrl+shift+., command+shift+.', toggleHideCaptureData);

        document.addEventListener('keypress', keyHandler);

        $('#saveMenuOption').click(() => {
            save();
            return false;
        });

        $('#downloadMenuOption').click(() => {
            saveJsonLocally();
            return false;
        });

        $('#uploadMenuOption').click(() => {
            $('#imageJobUploadFile').click();
            return false;
        });

        $('#imageJobUploadFile').on('change', () =>{
            $('#jobUploadSubmit').click();
        })

        $('#closeMenuOption').click(() => {
            window.location.href = '/secure/listing';
            return false;
        });

        $('#undoMenuOption').click(() => {
            undo();
            return false;
        });

        $('#redoMenuOption').click(() => {
            redo();
            return false;
        });

        $('#hideCaptureOption').click(() => {
            toggleHideCaptureData();
            return false;
        });

        $('#transparencyOption').click(() => {
            transparency = !transparency;
            draw();
            return false;
        });

        $('#predictionsOption').click(() => {
            predictionAutofill = !predictionAutofill;
            return false;
        });


        $('#jobUploadSubmit').click(function(e) {
            e.preventDefault();
            let agree = confirm("Doing this will merge the existing capture data with capture data from the file you uploaded. Consider making a backup copy using the Download Job Info button.")
            if (agree) {
                window.onbeforeunload = null;
                $('#jobUpload').submit();
            }
        });

        $('#undo').click(undo);
        $('#redo').click(redo);

        $('#letterCap').click(function() {
            setCaptureMode(CaptureModes.LETTER)
        });

        $('#wordCap').click( function() {
            setCaptureMode(CaptureModes.WORD)
        });

        $('#lineCap').click(function() {
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

        if (predictionEngine === undefined) {
            predictionAutofill = false;
            $('#predictionsOption').hide();
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
            state.setNotes($('#notes')[0].value);
            firebaseModal().then(token => {
            $.ajax({
                type: "POST",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + token);
                },
                headers: {
                    'Content-Type': 'application/json'
                },
                url: '/sec/api/saveDoc',
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

    async function init() {
        state.drawing = false;
        hideCapture = false;
        jobId = $('#imageId')[0].textContent;
        fontSize = $('#fontSlider')[0].value;
        transparency = true;
        predictionAutofill = predictionEngine !== undefined;
        textFieldEdit = false;
        await waitForFirebaseAuthState();
        let token = await firebaseModal();
        state.drawCallback = draw;
        await loadJob(jobId, state);
        background = await loadImage(jobId);
        await state.connectState(jobId, token);
        $('#notes')[0].value = state.getNotes();
        // Add event listeners
        initEventHandlersAndListeners();


        /**
         * Ideally, we might want to do some kind of scaling here to fit the browser window.
         * However, most images we are currently using are small enough that this isn't worth
         * the risk of doing some bad math and causing misalignment between the captured data
         * and what actually gets cropped on the backend
         */
        mainCanvas.width = drawingCanvas.width = background.width;
        mainCanvas.height = drawingCanvas.height = background.height;
        mainCtx.drawImage(background, 0, 0);

        if (document.fonts) {
            document.fonts.load(fontSize + "px Verdana");
            document.fonts.ready.then(() => {
                draw();
            })
        }

        setCaptureMode(CaptureModes.LETTER);
        $('#full-screen-load').delay(200).fadeOut();
        $('#main-content-container').delay(200).fadeIn();
    }

    function generateCropAndPredict(rectangle) {
        $('.alert-prediction').show();
        $('#prediction').text("Predicting...");
        let r  = rectangle;
        cropCanvas.width = 64;
        cropCanvas.height = 64;
        cropCtx.clearRect(0, 0, cropCanvas.width, cropCanvas.height);
        cropCtx.drawImage(background, ...rectangle.asArray , 0, 0, 64, 64);
        let imgSelector = $('#renderedCrop');
        let img = imgSelector[0];
        img.width = 64;
        img.height = 64;
        img.src = cropCanvas.toDataURL("image/png");
        imgSelector.off().on('load', (ev => tf.tidy( () => {callPredictionEngine(img, rectangle)})));

    }


    function callPredictionEngine(img, rectangle) {
        predictionEngine.tensorFlowPrediction(img).then((predictionResult) => {
            if (predictionAutofill && predictionResult !== undefined && state.eligibleForPredictions(rectangle)) {
                state.updateDrawnRectangleText(rectangle.uuid, predictionResult);
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