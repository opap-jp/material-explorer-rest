package jp.opap.material.dao

import com.mongodb.client.MongoDatabase

class MongoCacheDao(mongo: MongoDatabase) extends MongoDao(mongo) {
  override def collectionName = "cache"

  def insert(key: String, data: Array[Byte]): Unit = ???

  def findByKey(key: String): Option[Array[Byte]] = ???
}
