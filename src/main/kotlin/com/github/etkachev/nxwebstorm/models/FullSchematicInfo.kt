package com.github.etkachev.nxwebstorm.models

import com.google.gson.JsonObject

data class FullSchematicInfo(
  val type: SchematicTypeData,
  val id: String,
  val fileLocation: String,
  val description: String?
)

class SplitSchematicId(val collection: String, val id: String, private val schematicType: String) {
  val type: SchematicTypeEnum
    get() = mapSchematicTypeStringToEnum(schematicType)
}

fun mapSchematicTypeStringToEnum(type: String): SchematicTypeEnum {
  return when (type) {
    SchematicTypeEnum.PROVIDED.data -> SchematicTypeEnum.PROVIDED
    SchematicTypeEnum.CUSTOM_NX.data -> SchematicTypeEnum.CUSTOM_NX
    SchematicTypeEnum.CUSTOM_ANGULAR.data -> SchematicTypeEnum.CUSTOM_ANGULAR
    else -> SchematicTypeEnum.CUSTOM_NX
  }
}

/**
 * get id from schema.json. Will first find `id`, if not found, then look for `$id`, otherwise return null
 */
fun getIdFromJsonSchema(json: JsonObject): String? {
  val idProp = if (json.has(SchemaIdProp.STANDARD.prop)) {
    SchemaIdProp.STANDARD
  } else if (json.has(SchemaIdProp.NEW.prop)) {
    SchemaIdProp.NEW
  } else {
    SchemaIdProp.NONE
  }

  return if (idProp == SchemaIdProp.NONE) null else json.get(idProp.prop).asString
}

enum class SchemaIdProp(val prop: String) {
  /**
   * the standard `id` prop of schema.json
   */
  STANDARD("id"),

  /**
   * the new preferred property of `$id` for schema.json
   */
  NEW("\$id"),

  /**
   * no property for id, used for null cases.
   */
  NONE("")
}

data class SchematicTypeData(val collection: String, val type: SchematicTypeEnum)

enum class SchematicTypeEnum(val data: String) {
  /**
   * schematic that is provided from external package like angular schematics or ngrx schematics
   */
  PROVIDED("provided"),

  /**
   * schematics that are custom within nx project.
   */
  CUSTOM_NX("custom-nx"),

  /**
   * schematics that are custom within regular non-nx angular project.
   */
  CUSTOM_ANGULAR("custom-angular")
}
