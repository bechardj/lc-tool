$('.nav-link').filter((i,item) => item.href === window.location.href)
    .attr("href","#")
    .parent('.nav-item')
    .addClass('active');

if(window.location.href.includes("testenv") || window.location.href.includes("demo")) {
    $('.demo-warning')
        .show();
}