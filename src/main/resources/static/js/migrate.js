
function initMigration() {

    let firebaseLoggedIn;

    var firebaseConfig = {
        apiKey: "AIzaSyDKc9yuouNPNNLjk8M4K2p2nV0j0yLebUY",
        authDomain: "lc-tool.firebaseapp.com",
        projectId: "lc-tool",
        storageBucket: "lc-tool.appspot.com",
        messagingSenderId: "1055126397725",
        appId: "1:1055126397725:web:3a777ad69f49e4a1aff7f4",
        measurementId: "G-N25F94DW2Z"
    };


    // Initialize Firebase
    firebase.initializeApp(firebaseConfig);

    function getBearerToken() {
        return firebase.auth().currentUser.getIdToken();
    }

    async function getBearerTokenWithPrompt() {
        if (!firebaseLoggedIn) {
            await firebase.auth().signInWithPopup(new firebase.auth.GoogleAuthProvider());
        }
        return firebase.auth().currentUser.getIdToken();
    }

    function doBackendAuth(callback) {
        firebase.auth().currentUser.getIdToken().then(data => fetch('/doMigration', {
                method: 'post',
                headers: {
                    'Accept': 'application/json, text/plain, */*',
                    'Content-Type': 'application/json'
                },
                body: data
            }).then(response => callback.call())
        );
    }

    function popupLogin(callback, backendAuth) {
        if (!firebaseLoggedIn) {
            firebase.auth()
                .signInWithPopup(new firebase.auth.GoogleAuthProvider())
                .then((result) => {
                    if (backendAuth) {
                        doBackendAuth(callback);
                    } else {
                        callback.call();
                    }
                });
        } else {
            if (backendAuth) {
                doBackendAuth(callback);
            } else {
                callback.call();
            }
        }
    }

    firebase.auth().onAuthStateChanged(function (user) {
        if (user) {
            firebaseLoggedIn = true;
        } else {
            //$('.req-auth').hide();
            firebaseLoggedIn = false;
        }
        $('.wait-for-auth').removeClass('hidden-occupy');
    });

    function setAlertStatus(status, text) {
        let exportMsg = $('#migrateMsg');
        exportMsg.text(text);
        exportMsg.removeClass();
        exportMsg.addClass('alert');
        exportMsg.addClass(status);
        exportMsg.show();
    }

    $('#migrateBtn').click(
        () => {
            popupLogin(
                () => {
                    $(this).prop("disabled", true);
                    setAlertStatus('alert-warning', 'Migration started. Check console logs for progress information');
                }, true);
        })
}

initMigration();