package com.github.etkachev.nxwebstorm.ui.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class ExternalSchematicsSettingsConfigurable(val project: Project) : Configurable {
  private var externalSchematicsComponent: ExternalSchematicsSettingsComponent? = null

  override fun createComponent(): JComponent? {
    externalSchematicsComponent = ExternalSchematicsSettingsComponent()
    return externalSchematicsComponent!!.panel
  }

  override fun isModified(): Boolean {
    val settings: PluginProjectSettingsState = PluginProjectSettingsState.getInstance(this.project)
    val current = settings.externalLibs.joinToString(",")
    val newList = externalSchematicsComponent!!.externalSchematics.joinToString(",")
    return newList != current
  }

  override fun apply() {
    val settings: PluginProjectSettingsState = PluginProjectSettingsState.getInstance(this.project)
    settings.externalLibs = externalSchematicsComponent!!.externalSchematics
  }

  override fun getPreferredFocusedComponent(): JComponent {
    return externalSchematicsComponent!!.preferredFocusedComponent
  }

  override fun getDisplayName(): String {
    return "External Schematics"
  }

  override fun reset() {
    val settings: PluginProjectSettingsState = PluginProjectSettingsState.getInstance(this.project)
    externalSchematicsComponent!!.externalSchematics = settings.externalLibs
  }

  override fun disposeUIResources() {
    externalSchematicsComponent = null
  }
}
