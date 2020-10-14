package com.github.etkachev.nxwebstorm.ui.settings

import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JPanel

class ExternalSchematicsSettingsComponent {
  val panel: JPanel
  private var externalSchematicsList: JBList<String>

  val preferredFocusedComponent: JComponent
    get() = externalSchematicsList

  var externalSchematics: Array<String>
    get() = Array(externalSchematicsList.model.size) { i -> externalSchematicsList.model.getElementAt(i) }
    set(list) {
      val newList = DefaultListModel<String>()
      for (item in list) {
        newList.addElement(item.trim())
      }
      externalSchematicsList.model = newList
    }

  init {
    val list = JBList<String>()
    externalSchematicsList = list
    panel = ToolbarDecorator.createDecorator(list).setAddAction { addLine() }.setRemoveAction { removeLine() }
      .disableUpDownActions().createPanel()
  }

  private fun addLine() {
    val externalSchemDialog = NewExternalSchematicDialog(externalSchematics)
    if (externalSchemDialog.showAndGet()) {
      val newExternalSchem = externalSchemDialog.packageNameText
      val currentList = externalSchematicsList.model as DefaultListModel<String>
      currentList.addElement(newExternalSchem)
      externalSchematicsList.model = currentList
    }
  }

  private fun removeLine() {
    val selectedItemIndex = externalSchematicsList.selectedIndex
    if (selectedItemIndex > -1) {
      val currentList = externalSchematics.toMutableList()
      currentList.removeAt(selectedItemIndex)
      externalSchematics = currentList.toTypedArray()
    }
  }
}
