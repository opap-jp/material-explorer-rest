package jp.opap.material.dao

import com.mongodb.client.MongoDatabase
import com.mongodb.client.gridfs.{GridFSBucket, GridFSBuckets}

abstract class GridFsDao(val mongo: MongoDatabase) {
  val bucket: GridFSBucket = GridFSBuckets.create(this.mongo)
}
