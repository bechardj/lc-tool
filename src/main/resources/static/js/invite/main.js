$('#inviteListing').dataTable( {
    "iDisplayLength": 25,
    "initComplete": function() {
        $('#full-screen-load').delay(50).fadeOut();
        $('#main-content-container').delay(150).fadeIn();
    }
} );