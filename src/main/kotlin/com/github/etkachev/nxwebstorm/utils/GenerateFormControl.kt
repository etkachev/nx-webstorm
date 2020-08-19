package com.github.etkachev.nxwebstorm.utils

import com.google.gson.JsonObject
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import javax.swing.JComponent

enum class FormControlType {
    INVALID, BOOL, STRING, NUMBER, INTEGER
}

class FormCombo(val component: JComponent?, val type: FormControlType, val name: String, val description: String?) {

}


/**
 * Generate Form control based on json representation of schema.json of schematic
 */
class GenerateFormControl {
    fun getFormControl(prop: JsonObject, name: String): FormCombo? {
        if (!prop.has("type")) {
            return null
        }

        val type = prop.get("type").asString
        val description = if (prop.has("description")) prop.get("description").asString else null
        return when(type) {
            "boolean" -> FormCombo(getBoolControl(description), FormControlType.BOOL, name, description)
            "string" -> FormCombo(getTextField(), FormControlType.STRING, name, description)
            "number" -> FormCombo(getTextField(), FormControlType.NUMBER, name, description)
            "integer" -> FormCombo(getTextField(), FormControlType.INTEGER, name, description)
            else -> FormCombo(null, FormControlType.INVALID, name, description)
        }
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