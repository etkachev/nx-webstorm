package com.github.etkachev.nxwebstorm.utils

import com.github.etkachev.nxwebstorm.models.NxProjectType

fun getSchematicCommandFromValues(
  type: String,
  id: String,
  values: MutableMap<String, String>,
  nxProjectType: NxProjectType,
  dryRun: Boolean = true
): String {
  val keys = values.keys
  val dryRunString = if (dryRun) " --dry-run" else ""
  val flagCommands = keys.mapNotNull { key ->
    val value = values[key]
    val finalText = if (value == "true" || value == "false") {
      if (value == "true") "--$key" else null
    } else if (value != null && value.isNotBlank()) {
      val cleanedValue = if (value.contains(" ")) "'$value'" else value
      "--$key=$cleanedValue"
    } else {
      null
    }
    finalText
  }
  val nx = "node node_modules/@nrwl/cli/bin/nx.js"
  val ng = "node node_modules/@angular/cli/bin/ng"
  val prefix = if (type == "workspace-schematic") "$nx workspace-schematic $id" else "$nx generate $type:$id"
  val finalPrefix = if (nxProjectType == NxProjectType.Nx) prefix else "$ng generate $type:$id"
  val flags = flagCommands.joinToString(" ")
  return "$finalPrefix $flags --no-interactive$dryRunString"
}
