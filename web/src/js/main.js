$(function() {
    fetch("//localhost:8080/person")
        .then(response => response.json())
        .then(data => {
            let element = $("#message");
            let message = element.attr("data-text").replace("${name}", data.name);
            element.text(message);
        })
        .catch();

    fetch("//localhost:8080/repositories")
        .then(reponse => reponse.json())
        .then(data => {
            let elements = data.items
                .map(item => {
                    let html = "<tr><td>" + item.name + "</td><td>" + item.id + "</td><td>" + item.lastActivityAt + "</td></tr>";
                    return html;
                }).join("");

            $("#projects").append(elements);
        })
        .catch();
});
