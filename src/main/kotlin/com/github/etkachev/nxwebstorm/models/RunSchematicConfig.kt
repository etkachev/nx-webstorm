package com.github.etkachev.nxwebstorm.models

data class RunSchematicConfig(
  val cli: CliCommands,
  val command: String,
  val name: String,
  val args: Map<String, String>,
  val additionalArgs: List<String>
)
