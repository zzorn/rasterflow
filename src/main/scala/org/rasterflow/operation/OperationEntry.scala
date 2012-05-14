package org.rasterflow.operation

import org.rasterflow.Picture
import org.rasterflow.util.tasks.Task
import org.rasterflow.tile.{TileService, TileId}

/**
 *
 */
class OperationEntry(val picture: Picture, val operation: Operation) {

  private var undoData: AnyRef = null
  private var redoData: AnyRef = null
  private var taskStart: Long = 0
  private var memoryEstimate: Double = 0
  private var durationMs: Double = 0

  private val operationTask = new OperationTask {
    var tiles: Set[TileId] = null

    def operation = OperationEntry.this.operation
    def taskType = 'operation

    def startTask(): Set[TileId] = {
      taskStart = System.currentTimeMillis()
      tiles = OperationEntry.this.operation.startOperation(picture)
      tiles
    }

    def processSubTask(tile: TileId) {
      OperationEntry.this.operation.processTile(tile)
    }

    def endTask() {
      undoData = OperationEntry.this.operation.endOperation(picture, tiles)
      redoData = null
      durationMs = System.currentTimeMillis() - taskStart
      memoryEstimate = if (undoData != null) tiles.size else 0.0
    }
  }

  private val redoTask = new Task[TileId] {
    var tiles: Set[TileId] = null
    def operation = OperationEntry.this.operation
    def taskType = 'redo
    def startTask(): Set[TileId] = {tiles = OperationEntry.this.operation.startRedo(picture, redoData); tiles}
    def processSubTask(tile: TileId) {OperationEntry.this.operation.processTileAtRedo(tile)}
    def endTask() {
      undoData = OperationEntry.this.operation.endRedo(picture, tiles)
      redoData = null
    }
  }

  private val undoTask = new Task[TileId] {
    def operation = OperationEntry.this.operation
    def taskType = 'undo
    def startTask(): Set[TileId] = {redoData = OperationEntry.this.operation.undo(picture, undoData); null }
    def processSubTask(tile: TileId) {}
    def endTask() {
      undoData = null
    }
  }

  def doOperation() {
    OperationProcessor.queueTask(operationTask)
  }


  def undo() {
    OperationProcessor.queueTask(undoTask)
  }


  def redo() {
    OperationProcessor.queueTask(redoTask)
  }

  def canUndo: Boolean = {
    operation.canUndo(picture)
  }

  /**
   * Releases any non-essential cached undo and redo data.
   */
  def dropCachedData() {
    undoData = null
    redoData = null
  }

  /**
   * @return true if this action has required undo data.  If false, to undo this operation requires skipping back
   *         in the undo queue until an operation is found with hasUndoData == true,
   *         then redoing from that until the operation before this one.
   */
  def hasUndoData: Boolean = undoData != null

  /**
   * @return estimated or measured memory cost of the operation (both undo and redo data).
   *         No absolute unit, just in relation to other operations.
   *         Used when making decisions of what cached data to drop when memory is running low.
   */
  def operationMemoryCost: Double = memoryEstimate

  /**
   * @return estimated or measured processing time of the operation.
   *         No absolute unit, just in relation to other operations.
   *         Used when making decisions of what cached data to drop when memory is running low.
   */
  def operationProcessingTime: Double = durationMs



}