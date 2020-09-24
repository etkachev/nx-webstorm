package com.github.etkachev.nxwebstorm.ui.settings

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextArea
import javax.swing.JComponent
import javax.swing.JPanel
import com.intellij.ui.layout.panel
import javax.swing.BorderFactory

/**
 * Supports creating and managing a JPanel for the Settings Dialog.
 */
class PluginSettingsComponent {
  val panel: JPanel
  private val myExternalLibsField: JBTextArea
  private val myScanEverythingStatus = JBCheckBox("Scan all node_modules? This may take longer to find all schematics")

  init {
    val textArea = JBTextArea(5, 20)
    textArea.lineWrap = true
    textArea.wrapStyleWord = true
    textArea.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    myExternalLibsField = textArea
  }

  val preferredFocusedComponent: JComponent
    get() = myExternalLibsField
  var externalLibsText: String
    get() = myExternalLibsField.text
    set(libs) {
      myExternalLibsField.text = libs
    }
  var scanEverythingStatus: Boolean
    get() = myScanEverythingStatus.isSelected
    set(newStatus) {
      myScanEverythingStatus.isSelected = newStatus
    }

  init {
    panel = panel {
      titledRow("External Libs") {
        row {
          label("Enter external libs to scan:")
        }
        row {
          myExternalLibsField()
        }
      }
      titledRow("Scan Everything? (WIP)") {
        row {
          myScanEverythingStatus()
        }
      }
    }.withBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
  }
}
