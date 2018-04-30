package jp.opap.material.model

import java.util.UUID

import jp.opap.material.model.Manifest.normalize
import jp.opap.material.model.Tag.TagName

sealed trait Tag {
  val names: Seq[TagName]
}

object Tag {
  sealed trait TagName {
    val id: UUID
    val normalized: String
  }

  case class DeclaredTag(id: UUID, names: List[DeclaredTagName], generic: Option[DeclaredTagName]) extends Tag

  object DeclaredTag {
    def create(names: List[String], generic: Option[String]): DeclaredTag = {
      DeclaredTag(UUID.randomUUID(), names.map(new DeclaredTagName(_)), generic.map(new DeclaredTagName(_)))
    }
  }

  case class DeclaredTagName(id: UUID, name: String, normalized: String) extends TagName {
    def this(name: String) = this(UUID.randomUUID(), name, normalize(name))

    override def equals(obj: Any): Boolean = obj match {
      case t: DeclaredTagName => this.normalized == t.normalized
      case _ => false
    }
  }

  case class UndeclaredTag(id: UUID, name: String)
}
