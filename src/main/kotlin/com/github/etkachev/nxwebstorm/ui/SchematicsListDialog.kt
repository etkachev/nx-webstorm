package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.actionlisteners.SchematicActionListener
import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.utils.getSchematicListDescriptions
import com.github.etkachev.nxwebstorm.utils.getSchematicsListModel
import com.github.etkachev.nxwebstorm.utils.getSearchTextField
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.layout.panel
import javax.swing.JComponent
import javax.swing.ListSelectionModel

class SchematicsListDialog(val project: Project?, private val schematics: Map<String, SchematicInfo>) :
  DialogWrapper(project) {
  var schematicSelection: MutableMap<String, String> = mutableMapOf()

  private var cellWidth = 800

  init {
    super.init()
    schematicSelection = mutableMapOf()
  }

  override fun createCenterPanel(): JComponent? {
    val items = getSchematicListDescriptions(schematics)

    val jbList = JBList(getSchematicsListModel(items))
    jbList.selectionMode = ListSelectionModel.SINGLE_SELECTION
    jbList.fixedCellWidth = cellWidth
    jbList.addListSelectionListener(SchematicActionListener(jbList, items, schematics, this))
    val searchField = getSearchTextField(items, jbList)
    val scrollPane = JBScrollPane(jbList)
    return panel {
      row {
        searchField()
      }
      row {
        scrollPane()
      }
    }
  }
}
