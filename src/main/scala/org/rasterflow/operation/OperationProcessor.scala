package org.rasterflow.operation

import org.rasterflow.tile.TileId
import org.rasterflow.util.tasks.TaskService

/**
 * Thread pool used to run the operations.
 */
object OperationProcessor extends TaskService[TileId, OperationTask]()