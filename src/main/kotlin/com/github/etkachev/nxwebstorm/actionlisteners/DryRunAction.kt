package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.models.FormValueMap
import com.github.etkachev.nxwebstorm.services.MyProjectService
import com.github.etkachev.nxwebstorm.ui.RunTerminalWindow
import com.github.etkachev.nxwebstorm.utils.getSchematicCommandFromValues
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

class DryRunAction(
  proj: Project,
  private val type: String,
  private val id: String,
  private val formValues: FormValueMap,
  private val dialog: DialogWrapper
) : AbstractAction("Dry Run") {
  private var terminal = RunTerminalWindow(proj, "Dry Run")
  private var nxService = proj.getService<MyProjectService>(MyProjectService::class.java)
  override fun actionPerformed(e: ActionEvent?) {
    val values = formValues.formVal
    val projectType = nxService.nxProjectType
    val command = getSchematicCommandFromValues(type, id, values, projectType)
    dialog.close(DialogWrapper.CANCEL_EXIT_CODE)
    terminal.runAndShow(command)
  }
}
