package org.rasterflow.layer

import util.Rectangle
import org.rasterflow.util.Rectangle
import org.rasterflow.channel.{Raster, Channel}
import org.rasterflow.tile.TileId
import org.rasterflow.change.Change
import org.rasterflow.{Picture, PictureImpl}
import org.rasterflow.tasks.TileTask

/**
 *
 */
trait Layer {

  def identifier: Symbol

  def channels: Map[Symbol, Channel]

  def channel(name: Symbol): Option[Channel] = channels.get(name)

  /**
   * Returns the id:s of the tiles that need to be redrawn.
   */
  def getDirtyTiles: Set[TileId]

  /**
   * Clears the dirty status of the tiles in the layer.
   */
  def clearDirtyTiles()

  def takeUndoSnapshot(): Change

//  def runOperation(operation: Operation)
}

