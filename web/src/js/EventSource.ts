declare class EventSource {
    constructor(url: string)
    addEventListener(name: string, listener: (data: any) => void): void;
    close(): void;
}
