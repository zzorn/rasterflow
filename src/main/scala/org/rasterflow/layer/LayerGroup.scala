package org.rasterflow.layer

import org.flowpaint.util.Rectangle
import org.flowpaint.raster.picture.Picture
import org.flowpaint.raster.channel.Raster

/**
 * A group of layers, rendered as one.
 */

class LayerGroup() extends Layer {

  def channels = Map()

  // TODO: Override getdirty tiles etc
}

