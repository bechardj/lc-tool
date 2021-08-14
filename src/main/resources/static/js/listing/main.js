function initListing() {
    $('#jobListing').dataTable( {
        "iDisplayLength": 25,
        "initComplete": function() {
            $('#full-screen-load').delay(100).fadeOut();
            $('#main-content-container').delay(250).fadeIn();
        }
    } );

    $('#upload').click(() => {
        $('#fileUploadInput').click();
    })

    $('#imageUpload').fileupload({
        maxFileSize: 4000000,
        acceptFileTypes:  /(\.|\/)(jpe?g|png)$/i,
        })
        .on('fileuploadstart', function (e, data) {
            $('#imageUpload').fileupload('disable');
            $('#main-content-container').delay(100).fadeOut();
            $('#full-screen-load').delay(250).fadeIn();
        }).on('fileuploadfail', function (e, data) {
            $('#full-screen-load').fadeOut();
            notify("One or more uploads failed!", 5000);
            setTimeout(() => {location.reload();}, 5000);
        }).on('fileuploaddone', function (e, data) {
            let progress = parseInt(data.loaded / data.total * 100, 10);
            if (progress === 100) {
                location.reload();
            }
        });

    $('.confirm-delete').click ( e  => {
            if(!confirm("Delete This File?")) {
                e.preventDefault();
            }
        }
    )

}
initListing();