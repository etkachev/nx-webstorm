package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.models.FormValueMap
import com.github.etkachev.nxwebstorm.models.SchematicTypeEnum
import com.github.etkachev.nxwebstorm.services.MyProjectService
import com.github.etkachev.nxwebstorm.ui.RunTerminalWindow
import com.github.etkachev.nxwebstorm.utils.getSchematicCommandFromValues
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

class DryRunAction(
  proj: Project,
  private val collection: String,
  private val id: String,
  private val formValues: FormValueMap,
  private val dialog: DialogWrapper,
  private val type: SchematicTypeEnum,
  private val collectionPath: String
) : AbstractAction("Dry Run") {
  private var terminal = RunTerminalWindow(proj, "Dry Run")
  private var nxService = MyProjectService.getInstance(proj)
  override fun actionPerformed(e: ActionEvent?) {
    val values = formValues.formVal
    val projectType = nxService.nxProjectType
    val command = getSchematicCommandFromValues(collection, id, values, projectType, true, type, collectionPath)
    dialog.close(DialogWrapper.CANCEL_EXIT_CODE)
    terminal.runAndShow(command)
  }
}
