package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.models.DryRunButtonData
import com.github.etkachev.nxwebstorm.models.SchematicCommandData
import com.github.etkachev.nxwebstorm.services.MyProjectService
import com.github.etkachev.nxwebstorm.ui.RunTerminalWindow
import com.github.etkachev.nxwebstorm.utils.getSchematicCommandFromValues
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

class DryRunAction(
  proj: Project,
  private val actionData: DryRunButtonData,
  private val dialog: DialogWrapper
) : AbstractAction("Dry Run") {
  private var terminal = RunTerminalWindow(proj, "Dry Run")
  private var nxService = MyProjectService.getInstance(proj)
  override fun actionPerformed(e: ActionEvent?) {
    val (collection, id, formValues, type, collectionPath) = actionData
    val values = formValues.formVal
    val projectType = nxService.nxProjectType
    val schematicCommandData = SchematicCommandData(projectType, type, collectionPath)
    val command = getSchematicCommandFromValues(collection, id, values, schematicCommandData, true)
    dialog.close(DialogWrapper.CANCEL_EXIT_CODE)
    terminal.runAndShow(command)
  }
}
