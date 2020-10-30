package com.github.etkachev.nxwebstorm.ui.formcontrols

import com.github.etkachev.nxwebstorm.models.SearchTableModel
import com.github.etkachev.nxwebstorm.utils.createRowFilter
import com.intellij.ui.table.JBTable
import java.util.Vector
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableModel

class StringListTable(private val list: Array<String>) {
  fun getTable(default: String): SearchTableModel {
    val tableModel = createTableModel()
    val table = JBTable(tableModel)
    table.tableHeader = null
    table.selectionModel.setSelectionInterval(0, 0)
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
