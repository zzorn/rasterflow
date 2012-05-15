package org.rasterflow.blend

import org.rasterflow.tile.TileId
import org.rasterflow.channel.Channel
import org.rasterflow.operation.Operation
import org.rasterflow.Picture
import org.rasterflow.change.{TileChange, Changes}

/**
 * Wraps a blend along with the target of it in an operation, allowing it to be executed in multi-core fashion.
 */
case class BlendOperation(blender: Blender,
                          layer: Symbol,
                          tiles: Set[TileId],
                          targetChannel: Symbol,
                          topChannel: Symbol,
                          alphaChannel: Symbol) extends Operation[Changes, Unit] {

  def description = blender.name

  private var snapshot: TileChange = null

  override def startOperation(target: Picture): Set[TileId] = {
    snapshot = target.layerChannel(layer, targetChannel).get.takeSnapshot(layer)
    tiles
  }

  def endOperation(target: Picture, affectedTiles: Set[TileId]) {
    val undoData = snapshot
    snapshot = null
    undoData
  }

  def undo(target: Picture, undoData: Changes) {
    undoData.undo(target)
  }

  override def processTile(target: Picture, tile: TileId) {
    blender.blendData(
      target.layerChannel(layer, targetChannel).get.getTileForModification(tile),
      target.layerChannel(layer, topChannel).get.getTile(tile),
      target.layerChannel(layer, alphaChannel).get.getTile(tile))
  }
}