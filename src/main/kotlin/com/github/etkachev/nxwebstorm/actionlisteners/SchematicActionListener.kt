package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.ui.SchematicsListDialog
import com.intellij.ui.components.JBList
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class SchematicActionListener(
  private val list: JBList<String>,
  private val schematicList: List<Pair<String, String>>,
  private val schematics: Map<String, SchematicInfo>,
  private val panel: SchematicsListDialog
) : ListSelectionListener {
  override fun valueChanged(e: ListSelectionEvent?) {
    val selected = list.selectedValue
    val id = schematicList.find { e -> e.second == selected } ?: return
    val info = schematics[id.first] ?: return
    panel.schematicSelection["id"] = id.first
    panel.schematicSelection["file"] = info.fileLocation
  }
}
