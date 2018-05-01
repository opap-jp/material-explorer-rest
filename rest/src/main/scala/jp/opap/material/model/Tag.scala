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

  case class DeclaredTag(id: UUID, names: List[DeclaredTagName], generic: Option[DeclaredTagName]) extends Tag {
    def this(names: List[String], generic: Option[String], idGenerator: () => UUID) = {
      this(idGenerator(), names.map(new DeclaredTagName(idGenerator(), _)), generic.map(new DeclaredTagName(idGenerator(),_)))
    }
  }

  case class DeclaredTagName(id: UUID, name: String, normalized: String) extends TagName {
    def this(id: UUID, name: String) = this(id, name, normalize(name))

    override def equals(obj: Any): Boolean = obj match {
      case t: DeclaredTagName => this.normalized == t.normalized
      case _ => false
    }
  }

  case class UndeclaredTag(id: UUID, name: String)
}
