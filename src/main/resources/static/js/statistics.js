let statistics;



function graph() {
    const allCharCtx = $("#allCharacterChart")[0].getContext("2d");
    let chart = new Chart(allCharCtx, {
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
            legend: {
                display: false,
            }
        }
    });

    const upperCharCtx = $("#upperCharacterChart")[0].getContext("2d");
    let upperChart = new Chart(upperCharCtx, {
        type: 'bar',
        data: {
            labels: Object.keys(statistics.upperFrequency),
            datasets: [{
                label: 'Character Label Frequency',
                backgroundColor: 'rgb(217,52,90)',
                borderColor: 'rgb(217,52,90)',
                data: Object.values(statistics.upperFrequency)
            }]
        },
        options: {
            legend: {
                display: false,
            }
        }
    });

    const lowerCharCtx = $("#lowerCharacterChart")[0].getContext("2d");
    let lowerChart = new Chart(lowerCharCtx, {
        type: 'bar',
        data: {
            labels: Object.keys(statistics.lowerFrequency),
            datasets: [{
                label: 'Character Label Frequency',
                backgroundColor: 'rgb(217,52,90)',
                borderColor: 'rgb(217,52,90)',
                data: Object.values(statistics.lowerFrequency)
            }]
        },
        options: {
            legend: {
                display: false,
            }
        }
    });

    const punctuationCharCtx = $("#punctuationCharCtx")[0].getContext("2d");
    let punctuationChart = new Chart(punctuationCharCtx, {
        type: 'bar',
        data: {
            labels: Object.keys(statistics.punctuationFrequency),
            datasets: [{
                label: 'Character Label Frequency',
                backgroundColor: 'rgb(217,52,90)',
                borderColor: 'rgb(217,52,90)',
                data: Object.values(statistics.punctuationFrequency)
            }]
        },
        options: {
            legend: {
                display: false,
            },
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
            graph();
            addLabels();
        });
}

$(window).on('load', function() { init(); console.log("init");})