let firebaseLoggedIn;

var primaryColor = getComputedStyle(document.documentElement).getPropertyValue("--primary-color").trim();


$('.nav-link').filter((i, item) => item.href === window.location.href)
    .attr("href", "#")
    .parent('.nav-item')
    .addClass('active');

$('.nav-link').click(function() {
    let href = $(this).attr("href");
    if (href !== '#') {
        window.sessionStorage.setItem("loginTarget", href)
    }
})

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
                    }).then(response => response.json())
                        .then(data => {
                            {
                                if (data.error) {
                                    window.location = "/requestInvite";
                                } else {
                                    let target = window.sessionStorage.getItem("loginTarget");
                                    notify("Login Successful", 3000);
                                    if (target != null) {
                                        window.location = target;
                                    } else {
                                        window.location = "/";
                                    }
                                }
                            }
                        })
                );
                return false;
            }
        }
    });
}


async function getBearerTokenWithPrompt() {
    if (!firebaseLoggedIn) {
        notify("Login required - If you don't see a prompt, make sure pop-ups are enabled)", 5000);
        await firebase.auth().signInWithPopup(new firebase.auth.GoogleAuthProvider());
    }
    return firebase.auth().currentUser.getIdToken();
}

firebase.auth().onAuthStateChanged(function (user) {
    firebaseLoggedIn = !!user;
    $('.wait-for-auth').removeClass('hidden-occupy');
});

firebase.auth();

if(window.location.hostname === 'localhost') {
    fetch('/firebaseLogin', {
        method: 'post',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: 'devtoken'
    });
}