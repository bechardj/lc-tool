$('.nav-link').filter((i,item) => item.href === window.location.href)
    .attr("href","#")
    .parent('.nav-item')
    .addClass('active');