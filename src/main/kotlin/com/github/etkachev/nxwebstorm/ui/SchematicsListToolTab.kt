package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.actionlisteners.SchematicSelectionTabListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.panel
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.ListSelectionModel

class SchematicsListToolTab(val project: Project, private val schematics: Map<String, String>) {

  fun createCenterPanel(toolWindow: ToolWindow): JComponent? {
    val ids = schematics.keys
    val list = JBList(ids)
    list.selectionMode = ListSelectionModel.SINGLE_SELECTION
    list.background = Color(0, 0, 0, 0)
    list.addListSelectionListener(SchematicSelectionTabListener(project, list, schematics, toolWindow))
    val border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    return panel {
      row {
        list()
      }
    }.withBorder(border)
  }
}
