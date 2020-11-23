package com.github.etkachev.nxwebstorm.utils

import com.github.etkachev.nxwebstorm.models.CliCommands
import com.github.etkachev.nxwebstorm.models.NxProjectType
import com.github.etkachev.nxwebstorm.models.SchematicTypeEnum

fun getSchematicCommandFromValues(
  collection: String,
  id: String,
  values: MutableMap<String, String>,
  nxProjectType: NxProjectType,
  dryRun: Boolean = true,
  schematicType: SchematicTypeEnum
): String {
  val dryRunString = if (dryRun) " --dry-run" else ""
  val (nxPath, nxExec) = CliCommands.NX.data
  val (ngPath, ngExec) = CliCommands.NG.data
  val nx = "node $nxPath/$nxExec"
  val ng = "node $ngPath/$ngExec"
  // val prefix = if (type == "workspace-schematic") "$nx workspace-schematic $id" else "$nx generate $type:$id"
  val cli = if (nxProjectType == NxProjectType.Nx) nx else ng
  val newPrefix = when (schematicType) {
    SchematicTypeEnum.PROVIDED -> "$cli generate $collection:$id"
    SchematicTypeEnum.CUSTOM_NX -> "$cli workspace-schematic $id"
    /**
     * @TODO update to correct collection path
     */
    SchematicTypeEnum.CUSTOM_ANGULAR -> "schematics ./path-to-collection.json:$id"
  }
  // val finalPrefix = if (nxProjectType == NxProjectType.Nx) prefix else "$ng generate $type:$id"
  val flags = getCommandArguments(values).joinToString(" ")
  return "$newPrefix $flags --no-interactive$dryRunString"
}

fun getCommandArguments(values: Map<String, String>): List<String> {
  return values.keys.mapNotNull { key ->
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
}
