package com.github.etkachev.nxwebstorm.ui

import com.github.etkachev.nxwebstorm.actionlisteners.SchematicSelectionTabListener
import com.github.etkachev.nxwebstorm.models.SchematicInfo
import com.github.etkachev.nxwebstorm.utils.SearchListDecorator
import com.github.etkachev.nxwebstorm.utils.getSchematicListDescriptions
import com.github.etkachev.nxwebstorm.utils.getSchematicsListModel
import com.github.etkachev.nxwebstorm.utils.getSearchTextField
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.layout.panel
import java.awt.Color
import java.util.function.BiPredicate
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.ListSelectionModel

class SchematicsListToolTab(val project: Project, private val schematics: Map<String, SchematicInfo>) {

  fun createCenterPanel(toolWindow: ToolWindow): JComponent? {
    val items = getSchematicListDescriptions(schematics)

    val jbList = JBList(getSchematicsListModel(items))
    jbList.selectionMode = ListSelectionModel.SINGLE_SELECTION
    jbList.background = Color(0, 0, 0, 0)
    jbList.addListSelectionListener(SchematicSelectionTabListener(project, jbList, items, schematics, toolWindow))
    val border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    val searchField = getSearchTextField(items, jbList)
    val scrollPane = JBScrollPane(jbList)
    val matcher = BiPredicate<String, String?> { entry, str ->
      val result = if (str != null) entry.toLowerCase().contains(str.toLowerCase()) else false
      result
    }
    //TODO figure out rendering
    val pane = SearchListDecorator().decorate(jbList, matcher)
    // val scrollPane = JBScrollPane(pane)
    return panel {
      row {
        searchField()
      }
      row {
        scrollPane()
      }
    }.withBorder(border)
  }

  private fun entryFilter(entry: String, str: String): Boolean {
    return entry.toLowerCase().contains(str.toLowerCase())
  }
}
