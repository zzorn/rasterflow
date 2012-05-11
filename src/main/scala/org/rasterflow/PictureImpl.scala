package org.rasterflow

import change.{Changes, Change}
import tile.TileId
import util.command.CommandQueue

/**
 *
 */
class PictureImpl extends Picture {

  private var _layers: List[Layer] = Nil

  private val commandQueue = new CommandQueue[PictureImpl](this)

  def layer(name: Symbol): Option[Layer] = layers.find(_.identifier == name)

  def layers: List[Layer] = _layers

  def addLayer(layer: Layer) {
    require(layer != null)
    require(!_layers.contains(layer))

    _layers = _layers ::: List(layer)

    // onPictureChanged()
  }

  def removeLayer(layer: Layer) {
    require(layer != null)
    require(_layers.contains(layer))

    _layers -= layer

    // onPictureChanged()
  }


  /**
   * Retrieves the id:s of the tiles that need to be redrawn, and clears the dirty status at the same time.
   */
  def getAndClearDirtyTiles(): Set[TileId] = {
    val tiles = dirtyTiles
    clearDirtyTiles()
    tiles
  }

  /**
   * Retrieves the id:s of the tiles that need to be redrawn.
   */
  def dirtyTiles: Set[TileId] = {
    var dirty: Set[TileId] = Set[TileId]()
    _layers foreach {
      (l: Layer) =>
        dirty ++= l.getDirtyTiles
    }
    dirty
  }

  /**
   * Clears the dirty status of the tiles in the picture.
   */
  def clearDirtyTiles() {
    _layers foreach (_.clearDirtyTiles())
  }

  def takeUndoSnapshot(): Change = {
    Changes(_layers.map(_.takeUndoSnapshot()))
  }

  def hasDirtyTiles: Boolean = !dirtyTiles.isEmpty

}

