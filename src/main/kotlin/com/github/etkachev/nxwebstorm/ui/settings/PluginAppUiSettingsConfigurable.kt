package com.github.etkachev.nxwebstorm.ui.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

class PluginAppUiSettingsConfigurable : Configurable {
  private var settingsComponent: PluginAppUiSettingsComponent? = null
  override fun createComponent(): JComponent? {
    this.settingsComponent = PluginAppUiSettingsComponent()
    return this.settingsComponent!!.panel
  }

  override fun isModified(): Boolean {
    val settings = PluginSettingsState.instance
    return settingsComponent!!.schematicActionButtonPlacement != settings.schematicActionButtonsPlacement
  }

  override fun apply() {
    val settings = PluginSettingsState.instance
    settings.schematicActionButtonsPlacement = settingsComponent!!.schematicActionButtonPlacement
  }

  override fun reset() {
    val settings = PluginSettingsState.instance
    settingsComponent!!.schematicActionButtonPlacement = settings.schematicActionButtonsPlacement
  }

  @Nls(capitalization = Nls.Capitalization.Title)
  override fun getDisplayName(): String {
    return "App Ui Settings"
  }

  override fun disposeUIResources() {
    this.settingsComponent = null
  }
}
