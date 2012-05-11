package org.rasterflow.change

import org.rasterflow.Picture

/**
 * Several changes
 */
case class Changes(changes: List[Change]) extends Change {
  def redo(picture: Picture) {
    changes foreach (_.redo(picture))
  }

  def undo(picture: Picture) {
    changes foreach (_.undo(picture))
  }
}
