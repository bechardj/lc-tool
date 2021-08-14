class PredictionEngine {

    constructor() {
        this.model = undefined;
        this.labels = undefined;
    }

    async init() {
        try {
            await tf.ready();
            await tf.loadLayersModel('/ml/model.json')
                .then(model => {
                    this.model = model;
                    this.retrieveIncludedLabels();
                });
        } catch (e) {
            console.error("Failed to initialize prediction engine!");
            return false;
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

export { PredictionEngine };