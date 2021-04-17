function init() {
    $('#jobListing').dataTable( {
        "iDisplayLength": 25,
        "initComplete": function() {
            $('#full-screen-load').delay(100).fadeOut();
            $('#main-content-container').delay(250).fadeIn();
        }
    } );
}
init();