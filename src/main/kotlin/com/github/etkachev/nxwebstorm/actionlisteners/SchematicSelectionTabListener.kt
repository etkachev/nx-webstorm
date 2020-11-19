package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.models.FormValueMap
import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.services.MyProjectService
import com.github.etkachev.nxwebstorm.services.NodeDebugConfigState
import com.github.etkachev.nxwebstorm.ui.RunSchematicPanel
import com.github.etkachev.nxwebstorm.ui.RunTerminalWindow
import com.github.etkachev.nxwebstorm.utils.findFullSchematicIdByTypeAndId
import com.github.etkachev.nxwebstorm.utils.foldListOfMaps
import com.github.etkachev.nxwebstorm.utils.getSchematicCommandFromValues
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
    type: String,
    id: String,
    formMap: FormValueMap,
    required: JsonArray?
  ): () -> Unit {
    return { run(type, id, formMap, required) }
  }

  private fun runAction(type: String, id: String, formMap: FormValueMap, required: JsonArray?): () -> Unit {
    return { run(type, id, formMap, required, false) }
  }

  private fun debugAction(
    type: String,
    id: String,
    formMap: FormValueMap,
    required: JsonArray?
  ): () -> Unit {
    return { runDebug(type, id, formMap, required) }
  }

  private fun runDebug(type: String, id: String, formMap: FormValueMap, required: JsonArray?, dryRun: Boolean = true) {
    val values = formMap.formVal
    if (isMissingRequiredFields(required, values)) {
      return
    }
    val isCustomSchematic = type == "workspace-schematic"
    val dryRunArg = if (dryRun) "true" else "false"
    val command = if (isCustomSchematic) "workspace-schematic" else "generate"
    val name = if (isCustomSchematic) id else "$type:$id"
    val args = foldListOfMaps(arrayOf(values, mapOf(Pair("no-interactive", "true"), Pair("dry-run", dryRunArg))))
    NodeDebugConfigState.getInstance(this.project).execute(command, name, args)
  }

  private fun run(type: String, id: String, formMap: FormValueMap, required: JsonArray?, dryRun: Boolean = true) {
    val values = formMap.formVal
    if (isMissingRequiredFields(required, values)) {
      return
    }
    val projectType = this.nxService.nxProjectType
    val command = getSchematicCommandFromValues(type, id, values, projectType, dryRun)
    if (dryRun) {
      dryRunTerminal.runAndShow(command)
    } else {
      runTerminal.runAndShow(command)
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
    val type = table.getValueAt(selectedRow, 0).toString()
    val id = table.getValueAt(selectedRow, 1).toString()
    val fullId = findFullSchematicIdByTypeAndId(type, id, schematics) ?: return
    val info = schematics[fullId] ?: return
    val formMap = FormValueMap()
    val schematicPanel = RunSchematicPanel(project, id, info.fileLocation, formMap)
    val required = schematicPanel.required
    val panel = schematicPanel.generateCenterPanel(
      withBorder = true,
      addButtons = true,
      dryRunAction = this.dryRunAction(type, id, formMap, required),
      runAction = this.runAction(type, id, formMap, required),
      debugAction = this.debugAction(type, id, formMap, required)
    )
    val scrollPane = JBScrollPane(panel)
    val contentFactory = ContentFactory.SERVICE.getInstance()
    val content = contentFactory.createContent(scrollPane, tabName, false)
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
