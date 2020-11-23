package com.github.etkachev.nxwebstorm.actionlisteners

import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.ui.SchematicsListDialog
import com.github.etkachev.nxwebstorm.utils.getSchematicIdFromTableSelect
import com.github.etkachev.nxwebstorm.utils.splitSchematicId
import com.intellij.ui.table.JBTable
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class SchematicActionListener(
  private val table: JBTable,
  private val schematics: Map<String, SchematicInfo>,
  private val panel: SchematicsListDialog
) : ListSelectionListener {
  override fun valueChanged(e: ListSelectionEvent?) {
    if (e != null && e.valueIsAdjusting) {
      return
    }
    if (table.selectedRow == -1) {
      return
    }
    val selectedRow = table.selectedRow
    val fullId = getSchematicIdFromTableSelect(table, selectedRow, schematics) ?: return
    val schematicInfo = splitSchematicId(fullId) ?: return
    val info = schematics[fullId] ?: return
    panel.schematicSelection["id"] = schematicInfo.id
    panel.schematicSelection["collection"] = schematicInfo.collection
    panel.schematicSelection["file"] = info.fileLocation
    panel.schematicSelection["type"] = schematicInfo.type.data
  }
}
