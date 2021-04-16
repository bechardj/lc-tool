class PredictionEngine{
    constructor() {
        this.model = undefined;
        this.labels = undefined;
    }

    async init() {
        try {
            await tf.ready();
            await tf.loadLayersModel('/localModels/model.json')
                .then(model => {
                    console.log("Loaded local model.");
                    this.model = model;
                    this.retrieveLocalLabels();
                })
                .catch(err => {
                    console.log("Falling back to included model...");
                    tf.loadLayersModel('/ml/model.json')
                        .then(model => this.retrieveIncludedLabels(model));
                });
        } catch (e) {
            console.error("Failed to initialize prediction engine!");
            return false;
        }
        console.log(this.labels);
        return true;
    }

    validateLabels(labels) {
        for (let i = 0; i < labels.length; i++) {
            if(labels[i].length !== 1) {
                console.error("Encountered label longer than length 1 at index ", i);
                return false;
            }
        }
        return true;
    }

    retrieveIncludedLabels() {
        $.getJSON("/ml/labels.json",
            (response) => {
                this.labels = response.predictionLabels;
            },)
            .done()
            .fail(function( jqxhr, textStatus, error ) {
                let err = textStatus + ", " + error;
                console.log( "Request Failed: " + err );
            });
    }

    retrieveLocalLabels() {
        $.getJSON("/localModels/labels.json",
            (response) => {
                if (!this.validateLabels(response.predictionLabels)) {
                    console.log("Falling back to included labels (prediction result max index corresponds to 26 lower characters a-z)")
                    this.retrieveIncludedLabels();
                }
            })
            .fail(( jqxhr, textStatus, error ) => {
                let err = textStatus + ", " + error;
                console.warn("WARNING: You are using a local model without providing a labels.json file \n"
                    + "To see an example, look at http://" + window.location.host + "/ml/labels.json");
                console.log("Falling back to included labels (prediction result max index corresponds to 26 lower characters a-z)")
                this.retrieveIncludedLabels();
            });
    }

    async tensorFlowPrediction(img) {
        let input = tf.browser.fromPixels(img).mean(2)
            .toFloat()
            .expandDims(0)
            .expandDims(-1)
            .mul(0.003921569);
        let prediction = this.model.predict(input);

        let predictionData = await prediction.data();
        let index_label = predictionData.indexOf(Math.max(...predictionData));
        return this.labels[index_label];

    }

}

export {PredictionEngine};