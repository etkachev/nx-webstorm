package com.github.etkachev.nxwebstorm.actions

import com.github.etkachev.nxwebstorm.ui.RunSchematicDialog
import com.github.etkachev.nxwebstorm.ui.RunTerminalWindow
import com.github.etkachev.nxwebstorm.ui.SchematicsListDialog
import com.github.etkachev.nxwebstorm.utils.GetNxData
import com.github.etkachev.nxwebstorm.utils.getSchematicCommandFromValues
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class Generate : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    if (e.project == null) {
      return
    }

    val proj = e.project!!
    val schematics = GetNxData().getCustomSchematics(proj)

    val dialog = SchematicsListDialog(proj, schematics)
    dialog.setSize(1000, 800)
    val ok = dialog.showAndGet()
    if (ok && dialog.schematicSelection.containsKey("id")) {
      val id = dialog.schematicSelection["id"] ?: ""
      val fileLocation = dialog.schematicSelection["file"] ?: ""
      val formDialog = RunSchematicDialog(proj, id, fileLocation)
      val formOk = formDialog.showAndGet()
      if (formOk) {
        val values = formDialog.formMap.formVal
        val command = getSchematicCommandFromValues(id, values, false)
        val terminal = RunTerminalWindow(proj, "Run")
        terminal.runAndShow(command)
      }
    }
  }
}
