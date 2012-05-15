package org.rasterflow.operation

import java.awt.event.{ActionEvent, KeyEvent}
import javax.swing.{KeyStroke, AbstractAction, Action}
import org.rasterflow.Picture

/**
 * An operation object queue, for keeping undo and redo queues.
 */
class OperationQueue(picture: Picture) {

  private var undoQueue: List[OperationEntry] = Nil
  private var redoQueue: List[OperationEntry] = Nil

  /**
   * Runs the specified command, stores it in the undo-queue if it is undoable.
   */
  def doCommand[U <: AnyRef, R <: AnyRef](operation: Operation[U, R]) {
    if (operation == UndoOperation) undo()
    else if (operation == RedoOperation) redo()
    else {
      // Clear undo and redo queues if required
      if (operation.shouldClearUndoQueue) {
        undoQueue = Nil
        redoQueue = Nil
      }

      // Create entry, and store it if requested
      val entry = new OperationEntry(picture, operation)
      if (operation.storeInUndoQueue) {
        undoQueue ::= entry
      }

      // Run the command
      entry.doOperation()
    }
  }


  /**
   * Undoes the last command or redo, if it is possible to undo.
   */
  def undo() {
    if (canUndo) {
      // Pop the undo queue head
      val entry = undoQueue.head
      undoQueue = undoQueue.tail

      // Run undo
      entry.undo()

      // Store entry in redo queue
      redoQueue = entry :: redoQueue
    }
  }

  /**
   * Redoes the last undo, if it is possible to redo.
   */
  def redo() {
    if (canRedo) {
      // Pop redo queue head
      val entry = redoQueue.head
      redoQueue = redoQueue.tail

      // Run redo action
      entry.redo()

      // Store entry in undo queue
      undoQueue = entry :: undoQueue
    }
  }

  /**
   * @return true if we can undo the last command
   */
  def canUndo: Boolean = !undoQueue.isEmpty && undoQueue.head.canUndo

  /**
   * @return true if we can redo the last undo
   */
  def canRedo: Boolean = !redoQueue.isEmpty

}



