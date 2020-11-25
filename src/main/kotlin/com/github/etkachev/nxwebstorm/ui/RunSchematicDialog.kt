package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.actionlisteners.DryRunAction
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
    return arrayOf(DryRunAction(project, collection, id, formMap, this, type, collectionPath))
  }

  override fun createCenterPanel(): JComponent? {
    return RunSchematicPanel(project, id, schematicLocation, formMap).generateCenterPanel()
  }
}
