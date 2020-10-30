package com.github.etkachev.nxwebstorm.utils

import com.intellij.ui.components.JBTextField
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JTable
import javax.swing.RowFilter
import javax.swing.RowSorter
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.TableModel
import javax.swing.table.TableRowSorter

fun createRowFilter(
  table: JTable,
  defaultValue: String = "",
  fieldColumns: Int = 15
): JBTextField {
  var rs: RowSorter<out TableModel?>? = table.rowSorter
  if (rs == null) {
    table.autoCreateRowSorter = true
    rs = table.rowSorter
  }
  val rowSorter: TableRowSorter<out TableModel?> = (if (rs is TableRowSorter<*>) rs else null)
    ?: throw NoSuchElementException("Cannot find appropriate rowSorter: $rs")
  val tf = JBTextField(defaultValue, fieldColumns)
  tf.document.addDocumentListener(object : DocumentListener {
    override fun insertUpdate(e: DocumentEvent) {
      update()
    }

    override fun removeUpdate(e: DocumentEvent) {
      update()
    }

    override fun changedUpdate(e: DocumentEvent) {
      update()
    }

    private fun update() {
      val text = tf.text
      if (text.trim { it <= ' ' }.isEmpty()) {
        rowSorter.setRowFilter(null)
      } else {
        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)$text"))
      }
    }
  })
  return tf
}

class KeyUpDownListener(
  private val table: JTable,
  private val field: JBTextField
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
    }
  }

  private fun makeTableSelection() {
    val rowSelected = table.selectedRow
    val text = table.getValueAt(rowSelected, 0).toString()
    field.text = text
    table.clearSelection()
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
