package org.rasterflow.operation

import org.rasterflow.Picture
import org.rasterflow.tile.TileId

/**
 *
 */
object RedoOperation extends Operation[AnyRef, AnyRef] {

  def description = "Redo"
  def endOperation(target: Picture, affectedTiles: Set[TileId]) = null
  def undo(target: Picture, undoData: AnyRef) = null
}