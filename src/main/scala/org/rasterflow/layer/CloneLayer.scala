package org.rasterflow.layer

import org.flowpaint.util.Rectangle
import org.flowpaint.raster.picture.Picture
import org.flowpaint.raster.channel.Raster
import org.rasterflow.util.Rectangle

/**
 *
 */
case class CloneLayer(source: Layer) extends Layer {

  def channels = source.channels
}
