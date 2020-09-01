package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.actionlisteners.DryRunAction
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.project.Project
import javax.swing.Action
import javax.swing.JComponent

class RunSchematicDialog(private val project: Project, private val id: String, private val schematicLocation: String) :
    DialogWrapper(project) {
    var formMap: FormValueMap = FormValueMap()

    init {
        super.init()
    }

    override fun createLeftSideActions(): Array<Action> {
        return arrayOf(DryRunAction(project, id, formMap, this))
    }

    override fun createCenterPanel(): JComponent? {
        return RunSchematicPanel(project, id, schematicLocation, formMap).generateCenterPanel()
    }
}
