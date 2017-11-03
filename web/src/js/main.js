$(function() {
    fetch("//localhost:8080/person")
        .then(response => response.json())
        .then(data => {
            let element = $("#message");
            let message = element.attr("data-text").replace("${name}", data.name);
            element.text(message);
        });
});
