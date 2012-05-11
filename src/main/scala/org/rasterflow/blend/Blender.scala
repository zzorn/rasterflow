package org.rasterflow.blend

import org.rasterflow.tile.{DataTile, TileService, Tile}


/**
 * Trait for functions that blend a layer with an underlying one.
 */
trait Blender {

  def name: String = getClass.getSimpleName

  def blendBackground(under: Tile, over: Tile, alpha: Tile): Tile = {
    val result = TileService.allocateDataTile(under)
    blendData(result, over, alpha)
    result
  }

  def blendData(target: DataTile, over: Tile, alpha: Tile)
}
