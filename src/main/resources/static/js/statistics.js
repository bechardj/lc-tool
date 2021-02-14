let statistics;

const canvas = document.getElementById("characterChart");
const ctx = canvas.getContext("2d");

function graph() {
    let chart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: Object.keys(statistics.labelFrequency),
            datasets: [{
                label: 'Character Label Frequency',
                backgroundColor: 'rgb(217,52,90)',
                borderColor: 'rgb(217,52,90)',
                data: Object.values(statistics.labelFrequency)
            }]
        },
        options: {
        }
    });
}

function addLabels() {
    $('#totalLabels')[0].innerText += (" " + statistics.totalCaptured);
    $('#totalEdited')[0].innerText += (" " + statistics.pagesWithData);
    $('#totalCompleted')[0].innerText += (" " + statistics.pagesMarkedCompleted);
    $('#generatedTime')[0].innerText += (" " + statistics.dateGenerated);
}

function init() {
    $.getJSON("/calculateStatistics",
        function(response) {
            statistics = response;
            console.log(JSON.stringify(statistics));
            graph();
            addLabels();
        });
}

$(window).on('load', function() { init(); console.log("init");})