package com.github.etkachev.nxwebstorm.utils

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import javax.swing.JComponent

enum class FormControlType {
    INVALID, BOOL, STRING, NUMBER, INTEGER, LIST
}

class FormCombo(
        val component: JComponent?,
        private val initialType: FormControlType,
        val name: String,
        val description: String?,
        val enums: JsonArray?) {

    val type: FormControlType
        get() = if (enums != null) FormControlType.LIST else initialType

    val value: String?
        get() = if (component == null) null else when(component) {
            is JBTextField -> component.text
            is JBCheckBox -> if (component.isSelected) "true" else "false"
            else -> null
        }
}


/**
 * Generate Form control based on json representation of schema.json of schematic
 */
class GenerateFormControl() {
    var generated: FormCombo? = null

    fun getFormControl(prop: JsonObject, name: String): FormCombo? {
        if (!prop.has("type")) {
            return null
        }

        val type = prop.get("type").asString
        val description = if (prop.has("description")) prop.get("description").asString else null
        val enums = if (prop.has("enum")) prop.get("enum").asJsonArray else null
        val result =  when(type) {
            "boolean" -> FormCombo(getBoolControl(description), FormControlType.BOOL, name, description, enums)
            "string" -> FormCombo(getTextField(), FormControlType.STRING, name, description, enums)
            "number" -> FormCombo(getTextField(), FormControlType.NUMBER, name, description, enums)
            "integer" -> FormCombo(getTextField(), FormControlType.INTEGER, name, description, enums)
            else -> FormCombo(null, FormControlType.INVALID, name, description, enums)
        }
        generated = result
        return result
    }

    private fun getTextField(): JBTextField {
        val field = JBTextField()
        field.setSize(400, 10)
        return field
    }

    private fun getBoolControl(description: String?): JBCheckBox {
        return JBCheckBox(description)
    }
}