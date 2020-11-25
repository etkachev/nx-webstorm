package com.github.etkachev.nxwebstorm.models

enum class CliCommands(val data: CliData) {
  NX(CliData("node_modules/@nrwl/cli/bin", "nx.js")),
  NG(CliData("node_modules/@angular/cli/bin", "ng"))
}

/**
 * @property path the path to the executable within node_modules
 * @property exec the js file for executing
 */
data class CliData(val path: String, val exec: String)

data class SchematicCommandData(
  val nxProjectType: NxProjectType,
  val schematicType: SchematicTypeEnum,
  val collectionPath: String?
)
