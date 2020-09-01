package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.ui.SchematicsListDialog
import com.intellij.ui.components.JBList
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class SchematicActionListener(
    private val list: JBList<String>,
    private val schematics: Map<String, String>,
    private val panel: SchematicsListDialog
) : ListSelectionListener {
    override fun valueChanged(e: ListSelectionEvent?) {
        val id = list.selectedValue
        val fileLocation = schematics[id] ?: return
        panel.schematicSelection["id"] = id
        panel.schematicSelection["file"] = fileLocation
    }
}
