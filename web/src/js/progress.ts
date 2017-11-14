import * as $ from "jquery";
import { MaterialExplorer } from "./rest-client";

let jQuery: any = $;

$(function() {
    let es = new EventSource("//localhost:8080/progress");
    let projects: any = $("#progress-projects");
    let items: any = $("#progress-items");

    es.addEventListener("message", event => {
        $("#main").show()
        $("#loader").hide()
        var progress = JSON.parse(event.data);

        let element = (progress.processing === "project") ? projects : items;
        if (progress.processing !== "project") {
            projects.progress({
                percent: 100,
                text: {
                    success: "終了"
                }    
            });
        }
        let message = progress.name + "（" + progress.current + "/" + progress.max + "）";
        element.progress({
            duration : 0,
            percent: Math.min((progress.current / progress.max * 100), 99) || 0,
            text: {
                active: message
            }
        });
    });
    es.addEventListener("negative", event => {
        $("#main").show()
        $("#loader").hide()
        $("#message").text("システムの更新処理が終了しました。")
        
        projects.add(items).progress({
            duration: 0,
            percent: 100,
            text: {
                success: "終了"
            }
        })
        es.close();
    });
    es.addEventListener("close", event => {
        $("#message").text("システムの更新処理が終了しました。")

        projects.add(items).progress({
            duration: 0,
            percent: 100,
            text: {
                success: "終了"
            }
        })
        es.close();
    });
});
