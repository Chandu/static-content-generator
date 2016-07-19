// Place third party dependencies in the lib folder
//
// Configure loading modules from the lib directory,
// except 'app' ones,
requirejs.config({
    "baseUrl": "resources/lib",
    "paths": {
      "bootstrap": "bootstrap/javascripts/bootstrap"
    },
    "shim": {
        "bootstrap": ["jquery"]
    }
});

// Load the main app module to start the app
requirejs(["bootstrap"]);
