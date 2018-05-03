export default interface ThumbnailFile {
    file: {
        id: String,
        repositoryId: String,
        parentId: String,
        name: String,
        path: String,
        blobId: String,
    },
    thumbnail: {
        id: String,
        width: number,
        height: number,
    }
}
