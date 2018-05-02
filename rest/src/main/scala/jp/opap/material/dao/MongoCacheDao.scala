package jp.opap.material.dao

import com.mongodb.client.MongoDatabase

class MongoCacheDao(mongo: MongoDatabase) extends GridFsDao(mongo) with CacheDao {
  override def insert(key: String, data: Array[Byte]): Unit = {
    val stream = this.bucket.openUploadStream(key)
    stream.write(data)
    stream.close()
  }

  override def findByKey(key: String): Option[Array[Byte]] = {
    val stream = this.bucket.openDownloadStream(key)
    val length = stream.getGridFSFile.getLength.toInt
    if (length > 0) {
      val data = new Array[Byte](length)
      stream.read(data)
      Some(data)
    } else {
      None
    }
  }
}
