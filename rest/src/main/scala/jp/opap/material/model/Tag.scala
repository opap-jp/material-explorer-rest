package jp.opap.material.model

import java.util.UUID

import jp.opap.material.model.Manifest.normalize
import jp.opap.material.model.Tag.TagName

case class Tag(id: UUID, names: List[TagName], generic: Option[TagName]) {
  def this(names: List[String], generic: Option[String], idGenerator: () => UUID) = {
    this(idGenerator(), names.map(new TagName(idGenerator(), _)), generic.map(new TagName(idGenerator(), _)))
  }
}

object Tag {
  case class TagName(id: UUID, name: String, normalized: String) {
    def this(id: UUID, name: String) = this(id, name, normalize(name))

    override def equals(obj: Any): Boolean = obj match {
      case t: TagName => this.normalized == t.normalized
      case _ => false
    }
  }
}
