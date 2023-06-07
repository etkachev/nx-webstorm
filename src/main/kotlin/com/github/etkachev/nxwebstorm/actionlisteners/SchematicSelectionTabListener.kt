package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.models.FormValueMap
import com.github.etkachev.nxwebstorm.models.SchematicCommandData
import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.models.SchematicRunData
import com.github.etkachev.nxwebstorm.models.SchematicTypeEnum
import com.github.etkachev.nxwebstorm.services.MyProjectService
import com.github.etkachev.nxwebstorm.services.NodeDebugConfigState
import com.github.etkachev.nxwebstorm.ui.RunSchematicPanel
import com.github.etkachev.nxwebstorm.ui.RunTerminalWindow
import com.github.etkachev.nxwebstorm.utils.foldListOfMaps
import com.github.etkachev.nxwebstorm.utils.getSchematicCommandArgs
import com.github.etkachev.nxwebstorm.utils.getSchematicIdFromTableSelect
import com.github.etkachev.nxwebstorm.utils.splitSchematicId
import com.google.gson.JsonArray
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import javax.swing.SwingUtilities
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class SchematicSelectionTabListener(
  private val project: Project,
  private val table: JBTable,
  private val schematics: Map<String, SchematicInfo>,
  private val toolWindow: ToolWindow,
  private val searchField: JBTextField
) :
  ListSelectionListener {
  private var dryRunTerminal = RunTerminalWindow(project, "Dry Run")
  private var runTerminal = RunTerminalWindow(project, "Run")
  private var tabName = "Generate - Schematic"
  private var nxService = MyProjectService.getInstance(project)

  private fun dryRunAction(
    runData: SchematicRunData
  ): () -> Unit {
    return { run(runData) }
  }

  private fun runAction(
    runData: SchematicRunData
  ): () -> Unit {
    return { run(runData, false) }
  }

  private fun debugAction(
    runData: SchematicRunData
  ): () -> Unit {
    return { runDebug(runData) }
  }

  private fun runDebug(
    runData: SchematicRunData,
    dryRun: Boolean = true
  ) {
    val (collection, id, formMap, required, type, collectionPath) = runData
    val values = formMap.formVal
    if (isMissingRequiredFields(required, values)) {
      return
    }
    val dryRunArg = if (dryRun) "true" else "false"
    val command = when (type) {
      SchematicTypeEnum.CUSTOM_NX -> "workspace-schematic"
      SchematicTypeEnum.PROVIDED -> "generate"
      SchematicTypeEnum.CUSTOM_ANGULAR -> ""
    }
    val projDir = this.project.basePath
    val name = when (type) {
      SchematicTypeEnum.CUSTOM_NX -> id
      SchematicTypeEnum.PROVIDED -> "$collection:$id"
      SchematicTypeEnum.CUSTOM_ANGULAR -> "$projDir/$collectionPath:$id"
    }
    val args = foldListOfMaps(arrayOf(values, mapOf(Pair("no-interactive", "true"), Pair("dry-run", dryRunArg))))
    NodeDebugConfigState.getInstance(this.project).execute(command, name, args, type)
  }

  private fun run(
    runData: SchematicRunData,
    dryRun: Boolean = true
  ) {
    val (collection, id, formMap, required, type, collectionPath) = runData
    val values = formMap.formVal
    if (isMissingRequiredFields(required, values)) {
      return
    }
    val projectType = this.nxService.nxProjectType
    val schematicCommandData = SchematicCommandData(projectType, type, collectionPath)
    val isPnpm = this.nxService.isPnpm
    val commands = getSchematicCommandArgs(collection, id, values, schematicCommandData, dryRun, isPnpm)
    if (dryRun) {
      dryRunTerminal.runAndShow(commands.joinToString(" "))
    } else {
      runTerminal.runAndShow(commands.joinToString(" "))
    }
  }

  private fun isMissingRequiredFields(required: JsonArray?, values: MutableMap<String, String>): Boolean {
    if (required == null) {
      return false
    }

    val mappedRequired = required.mapNotNull { r -> r.asString }.toTypedArray()
    val missing =
      values.keys.filter { k -> mappedRequired.contains(k) && (values[k] == null || values[k]!!.trim() == "") }
    if (missing.count() == 0) {
      return false
    }

    val joinedKeys = missing.joinToString()
    val message = "Missing the following required fields: $joinedKeys"
    Messages.showMessageDialog(message, "Oops", Messages.getWarningIcon())
    return true
  }

  override fun valueChanged(e: ListSelectionEvent?) {
    if (e == null || e.valueIsAdjusting) {
      return
    }
    SwingUtilities.invokeLater { runSelectedLogic() }
  }

  private fun runSelectedLogic() {
    val selectedRow = table.selectedRow
    if (selectedRow == -1) {
      return
    }
    val fullId = getSchematicIdFromTableSelect(table, selectedRow, schematics) ?: return
    val info = schematics[fullId] ?: return
    val schematicInfo = splitSchematicId(fullId) ?: return
    val collection = schematicInfo.collection
    val id = schematicInfo.id
    val formMap = FormValueMap()
    val schematicPanel = RunSchematicPanel(project, id, info.fileLocation, formMap)
    val required = schematicPanel.required
    val runData = SchematicRunData(collection, id, formMap, required, schematicInfo.type, info.collectionPath)
    val panel = schematicPanel.generateCenterPanel(
      withBorder = true,
      addButtons = true,
      dryRunAction = this.dryRunAction(runData),
      runAction = this.runAction(runData),
      debugAction = this.debugAction(runData)
    )
    val scrollPane = JBScrollPane(panel)
    val content = ContentFactory.getInstance().createContent(scrollPane, tabName, false)
    val existingTab = toolWindow.contentManager.findContent(tabName)
    if (existingTab != null) {
      toolWindow.contentManager.removeContent(existingTab, true)
    }
    toolWindow.contentManager.addContent(content)
    toolWindow.contentManager.setSelectedContent(content)
    searchField.text = ""

    table.clearSelection()
  }
}
