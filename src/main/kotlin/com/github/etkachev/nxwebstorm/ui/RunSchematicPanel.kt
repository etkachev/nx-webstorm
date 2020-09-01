package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.actionlisteners.CheckboxListener
import com.github.etkachev.nxwebstorm.actionlisteners.TextControlListener
import com.github.etkachev.nxwebstorm.utils.FormControlType
import com.github.etkachev.nxwebstorm.utils.GenerateFormControl
import com.github.etkachev.nxwebstorm.utils.ReadJsonFile
import com.intellij.openapi.project.Project
import com.intellij.ui.layout.panel
import java.awt.event.ActionEvent
import javax.swing.BorderFactory
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JTextField

class FormValueMap {
    var formVal: MutableMap<String, String> = mutableMapOf()

    fun valueGetter(key: String): () -> String {
        return { formVal[key] ?: "" }
    }

    fun valueSetter(key: String): (String) -> Unit {
        return { x: String -> setFormValueOfKey(key, x) }
    }

    fun nullValueGetter(key: String): () -> String? {
        return { formVal[key] }
    }

    fun nullValueSetter(key: String): (String?) -> Unit {
        return { x: String? -> setFormValueOfKey(key, x) }
    }

    fun boolValueGetter(key: String): () -> Boolean {
        return { formVal[key] == "true" }
    }

    fun boolValueSetter(key: String): (Boolean) -> Unit {
        return { x: Boolean -> formVal[key] = if (x) "true" else "false" }
    }

    fun setFormValueOfKey(key: String, value: String?) {
        formVal[key] = value ?: ""
    }
}

class RunSchematicPanel(
    private val project: Project,
    private val id: String,
    private val schematicLocation: String,
    private val formMap: FormValueMap = FormValueMap()
) {

    private var defaultAction: (ActionEvent) -> Unit = fun(_: ActionEvent) {
        System.console().printf("Default Action")
    }

    fun generateCenterPanel(
        withBorder: Boolean = false,
        addButtons: Boolean = false,
        dryRunAction: (ActionEvent) -> Unit = defaultAction,
        runAction: (ActionEvent) -> Unit = defaultAction
    ): JComponent? {
        val json = ReadJsonFile().fromFileUrl(project, schematicLocation)
        val props = json.get("properties")?.asJsonObject ?: return null
        val required = if (json.has("required")) json.get("required").asJsonArray else null
        val formControls = props.keySet()
            .mapNotNull { key -> GenerateFormControl(required).getFormControl(props.get(key).asJsonObject, key) }
        formControls.forEach { f -> formMap.setFormValueOfKey(f.name, f.value) }

        val panel = panel {
            row {
                label("ng generate workspace-schematic:$id")
            }
            formControls.mapNotNull { control ->
                val comp = control.component ?: return null
                val key = control.name
                val vg = formMap.valueGetter(key)
                val vs = formMap.valueSetter(key)
                val vbg = formMap.boolValueGetter(key)
                val vbs = formMap.boolValueSetter(key)
                val nvg = formMap.nullValueGetter(key)
                val nvs = formMap.nullValueSetter(key)
                titledRow(control.finalName) {
                    if (control.type != FormControlType.BOOL) {
                        row { label(control.description ?: "") }
                    }
                    row {
                        when (control.type) {
                            FormControlType.LIST -> (comboBox<String>(
                                DefaultComboBoxModel(control.enums), nvg,
                                nvs
                            ).component.editor.editorComponent as JTextField).document.addDocumentListener(
                                TextControlListener(formMap, control)
                            )
                            FormControlType.STRING, FormControlType.INTEGER, FormControlType.NUMBER -> textField(
                                vg,
                                vs
                            ).component.document.addDocumentListener(TextControlListener(formMap, control))
                            FormControlType.BOOL -> checkBox(
                                control.description
                                    ?: "", vbg, vbs
                            ).component.addActionListener(CheckboxListener(formMap, control))
                            else -> comp().onApply { formMap.setFormValueOfKey(control.name, control.value) }
                        }
                    }
                }
            }
            if (addButtons) {
                row {
                    button("Dry Run", dryRunAction)
                    right {
                        button("Run", runAction)
                    }
                }
            }
        }

        if (withBorder) {
            panel.withBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
        }
        return panel
    }
}
