import * as $ from "jquery";
import { MaterialExplorer } from "./rest-client";

$(function() {
    let rest = MaterialExplorer.resources;

    rest.repositories()
        .then(data => {
            let elements = data.items
                .map(item => {
                    var sub = (s: number, l: number) => item.lastActivityAt.substr(s, l);
                    var date = sub(0, 4) + "/" + sub(5, 2) + "/" + sub(8, 2) + " " + sub(11, 8);
                    let html = "<tr><td>" + item.name + "</td><td>" + item.id + "</td><td>" + date + "</td></tr>";
                    return html;
                }).join("");

            $("#projects").append(elements);
        })
        .catch();
});
