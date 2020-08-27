package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.ui.RunSchematicDialog
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class SchematicActionListener(private val project: Project, private val list: JBList<String>, private val schematics: Map<String, String>, private val panel: DialogWrapper): ListSelectionListener {
    override fun valueChanged(e: ListSelectionEvent?) {
            val id = list.selectedValue
            val fileLocation = schematics[id] ?: return
            panel.close(0)
            val dialog = RunSchematicDialog(project, id, fileLocation)
            dialog.setSize(800, 600)
            val ok = dialog.showAndGet()
            if (ok) {
                val values = dialog.formValue
            }
    }

}