package com.github.etkachev.nxwebstorm.ui.buttons

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

object SchematicActionButtons {
  fun run(action: () -> Unit) = RunSchematicActionButton(action)
  fun debug(action: () -> Unit) = DebugSchematicActionButton(action)
  fun dryRun(action: () -> Unit) = DryRunSchematicActionButton(action)
  fun refresh(action: () -> Unit) = RefreshSchematicsListActionButton(action)
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

class RefreshSchematicsListActionButton(private val action: () -> Unit) : AnAction(
  "Refresh",
  "Re-fetch all possible schematics",
  AllIcons.Actions.Refresh
) {
  override fun actionPerformed(e: AnActionEvent) {
    this.action.invoke()
  }
}
