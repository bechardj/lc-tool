
function setAlertStatus(status, text) {
    let exportMsg = $('#exportMsg');
    exportMsg.text(text);
    exportMsg.removeClass();
    exportMsg.addClass('alert');
    exportMsg.addClass(status);
    exportMsg.show();
}

$('#exportBtn').click(function() {
    $(this).prop("disabled",true);
    setAlertStatus('alert-warning', 'Running export...');
    let request = $.get("/exportAll")
    request.done(function (data) {
        setAlertStatus('alert-success', 'Complete');
        $('#exportBtn').prop("disabled",false);
    });
    request.fail(function (XMLHttpRequest, textStatus, errorThrown) {
        setAlertStatus('alert-error', 'Export failed! Check console logs...');
        $('#exportBtn').prop("disabled",false);
    });
})