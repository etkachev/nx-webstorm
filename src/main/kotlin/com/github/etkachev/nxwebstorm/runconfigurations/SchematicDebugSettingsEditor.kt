package com.github.etkachev.nxwebstorm.runconfigurations

import com.intellij.openapi.options.SettingsEditor
import javax.swing.JComponent
import javax.swing.JPanel

class SchematicDebugSettingsEditor : SettingsEditor<SchematicDebugRunConfiguration>() {
  override fun resetEditorFrom(s: SchematicDebugRunConfiguration) {
  }

  override fun applyEditorTo(s: SchematicDebugRunConfiguration) {
  }

  override fun createEditor(): JComponent {
    return JPanel()
  }
}
