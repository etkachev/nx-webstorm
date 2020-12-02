package com.github.etkachev.nxwebstorm.utils

import com.github.etkachev.nxwebstorm.models.FormComboStringParams
import com.github.etkachev.nxwebstorm.services.MyProjectService
import com.github.etkachev.nxwebstorm.ui.formcontrols.BasicAutoCompleteField
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.openapi.ui.ComboBox

/**
 * Generate Form control based on json representation of schema.json of schematic
 */
class GenerateFormControl(private val required: JsonArray?, val project: Project) {
  private var generated: FormCombo? = null
  private var projectService = MyProjectService.getInstance(project)
  private var allProjects = projectService.projectList

  private val requiredFields: List<String>
    get() = required?.mapNotNull { r -> r?.asString } ?: emptyList()

  fun getFormControl(prop: JsonObject, name: String): FormCombo? {
    if (!prop.has("type")) {
      return null
    }

    val type = prop.get("type").asString
    val description = if (prop.has("description")) prop.get("description").asString else null
    val source = getDefaultObjectProps(prop, "\$source")
    val enums = if (prop.has("enum")) prop.get("enum").asJsonArray else null
    val xPrompt = if (prop.has("x-prompt")) prop.get("x-prompt") else null
    val xPromptIsObject = xPrompt?.isJsonObject == true
    val xPromptObj = if (xPromptIsObject) xPrompt!!.asJsonObject else null
    val default = if (prop.has("default")) prop.get("default").asString else null
    val result = when (type) {
      "boolean" -> FormCombo(
        getBoolControl(description, default),
        FormControlType.BOOL,
        name,
        description,
        null,
        requiredFields
      )
      "string" -> getFormComboOfString(FormComboStringParams(name, description, enums, xPromptObj, default, source))
      "number" -> FormCombo(
        getTextField(default),
        FormControlType.NUMBER,
        name,
        description,
        null,
        requiredFields
      )
      "integer" -> FormCombo(
        getTextField(default),
        FormControlType.INTEGER,
        name,
        description,
        null,
        requiredFields
      )
      else -> FormCombo(null, FormControlType.INVALID, name, description, null, requiredFields)
    }
    generated = result
    return result
  }

  private fun getTextField(default: String?): JBTextField {
    val field = if (default == null) JBTextField() else JBTextField(default)
    field.setSize(400, 10)
    return field
  }

  private fun getBoolControl(description: String?, default: String?): JBCheckBox {
    val selected = when (default) {
      "true", "yes", "1" -> true
      else -> false
    }
    return JBCheckBox(description, selected)
  }

  private fun getFormComboOfString(
    params: FormComboStringParams
  ): FormCombo {
    val (name, description, enums, xPrompt, default, source) = params
    val isList = if (xPrompt?.has("type") == true) xPrompt.get("type").asString == "list" else false
    val xPromptItems = if (xPrompt?.has("items") == true) xPrompt.get("items").asJsonArray else null
    if (enums == null && (!isList || xPromptItems == null)) {
      if (source != null) {
        if (source.asString == "projectName") {
          val autoComplete = BasicAutoCompleteField(this.allProjects).createComponent(default)
          return FormCombo(autoComplete, FormControlType.AUTOCOMPLETE, name, description, null, requiredFields)
        }
      }
      return FormCombo(getTextField(default), FormControlType.STRING, name, description, enums, requiredFields)
    }

    val stringOptions = enums?.map { o -> o.asString }?.toTypedArray()
      ?: (
        xPromptItems?.mapNotNull
        { i ->
          val obj = i.asJsonObject
          val value = if (obj.has("value")) obj.get("value").asString else null
          value
        }?.toTypedArray()
          ?: emptyArray<String>()
        )
    return FormCombo(
      getSelectDropdown(stringOptions, default),
      FormControlType.LIST,
      name,
      description,
      stringOptions,
      requiredFields
    )
  }

  private fun getSelectDropdown(options: Array<String>, default: String?): ComboBox<String> {
    val control = ComboBox(options)
    if (default != null) {
      control.item = default
    }
    return control
  }

  private fun getDefaultObjectProps(prop: JsonObject, propName: String): JsonElement? {
    if (!prop.has("\$default") || !prop.get("\$default").isJsonObject) {
      return null
    }
    val obj = prop.get("\$default").asJsonObject
    if (!obj.has(propName)) {
      return null
    }
    return obj.get(propName)
  }
}
