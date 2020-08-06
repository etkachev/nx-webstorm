package com.github.etkachev.nxwebstorm.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import com.intellij.ui.components.JBList
import javax.swing.JComponent
import javax.swing.ListSelectionModel
import com.github.etkachev.nxwebstorm.actions.SchematicActionListener

class SchematicsListDialog(project: Project?, private val ids: List<String>): DialogWrapper(project) {
    init {
        super.init()
    }
    override fun createCenterPanel(): JComponent? {
        val list = JBList(ids)
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.fixedCellWidth = 800
        list.addListSelectionListener(SchematicActionListener(list, this))
        return panel {
            row {
                list()
            }
        }
    }
}