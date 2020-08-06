package com.github.etkachev.nxwebstorm.actions

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBList
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class SchematicActionListener(private val list: JBList<String>, private val panel: DialogWrapper): ListSelectionListener {
    override fun valueChanged(e: ListSelectionEvent?) {
        if (!list.valueIsAdjusting) {
            val id = list.selectedValue
            panel.close(0)
            Messages.showMessageDialog("Schematic for $id", "Nx", Messages.getInformationIcon())
        }
    }

}