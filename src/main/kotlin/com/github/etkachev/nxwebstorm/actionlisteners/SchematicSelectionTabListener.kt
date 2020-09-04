package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.models.FormValueMap
import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.ui.RunSchematicPanel
import com.github.etkachev.nxwebstorm.ui.RunTerminalWindow
import com.github.etkachev.nxwebstorm.utils.findFullSchematicIdByTypeAndId
import com.github.etkachev.nxwebstorm.utils.getSchematicCommandFromValues
import com.google.gson.JsonArray
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import java.awt.event.ActionEvent
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

  private fun dryRunAction(id: String, formMap: FormValueMap, required: JsonArray?): (ActionEvent) -> Unit {
    return { run(id, formMap, required) }
  }

  private fun runAction(id: String, formMap: FormValueMap, required: JsonArray?): (ActionEvent) -> Unit {
    return { run(id, formMap, required, false) }
  }

  private fun run(id: String, formMap: FormValueMap, required: JsonArray?, dryRun: Boolean = true) {
    val values = formMap.formVal
    checkRequiredFields(required, values)
    val command = getSchematicCommandFromValues(id, values, dryRun)
    if (dryRun) {
      dryRunTerminal.runAndShow(command)
    } else {
      runTerminal.runAndShow(command)
    }
  }

  private fun checkRequiredFields(required: JsonArray?, values: MutableMap<String, String>) {
    if (required == null) {
      return
    }

    val mappedRequired = required.mapNotNull { r -> r.asString }.toTypedArray()
    val missing =
      values.keys.filter { k -> mappedRequired.contains(k) && (values[k] == null || values[k]!!.trim() == "") }
    if (missing.count() == 0) {
      return
    }

    val joinedKeys = missing.joinToString()
    val message = "Missing the following required fields: $joinedKeys"
    Messages.showMessageDialog(message, "Oops", Messages.getWarningIcon())
  }

  override fun valueChanged(e: ListSelectionEvent?) {
    if (e == null || e.valueIsAdjusting) {
      return
    }
    val type = table.getValueAt(table.selectedRow, 0).toString()
    val id = table.getValueAt(table.selectedRow, 1).toString()
    val fullId = findFullSchematicIdByTypeAndId(type, id, schematics) ?: return
    val info = schematics[fullId] ?: return
    val formMap = FormValueMap()
    val schematicPanel = RunSchematicPanel(project, id, info.fileLocation, formMap)
    val required = schematicPanel.required
    val panel = schematicPanel.generateCenterPanel(
      withBorder = true, addButtons = true,
      dryRunAction = dryRunAction(id, formMap, required), runAction = runAction(id, formMap, required)
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
