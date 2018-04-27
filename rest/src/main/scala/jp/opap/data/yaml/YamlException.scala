package jp.opap.data.yaml

import java.io.IOException

sealed trait YamlException extends Throwable {
}

object YamlException {
  case class TypeException(actual: Node) extends YamlException
  case class UnsupportedMappingKeyException(key: Any, value: Any) extends IOException
}
