$('.nav-link').filter((i,item) => item.href === window.location.href)
    .attr("href","#")
    .parent('.nav-item')
    .addClass('active');

if(window.location.href.includes("testenv") || window.location.href.includes("demo")) {
    $('.demo-warning')
        .show();
}

function notify(message, delay) {
    $('.notifications').show();
    setTimeout(() => {$('.notifications').hide();}, delay);
    $('.toast-body')[0].innerHTML = message;
    $('.toast').data("delay", delay).toast('show');
}