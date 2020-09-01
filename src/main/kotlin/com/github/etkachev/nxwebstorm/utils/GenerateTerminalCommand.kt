package com.github.etkachev.nxwebstorm.utils

fun getSchematicCommandFromValues(id: String, values: MutableMap<String, String>, dryRun: Boolean = true): String {
    val keys = values.keys
    val dryRunString = if (dryRun) " --dry-run" else ""
    val flagCommands = keys.mapNotNull { key ->
        val value = values[key]
        val finalText = if (value == "true" || value == "false") {
            if (value == "true") "--$key" else null
        } else if (value != null) {
            "--$key=$value"
        } else {
            null
        }
        finalText
    }
    val flags = flagCommands.joinToString(" ")
    return "nx workspace-schematic $id $flags --no-interactive$dryRunString"
}
