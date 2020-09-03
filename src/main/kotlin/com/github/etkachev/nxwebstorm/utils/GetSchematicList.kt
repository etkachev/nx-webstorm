package com.github.etkachev.nxwebstorm.utils

import com.github.etkachev.nxwebstorm.models.SchematicInfo

fun getSchematicListDescriptions(schematics: Map<String, SchematicInfo>): List<Pair<String, String>> {
  val ids = schematics.keys
  return ids.mapNotNull { id ->
    val split = id.split("--SPLIT--")
    if (split.count() != 2) {
      return@mapNotNull null
    }
    val prefix = split[0]
    val schematicId = split[1]
    val description = schematics[id]?.description
    val suffix = if (description != null) " - $description" else ""
    id to "$prefix - $schematicId$suffix"
  }
}
