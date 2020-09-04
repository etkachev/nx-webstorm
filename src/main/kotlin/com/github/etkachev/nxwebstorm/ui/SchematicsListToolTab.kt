package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.actionlisteners.SchematicSelectionTabListener
import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.layout.panel
import javax.swing.BorderFactory
import javax.swing.JComponent

class SchematicsListToolTab(val project: Project, private val schematics: Map<String, SchematicInfo>) {

  fun createCenterPanel(toolWindow: ToolWindow): JComponent? {
    val generateTable = GenerateTable(schematics)
    val tableData = generateTable.getTable()
    val searchField = tableData.field
    val table = tableData.table
    table.selectionModel.addListSelectionListener(
      SchematicSelectionTabListener(
        project,
        table,
        schematics,
        toolWindow,
        searchField
      )
    )
    val scrollPane = JBScrollPane(table)

    val border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    return panel {
      row {
        searchField()
      }
      row {
        scrollPane()
      }
    }.withBorder(border)
  }
}
