package org.rasterflow.util

/**
 * A rectangular area defined with integer coordinates.
 */
trait Rectangle {

  def x1: Int

  def y1: Int

  def width: Int

  def height: Int

  final def x2: Int = x1 + width

  final def y2: Int = y1 + height

  final def centerX = x1 + width / 2

  final def centerY = y1 + height / 2

  final def contains(x: Int, y: Int) = x >= x1 && x < x2 && y >= y1 && y < y2

  final def intersects(other: Rectangle): Boolean = {
    intersects(other.x1, other.y1, other.width, other.height)
  }

  final def intersects(x: Int, y: Int, w: Int, h: Int): Boolean = {
    x1 < x + w && x < x2 &&
      y1 < y + h && y < y2
  }

  final def union(r2: Rectangle): Rectangle = {
    val nx1 = x1 min r2.x1
    val ny1 = y1 min r2.y1
    val nw = (x2 max r2.x2) - nx1
    val nh = (y2 max r2.y2) - ny1
    RectangleImpl(nx1, ny1, nw, nh)
  }

  final def iterate(minX: Float, minY: Float, maxX: Float, maxY: Float, visitor: (Int, Int) => Unit) {

    // Use segment bounding box to reduce the area needed to be iterated through
    val sX = math.max(minX.toInt, x1)
    val sY = math.max(minY.toInt, y1)
    val eX = math.min(maxX.toInt, x2)
    val eY = math.min(maxY.toInt, y2)

    var y = sY
    while (y <= eY) {

      var x = sX
      while (x <= eX) {

        visitor(x, y)

        x += 1
      }

      y += 1
    }
  }
}