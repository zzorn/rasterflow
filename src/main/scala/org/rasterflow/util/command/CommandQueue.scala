package org.rasterflow.util.command

import java.awt.event.{ActionEvent, KeyEvent}
import javax.swing.{KeyStroke, AbstractAction, Action}

/**
 * A command queue object, for keeping undo and redo queues.
 *
 * @param document the document to apply the commands to
 */
// TODO: Handle commands that may take some time without blocking.
// TODO: Also provide cancel for current command, or whole command queue
// TODO: Also provide progress indicator for currently queued operations (individual or all)
class CommandQueue[T](document: T) {

  private var undoQueue: List[(Command[T], Object)] = Nil
  private var redoQueue: List[(Command[T], Object)] = Nil
  private var listeners: List[()=>Unit] = Nil

  /**
   * Runs the specified command, stores it in the undo-queue if it is undoable.
   */
  def doCommand(command: Command[T]) {
    runCommand(command)
  }


  /**
   * Undoes the last command or redo, if it is possible to undo.
   */
  def undo() {
    if (canUndo) {
      // Pop the undo queue head
      val (command, undoData) = undoQueue.head
      undoQueue = undoQueue.tail

      // Run undo
      val redoData = command.undoAction(document, undoData)

      // Store redo data in redo queue
      redoQueue = (command, redoData) :: redoQueue

      notifyListeners()
    }
  }

  /**
   * Redoes the last undo, if it is possible to redo.
   */
  def redo() {
    if (canRedo) {
      // Pop redo queue head
      val (command, redoData) = redoQueue.head
      redoQueue = redoQueue.tail

      // Run redo action, if one exists, otherwise run the original action again
      val undoData = if (command.redoAction != null) command.redoAction(document, redoData)
      else command.action(document)

      // Store undo data in undo queue
      undoQueue = (command, undoData) :: undoQueue

      notifyListeners()
    }
  }

  /**
   * @return true if we can undo the last command
   */
  def canUndo: Boolean = !undoQueue.isEmpty && undoQueue.head._1.canUndo(document)

  /**
   * @return true if we can redo the last undo
   */
  def canRedo: Boolean = !redoQueue.isEmpty

  /**
   * Creates a swing action that provides undo.
   */
  def createUndoAction: Action = new AbstractAction("Undo") {
    putValue(Action.SHORT_DESCRIPTION, "Undo the last operation")
    putValue(Action.LONG_DESCRIPTION, "Undo the last operation")
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U)
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_DOWN_MASK))

    setEnabled(canUndo)

    addListener(() => {
      setEnabled(canUndo)
    })

    def actionPerformed(e: ActionEvent) {
      undo()
    }
  }


  /**
   * Creates a swing action that provides redo.
   */
  def createRedoAction: Action = new AbstractAction("Redo") {
    putValue(Action.SHORT_DESCRIPTION, "Redo the last undoed operation")
    putValue(Action.LONG_DESCRIPTION, "Redo the last undoed operation")
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R)
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK))

    setEnabled(canRedo)

    addListener(() => {
      setEnabled(canRedo)
    })

    def actionPerformed(e: ActionEvent) {
      redo()
    }
  }


  /**
   * Adds a listener that is notified when a command is done, undone, or redone.
   */
  def addListener(listener : () => Unit) {
    listeners ::= listener
  }




  private def runCommand(command: Command[T]) {
    // Clear undo and redo queues if required
    if (command.clearUndoQueue) {
      undoQueue = Nil
      redoQueue = Nil
    }

    // Run the command
    val undoData = command.action(document)

    // Store undo data, if the command has an undo action
    if (command.undoAction != null) undoQueue ::= (command, undoData)

    notifyListeners()
  }


  private def notifyListeners() {
    listeners foreach (_())
  }

}



