package org.rasterflow.blend

import org.rasterflow.tile.{TileService, Tile, DataTile}


/**
 * Replaces the target completely with the source without using alpha.
 */
object OpaqueBlender extends Blender {

  def blendData(target: DataTile, over: Tile, alpha: Tile) {
    var i = 0
    while (i < TileService.tilePixelCount) {
      target.data(i) = over(i)
      i += 1
    }
  }

}