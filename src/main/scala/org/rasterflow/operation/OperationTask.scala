package org.rasterflow.operation

import com.sun.jmx.snmp.tasks.Task
import org.rasterflow.tile.TileId

/**
 *
 */
trait OperationTask extends Task[TileId] {

  def taskType: Symbol
  def operation: Operation[_ <: AnyRef, _ <: AnyRef]

}