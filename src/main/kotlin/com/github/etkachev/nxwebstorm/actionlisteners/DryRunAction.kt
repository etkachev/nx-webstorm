package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.ui.FormValueMap
import com.github.etkachev.nxwebstorm.ui.RunTerminalWindow
import com.intellij.openapi.project.Project
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

class DryRunAction(private val proj: Project, private val formValues: FormValueMap) : AbstractAction("Dry Run") {
    override fun actionPerformed(e: ActionEvent?) {
        val hi = formValues.formVal
        val terminal = RunTerminalWindow(proj)
        terminal.runAndShow("ls", "Dry Run")
    }
}