package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.utils.FormControlType
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.project.Project
import javax.swing.JComponent
import com.intellij.ui.layout.panel
import com.github.etkachev.nxwebstorm.utils.GenerateFormControl
import com.github.etkachev.nxwebstorm.utils.ReadJsonFile
import com.intellij.openapi.ui.DialogPanel


class RunSchematicDialog(private val project: Project?, private val id: String, private val schematicLocation: String): DialogWrapper(project) {
    init {
        super.init()
    }

    var currentPanel: DialogPanel? = null

    override fun createCenterPanel(): JComponent? {
        if (project == null) {
            return null
        }
        val json = ReadJsonFile().fromFileUrl(project, schematicLocation)
        val props = json.get("properties")?.asJsonObject ?: return null
        val formControls = props.keySet().mapNotNull { key -> GenerateFormControl().getFormControl(props.get(key).asJsonObject, key) }
        val result = panel {
            row {
                label("ng generate workspace-schematic:$id")
            }
            formControls.mapNotNull {
                control ->
                val comp = control.component ?: return null
                val desc = control.description ?: ""
                titledRow(control.name) {
                    if (control.type != FormControlType.BOOL) {
                        row { label(control.description ?: "") }
                    }
                    row {
                        when(control.type) {
                            FormControlType.BOOL -> checkBox(desc)
                            FormControlType.STRING, FormControlType.NUMBER, FormControlType.INTEGER -> comp()
                            else -> comp()
                        }
                    }
                }
            }
        }
        currentPanel = result
        return result
    }
}