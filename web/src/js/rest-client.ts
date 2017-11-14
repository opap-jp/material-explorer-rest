// この変数（プレースホルダ）は webpack によって置換されます。
declare let SERVICE_HOST: string;

export class MaterialExplorer {
    static host: string = SERVICE_HOST;
    static request: (path :string) => Promise<Response> = (path) => fetch(MaterialExplorer.host + path);

    public static readonly resources = (function() {
        let request = MaterialExplorer.request;
        let filterOk: (r: Response) => Promise<Response> = r => r.ok ? Promise.resolve(r) : Promise.reject(r);

        return  {
            person: () => request("/person")
                .then(filterOk)
                .then(r => r.json()),
            repositories: () => request("/repositories")
                .then(filterOk)
                .then(r => r.json()) as Promise<{ items: Repository[] }>,
            items: () => request("/items")
                .then(filterOk)
                .then(r => r.json()) as Promise<{ items: RepositoryItem[] }>,
        };
    })();
}

export interface Repository {
    id: string;
    name: string;
    title: string;
    lastActivityAt: string;
}

export interface RepositoryItem {
    projectId: string;
    path: string;
}
