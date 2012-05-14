package org.rasterflow.util.tasks

/**
 * A task that may have sub-tasks.
 */
trait Task[SubTask] {

  /**
   * Called when the task is started.
   * @return sub task data structures that should be processed.
   */
  def startTask(): Set[SubTask]

  /**
   * Called once per SubTask returned by startTask.
   * Should do the heavy computing.
   */
  def processSubTask(subTask: SubTask)

  /**
   * Called after all sub-tasks have been processed.
   */
  def endTask()
}