package org.rasterflow

/**
 *
 */
trait Picture {

  def layer(layerName: Symbol): Option[Layer]

}