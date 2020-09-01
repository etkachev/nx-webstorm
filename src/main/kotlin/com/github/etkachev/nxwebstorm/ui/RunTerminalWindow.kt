package com.github.etkachev.nxwebstorm.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import org.jetbrains.plugins.terminal.ShellTerminalWidget
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory
import org.jetbrains.plugins.terminal.TerminalView

class RunTerminalWindow(private val proj: Project, private val tabName: String? = null) {

  var terminalView: TerminalView = TerminalView.getInstance(proj)
  var shell: ShellTerminalWidget? = null

  private fun createShell() {
    shell = terminalView.createLocalShellWidget(null, tabName)
  }

  fun runAndShow(command: String) {
    ToolWindowManager.getInstance(proj).getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID)
      ?: return

    if (shell == null) {
      createShell()
    }
    shell!!.executeCommand(command)
  }
}
