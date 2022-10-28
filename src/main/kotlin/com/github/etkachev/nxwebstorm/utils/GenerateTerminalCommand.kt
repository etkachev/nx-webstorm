package com.github.etkachev.nxwebstorm.utils

import com.github.etkachev.nxwebstorm.models.CliCommands
import com.github.etkachev.nxwebstorm.models.NxProjectType
import com.github.etkachev.nxwebstorm.models.SchematicCommandData
import com.github.etkachev.nxwebstorm.models.SchematicTypeEnum

fun getSchematicCommandArgs(
  collection: String,
  id: String,
  values: MutableMap<String, String>,
  commandData: SchematicCommandData,
  dryRun: Boolean = true,
  isPnpm: Boolean = false
): List<String> {
  val result = mutableListOf<String>()
  val (nxPath, nxExec) = CliCommands.NX.data
  val (ngPath, ngExec) = CliCommands.NG.data
  val (nxProjectType, schematicType, collectionPath) = commandData
  val nxBinPath = "$nxPath/$nxExec"
  val ngBinPath = "$ngPath/$ngExec"
  val cli = if (nxProjectType == NxProjectType.Nx) {
    if (isPnpm) "nx" else nxBinPath
  } else {
    ngBinPath
  }
  val exec = if (isPnpm) "pnpm" else "node"
  val initialPrefix = when (schematicType) {
    SchematicTypeEnum.PROVIDED -> listOf(exec, cli, "generate", "$collection:$id")
    SchematicTypeEnum.CUSTOM_NX -> listOf(exec, cli, "workspace-generator", id)
    SchematicTypeEnum.CUSTOM_ANGULAR -> listOf("schematics", ".$collectionPath:$id")
  }
  val flags = getCommandArguments(values)
  result.addAll(initialPrefix)
  result.addAll(flags)
  result.add("--no-interactive")
  if (dryRun) {
    result.add("--dry-run")
  }
  return result.toList()
}

fun getCommandArguments(values: Map<String, String>): List<String> {
  return values.keys.mapNotNull { key ->
    val value = values[key]
    val finalText = if (value == "true" || value == "false") {
      if (value == "true") "--$key" else null
    } else if (value != null && value.isNotBlank()) {
      val cleanedValue = if (value.contains(" ") || value.contains("!")) "'$value'" else value
      "--$key=$cleanedValue"
    } else {
      null
    }
    finalText
  }
}
