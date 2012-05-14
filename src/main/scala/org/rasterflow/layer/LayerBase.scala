package org.rasterflow.layer

import util.Rectangle
import org.rasterflow.channel.{Raster, Channel}
import org.rasterflow.tile.TileId
import org.rasterflow.{Picture, PictureImpl}
import org.rasterflow.tasks.TileTask
import org.rasterflow.change.{Changes, Change}
import org.rasterflow.util.{ParameterChecker, Rectangle}

/**
 *
 */
class LayerBase extends Layer {

  private var _identifier: Symbol = 'layer

  def identifier: Symbol = _identifier

  def setIdentifier(id: Symbol) {
    ParameterChecker.requireIsIdentifier(id, "id")

    _identifier = id
    // onLayerChanged()
  }


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
    Changes(channels.values.map(_.takeSnapshot(identifier)))
  }

  def runOperation(operation: TileTask) {
    throw new UnsupportedOperationException("Not implemented for this layer type")
  }

}