package org.rasterflow.util.tasks

/**
 * Listener that is notified of the progress of some task.
 */
trait TaskListener[T <: Task] {

  /**
   * Called just before the specified task is started.
   */
  def onStarted(task: T)

  /**
   * Called during the progress of the task.
   * @param progress the progress of the task, from 0f to 1f
   */
  def onProgress(task: T, progress: Float)

  /**
   * Called just after the specified task has finished.
   */
  def onFinished(task: T)

}