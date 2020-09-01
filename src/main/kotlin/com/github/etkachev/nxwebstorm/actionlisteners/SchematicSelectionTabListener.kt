package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.ui.FormValueMap
import com.github.etkachev.nxwebstorm.ui.RunSchematicPanel
import com.github.etkachev.nxwebstorm.ui.RunTerminalWindow
import com.github.etkachev.nxwebstorm.utils.getSchematicCommandFromValues
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBList
import com.intellij.ui.content.ContentFactory
import java.awt.event.ActionEvent
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class SchematicSelectionTabListener(
    private val project: Project,
    private val list: JBList<String>,
    private val schematics: Map<String, String>,
    private val toolWindow: ToolWindow
) :
    ListSelectionListener {
    private var dryRunTerminal = RunTerminalWindow(project, "Dry Run")
    private var runTerminal = RunTerminalWindow(project, "Run")
    private var tabName = "Generate - Schematic"

    private fun dryRunAction(id: String, formMap: FormValueMap): (ActionEvent) -> Unit {
        return { run(id, formMap) }
    }

    private fun runAction(id: String, formMap: FormValueMap): (ActionEvent) -> Unit {
        return { run(id, formMap, false) }
    }

    private fun run(id: String, formMap: FormValueMap, dryRun: Boolean = true) {
        val values = formMap.formVal
        val command = getSchematicCommandFromValues(id, values, dryRun)
        if (dryRun) {
            dryRunTerminal.runAndShow(command)
        } else {
            runTerminal.runAndShow(command)
        }
    }

    override fun valueChanged(e: ListSelectionEvent?) {
        if (e == null || e.valueIsAdjusting) {
            return
        }
        val id = list.selectedValue
        val fileLocation = schematics[id] ?: return
        val formMap = FormValueMap()
        val schematicPanel = RunSchematicPanel(project, id, fileLocation, formMap)
        val panel = schematicPanel.generateCenterPanel(
            withBorder = true, addButtons = true,
            dryRunAction = dryRunAction(id, formMap), runAction = runAction(id, formMap)
        )
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(panel, tabName, false)
        val existingTab = toolWindow.contentManager.findContent(tabName)
        if (existingTab != null) {
            toolWindow.contentManager.removeContent(existingTab, true)
        }
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
    }
}
