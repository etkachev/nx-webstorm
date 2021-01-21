package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.services.MyProjectService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import org.jetbrains.plugins.terminal.ShellTerminalWidget
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory
import org.jetbrains.plugins.terminal.TerminalView

class RunTerminalWindow(private val proj: Project, private val tabName: String? = null) {

  var terminalView: TerminalView = TerminalView.getInstance(proj)
  var shell: ShellTerminalWidget? = null
  private val nxService = MyProjectService.getInstance(this.proj)
  private val workingDir: String?
    get() = if (nxService.configuredRootPath == "/") null else "${this.proj.basePath}/${nxService.configuredRootPath}"
  var terminalWindow = getWindowManager()

  private fun createShell(window: ToolWindow): Content {
    shell = terminalView.createLocalShellWidget(workingDir, tabName)
    val tabContent = window.contentManager.findContent(tabName)
    return tabContent!!
  }

  fun runAndShow(command: String) {
    if (terminalWindow == null) {
      return
    }

    val window = terminalWindow!!

    // If shell instance doesn't exist, cleanup tabs and create new instance of the shell
    if (shell == null) {
      cleanUpExistingTabs(window)
      createShell(window)
    }

    // If tab with matching name doesn't exist, create new shell instance with matching tab name
    val existingTab = window.contentManager.findContent(tabName) ?: createShell(window)
    // If shell is still running commands, don't continue
    if (shell!!.hasRunningCommands()) {
      return
    }
    window.show()
    window.contentManager.setSelectedContent(existingTab)
    shell!!.executeCommand(command)
  }

  private fun getWindowManager(): ToolWindow? {
    return ToolWindowManager.getInstance(proj).getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID)
  }

  private fun cleanUpExistingTabs(window: ToolWindow) {
    // if it can't find the existing tab, then leave recursive flow
    val existingTab = window.contentManager.findContent(tabName) ?: return

    window.contentManager.removeContent(existingTab, true)
    cleanUpExistingTabs(window)
  }
}
