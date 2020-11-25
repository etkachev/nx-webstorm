package com.github.etkachev.nxwebstorm.models

import com.google.gson.JsonArray

/**
 * @property collectionPath path to collection this schematic is located. Mainly used for custom angular schematic runs
 */
data class SchematicInfo(val fileLocation: String, val description: String? = null, val collectionPath: String? = null)

/**
 * meta data needed for actions on dry-run or running schematics.
 */
data class SchematicRunData(
  val collection: String,
  val id: String,
  val formMap: FormValueMap,
  val required: JsonArray?,
  val type: SchematicTypeEnum,
  val collectionPath: String?
)

/**
 * data for the DryRunAction class from the generate action popup.
 */
data class DryRunButtonData(
  val collection: String,
  val id: String,
  val formValues: FormValueMap,
  val type: SchematicTypeEnum,
  val collectionPath: String
)
