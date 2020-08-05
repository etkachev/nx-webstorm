package com.github.etkachev.nxwebstorm.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import javax.swing.JComponent
import com.github.etkachev.nxwebstorm.actions.openSchematicButtonAction

class SchematicsListDialog(project: Project?, private val ids: List<String>): DialogWrapper(project) {
    init {
        super.init()
    }
    override fun createCenterPanel(): JComponent? {
        return panel {
            ids.map { id ->
                row {
                    button(id, openSchematicButtonAction(id))
                }
            }
        }
    }
}