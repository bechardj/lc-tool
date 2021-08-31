
function showModal($modal) {
    $('.modal-content').hide();
    $modal.show();
    $('#modal-overlay').fadeIn(100);
}

function firebaseModal() {
    return new Promise((resolve, reject) => {
        if (firebaseLoggedIn) {
            getBearerTokenWithPrompt().then((token) => resolve(token));
        } else {
            $('#firebaseSignIn').off().click(() => {
                getBearerTokenWithPrompt().then((token) => {
                    $('#modal-overlay').fadeOut(100);
                    resolve(token);
                });
            })
            $('#firebaseSignInIgnore').off().click(() => {
                $('#modal-overlay').fadeOut(100);
                reject();
            })
            const $modal = $('#firebaseModal');
            showModal($modal);
        }
    });
}

