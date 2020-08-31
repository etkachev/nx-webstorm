package com.github.etkachev.nxwebstorm.ui

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory
import org.jetbrains.plugins.terminal.TerminalView


class RunTerminalWindow(private val proj: Project) {

    fun runAndShow(command: String, tabName: String? = null) {
        val window = ToolWindowManager.getInstance(proj).getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID)
        val terminalView = TerminalView.getInstance(proj)
        if (window == null) return

//        val console = TextConsoleBuilderFactory.getInstance().createBuilder(proj).console
//        window.show()
        terminalView.createLocalShellWidget(null, tabName).executeCommand(command)
    }
}