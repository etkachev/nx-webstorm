package com.github.etkachev.nxwebstorm.utils

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.openapi.ui.ComboBox

/**
 * Generate Form control based on json representation of schema.json of schematic
 */
class GenerateFormControl(private val required: JsonArray?) {
    private var generated: FormCombo? = null

    private val requiredFields: List<String>
        get() = required?.mapNotNull { r -> r?.asString } ?: emptyList()

    fun getFormControl(prop: JsonObject, name: String): FormCombo? {
        if (!prop.has("type")) {
            return null
        }

        val type = prop.get("type").asString
        val description = if (prop.has("description")) prop.get("description").asString else null
        val enums = if (prop.has("enum")) prop.get("enum").asJsonArray else null
        val default = if (prop.has("default")) prop.get("default").asString else null
        val result = when (type) {
            "boolean" -> FormCombo(
                getBoolControl(description, default), FormControlType.BOOL, name, description, null,
                requiredFields
            )
            "string" -> getFormComboOfString(name, description, enums, default)
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
        name: String,
        description: String?,
        enums: JsonArray?,
        default: String?
    ): FormCombo {
        if (enums == null) {
            return FormCombo(getTextField(default), FormControlType.STRING, name, description, enums, requiredFields)
        }

        val stringOptions = enums.map { o -> o.asString }.toTypedArray()
        return FormCombo(
            getSelectDropdown(stringOptions, default), FormControlType.LIST, name, description, stringOptions,
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
}
