package com.github.etkachev.nxwebstorm.ui.settings

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import javax.swing.JComponent
import javax.swing.JPanel
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.selected
import javax.swing.BorderFactory

/**
 * Supports creating and managing a JPanel for the Settings Dialog.
 */
class PluginSettingsComponent {
  val panel: JPanel
  private val myExternalLibsField: JBTextArea
  private val myScanExplicitLibsStatus: JBCheckBox
  private val myCustomSchematicsDirectory: JBTextField = JBTextField()

  init {
    val textArea = JBTextArea(5, 20)
    textArea.lineWrap = true
    textArea.wrapStyleWord = true
    textArea.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    myExternalLibsField = textArea

    val checkBox =
      JBCheckBox(
        "Scan only explicit external libs (faster). " +
          "If off, it will scan all of node_modules (slower)."
      )
    myScanExplicitLibsStatus = checkBox
  }

  val preferredFocusedComponent: JComponent
    get() = myExternalLibsField
  var externalLibsText: String
    get() = myExternalLibsField.text
    set(libs) {
      myExternalLibsField.text = libs
    }
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
    panel = panel {
      titledRow("Scan Explicit External Libs? (WIP)") {
        row {
          myScanExplicitLibsStatus()
        }
      }
      titledRow("External Libs") {
        row {
          label("Enter external libs to scan:")
        }
        row {
          myExternalLibsField().enableIf(myScanExplicitLibsStatus.selected)
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
