import { StatisticsInfo } from "../statistics.js";

function initLanding() {
    $('#loginButton').click(() => {
        window.location.href = '/login';
    });
    let statisticsInfo = new StatisticsInfo('/statistics');
    statisticsInfo.init();
}

initLanding();

