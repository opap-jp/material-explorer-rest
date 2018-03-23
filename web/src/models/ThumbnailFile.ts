export default interface ThumbnailFile {
    file: {
        id: String,
        repositoryId: String,
        parentId: String,
        name: String,
        path: String,
    },
    thumbnail: {
        fileId: String,
        width: number,
        height: number,
    }
}
