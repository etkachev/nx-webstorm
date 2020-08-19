package com.github.etkachev.nxwebstorm.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.project.Project
import javax.swing.JComponent
import com.intellij.ui.layout.panel
import com.github.etkachev.nxwebstorm.utils.GenerateFormControl
import com.github.etkachev.nxwebstorm.utils.ReadJsonFile


class RunSchematicDialog(private val project: Project?, private val id: String, private val schematicLocation: String): DialogWrapper(project) {
    init {
        super.init()
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
                label(id)
            }
        }
    }
}