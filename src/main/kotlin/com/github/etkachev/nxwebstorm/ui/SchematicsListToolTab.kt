package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.actionlisteners.ReFetchSchematicsListener
import com.github.etkachev.nxwebstorm.actionlisteners.SchematicSelectionTabListener
import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.services.MyProjectService
import com.github.etkachev.nxwebstorm.services.NodeDebugConfigState
import com.github.etkachev.nxwebstorm.ui.buttons.SchematicActionButtons
import com.github.etkachev.nxwebstorm.utils.FindAllSchematics
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.dsl.gridLayout.VerticalAlign
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
  private val nxService = MyProjectService.getInstance(project)
  private var reFetchListener: ReFetchSchematicsListener = ReFetchSchematicsListener(
    toolWindow,
    contentFactory,
    tabName,
    this,
    schematicFetcher
  )

  /**
   * Return function that will be used within `ReFetchSchematicListener` to cleanup un-needed listener.
   * Honestly I don't know if this is automatically cleaned up with the garbage collector, but just in case...
   */
  private fun getRemoveSelectionListener(table: JBTable, listener: SchematicSelectionTabListener): () -> Unit {
    return { removeRowSelectionListener(table, listener) }
  }

  /**
   * function that is used to remove list selection listener for the given table passed in.
   */
  private fun removeRowSelectionListener(table: JBTable, listener: SchematicSelectionTabListener) {
    table.selectionModel.removeListSelectionListener(listener)
  }

  private fun getRefreshButton(removeListener: () -> Unit): ActionButton {
    val actionGroup = DefaultActionGroup()
    actionGroup.add(SchematicActionButtons.refresh(this.reFetchListener.getActionListener(removeListener)))
    val myActions = actionGroup.getChildren(null)
    val firstBtn = myActions[0]
    val presentation = firstBtn.templatePresentation
    return ActionButton(firstBtn, presentation, ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)
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

    if (!nxService.nxDebugConfigSetup) {
      NodeDebugConfigState.getInstance(project).setupDebugConfig()
      nxService.setNxDebugConfigSetupDone()
    }

    val refreshButton = this.getRefreshButton(removeListener)
    refreshButton.isEnabled = true

    val border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    return panel {
      row {
        cell(searchField)
        cell(refreshButton)
      }.layout(RowLayout.PARENT_GRID)
      row {
        scrollCell(table).resizableColumn().horizontalAlign(HorizontalAlign.FILL).verticalAlign(VerticalAlign.FILL)
      }.layout(RowLayout.PARENT_GRID).resizableRow()
    }.withBorder(border)
  }
}
