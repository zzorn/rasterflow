package org.rasterflow.operation

import org.rasterflow.tile.TileId
import org.rasterflow.util.tasks.Task

/**
 *
 */
trait OperationTask extends Task[TileId] {

  def taskType: Symbol
  def operation: Operation[_ <: AnyRef, _ <: AnyRef]

}