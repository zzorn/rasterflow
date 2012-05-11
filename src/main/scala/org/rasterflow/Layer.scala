package org.rasterflow

import change.Change
import channel.{Raster, Channel}
import tile.TileId
import util.Rectangle

/**
 *
 */
trait Layer {

  private var _identifier: Symbol = 'layer

  def picture: PictureImpl

  def identifier: Symbol = _identifier

  def setIdentifier(id: Symbol) {
    require(id != null)
    _identifier = id
    // onLayerChanged()
  }


  def channel(name: Symbol): Option[Channel] = None

  def channels: Map[Symbol, Channel]

  /**
   * Render the layer on top of the specified target raster and data.
   * The target raster and data already contain the combined underlying layer raster and data information.
   * Only the area falling within the specified area need to be rendered.
   */
  def renderLayer(area: Rectangle, targetRaster: Raster) //, targetData: DataMap)

  /**
   * Retrieves the id:s of the tiles that need to be redrawn.
   */
  def getDirtyTiles: Set[TileId] = {
    var dirty: Set[TileId] = Set()
    channels.values foreach (c => dirty ++= c.dirtyTileIds)
    dirty
  }

  /**
   * Clears the dirty status of the tiles in the layer.
   */
  def clearDirtyTiles() {
    channels.values foreach (_.cleanDirtyTiles())
  }

  def takeUndoSnapshot(): Change = {
    null //Changes(channels.values.map(_.takeSnapshot(identifier)))
  }

  /*
  def runOperation(operation: Operation) {
    throw new UnsupportedOperationException("Not implemented for this layer type")
  }
*/

}

