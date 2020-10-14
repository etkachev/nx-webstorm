package com.github.etkachev.nxwebstorm.ui.settings

import com.intellij.ui.CollectionListModel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import javax.swing.JComponent
import javax.swing.JPanel
import com.intellij.ui.layout.panel
import javax.swing.BorderFactory

/**
 * Supports creating and managing a JPanel for the Settings Dialog.
 */
class PluginSettingsComponent {
  val panel: JPanel
  private val myScanExplicitLibsStatus: JBCheckBox
  private val myCustomSchematicsDirectory: JBTextField = JBTextField()
  private val myExternalLibsList: CollectionListModel<String>

  val preferredFocusedComponent: JComponent
    get() = myScanExplicitLibsStatus
  var scanExplicitLibsStatus: Boolean
    get() = myScanExplicitLibsStatus.isSelected
    set(newStatus) {
      myScanExplicitLibsStatus.isSelected = newStatus
    }
  var customSchematicsDirText: String
    get() = myCustomSchematicsDirectory.text
    set(dir) {
      myCustomSchematicsDirectory.text = dir
    }

  init {
    val checkBox =
      JBCheckBox(
        "Scan only explicit external libs (faster). " +
          "If off, it will scan all of node_modules (slower)."
      )
    myScanExplicitLibsStatus = checkBox
    myExternalLibsList = CollectionListModel()

    panel = panel {
      titledRow("Scan Explicit External Libs?") {
        row {
          myScanExplicitLibsStatus()
        }
        row {
          label("If turned on, head over to 'External Schematics' setting page to configure")
        }
      }
      titledRow("Custom Schematics Directory") {
        row {
          label("Enter directory where custom schematics are located")
        }
        row {
          myCustomSchematicsDirectory()
        }
      }
    }.withBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
  }
}
