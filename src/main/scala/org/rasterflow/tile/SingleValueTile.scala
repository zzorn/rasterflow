package org.rasterflow.tile

/**
 * A tile with just one value for all positions.
 */
case class SingleValueTile(value: Float) extends Tile {

  def apply(index: Int) = value

  def update(index: Int, value: Float) {
    throw new UnsupportedOperationException("Not supported")
  }

  def copy() = this
}