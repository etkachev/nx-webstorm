package com.github.etkachev.nxwebstorm.utils

import com.github.etkachev.nxwebstorm.models.FullSchematicInfo
import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.models.SchematicTypeData
import com.github.etkachev.nxwebstorm.models.SchematicTypeEnum
import com.github.etkachev.nxwebstorm.models.SplitSchematicId
import com.github.etkachev.nxwebstorm.models.mapSchematicTypeStringToEnum
import com.intellij.ui.table.JBTable

fun splitSchematicId(id: String): SplitSchematicId? {
  val split = id.split("--SPLIT--")
  if (split.count() != 3) {
    return null
  }
  val collection = split[0]
  val schematicId = split[1]
  val type = split[2]
  return SplitSchematicId(collection, schematicId, type)
}

fun findFullSchematicIdByTypeAndId(
  collection: String,
  id: String,
  schematics: Map<String, SchematicInfo>,
  type: SchematicTypeEnum
): String? {
  val toMatch = generateUniqueSchematicKey(collection, id, type)
  // @TODO figure out why provided schematics are coming back as nx-custom
  return schematics.keys.find { key ->
    key == toMatch
  }
}

fun generateUniqueSchematicKey(collection: String, id: String, type: SchematicTypeEnum): String {
  val typeData = type.data
  return "$collection--SPLIT--$id--SPLIT--$typeData"
}

fun getSchematicData(schematics: Map<String, SchematicInfo>): Array<FullSchematicInfo> {
  return schematics.keys.mapNotNull { id ->
    val splitData = splitSchematicId(id) ?: return@mapNotNull null
    val collection = splitData.collection
    val type = SchematicTypeData(collection, splitData.type)
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

fun getSchematicIdFromTableSelect(table: JBTable, selectedRow: Int, schematics: Map<String, SchematicInfo>): String? {
  val collection = table.getValueAt(selectedRow, 0).toString()
  val id = table.getValueAt(selectedRow, 1).toString()
  val type = table.model.getValueAt(table.convertRowIndexToModel(selectedRow), 3).toString()
  val enumType = mapSchematicTypeStringToEnum(type)
  return findFullSchematicIdByTypeAndId(collection, id, schematics, enumType)
}
