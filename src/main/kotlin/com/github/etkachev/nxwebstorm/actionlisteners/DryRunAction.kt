package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.ui.FormValueMap
import com.github.etkachev.nxwebstorm.ui.RunTerminalWindow
import com.github.etkachev.nxwebstorm.utils.getSchematicCommandFromValues
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

class DryRunAction(
    proj: Project, private val id: String, private val formValues: FormValueMap,
    private val dialog: DialogWrapper
) : AbstractAction("Dry Run") {
    private var terminal = RunTerminalWindow(proj, "Dry Run")
    override fun actionPerformed(e: ActionEvent?) {
        val values = formValues.formVal
        val command = getSchematicCommandFromValues(id, values)
        dialog.close(DialogWrapper.CANCEL_EXIT_CODE)
        terminal.runAndShow(command)
    }
}
