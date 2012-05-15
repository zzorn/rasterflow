package org.rasterflow

import channel.Channel
import layer.Layer
import operation.Operation

/**
 *
 */
trait Picture {

  def layer(layerName: Symbol): Option[Layer]

  def layerChannel(layerName: Symbol, channelName: Symbol): Option[Channel] = {
    layer(layerName).flatMap(_.channel(channelName))
  }

  def addLayer(layerName: Symbol, layer: Layer)

  def removeLayer(layerName: Symbol)

  def doOperation(operation: Operation[_ <: AnyRef, _ <: AnyRef])


}