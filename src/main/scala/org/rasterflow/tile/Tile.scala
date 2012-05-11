package org.rasterflow.tile


/**
 * A basic unit of image storage, a constant sized block of values for a single channel.
 */
trait Tile {

  /**
   * @return width of the tile.
   */
  final def width = TileService.tileWidth

  /**
   * @return height of the tile.
   */
  final def height = TileService.tileHeight

  /**
   * @return number of pixels in the tile.
   */
  final def pixelCount = TileService.tilePixelCount

  /**
   * @return the value at the specified position.  The specified position should be inside tile bounds, with 0,0 at upper left corner.
   */
  final def apply(tileX: Int, tileY: Int): Float = apply(tileY * TileService.tileWidth + tileX)

  /**
   * Set the value at the specified position.  The position should be within the tile bounds (0,0 at upper left).
   */
  final def update(tileX: Int, tileY: Int, value: Float) {
    update(tileY * TileService.tileWidth + tileX, value)
  }

  /**
   * @return the value at the specified index.
   */
  def apply(index: Int): Float

  /**
   * Change the value at the specified index.
   */
  def update(index: Int, value: Float)

  /**
   * A new tile which is a copy of this tile.
   * Any changes to this tile will not affect the created copy.
   */
  def copy(): Tile

  /**
   * @return the value at the specified index, multiplied by 255 and converted to an integer.
   */
  final def getByte(index: Int): Int = (255 * apply(index)).toInt

}