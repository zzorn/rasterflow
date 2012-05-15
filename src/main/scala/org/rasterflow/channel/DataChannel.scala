package org.rasterflow.channel

import org.rasterflow.util.Rectangle
import org.rasterflow.tile._
import org.rasterflow.change.TileChange
import org.rasterflow.blend.Blender


/**
 *
 * @param identifier id of the channel
 * @param undoEnabled true if this channel should store old versions of tiles for undo snapshots.
 */
// TODO: As a memory optimization, we could check if a tile is 100% the same value after a brush stroke, and replace it with a solid tile in that case..
final class DataChannel(val identifier: Symbol, val undoEnabled: Boolean = true) extends Channel {

  private var _defaultTile: Tile = ZeroTile

  var tiles: Map[TileId, DataTile] = Map()

  private var dirtyTiles: Set[TileId] = Set()
  private var _allDirty: Boolean = true

  private var oldTiles: Map[TileId, DataTile] = Map()
  private var newTiles: Map[TileId, DataTile] = Map()
  private var oldDefaultTile: Tile = null


  /**
   * The tile to use as default for this channel (e.g. a solid value)
   */
  def defaultTile: Tile = _defaultTile

  /**
   * Set the tile to use as default for this channel (e.g. a solid value)
   */
  def setDefaultTile(newDefaultTile: Tile) {
    if (undoEnabled) {
      if (newDefaultTile != defaultTile) {
        oldDefaultTile = defaultTile
        _defaultTile = newDefaultTile
        _allDirty = true
      }
    }
    else _defaultTile = newDefaultTile
  }


  def getTile(tileId: TileId, unmodified: Boolean): Tile = {
    // Check if the specified tile was modified, and we want the unmodified version
    if (unmodified && newTiles.contains(tileId)) {
      // Get old tile or (old) default tile if no oldTile found for the location
      oldTiles.get(tileId).getOrElse( if (oldDefaultTile == null) defaultTile else oldDefaultTile )
    }
    else {
      // The current version is ok
      tiles.get(tileId).getOrElse(defaultTile)
    }
  }

  def getTilesIn(area: Rectangle): Map[TileId, Tile] = {
    var result = Map[TileId, Tile]()
    tiles.iterator foreach {
      entry =>
        if (entry._1.intersects(area)) result += entry
    }
    result
  }

  def getTileIdsIn(area: Rectangle): Set[TileId] = {
    var result = Set[TileId]()
    tiles.iterator foreach {
      entry =>
        if (entry._1.intersects(area)) result += entry._1
    }
    result
  }

  def getValueAt(x: Int, y: Int): Float = {
    val tileId = TileId.forLocation(x, y)
    val tile: Tile = getTile(tileId)
    tile(x - tileId.tileX * TileService.tileWidth, y - tileId.tileY * TileService.tileHeight)
  }

  def getAntialiasedValueAt(x: Float, y: Float): Float = {
    val x1 = x.floor.toInt
    val x2 = x.ceil.toInt
    val y1 = y.floor.toInt
    val y2 = y.ceil.toInt

    if (x1 == x2 && y1 == y2) getValueAt(x1, y1)
    else {
      val xf = x - x1
      val yf = y - y1

      val v1 = getValueAt(x1, y1) * (1f - xf) + getValueAt(x2, y1) * xf
      val v2 = getValueAt(x1, y2) * (1f - xf) + getValueAt(x2, y2) * xf

      v1 * (1f - yf) + v2 * yf
    }
  }

  def setValueAt(x: Int, y: Int, value: Float) {
    val tileId = TileId.forLocation(x, y)
    getTileForModification(tileId).update(x - tileId.tileX * TileService.tileWidth, y - tileId.tileY * TileService.tileHeight, value)
  }


  def blend(over: Channel, area: Rectangle, alpha: Option[Channel], blender: Blender) {
    // Blend the background
    // TODO: Handle undo queue for bg tile too
    _defaultTile = blender.blendBackground(defaultTile, over.defaultTile, if (alpha.isDefined) alpha.get.defaultTile else OneTile)

    // Find the tiles in the area with some content in this layer, the layer to blend, or the channel to blend by
    var tilesToBlend: Set[TileId] = getTileIdsIn(area) ++ over.getTileIdsIn(area)
    if (alpha.isDefined) tilesToBlend ++= alpha.get.getTileIdsIn(area)

    // Blend tiles with data
    tilesToBlend foreach {
      (tid: TileId) =>
        val alphaTile = if (alpha.isDefined) alpha.get.getTile(tid) else OneTile
        blender.blendData(getTileForModification(tid), over.getTile(tid), alphaTile)
    }
  }

  def getTileForModification(tileId: TileId): DataTile = {

    addDirtyTile(tileId)

    if (undoEnabled) {
      if (!newTiles.contains(tileId)) {
        // Store the current state of the tile, so we can undo to it, if there was any edits for the tile
        if (tiles.contains(tileId)) oldTiles += tileId -> tiles(tileId)

        // Create and return a new copy to work on
        val newTile = TileService.allocateDataTile(getTile(tileId))
        newTiles += tileId -> newTile
        tiles -= tileId
        tiles += tileId -> newTile
        newTile
      }
      else tiles(tileId)
    }
    else tiles(tileId)
  }

  private def cleanRecordedChanges() {
    oldTiles = Map()
    newTiles = Map()
    oldDefaultTile = null
  }

  def takeSnapshot(layer: Symbol): TileChange = {
    val change = new TileChange(layer, identifier, oldTiles, newTiles, oldDefaultTile, defaultTile)

    cleanRecordedChanges()

    change
  }

  def undoChange(change: TileChange) {
    cleanRecordedChanges()

    // Remove tiles that were added
    change.newTiles.keySet foreach (t => tiles -= t)

    // Restore tiles that were changed
    tiles ++= change.oldTiles

    // Mark both restored and removed tile positions as dirty
    dirtyTiles ++= change.oldTiles.keySet
    dirtyTiles ++= change.newTiles.keySet

    // If default tile changed, restore it
    if (change.defaultTileChanged) {
      _defaultTile = change.oldDefaultTile
      _allDirty = true
    }
  }

  def redoChange(change: TileChange) {
    cleanRecordedChanges()

    change.oldTiles.keySet foreach (t => tiles -= t)

    // Restore new and updated tiles
    tiles ++= change.newTiles

    // Mark both restored and removed tile positions as dirty
    dirtyTiles ++= change.oldTiles.keySet
    dirtyTiles ++= change.newTiles.keySet

    // If default tile changed, restore it to the new one
    if (change.defaultTileChanged) {
      _defaultTile = change.newDefaultTile
      _allDirty = true
    }
  }

  def cleanDirtyTiles() {
    dirtyTiles = Set()
    _allDirty = false
  }

  def dirtyTileIds = dirtyTiles

  def allDirty: Boolean = _allDirty


  private def addDirtyTile(tileId: TileId) {
    dirtyTiles += tileId
  }

}