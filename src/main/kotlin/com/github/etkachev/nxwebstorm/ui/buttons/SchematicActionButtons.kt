package com.github.etkachev.nxwebstorm.ui.buttons

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import icons.PluginIcons
import java.awt.event.ActionEvent

class SchematicActionButtons {
  companion object {
    fun run(action: () -> Unit) = RunSchematicActionButton(action)
    fun debug(action: () -> Unit) = DebugSchematicActionButton(action)
    fun dryRun(action: () -> Unit) = DryRunSchematicActionButton(action)
  }
}

class RunSchematicActionButton(private val action: () -> Unit) : AnAction(
  "Run",
  "Run Schematic",
  AllIcons.Actions.Execute
) {
  override fun actionPerformed(e: AnActionEvent) {
    this.action.invoke()
  }
}

class DebugSchematicActionButton(private val action: () -> Unit) : AnAction(
  "Debug",
  "Debug Schematic",
  AllIcons.Actions.StartDebugger
) {
  override fun actionPerformed(e: AnActionEvent) {
    this.action.invoke()
  }
}

class DryRunSchematicActionButton(private val action: () -> Unit) : AnAction(
  "Dry Run",
  "Dry Run Schematic",
  AllIcons.General.RunWithCoverage
) {
  override fun actionPerformed(e: AnActionEvent) {
    this.action.invoke()
  }
}
