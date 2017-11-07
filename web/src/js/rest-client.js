var MaterialExplorer = MaterialExplorer || (function() {
    var host = "http://localhost:8080";
    var request = (path) => fetch(host + path);

    return {
        resources: {
            person: () => request("/person"),
            repositories: () => request("/repositories"),
        },
    }
})();
