package com.github.etkachev.nxwebstorm.ui.settings

import com.github.etkachev.nxwebstorm.models.SchematicActionButtonPlacement
import com.intellij.openapi.ui.ComboBox
import javax.swing.JPanel
import com.intellij.ui.layout.panel
import javax.swing.DefaultComboBoxModel

class PluginAppUiSettingsComponent {
  private val schematicActionButtonOptions = arrayOf(
    SchematicActionButtonPlacement.TOP.data,
    SchematicActionButtonPlacement.BOTTOM.data
  )
  val panel: JPanel
  val schematicActionButtonDropdown: ComboBox<String> = ComboBox(
    DefaultComboBoxModel(
      this.schematicActionButtonOptions
    )
  )
  var schematicActionButtonPlacement: String
    get() {
      val selectedIndex = this.schematicActionButtonDropdown.selectedIndex
      if (selectedIndex < 0) {
        return SchematicActionButtonPlacement.TOP.data
      }
      return this.schematicActionButtonDropdown.getItemAt(selectedIndex)
    }
    set(value) {
      val matchedIndex = this.schematicActionButtonOptions.indexOf(value)
      val finalIndex =
        if (matchedIndex < 0) {
          this.schematicActionButtonOptions.indexOf(SchematicActionButtonPlacement.TOP.data)
        } else {
          matchedIndex
        }
      this.schematicActionButtonDropdown.selectedIndex = finalIndex
    }

  init {
    this.panel = panel {
      titledRow("Run Schematic Action Bar") {
        row {
          label("Select where you would like the action bar to be located")
        }
        row {
          schematicActionButtonDropdown()
        }
      }
    }
  }
}
