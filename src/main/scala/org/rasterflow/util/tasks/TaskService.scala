package org.rasterflow.util.tasks

import java.lang.Object
import javax.swing.SwingUtilities
import java.util.concurrent.{TimeUnit, Executors}
import org.rasterflow.tile.TileId
import java.util.{ArrayList, HashSet, LinkedList}
import scala.collection.JavaConversions._

/**
 * Allows spawning tasks that run subtasks on different threads, speeding up the application on multi-core
 * processors, and allowing rendering to be done in the background.
 */
class TaskService[SubTask, T <: Task[SubTask]] {

  private var listeners: List[TaskListener[T]] = Nil

  private val queue: LinkedList[T] = new LinkedList[T]()
  private val subTasksLeft: ArrayList[SubTask] = new ArrayList[SubTask]()
  private var initialSubTasksLeft: Int = 0
  private var currentTask: T = null.asInstanceOf[T]

  private val lock = new Object()

  // Create a new executor with thread number equal to available processors
  private val taskExecutor = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors())

  /**
   * Run a task, processing any sub-tasks it has on worker threads, using all available cores.
   * If some task is currently ongoing, the added task is queued until the ongoing tasks complete.
   */
  def queueTask(task: T) {
    require(task != null)

    lock synchronized {
      if (currentTask != null) {
        queue.addFirst(task)
      }
      else {
        startOperation(task)
      }
    }
  }

  /**
   * @return true if there is some task currently running.
   */
  def taskRunning: Boolean = {
    lock synchronized {
      currentTask != null
    }
  }

  /**
   * Stops all currently running tasks.
   */
  def stopAll() {
    // Shut down ongoing tasks, and wait for a while for them to finish / stop
    taskExecutor.shutdownNow()
    taskExecutor.awaitTermination(10, TimeUnit.MINUTES)

    lock synchronized {
      // Cleanup
      queue.clear()
      subTasksLeft.clear()
      val ongoingTask = currentTask
      currentTask = null.asInstanceOf[T]
      initialSubTasksLeft = 0

      // Notify listeners of stop
      if (ongoingTask != null) {
        SwingUtilities.invokeLater(new Runnable {
          def run() {
            listeners foreach {
              _.onFinished(ongoingTask)
            }
          }
        })
      }
    }
  }

  /**
   * Adds a listener that is notified of task progression.  The listener is invoked in the Swing event handling thread.
   */
  def addTaskListener(listener: TaskListener[T]) {
    listeners ::= listener
  }

  /**
   * Removes a task listener.
   */
  def removeTaskListener(listener: TaskListener[T]) {
    listeners = listeners.filterNot(_ == listener)
  }



  /** This function should only be called from a synchronized context */
  private def startOperation(task: T) {
    currentTask = task

    // Notify any listeners, in the main event dispatch thread
    SwingUtilities.invokeLater(new Runnable {
      def run() {
        listeners foreach {
          _.onStarted(task)
        }
      }
    })

    // Keep track of the sub-tasks that are part of the task
    subTasksLeft.clear()
    val subTasks: Set[SubTask] = task.startTask()
    if (subTasks != null) subTasks foreach {subTask =>
      subTasksLeft.add(subTask)
    }
    initialSubTasksLeft = subTasksLeft.size()

    // Zero progress
    notifyProgress(task, 0f)

    if (subTasksLeft.isEmpty) {
      // No sub-tasks, end the task.
      onOperationFinished()
    }
    else {
      // Schedule calculation of the sub-tasks
      subTasksLeft foreach {subTask =>
        taskExecutor.submit(SubTaskWrapper(task, subTask))
      }
    }
  }

  private def removeRemainingSubTask(subTask: SubTask) {
    lock synchronized {
      subTasksLeft.remove(subTask)

      if (subTasksLeft.isEmpty) {
        onOperationFinished()
      }
      else {
        // Notify any listeners about progress, in the main event dispatch thread
        val op = currentTask
        val progress = if (initialSubTasksLeft == 0) 1.0f else 1.0f - 1.0f * subTasksLeft.size() / initialSubTasksLeft
        notifyProgress(op, progress)
      }
    }
  }


  private def notifyProgress(op: T, progress: Float) {
    SwingUtilities.invokeLater(new Runnable {
      def run() {
        listeners foreach {
          _.onProgress(op, progress)
        }
      }
    })
  }

  /** This function should only be called from a synchronized context */
  private def onOperationFinished() {
    val completedTask = currentTask
    currentTask = null.asInstanceOf[T]

    // Do any post-processing
    completedTask.endTask()

    // Full progress
    notifyProgress(completedTask, 1f)

    // Notify any listeners, in the main event dispatch thread
    SwingUtilities.invokeLater(new Runnable {
      def run() {
        listeners foreach {
          _.onFinished(completedTask)
        }
      }
    })

    // Start next operation if one is queued
    if (!queue.isEmpty) {
      startOperation(queue.removeLast())
    }
  }

  private final case class SubTaskWrapper(task: T, subTask: SubTask) extends Runnable {
    def run() {
      try {
        task.processSubTask(subTask)
      }
      finally {
        removeRemainingSubTask(subTask)
      }
    }
  }


}