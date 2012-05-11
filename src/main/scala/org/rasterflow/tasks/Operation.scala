package org.rasterflow.tasks

import org.rasterflow.tile.TileId


/**
 * Some operation that can be applied to a picture.
 * Can be run in a separate worker thread, so should not have external shared dependencies, and should be thread safe.
 */
// TODO: Take immutable snapshot of picture, and pass it as argument to doOperation?
trait Operation {

  /**
   * Description of the operation, for undo stack UI etc.
   */
  def description: String

  /**
   * Returns the id:s of the tiles that the operation will change.
   */
  def affectedTiles: Set[TileId]

  /**
   * Apply the operation to the affected tile.
   * Called by the TaskService from many threads, with all the tileId:s returned by affectedTiles.
   */
  def doOperation(tileId: TileId)

}