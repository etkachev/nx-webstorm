package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.models.NxProjectType
import com.github.etkachev.nxwebstorm.services.MyProjectService
import com.github.etkachev.nxwebstorm.utils.FindAllSchematics
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class GenerateToolWindow : ToolWindowFactory {
  private var tabName = "Generate"
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val contentFactory = ContentFactory.SERVICE.getInstance()
    val schematicFetcher = FindAllSchematics(project)
    val allSchematics = schematicFetcher.findAll()
    val listPanel =
      SchematicsListToolTab(project, toolWindow, contentFactory, tabName, schematicFetcher).createCenterPanel(
        allSchematics
      )
    val content = contentFactory.createContent(listPanel, tabName, false)
    toolWindow.contentManager.addContent(content)
  }

  override fun isApplicable(project: Project): Boolean {
    val projectRoot = ProjectRootManager.getInstance(project)
    if (projectRoot.contentRootsFromAllModules.isEmpty()) {
      return false
    }
    val projectService = MyProjectService.getInstance(project)
    return projectService.nxProjectType == NxProjectType.Nx || projectService.nxProjectType == NxProjectType.Angular
  }
}
