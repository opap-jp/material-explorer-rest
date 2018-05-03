package jp.opap.material.dao

trait CacheDao {
  def insert(key: String, data: Array[Byte]): Unit
  def findByKey(key: String): Option[Array[Byte]]
  def loadIfAbsent(key: String, supplier: String => Array[Byte]): Array[Byte]
}
