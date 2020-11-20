package com.github.etkachev.nxwebstorm.actions

import com.github.etkachev.nxwebstorm.services.MyProjectService
import com.intellij.openapi.actionSystem.AnActionEvent

import com.intellij.openapi.actionSystem.DefaultActionGroup

class NxActionGroup : DefaultActionGroup() {

  override fun update(event: AnActionEvent) {
    val project = event.project
    if (project == null) {
      event.presentation.isEnabled = false
      return
    }

    val projectService = MyProjectService.getInstance(project)

    val isValidNx = projectService.isValidNxProject || projectService.isAngularProject
    event.presentation.isEnabled = isValidNx
  }
}
