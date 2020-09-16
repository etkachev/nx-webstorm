package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.actionlisteners.ReFetchSchematicsListener
import com.github.etkachev.nxwebstorm.actionlisteners.SchematicSelectionTabListener
import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.utils.FindAllSchematics
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.layout.panel
import com.intellij.ui.table.JBTable
import javax.swing.BorderFactory
import javax.swing.JComponent

class SchematicsListToolTab(
  val project: Project,
  private val toolWindow: ToolWindow,
  contentFactory: ContentFactory,
  tabName: String,
  schematicFetcher: FindAllSchematics
) {

  private var reFetchListener: ReFetchSchematicsListener = ReFetchSchematicsListener(
    toolWindow,
    contentFactory,
    tabName, this, schematicFetcher
  )

  private fun getRemoveSelectionListener(table: JBTable, listener: SchematicSelectionTabListener): () -> Unit {
    return { removeRowSelectionListener(table, listener) }
  }

  private fun removeRowSelectionListener(table: JBTable, listener: SchematicSelectionTabListener) {
    table.selectionModel.removeListSelectionListener(listener)
  }

  fun createCenterPanel(schematics: Map<String, SchematicInfo>): JComponent? {
    val generateTable = GenerateTable(schematics)
    val tableData = generateTable.getTable()
    val searchField = tableData.field
    val table = tableData.table
    val listener = SchematicSelectionTabListener(
      project,
      table,
      schematics,
      toolWindow,
      searchField
    )
    table.selectionModel.addListSelectionListener(
      listener
    )
    val removeListener = getRemoveSelectionListener(table, listener)
    val scrollPane = JBScrollPane(table)

    val border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    return panel {
      row {
        searchField()
        right {
          button(
            "Refresh",
            reFetchListener.getActionListener(removeListener)
          )
        }
      }
      row {
        scrollPane()
      }
    }.withBorder(border)
  }
}
