
class StatisticsInfo {
    constructor(endpoint) {
        this.endpoint = endpoint;
        this.statistics = undefined;
    }

    init() {
        $.getJSON(this.endpoint,
             (response) => {
                this.statistics = response;
                this.graph();
                this.addLabels();
                 $('#full-screen-load').delay(500).fadeOut();
                 $('#main-content-container').delay(750).fadeIn();
            });
    }

    graph() {
        const allCharCtx = $("#allCharacterChart")[0].getContext("2d");
        let chart = new Chart(allCharCtx, {
            type: 'bar',
            data: {
                labels: Object.keys(this.statistics.labelFrequency),
                datasets: [{
                    label: 'Character Label Frequency',
                    backgroundColor: 'rgb(217,52,90)',
                    borderColor: 'rgb(217,52,90)',
                    data: Object.values(this.statistics.labelFrequency)
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
                labels: Object.keys(this.statistics.upperFrequency),
                datasets: [{
                    label: 'Character Label Frequency',
                    backgroundColor: 'rgb(217,52,90)',
                    borderColor: 'rgb(217,52,90)',
                    data: Object.values(this.statistics.upperFrequency)
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
                labels: Object.keys(this.statistics.lowerFrequency),
                datasets: [{
                    label: 'Character Label Frequency',
                    backgroundColor: 'rgb(217,52,90)',
                    borderColor: 'rgb(217,52,90)',
                    data: Object.values(this.statistics.lowerFrequency)
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
                labels: Object.keys(this.statistics.otherFrequency),
                datasets: [{
                    label: 'Character Label Frequency',
                    backgroundColor: 'rgb(217,52,90)',
                    borderColor: 'rgb(217,52,90)',
                    data: Object.values(this.statistics.otherFrequency)
                }]
            },
            options: {
                legend: {
                    display: false,
                },
            }
        });

        const byUserCtx = $("#byUserCtx")[0].getContext("2d");
        let byUserChart = new Chart(byUserCtx, {
            type: 'horizontalBar',
            data: {
                labels: Object.keys(this.statistics.userCounts),
                datasets: [{
                    label: 'User Collected Labels',
                    backgroundColor: 'rgb(217,52,90)',
                    borderColor: 'rgb(217,52,90)',
                    data: Object.values(this.statistics.userCounts)
                }]
            },
            options: {
                indexAxis: 'y',
                legend: {
                    display: false,
                },
            }
        });

    }

    addLabels() {
        $('#totalLabels')[0].innerText += (" " + this.statistics.totalCaptured);
        $('#totalEdited')[0].innerText += (" " + this.statistics.pagesWithData);
        $('#totalCompleted')[0].innerText += (" " + this.statistics.pagesMarkedCompleted);
        $('#generatedTime')[0].innerText += (" " + this.statistics.dateGenerated);
    }
}

export {StatisticsInfo};