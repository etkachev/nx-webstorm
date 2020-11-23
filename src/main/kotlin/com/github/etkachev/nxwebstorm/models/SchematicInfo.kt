package com.github.etkachev.nxwebstorm.models

/**
 * @property collectionPath path to collection this schematic is located. Mainly used for custom angular schematic runs
 */
data class SchematicInfo(val fileLocation: String, val description: String? = null, val collectionPath: String? = null)
