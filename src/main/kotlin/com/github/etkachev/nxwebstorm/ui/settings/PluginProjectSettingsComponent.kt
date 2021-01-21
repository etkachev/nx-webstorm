package com.github.etkachev.nxwebstorm.ui.settings

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import javax.swing.JComponent
import javax.swing.JPanel
import com.intellij.ui.layout.panel
import javax.swing.BorderFactory

/**
 * Supports creating and managing a JPanel for the Settings Dialog.
 */
class PluginProjectSettingsComponent {
  val panel: JPanel
  private val myScanExplicitLibsStatus: JBCheckBox
  private val myCustomSchematicsDirectory: JBTextField = JBTextField()
  private val myRootNxDirectory: JBTextField = JBTextField()

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

  var rootNxDirectoryText: String
    get() = myRootNxDirectory.text
    set(dir) {
      myRootNxDirectory.text = dir
    }

  init {
    val checkBox =
      JBCheckBox(
        "Scan explicit external libs (faster). " +
          "If off, it will scan all of node_modules (slower)."
      )
    myScanExplicitLibsStatus = checkBox

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
      titledRow("Root Nx Directory") {
        row {
          label("Enter directory where your nx project resides")
        }
        row {
          myRootNxDirectory()
        }
      }
    }.withBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
  }
}
