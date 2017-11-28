package jp.opap.material.facade

import scala.beans.BeanProperty
import scala.collection.mutable

class RepositoryDataEventEmitter() {
  var _isRunning: Boolean = false
  def setRunning(value: Boolean): Unit = synchronized {
    _isRunning = value
  }
  def getRunning: Boolean = synchronized {
    _isRunning
  }

  protected val listeners: mutable.Set[ProgressListener] = mutable.Set()

  def subscribe(listener: ProgressListener): Unit = synchronized {
    if (_isRunning)
      this.listeners += listener
  }

  def unsubscribe(listener: ProgressListener): Unit = synchronized {
    this.listeners.remove(listener)
  }

  def publish(progress: Progress): Unit = synchronized {
    this.listeners.foreach(listener => listener.onUpdate(progress))
  }

  def finish(): Unit = synchronized {
    this.listeners.foreach(listener => listener.onFinish())
    this.setRunning(false)
  }
}

trait ProgressListener {
  def onUpdate(progress: Progress)
  def onFinish()
}

case class Progress(@BeanProperty current: Int, @BeanProperty max: Int, @BeanProperty processing: String, @BeanProperty name: String)
