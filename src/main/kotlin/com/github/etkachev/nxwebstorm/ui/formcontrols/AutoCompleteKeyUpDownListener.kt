package com.github.etkachev.nxwebstorm.ui.formcontrols

import com.intellij.ui.components.JBTextField
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JTable

class AutoCompleteKeyUpDownListener(
  private val table: JTable,
  private val field: JBTextField,
  private val autoCompleteFocusListener: AutoCompleteFocusListenerPopup
) : KeyListener {
  override fun keyTyped(e: KeyEvent?) {
  }

  override fun keyPressed(e: KeyEvent?) {
  }

  override fun keyReleased(e: KeyEvent?) {
    if (e == null) {
      return
    }
    when (e.keyCode) {
      KeyEvent.VK_UP -> cycleTableSelectionUp()
      KeyEvent.VK_DOWN -> cycleTableSelectionDown()
      KeyEvent.VK_ENTER -> makeTableSelection()
      else -> makeSurePopupIsOpen()
    }
  }

  private fun makeSurePopupIsOpen() {
    if (autoCompleteFocusListener.popupExists) {
      return
    }
    autoCompleteFocusListener.openPopup()
  }

  private fun makeTableSelection() {
    val rowSelected = table.selectedRow
    val text = table.getValueAt(rowSelected, 0).toString()
    field.text = text
    table.clearSelection()
    this.autoCompleteFocusListener.closePopup()
  }

  private fun cycleTableSelectionDown() {
    val selModel = table.selectionModel
    val index0 = selModel.minSelectionIndex
    if (index0 == -1) {
      selModel.setSelectionInterval(0, 0)
    } else if (index0 > -1) {
      selModel.setSelectionInterval(index0 + 1, index0 + 1)
    }
  }

  private fun cycleTableSelectionUp() {
    val selModel = table.selectionModel
    val index0 = selModel.minSelectionIndex
    if (index0 > 0) {
      selModel.setSelectionInterval(index0 - 1, index0 - 1)
    }
  }
}
