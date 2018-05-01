package jp.opap.material.model

import java.util.UUID

object Components {
  sealed trait IntermediateComponent {
    val id: UUID
    val name: String

    final override def toString: String = {
      this match {
        case dir: IntermediateDirectory => s"${dir.path.getOrElse("/")} (${dir.children.size})"
        case file: IntermediateFile => s"${file.path}"
      }
    }
  }

  /**
    *
    * @param id
    * @param name
    * @param path empty のとき、ルート要素です。
    * @param children
    */
  case class IntermediateDirectory(id: UUID, name: String, path: Option[String], children: Map[String, IntermediateComponent]) extends IntermediateComponent {
  }

  case class IntermediateFile(id: UUID, name: String, path: String) extends IntermediateComponent {
  }
}

