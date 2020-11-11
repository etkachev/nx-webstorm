package com.github.etkachev.nxwebstorm.actions

import com.github.etkachev.nxwebstorm.services.MyProjectService
import com.github.etkachev.nxwebstorm.ui.RunSchematicDialog
import com.github.etkachev.nxwebstorm.ui.RunTerminalWindow
import com.github.etkachev.nxwebstorm.ui.SchematicsListDialog
import com.github.etkachev.nxwebstorm.utils.FindAllSchematics
import com.github.etkachev.nxwebstorm.utils.getSchematicCommandFromValues
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class Generate : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    if (e.project == null) {
      return
    }

    val proj = e.project!!
    val schematics = FindAllSchematics(proj).findAll()
    val nxService = proj.getService<MyProjectService>(MyProjectService::class.java)

    val dialog = SchematicsListDialog(proj, schematics)
    dialog.setSize(1000, 800)
    val ok = dialog.showAndGet()
    if (ok && dialog.schematicSelection.containsKey("id")) {
      val id = dialog.schematicSelection["id"] ?: ""
      val type = dialog.schematicSelection["type"] ?: ""
      val fileLocation = dialog.schematicSelection["file"] ?: ""
      val formDialog = RunSchematicDialog(proj, type, id, fileLocation)
      val formOk = formDialog.showAndGet()
      if (formOk) {
        val values = formDialog.formMap.formVal
        val projectType = nxService.nxProjectType
        val command = getSchematicCommandFromValues(type, id, values, projectType, false)
        val terminal = RunTerminalWindow(proj, "Run")
        terminal.runAndShow(command)
      }
    }
  }
}
