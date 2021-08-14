import { StatisticsInfo } from "../statistics.js";

function initLanding() {
    $('#loginButton').click(() => {
        window.location.href = '/login';
    });
    let statisticsInfo = new StatisticsInfo('/statistics');
    statisticsInfo.init();
    $('#dismissReleaseNotes').click(function(event) {
        event.preventDefault();
        let href = $(this).attr('href');
        $.get(href);
        $('#releaseNotes').slideUp();
    });
}

initLanding();

