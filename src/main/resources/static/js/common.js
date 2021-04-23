let firebaseLoggedIn;

$('.nav-link').filter((i, item) => item.href === window.location.href)
    .attr("href", "#")
    .parent('.nav-item')
    .addClass('active');

if (window.location.href.includes("testenv") || window.location.href.includes("demo")) {
    $('.demo-warning')
        .show();
}

function notify(message, delay) {
    $('.notifications').show();
    setTimeout(() => {
        $('.notifications').hide();
    }, delay);
    $('.toast-body')[0].innerHTML = message;
    $('.toast').data("delay", delay).toast('show');
}

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

function initLogin(id) {
    var ui = new firebaseui.auth.AuthUI(firebase.auth());
    ui.start(id, {
        signInSuccessUrl: '/',
        signInOptions: [
            {
                provider: firebase.auth.GoogleAuthProvider.PROVIDER_ID,
                requireDisplayName: true
            }
        ],
        callbacks: {
            signInSuccessWithAuthResult: (authObject, redirectUrl) => {
                firebase.auth().currentUser.getIdToken().then(data => fetch('/firebaseLogin', {
                        method: 'post',
                        headers: {
                            'Accept': 'application/json, text/plain, */*',
                            'Content-Type': 'application/json'
                        },
                        body: data
                    }
                    ).then(response => window.location = "/")
                );
                return false;
            }
        }
    });
}

function getBearerToken() {
    return firebase.auth().currentUser.getIdToken();
}

async function getBearerTokenWithPrompt() {
    if (!firebaseLoggedIn) {
        notify("Login required - If you don't see a prompt, make sure pop-ups are enabled)", 5000);
        await firebase.auth().signInWithPopup(new firebase.auth.GoogleAuthProvider());
    }
    return firebase.auth().currentUser.getIdToken();
}

function doBackendAuth(callback) {
    firebase.auth().currentUser.getIdToken().then(data => fetch('/firebaseLogin', {
            method: 'post',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json'
            },
            body: data
        }).then(response => callback.call())
    );

}
// function doBackendAuth(callback) {
//     firebase.auth().currentUser.getIdToken().then(data => {
//         $.ajax({
//             type: "POST",
//             headers: {
//                 'Accept': 'application/json, text/plain, */*',
//                 'Content-Type': 'application/json'
//             },
//             url: '/firebaseLogin',
//             body: data,
//             success: function (data) {
//                 console.log("submission success");
//                 callback.call();
//             },
//             error: function (XMLHttpRequest, textStatus, errorThrown) {
//                 console.log(errorThrown);
//             }
//         });
//         }
//     );

// }

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
        console.log("Logged in.")
    } else {
        //$('.req-auth').hide();
        firebaseLoggedIn = false;
        console.log("Not logged in.")
    }
    $('.wait-for-auth').removeClass('hidden-occupy');
});

firebase.auth();

