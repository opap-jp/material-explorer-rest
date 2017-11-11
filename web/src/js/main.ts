import * as $ from "jquery";
import { MaterialExplorer } from "./rest-client";

$(function() {
    let rest = MaterialExplorer.resources;

    let table = $("#projects");
    let loading = $("#loader");
    rest.repositories()
        .then(data => {
            let elements = data.items
                .map(item => {
                    var sub = (s: number, l: number) => item.lastActivityAt.substr(s, l);
                    var date = sub(0, 4) + "/" + sub(5, 2) + "/" + sub(8, 2) + " " + sub(11, 8);
                    let html = "<tr><td>" + item.name + "</td><td>" + item.id + "</td><td>" + date + "</td></tr>";
                    return html;
                }).join("");
            loading.hide();
            table.find("tbody").append(elements);
        })
        .catch(error => {
            table.hide();
            loading.hide();
            $("#error-message").show()
        });
});
