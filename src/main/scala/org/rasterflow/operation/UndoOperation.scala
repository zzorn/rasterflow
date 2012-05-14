package org.rasterflow.operation

import org.rasterflow.Picture
import org.rasterflow.tile.TileId

/**
 *
 */
object UndoOperation extends Operation[AnyRef, AnyRef] {

  def description = "Undo"
  def endOperation(target: Picture, affectedTiles: Set[TileId]) = null
  def undo(target: Picture, undoData: AnyRef) = null
}