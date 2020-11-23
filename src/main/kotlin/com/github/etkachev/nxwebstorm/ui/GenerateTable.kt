package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.models.SearchTableModel
import com.github.etkachev.nxwebstorm.utils.createRowFilter
import com.github.etkachev.nxwebstorm.utils.getSchematicData
import com.intellij.ui.table.JBTable
import java.util.Vector
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableModel

class GenerateTable(data: Map<String, SchematicInfo>) {
  var fullData = getSchematicData(data)

  fun getTable(): SearchTableModel {
    val tableModel = createTableModel()
    val table = JBTable(tableModel)
    table.removeColumn(table.columnModel.getColumn(3))
    val filterField = createRowFilter(table)
    return SearchTableModel(filterField, table)
  }

  private fun createTableModel(): TableModel {
    val columns: Vector<String> = Vector(listOf("Collection", "Name", "Description", "TypeData"))
    val rows: Vector<Vector<Any>> = Vector()
    for (info in fullData) {
      val v: Vector<Any> = Vector()
      v.add(info.type.collection)
      v.add(info.id)
      v.add(info.description)
      v.add(info.type.type.data)
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
