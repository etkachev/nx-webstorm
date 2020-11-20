package com.github.etkachev.nxwebstorm.models

enum class CliCommands(val data: CliData) {
  NX(CliData("node_modules/@nrwl/cli/bin", "nx.js")),
  NG(CliData("node_modules/@angular/cli/bin", "ng"))
}

data class CliData(val path: String, val exec: String)
