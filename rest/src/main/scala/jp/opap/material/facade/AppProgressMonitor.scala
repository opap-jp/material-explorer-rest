package jp.opap.material.facade

import java.util.concurrent.atomic.AtomicInteger

import org.eclipse.jgit.lib.ProgressMonitor

class AppProgressMonitor extends ProgressMonitor {
  var title: Option[String] = Option.empty
  var totalWorks: Option[Int] = Option.empty
  var totalTasks: Option[Int] = Option.empty
  val completed: AtomicInteger = new AtomicInteger()

  override def isCancelled: Boolean = false

  override def start(totalTasks: Int): Unit = {
    this.totalTasks = Option(totalTasks)
    System.out.println("start")
  }

  override def beginTask(title: String, totalWork: Int): Unit = {
    this.title = Option(title)
    this.totalWorks = Option(totalWork)
    this.completed.set(0)
  }

  override def update(completed: Int): Unit = {
    val progress = this.completed.addAndGet(completed)
    this.title.flatMap(title =>
      this.totalWorks.flatMap(total =>
        this.totalTasks.map(tasks =>
          title + ": (" + progress + "/" + total + "/"+ tasks  + ")"
        )
      )
    )
      .foreach(System.out.println)
  }

  override def endTask(): Unit = {
    System.out.println("endTask")
  }
}
