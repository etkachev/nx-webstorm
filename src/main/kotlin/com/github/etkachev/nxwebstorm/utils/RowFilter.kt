package com.github.etkachev.nxwebstorm.utils

import com.intellij.ui.components.JBTextField
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

