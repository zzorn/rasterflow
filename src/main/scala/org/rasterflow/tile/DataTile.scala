package org.rasterflow.tile


/**
 * A tile with custom data for each position.
 */
final class DataTile() extends Tile {

  val data: Array[Float] = new Array[Float](width * height)

  def update(index: Int, value: Float) {
    data(index) = value
  }

  def apply(index: Int) = data(index)

  def copy(): DataTile = TileService.allocateDataTile(this)

  def fillWith(value: Float) {
    var i = pixelCount - 1;
    while (i >= 0) {
      data(i) = value;
      i -= 1;
    }
  }

  def copyDataFrom(source: DataTile) {
    System.arraycopy(source.data, 0, data, 0, pixelCount)
  }

}