package com.github.etkachev.nxwebstorm.models

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

data class FormComboStringParams(
  val name: String,
  val description: String?,
  val enums: JsonArray?,
  val xPrompt: JsonObject?,
  val default: String?,
  val source: JsonElement?
)
