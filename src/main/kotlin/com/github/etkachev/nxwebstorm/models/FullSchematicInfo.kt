package com.github.etkachev.nxwebstorm.models

data class FullSchematicInfo(val type: String, val id: String, val fileLocation: String, val description: String?)

data class SplitSchematicId(val type: String, val id: String)
