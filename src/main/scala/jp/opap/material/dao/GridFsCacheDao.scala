package jp.opap.material.dao

import java.io.ByteArrayOutputStream

import com.mongodb.MongoGridFSException
import com.mongodb.client.MongoDatabase
import com.mongodb.client.gridfs.GridFSUploadStream

class GridFsCacheDao(mongo: MongoDatabase) extends GridFsDao(mongo) with CacheDao {
  override def insert(key: String, data: Array[Byte]): Unit = {
    var stream: GridFSUploadStream = null
    try {
      stream = this.bucket.openUploadStream(key)
      stream.write(data)
    } finally {
      if (stream != null)
        stream.close()
    }
  }

  override def findByKey(key: String): Option[Array[Byte]] = {
    val baos = new ByteArrayOutputStream()
    try {
      this.bucket.downloadToStream(key, baos)
      Some(baos.toByteArray)
    } catch {
      case _: MongoGridFSException =>  None
    } finally {
      baos.close()
    }
  }

  override def loadIfAbsent(key: String, supplier: String => Array[Byte]): Array[Byte] = {
    this.findByKey(key) match {
      case Some(data) => data
      case None =>
        val data = supplier(key)
        this.insert(key, data)
        data
    }
  }
}
