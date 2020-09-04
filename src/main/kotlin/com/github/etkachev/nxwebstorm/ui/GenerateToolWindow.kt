package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.utils.FindAllSchematics
import com.github.etkachev.nxwebstorm.utils.GetNxData
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class GenerateToolWindow : ToolWindowFactory {
  private var tabName = "Generate"
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val contentFactory = ContentFactory.SERVICE.getInstance()
    val allSchematics = FindAllSchematics(project).findAll()
    val listPanel = SchematicsListToolTab(project, allSchematics).createCenterPanel(toolWindow)
    val content = contentFactory.createContent(listPanel, tabName, false)
    toolWindow.contentManager.addContent(content)
  }

  override fun isApplicable(project: Project): Boolean {
    return GetNxData().isValidNxProject(project)
  }
}
