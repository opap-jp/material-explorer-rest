package jp.opap.material.dao

trait CacheDao {
  def insert(key: String, data: Array[Byte]): Unit
  def findByKey(key: String): Option[Array[Byte]]
}
