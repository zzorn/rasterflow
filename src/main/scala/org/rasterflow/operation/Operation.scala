package org.rasterflow.operation

import org.rasterflow.Picture
import org.rasterflow.util.tasks.Task
import org.rasterflow.tile.TileId

/**
 * An operation on a picture, that can be undone or redone.
 */
// TODO: Concept of open tasks / operations?
// e.g. stroke points are added to a stroke operation one by one,
// a color hue change operation is updated continuously when slider dragged to show preview.
// Only when the button is released / dialog closed is the operation finished, or canceled.
// Could have concept of preview build in, e.g. feed in datapoints such as pen positions / input, or slider positions.
// Those kinds of activities could also just store data internally and generate operation / command queue entry at end, but a common operation trait might be useful.
trait Operation[UndoData <: AnyRef, RedoData <: AnyRef] {

  /**
   * Description of the operation, for undo stack UI etc.
   */
  def description: String

  /**
   * Starts the action, returning any tiles that should be processed.
   * @param target the picture the operation is targeted at.
   * @return any tile ids that should be processed with processTile in worker threads.
   *         Empty or null to continue directly to endOperation.
   */
  def startOperation(target: Picture): Set[TileId] = Set()

  /**
   * Processes a tile, doing any potentially heavy calculation.
   * Called from a worker thread, so do not modify anything else than the tile.
   * Each tile is processed by at most one worker thread at the same time.
   */
  def processTile(target: Picture, tile: TileId) {}

  /**
   * Finishes the operation.  Called after start operation and all processTile calls.
   * @return optional data object that is passed to undo action.
   */
  def endOperation(target: Picture, affectedTiles: Set[TileId]): UndoData

  /**
   * Function that undoes the action, given a data object generated when the action was first done, or later redone.
   * @param undoData data returned by the doOperation, or redo function, can be used to store undo data.
   * @return data object passed to the redo action.
   */
  def undo(target: Picture, undoData: UndoData): RedoData

  /**
   * Redoes the an undone action.  Receives a data object from the undo.
   * @param target the picture the operation is targeted at.
   * @param redoData a data object created when the operation was undoed.
   * @return tiles that should be processed, null or empty set for no processing.
   */
  def startRedo(target: Picture, redoData: RedoData): Set[TileId] = {
    startOperation(target)
  }

  /**
   * Processes a tile during redo.  Delegates to processTile by default.
   */
  def processTileAtRedo(target: Picture, tile: TileId) {
    processTile(target, tile)
  }

  /**
   * Finishes redoing an undone action.  Called after start redo and all processTileAtRedo calls.
   * @return data object for undo.
   */
  def endRedo(target: Picture, redoData: RedoData, affectedTiles: Set[TileId]): UndoData = {
    endOperation(target, affectedTiles)
  }

  /**
   * @return true if this action can be undone.
   */
  def canUndo(target: Picture): Boolean = true

  /**
   * Flag to indicate whether all previous entries in the undo-redo queue should be cleared when this action is run.
   */
  def shouldClearUndoQueue: Boolean = false

  /**
   * @return true if the operation is undoable and should be stored, false if it is should not be stored.
   *         For example undo and redo operation should naturally not be stored, and an operation that does not
   *         change the picture need not be stored.
   */
  def storeInUndoQueue: Boolean = true

}