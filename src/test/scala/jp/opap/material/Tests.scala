package jp.opap.material

import java.io.InputStream

import com.google.common.io.ByteStreams

object Tests {
  def getResource(path: String): Array[Byte] = {
    ByteStreams.toByteArray(this.getClass.getClassLoader.getResourceAsStream(path))
  }

  def getResourceAsStrean(path: String): InputStream = {
    this.getClass.getClassLoader.getResourceAsStream(path)
  }
}
