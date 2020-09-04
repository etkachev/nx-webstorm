package com.github.etkachev.nxwebstorm.utils

import com.github.etkachev.nxwebstorm.models.FullSchematicInfo
import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.models.SplitSchematicId

fun splitSchematicId(id: String): SplitSchematicId? {
  val split = id.split("--SPLIT--")
  if (split.count() != 2) {
    return null
  }
  val type = split[0]
  val schematicId = split[1]
  return SplitSchematicId(type, schematicId)
}

fun findFullSchematicIdByTypeAndId(type: String, id: String, schematics: Map<String, SchematicInfo>): String? {
  return schematics.keys.find { key ->
    val toMatch = generateUniqueSchematicKey(type, id)
    key == toMatch
  }
}

fun generateUniqueSchematicKey(type: String, id: String): String {
  return "$type--SPLIT--$id"
}

fun getSchematicData(schematics: Map<String, SchematicInfo>): Array<FullSchematicInfo> {
  return schematics.keys.mapNotNull { id ->
    val splitData = splitSchematicId(id) ?: return@mapNotNull null
    val type = splitData.type
    val schematicId = splitData.id
    val description = schematics[id]?.description
    val fileLocation = schematics[id]?.fileLocation ?: ""
    FullSchematicInfo(type, schematicId, fileLocation, description)
  }.toTypedArray()
}

fun flattenMultipleMaps(vararg maps: Map<String, SchematicInfo>): Map<String, SchematicInfo> {
  return arrayOf(maps).flatten().fold(mutableMapOf<String, SchematicInfo>(), { acc, e ->
    for (key in e.keys) {
      val value = e[key] ?: continue
      acc[key] = value
    }
    return@fold acc
  }).toMap()
}
