package org.rasterflow.blend

import org.rasterflow.tile.TileId
import org.rasterflow.channel.Channel

/**
 * Wraps a blend along with the target of it in an operation, allowing it to be executed in multi-core fashion.
 */
case class BlendOperation(blender: Blender,
                          tiles: Set[TileId],
                          targetChannel: Channel,
                          topChannel: Channel,
                          alphaChannel: Channel) extends TileTask {

  def description = blender.name

  def affectedTiles = tiles

  def doOperation(tileId: TileId) {
    blender.blendData(
      targetChannel.getTileForModification(tileId),
      topChannel.getTile(tileId),
      alphaChannel.getTile(tileId))
  }
}