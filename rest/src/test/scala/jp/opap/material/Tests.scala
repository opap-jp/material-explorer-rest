package jp.opap.material

import java.io.File

object Tests {
  def getResourceFile(path: String): File = new File(ClassLoader.getSystemResource(path).toURI)
}
