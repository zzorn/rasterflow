package org.rasterflow.util.command

/**
 * An action that can be undone or redone.
 *
 * @param description Description of the action, for menu entries and such.
 * @param action The action to do when first applied.  Returns optional data object that is passed to undo action.
 * @param undoAction Function that undoes the action, given a data object generated when the action was first done, or later redone.  Returns data object passed to the redo action.
 * @param redoAction Redoes the an undone action.  Receives a data object from the undo, returns data object for undo.
 * @param canUndo Function that returns true if this action can be undone.  Assumed true if not provided, and there is an undo function.
 * @param clearUndoQueue flag to indicate whether all previous entries in the undo-redo queue should be cleared when this action is ran.
 * @tparam T the type of document the command should be applied to.
 */
case class Command[T](description: String,
                      action: (T) => Object,
                      undoAction: (T, Object) => Object,
                      redoAction: (T, Object) => Object = null,
                      canUndo: (T) => Boolean = null,
                      clearUndoQueue: Boolean = false) {

  /**
   * A simple non-undoable command.
   *
   * @param description Description of the action, for menu entries and such.
   * @param action The action to do when first applied.
   */
  def this(description: String,
           action: (T) => Unit) {
    this(description, (x: T) => {
      action(x); null
    }, null, null, null, false)
  }
}
