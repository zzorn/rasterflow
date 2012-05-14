package org.rasterflow

import layer.Layer
import operation.Operation

/**
 *
 */
trait Picture {

  def layer(layerName: Symbol): Option[Layer]

  def addLayer(layerName: Symbol, layer: Layer)

  def removeLayer(layerName: Symbol)

  def doOperation(operation: Operation[_ <: AnyRef, _ <: AnyRef])


}