import * as $ from "jquery";
import { MaterialExplorer } from "./rest-client";
import { toDictionary } from "./data";

$(function() {
    let rest = MaterialExplorer.resources;

    let table = $("#projects");
    let itemTable = $("#project-items")
    let loading = $("#loader");

    rest.images()
        .then(data => {
            let elements = data.items.map(item => {
                let card = $('<div class="ui fluid card"></div>')
                    .append('<div class="image"><img src="' + rest.thumbnail(item.thumbnail.fileId) + '"></div>')
                    .append('<div class="content">'
                        + '<a class="header">' + item.file.head.name + '</a>'
                        + '<div class="meta"><span class="date">' + item.file.head.path + '</span></div>'
                        + '</div>')
                return $('<div class="column"></div>')
                    .append(card);
            });
            $("#thumbnails").append(elements);
        })

    Promise.all([rest.repositories(), rest.items()])
        .then(dataSet => {
            let repositories = dataSet[0].items;
            let repositoryitems = dataSet[1].items;
            let repositoryDictionary = toDictionary(repositories, e => e.id);


            let elements = repositories.map(item => {
                var sub = (s: number, l: number) => item.lastActivityAt.substr(s, l);
                var date = sub(0, 4) + "/" + sub(5, 2) + "/" + sub(8, 2) + " " + sub(11, 8);
                let html = "<tr><td>" + item.name + "</td><td>" + item.title + "</td><td>" + date + "</td></tr>";
                return html;
            }).join("");

            let itemElements = repositoryitems.map(item => {
                let repository = repositoryDictionary[item.projectId]
                let html = "<tr><td>" + repository.name + "（" + repository.title + "）</td><td>" + item.path + "</td></tr>";
                return html;
            }).join("");

            loading.hide();
            table.find("tbody")
                .append(elements);
            itemTable.find("tbody")
                .append(itemElements);
            table.add(itemTable).show();
        }).catch(error => {
            loading.hide();
            $("#error-message").show()
        });
});
