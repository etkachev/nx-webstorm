package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.utils.GetNxData
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class GenerateToolWindow : ToolWindowFactory {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val contentFactory = ContentFactory.SERVICE.getInstance()
    val schematics = GetNxData().getCustomSchematics(project)
    val listPanel = SchematicsListToolTab(project, schematics).createCenterPanel(toolWindow)
    val content = contentFactory.createContent(listPanel, "Generate", false)
    toolWindow.contentManager.addContent(content)
  }

  override fun isApplicable(project: Project): Boolean {
    val projects = try {
      GetNxData().getProjects(project)
    } catch (e: NoSuchElementException) {
      null
    }
    return projects != null
  }
}
