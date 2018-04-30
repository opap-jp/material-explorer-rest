package jp.opap.material.model

import jp.opap.material.model.Manifest.normalize
import jp.opap.material.model.Tag.TagName

sealed trait Tag {
  val names: Seq[TagName]
}

object Tag {
  sealed trait TagName {
    val normalized: String
  }

  case class DeclaredTag(names: List[DeclaredTagName], generic: Option[DeclaredTagName]) extends Tag

  object DeclaredTag {
    def create(names: List[String], generic: Option[String]): DeclaredTag = {
      DeclaredTag(names.map(new DeclaredTagName(_)), generic.map(new DeclaredTagName(_)))
    }
  }

  case class DeclaredTagName(name: String, normalized: String) extends TagName {
    def this(name: String) = this(name, normalize(name))

    override def equals(obj: Any): Boolean = obj match {
      case t: DeclaredTagName => this.normalized == t.normalized
      case _ => false
    }
  }

  case class UndeclaredTag(name: String)
}
