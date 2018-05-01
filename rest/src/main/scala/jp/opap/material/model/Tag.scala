package jp.opap.material.model

import jp.opap.material.model.Manifest.normalize
import jp.opap.material.model.Tag.TagName

case class Tag(names: List[TagName], generic: Option[TagName])

object Tag {
  def create(names: List[String], generic: Option[String]): Tag = {
    Tag(names.map(new TagName(_)), generic.map(new TagName(_)))
  }

  case class TagName(name: String, normalized: String) {
    def this(name: String) = this(name, normalize(name))

    override def equals(obj: Any): Boolean = obj match {
      case t: TagName => this.normalized == t.normalized
      case _ => false
    }
  }
}
