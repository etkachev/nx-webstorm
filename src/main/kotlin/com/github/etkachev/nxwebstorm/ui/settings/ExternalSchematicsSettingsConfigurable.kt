package com.github.etkachev.nxwebstorm.ui.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class ExternalSchematicsSettingsConfigurable : Configurable {
  private var externalSchematicsComponent: ExternalSchematicsSettingsComponent? = null

  override fun createComponent(): JComponent? {
    externalSchematicsComponent = ExternalSchematicsSettingsComponent()
    return externalSchematicsComponent!!.panel
  }

  override fun isModified(): Boolean {
    val settings: PluginSettingsState = PluginSettingsState.instance
    val current = settings.externalLibs
    val newList = externalSchematicsComponent!!.externalSchematics.joinToString(",")
    return newList != current
  }

  override fun apply() {
    val settings: PluginSettingsState = PluginSettingsState.instance
    settings.externalLibs = externalSchematicsComponent!!.externalSchematics.joinToString(",")
  }

  override fun getPreferredFocusedComponent(): JComponent {
    return externalSchematicsComponent!!.preferredFocusedComponent
  }

  override fun getDisplayName(): String {
    return "External Schematics"
  }

  override fun reset() {
    val settings: PluginSettingsState = PluginSettingsState.instance
    externalSchematicsComponent!!.externalSchematics = settings.externalLibs.split(",").toTypedArray()
  }

  override fun disposeUIResources() {
    externalSchematicsComponent = null
  }
}
