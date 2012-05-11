package org.rasterflow.change

import org.rasterflow.Picture


/**
 * Stores a change done to a picture, to allow undoing and redoing it.
 */
trait Change {

  def undo(picture: Picture)

  def redo(picture: Picture)


}