package com.github.etkachev.nxwebstorm.actions

import com.github.etkachev.nxwebstorm.utils.GetNxData
import com.intellij.openapi.actionSystem.AnActionEvent

import com.intellij.openapi.actionSystem.DefaultActionGroup

class NxActionGroup : DefaultActionGroup() {
  override fun update(event: AnActionEvent) {
    val project = event.project
    if (project == null) {
      event.presentation.isEnabled = false
      return
    }
    val isValidNx = GetNxData().isValidNxProject(project)
    event.presentation.isEnabled = isValidNx
  }
}
