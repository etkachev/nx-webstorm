package com.github.etkachev.nxwebstorm.ui.formcontrols

import com.github.etkachev.nxwebstorm.models.SearchTableModel
import com.github.etkachev.nxwebstorm.utils.KeyUpDownListener
import com.github.etkachev.nxwebstorm.utils.createRowFilter
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import java.awt.Dimension
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.util.Vector
import javax.swing.JComponent
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableModel

class BasicAutoCompleteField(list: Array<String>) {
  var table: ListTable = ListTable(list)
  var focusListener: FocusListenerPopup? = null

  fun createComponent(default: String?): JComponent? {
    val searchTable = this.table.getTable(default ?: "")
    val chooser = JBPopupFactory.getInstance().createPopupChooserBuilder(searchTable.table)
    this.focusListener = FocusListenerPopup(chooser, searchTable.field)
    searchTable.field.addFocusListener(FocusListenerPopup(chooser, searchTable.field))
    searchTable.field.addKeyListener(KeyUpDownListener(searchTable.table, searchTable.field))
    return searchTable.field
  }
}

class FocusListenerPopup(
  private val chooser: PopupChooserBuilder<Any>,
  private val field: JBTextField
) : FocusListener {
  var popup: JBPopup? = null
  override fun focusGained(e: FocusEvent?) {
    popup = chooser.createPopup()
    chooser.scrollPane.preferredSize = Dimension(300, 300)
    popup!!.setRequestFocus(false)
    popup!!.showUnderneathOf(field)
  }

  override fun focusLost(e: FocusEvent?) {
    this.closePopup()
  }

  fun closePopup() {
    if (this.popup == null || this.popup!!.isDisposed) {
      return
    }

    this.popup!!.closeOk(null)
    this.popup = null
  }
}

class ListTable(private val list: Array<String>) {
  fun getTable(default: String): SearchTableModel {
    val tableModel = createTableModel()
    val table = JBTable(tableModel)
    table.tableHeader = null
    val filterField = createRowFilter(table, default, 25)
    return SearchTableModel(filterField, table)
  }

  private fun createTableModel(): TableModel {
    val columns: Vector<String> = Vector(listOf("Item"))
    val rows: Vector<Vector<Any>> = Vector()
    for (item in list) {
      val v: Vector<Any> = Vector()
      v.add(item)
      rows.add(v)
    }
    return object : DefaultTableModel(rows, columns) {
      override fun isCellEditable(row: Int, column: Int): Boolean {
        return false
      }

      override fun getColumnClass(columnIndex: Int): Class<*>? {
        return String::class.java
      }
    }
  }
}


