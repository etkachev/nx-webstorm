package com.github.etkachev.nxwebstorm.actions

import com.github.etkachev.nxwebstorm.models.mapSchematicTypeStringToEnum
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
    val nxService = MyProjectService.getInstance(proj)

    val dialog = SchematicsListDialog(proj, schematics)
    dialog.setSize(1000, 800)
    val ok = dialog.showAndGet()
    if (ok && dialog.schematicSelection.containsKey("id")) {
      val id = dialog.schematicSelection["id"] ?: ""
      val collection = dialog.schematicSelection["collection"] ?: ""
      val fileLocation = dialog.schematicSelection["file"] ?: ""
      val type = dialog.schematicSelection["type"] ?: ""
      val collectionPath = dialog.schematicSelection["collectionPath"] ?: ""
      val enumType = mapSchematicTypeStringToEnum(type)
      val formDialog = RunSchematicDialog(proj, collection, id, fileLocation, enumType, collectionPath)
      val formOk = formDialog.showAndGet()
      if (formOk) {
        val values = formDialog.formMap.formVal
        val projectType = nxService.nxProjectType
        val command =
          getSchematicCommandFromValues(collection, id, values, projectType, false, enumType, collectionPath)
        val terminal = RunTerminalWindow(proj, "Run")
        terminal.runAndShow(command)
      }
    }
  }
}
