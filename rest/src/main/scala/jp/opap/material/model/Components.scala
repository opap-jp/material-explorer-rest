package jp.opap.material.model

import java.util.UUID

import scala.beans.BeanProperty

object Components {
  type FileElement = LeafElement[MetaHead, MetaFile]
  type Component = ComponentElement[MetaHead, MetaFile]
}

sealed trait GenericComponent[H, L]
case class Composite[H, L](head: H, children: Seq[GenericComponent[H, L]]) extends GenericComponent[H, L]
case class Leaf[H, L](head: H, payload: L) extends GenericComponent[H, L]

sealed trait ComponentElement[H, L]
case class CompositeElement[H, L](@BeanProperty head: H) extends ComponentElement[H, L]
case class LeafElement[H, L](@BeanProperty head: H, @BeanProperty payload: L) extends ComponentElement[H, L]

case class IntermediateHead(name: String, path: String)
case class IntermediateLeaf()

case class MetaHead(@BeanProperty id: UUID, @BeanProperty repositoryId: String, @BeanProperty parentId: Option[UUID], @BeanProperty name: String, @BeanProperty path: String)
case class MetaFile()

trait IThumbnail {
  val fileId: UUID
  val  width: Int
  val height: Int
}

case class ThumbnailInfo(@BeanProperty fileId: UUID, @BeanProperty width: Int, @BeanProperty height: Int) extends IThumbnail

case class Thumbnail(@BeanProperty fileId: UUID, @BeanProperty data: Array[Byte], @BeanProperty width: Int, @BeanProperty height: Int) extends IThumbnail
