package org.rasterflow.tile

import org.rasterflow.util.Rectangle

trait TileId extends Rectangle {
  def width = TileService.tileWidth
  def height = TileService.tileHeight
}

/**
 * Identifier for a cell at the specified cell indexes.
 */
final case class TilePosId(tileX: Int, tileY: Int) extends TileId {
  def x1 = tileX * TileService.tileWidth
  def y1 = tileY * TileService.tileHeight
}

/**
 * Identifier for the default cell used for unchanged backgrounds.
 */
case object DefaultTileId extends TileId {
  def x1 = 0
  def y1 = 0
}

object TileId {

  private val tileIdCacheSizeX = 50
  private val tileIdCacheSizeY = 50
  private val tileIdCache: Array[TilePosId] = new Array[TilePosId](tileIdCacheSizeX * tileIdCacheSizeY);

  def forLocation(canvasX: Int, canvasY: Int): TilePosId = {
    // Integer division is truncated towards zero, so if the canvas coordinates are less
    // than zero we subtract one to ensure the negative tile indexes start with -1.
    val tileX = canvasX / TileService.tileWidth - (if (canvasX < 0) 1 else 0)
    val tileY = canvasY / TileService.tileHeight - (if (canvasY < 0) 1 else 0)

    // Use cached tile id if available, otherwise create new.
    if (tileX >= 0 && tileX < tileIdCacheSizeX &&
      tileY >= 0 && tileY < tileIdCacheSizeY) {

      val index: Int = tileX + tileY * tileIdCacheSizeX

      var cachedId = tileIdCache(index)
      if (cachedId == null) {
        cachedId = new TilePosId(tileX, tileY)
        tileIdCache(index) = cachedId
      }

      cachedId
    }
    else {
      // Outside cached area, create new
      new TilePosId(tileX, tileY)
    }
  }

}
