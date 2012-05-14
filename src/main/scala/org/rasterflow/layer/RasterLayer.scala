package org.rasterflow.layer

import _root_.org.flowpaint.util.Rectangle
import org.flowpaint.raster.picture.Picture
import org.flowpaint.model2.Operation
import org.flowpaint.raster.channel.Raster
import org.rasterflow.channel.Raster
import org.rasterflow.util.Rectangle
import org.rasterflow.tasks.TileTask

/**
 * A layer with raster data, rendering the data on top of the provided raster data.
 */
class RasterLayer() extends Layer {
  var raster: Raster = new Raster()

  override def channel(name: Symbol) = raster.channels.get(name)

  def channels = null //raster.channels

  def renderLayer(area: Rectangle, targetRaster: Raster) {
    targetRaster.overlay(raster, area)
  }

  override def runOperation(operation: TileTask) {
    val affectedChannels = operation.affectedChannels(picture, this)
    val tiles = operation.affectedTiles(picture, this)

    tiles foreach {tileId =>
      val tiles = (affectedChannels map (c => (c, channel(c).get))).toMap
      //operation.renderToTile(picture, this, tileId, tiles)
    }
  }

}

