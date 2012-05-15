package org.rasterflow.layer

import org.rasterflow.channel.Channel
import org.rasterflow.tile.TileId
import org.rasterflow.change.Change

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


}

