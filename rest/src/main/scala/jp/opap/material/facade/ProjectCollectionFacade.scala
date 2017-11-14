package jp.opap.material.facade

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import jp.opap.material.dao.{MongoItemDao, MongoProjectDao}
import jp.opap.material.model.{Item, Project}
import jp.opap.material.{AppConfiguration, ProjectConfig, ProjectInfo}

import scala.collection.JavaConverters._

class ProjectCollectionFacade(val projectDao: MongoProjectDao, val itemDao: MongoItemDao, val eventEmitter: ProjectDataEventEmitter) {
  // val loaders: Seq[ProjectLoader] = Seq(new GitLabProjectLoader())
  val loaders: Seq[ProjectLoader] = Seq(new ProjectLoaderMock())

  def updateProjects(targets1: Seq[(String, String)], configuration: AppConfiguration): Unit = {
    def loadProjectConfig(): ProjectConfig = {
      val mapper = new ObjectMapper(new YAMLFactory())
      try {
        mapper.readValue(new File(configuration.projects), classOf[ProjectConfig])
      } catch {
        case e: Exception => throw e
      }
    }

    def loadProject(info: ProjectInfo, store: Map[String, Project]): Option[DeferredProject] = {
      this.loaders.view
        .find(loader => loader.shouldDispatch(info.protocol))
        .map(loader => loader.loadByProjectInfo(info, store))
        .filter(project => project.isModified)
    }

    eventEmitter.setRunning(true)

    // 取得を試みるプロジェクトのリスト
    val targets = loadProjectConfig().projects.asScala

    // プロジェクト ID をインデックスとする、データベースに保存されているプロジェクトの辞書
    val storedProjects = this.projectDao.findProjects()
      .map(p => p.id -> p)
      .toMap

    // 変更されたプロジェクトのみ取得します
    val deferredProjects = targets.view.par
      .map(info => loadProject(info, storedProjects))
      .flatMap(p => p.iterator)

    val projectCounter = new AtomicInteger(0)
    val projectCount = deferredProjects.count(_ => true)

    // 変更されたプロジェクトを更新します
    deferredProjects
      .map(_.project)
      .foreach(item => {
        val i = projectCounter.incrementAndGet()
        this.eventEmitter.publish(Progress(i, projectCount, "project", item.name))
        projectDao.updateProject(item)
      })

    // 変更されたプロジェクトのファイルを取得して保存します
    val itemCounter = new AtomicInteger(0)

    val itemCount = deferredProjects
      .map(_.items.count)
      .sum

    deferredProjects.seq.asJava.stream()
      .map[ItemIterable](project => project.items)
      .forEach(items => {
        items.iterator.forEach(entry => {
          val i = itemCounter.incrementAndGet()
          this.eventEmitter.publish(Progress(i, itemCount, "item", entry.path))
          itemDao.update(entry)
        })
      })

    // 参照されなくなったプロジェクトを削除します
    storedProjects.keySet
      .par
      .diff(targets.map(info => info.id).toSet)
      .foreach(projectDao.removeProjectById)

    this.eventEmitter.finish()
  }
}
