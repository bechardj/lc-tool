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
var ui = new firebaseui.auth.AuthUI(firebase.auth());
ui.start('#firebaseui-auth-container', {
    signInSuccessUrl: '/',
    signInOptions: [
        {
            provider: firebase.auth.GoogleAuthProvider.PROVIDER_ID,
            requireDisplayName: true
        }
    ],
    callbacks: {
        signInSuccessWithAuthResult: (authObject, redirectUrl) => {
            console.log(authObject);
            firebase.auth().currentUser.getIdToken().then(data => fetch('/firebaseLogin', {
                method: 'post',
                headers: {
                    'Accept': 'application/json, text/plain, */*',
                    'Content-Type': 'application/json'
                },
                body: data}
                ).then(response => window.location = "/")
            );
            return false;
        }
    }
});