package org.rasterflow.channel

import org.rasterflow.util.Rectangle
import org.rasterflow.blend.{OpaqueBlender, Blender}


/**
 *
 */
// TODO: Undo history saving - but do it with the command history - they can store the blocks of the layers that they changed
class Raster {

  private var _channels: Map[Symbol, Channel] = Map()

  // Map from row indexes to map from column indexes to blocks of pixel data.
  //  private val blocks: mutable.Map[Int, Map[Int, Block]] = new HashMap()

  def channels: Map[Symbol, Channel] = _channels

  // TODO: The default tiles for each channel for this raster (-> procedural tiles could also be used - converted to actual raster when modified)
  // TODO: Should getBlocks instantiate missing blocks with default tiles in that case? do we even need it?


  /*
    def getBlocks(area: Rectangle): List[Block] = {
      val xc1 = area.x1 / blockSize
      val yc1 = area.y1 / blockSize
      val xc2 = area.x2 / blockSize
      val yc2 = area.y2 / blockSize

      var result: List[Block] = Nil

      var yc = yc1
      while(yc <= yc2) {
        val columns = blocks.getOrElse(yc, null)
        if (columns != null) {
          var xc = xc1
          while(xc <= xc2) {
            val block = columns.getOrElse(xc, null)
            if (block != null) result = block :: result
            xc += 1
          }
        }

        yc += 1
      }

      result
    }
  */

  /**
   * Renders the specified raster on top of this raster, for the specified area, with the specified blending function.
   * The blending function to use is channel specific.
   */
  def overlay(raster: Raster, area: Rectangle, channelBlenders: Map[Symbol, Blender] = Map()) {

    val alpha = raster.channels.get('alpha)

    raster.channels.values foreach {
      c =>
        val channelId = c.identifier

        // Add missing channels
        if (!_channels.contains(channelId)) _channels += channelId -> new DataChannel(channelId)

        // Get blending func
        val blender = channelBlenders.getOrElse(channelId, OpaqueBlender)

        // Overlay
        _channels(channelId).blend(c, area, alpha, blender)
    }
  }

}

