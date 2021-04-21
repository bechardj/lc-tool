import { StatisticsInfo } from "../statistics.js";

function initLanding() {
    $('#loginButton').click(() => {
        popupLogin(() => {
            window.location.href = '/secure/listing';
        }, true)
    });
    let statisticsInfo = new StatisticsInfo('/calculateStatistics');
    statisticsInfo.init();
}

initLanding();

