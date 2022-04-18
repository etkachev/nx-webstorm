package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.actionlisteners.SchematicActionListener
import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class SchematicsListDialog(val project: Project?, private val schematics: Map<String, SchematicInfo>) :
  DialogWrapper(project) {
  var schematicSelection: MutableMap<String, String> = mutableMapOf()

  init {
    super.init()
    schematicSelection = mutableMapOf()
  }

  override fun createCenterPanel(): JComponent? {
    val generateTable = GenerateTable(schematics)
    val tableData = generateTable.getTable()
    val searchField = tableData.field
    searchField.columns = 50
    val table = tableData.table
    table.selectionModel.addListSelectionListener(SchematicActionListener(table, schematics, this))
    return panel {
      row {
        cell(searchField)
      }
      row {
        scrollCell(table)
      }
    }
  }
}
