package com.github.etkachev.nxwebstorm.models

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
