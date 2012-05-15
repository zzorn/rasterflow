package org.rasterflow.layer

import org.rasterflow.channel.Raster
import org.rasterflow.util.Rectangle

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


}

