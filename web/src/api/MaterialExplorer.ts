import Repository from "@/models/Repository";
import RepositoryItem from "@/models/RepositoryItem";
import ThumbnailFile from "@/models/ThumbnailFile";

// この変数（プレースホルダ）は webpack によって置換されます。
declare let SERVICE_HOST: string;

export default class MaterialExplorer {
    static host: string = SERVICE_HOST;
    static resolve: (path: string) => string = (path) => MaterialExplorer.host + path;
    static request: (path: string) => Promise<Response> = (path) => fetch(MaterialExplorer.resolve(path));

    public static readonly resources = (function() {
        let resolve = MaterialExplorer.resolve;
        let request = MaterialExplorer.request;
        let ok: (r: Response) => Promise<Response> = r => r.ok ? Promise.resolve(r) : Promise.reject(r);

        return {
            person: () => request("/person")
                .then(ok)
                .then(r => r.json()),
            repositories: () => request("/repositories")
                .then(ok)
                .then(r => r.json()) as Promise<{ items: Repository[] }>,
            items: () => request("/items")
                .then(ok)
                .then(r => r.json()) as Promise<{ items: RepositoryItem[] }>,
            images: () => request("/images")
                .then(ok)
                .then(r => r.json() as Promise<{ items: ThumbnailFile[] }>),
            thumbnail: (fileId: String) =>  resolve("/thumbnail/" + fileId)
        };
    })();
}
