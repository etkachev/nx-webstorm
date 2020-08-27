package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.utils.FormControlType
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.project.Project
import javax.swing.JComponent
import com.intellij.ui.layout.panel
import com.github.etkachev.nxwebstorm.utils.GenerateFormControl
import com.github.etkachev.nxwebstorm.utils.ReadJsonFile


class RunSchematicDialog(private val project: Project?, private val id: String, private val schematicLocation: String): DialogWrapper(project) {
    var formValue: MutableMap<String, String> = mutableMapOf()

    init {
        super.init()
        formValue = mutableMapOf()
    }

    override fun createCenterPanel(): JComponent? {
        if (project == null) {
            return null
        }
        val json = ReadJsonFile().fromFileUrl(project, schematicLocation)
        val props = json.get("properties")?.asJsonObject ?: return null
        val formControls = props.keySet().mapNotNull { key -> GenerateFormControl().getFormControl(props.get(key).asJsonObject, key) }
        return panel {
            row {
                label("ng generate workspace-schematic:$id")
            }
            formControls.mapNotNull {
                control ->
                val comp = control.component ?: return null
                titledRow(control.name) {
                    if (control.type != FormControlType.BOOL) {
                        row { label(control.description ?: "") }
                    }
                    row {
                        when(control.type) {
                            FormControlType.BOOL -> comp().onApply { formValue[control.name] = control.value ?: "" }
                            FormControlType.STRING, FormControlType.NUMBER, FormControlType.INTEGER -> comp().onApply { formValue[control.name] = control.value ?: "" }
                            else -> comp().onApply { formValue[control.name] = control.value ?: "" }
                        }
                    }
                }
            }
        }
    }
}