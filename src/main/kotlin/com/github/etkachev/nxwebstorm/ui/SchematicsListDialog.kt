package com.github.etkachev.nxwebstorm.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import com.intellij.ui.components.JBList
import javax.swing.JComponent
import javax.swing.ListSelectionModel
import com.github.etkachev.nxwebstorm.actionlisteners.SchematicActionListener

class SchematicsListDialog(val project: Project?, private val schematics: Map<String, String>) :
        DialogWrapper(project) {
  var schematicSelection: MutableMap<String, String> = mutableMapOf()

  init {
    super.init()
    schematicSelection = mutableMapOf()
  }

  override fun createCenterPanel(): JComponent? {
    val ids = schematics.keys
    val list = JBList(ids)
    list.selectionMode = ListSelectionModel.SINGLE_SELECTION
    list.fixedCellWidth = 800
    list.addListSelectionListener(SchematicActionListener(list, schematics, this))
    return panel {
      row {
        list()
      }
    }
  }
}
