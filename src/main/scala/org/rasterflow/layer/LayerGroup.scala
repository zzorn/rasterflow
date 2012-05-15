package org.rasterflow.layer

/**
 * A group of layers, rendered as one.
 */

class LayerGroup() extends Layer {

  def channels = Map()

  // TODO: Override getdirty tiles etc
}

