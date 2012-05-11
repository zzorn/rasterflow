package org.rasterflow.tasks

import java.lang.Object
import java.util.{HashSet, LinkedList}
import javax.swing.SwingUtilities
import java.util.concurrent.{TimeUnit, Executors}
import org.rasterflow.tile.TileId

/**
 * Allows spawning render jobs for tiles on different threads, speeding up the application on multi-core
 * processors, and allowing rendering to be done in the background.
 */
object TaskService {

  private var listeners: List[TaskListener] = Nil

  private val queue: LinkedList[Operation] = new LinkedList[Operation]()
  private val tilesLeft: HashSet[TileId] = new HashSet[TileId]()
  private var initialTilesLeft: Int = 0
  private var currentOperation: Operation = null

  private val lock = new Object()

  // Create a new executor with thread number equal to available processors
  private val taskExecutor = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors())

  /**
   * Run an operation, splitting it into tiles, and processing each tile separately, using all available cores.
   */
  def queueOperation(operation: Operation) {
    require(operation != null)

    if (operationRunning) {
      lock synchronized {
        queue.addFirst(operation)
      }
    }
    else {
      lock synchronized {
        startOperation(operation)
      }
    }
  }

  def operationRunning: Boolean = {
    lock synchronized {
      currentOperation != null
    }
  }

  def stopAll() {
    // Shut down ongoing tasks, and wait for a while for them to finish / stop
    taskExecutor.shutdownNow()
    taskExecutor.awaitTermination(10, TimeUnit.MINUTES)

    lock synchronized {
      // Cleanup
      queue.clear()
      tilesLeft.clear()
      val ongoingOp = currentOperation
      currentOperation = null
      initialTilesLeft = 0

      // Notify listeners of stop
      if (ongoingOp != null) {
        SwingUtilities.invokeLater(new Runnable {
          def run() {
            listeners foreach {
              _.onFinished(ongoingOp)
            }
          }
        })
      }
    }
  }

  def addTaskListener(listener: TaskListener) {
    listeners ::= listener
  }

  def removeTaskListener(listener: TaskListener) {
    listeners = listeners.filterNot(_ == listener)
  }

  /** This function should only be called from a synchronized context */
  private def startOperation(operation: Operation) {
    currentOperation = operation

    // Keep track of the tiles that are part of the operation
    tilesLeft.clear()
    operation.affectedTiles foreach {tileId =>
      tilesLeft.add(tileId)
    }
    initialTilesLeft = tilesLeft.size()

    // Notify any listeners, in the main event dispatch thread
    SwingUtilities.invokeLater(new Runnable {
      def run() {
        listeners foreach {
          _.onStarted(operation)
        }
      }
    })

    // Schedule calculation of the tiles
    operation.affectedTiles foreach {tileId =>
        taskExecutor.submit(Task(operation, tileId))
    }
  }

  private def removeRemainingTileId(tileId: TileId) {
    lock synchronized {
      tilesLeft.remove(tileId)

      if (tilesLeft.isEmpty) {
        onOperationFinished()
      }
      else {
        // Notify any listeners about progress, in the main event dispatch thread
        val op = currentOperation
        val progress = if (initialTilesLeft == 0) 1.0f else 1.0f - 1.0f * tilesLeft.size() / initialTilesLeft
        SwingUtilities.invokeLater(new Runnable {
          def run() {
            listeners foreach {
              _.onProgress(op, progress)
            }
          }
        })
      }
    }
  }

  /** This function should only be called from a synchronized context */
  private def onOperationFinished() {
    val completedOperation = currentOperation
    currentOperation = null

    // Notify any listeners, in the main event dispatch thread
    SwingUtilities.invokeLater(new Runnable {
      def run() {
        listeners foreach {
          _.onFinished(completedOperation)
        }
      }
    })

    // Start next operation if one is queued
    if (!queue.isEmpty) {
      startOperation(queue.removeLast())
    }
  }

  private final case class Task(operation: Operation, tileId: TileId) extends Runnable {
    def run() {
      try {
        operation.doOperation(tileId)
      }
      finally {
        removeRemainingTileId(tileId)
      }
    }
  }


}