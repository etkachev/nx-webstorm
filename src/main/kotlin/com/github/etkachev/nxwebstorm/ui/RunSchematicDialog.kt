package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.actionlisteners.DryRunAction
import com.github.etkachev.nxwebstorm.models.DryRunButtonData
import com.github.etkachev.nxwebstorm.models.FormValueMap
import com.github.etkachev.nxwebstorm.models.SchematicTypeEnum
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.project.Project
import javax.swing.Action
import javax.swing.JComponent

class RunSchematicDialog(
  private val project: Project,
  private val collection: String,
  private val id: String,
  private val schematicLocation: String,
  private val type: SchematicTypeEnum,
  private val collectionPath: String
) :
  DialogWrapper(project) {
  var formMap: FormValueMap = FormValueMap()

  init {
    super.init()
  }

  override fun createLeftSideActions(): Array<Action> {
    val runData = DryRunButtonData(collection, id, formMap, type, collectionPath)
    return arrayOf(DryRunAction(project, runData, this))
  }

  override fun createCenterPanel(): JComponent? {
    return RunSchematicPanel(project, id, schematicLocation, formMap).generateCenterPanel()
  }
}
