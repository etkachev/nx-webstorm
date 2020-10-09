package com.github.etkachev.nxwebstorm.ui.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.Nullable
import javax.swing.JComponent

class PluginSettingsConfigurable : Configurable {
  private var mySettingsComponent: PluginSettingsComponent? = null

  // A default constructor with no arguments is required because this implementation
  // is registered as an applicationConfigurable EP
  @Nls(capitalization = Nls.Capitalization.Title)
  override fun getDisplayName(): String {
    return "Nx Plugin Settings"
  }

  override fun getPreferredFocusedComponent(): JComponent {
    return mySettingsComponent!!.preferredFocusedComponent
  }

  @Nullable
  override fun createComponent(): JComponent? {
    mySettingsComponent = PluginSettingsComponent()
    return mySettingsComponent!!.panel
  }

  override fun isModified(): Boolean {
    val settings: PluginSettingsState = PluginSettingsState.instance
    var modified: Boolean = mySettingsComponent!!.externalLibsText != settings.externalLibs
    modified =
      modified or (mySettingsComponent!!.scanExplicitLibsStatus != settings.scanExplicitLibs) or (mySettingsComponent!!.customSchematicsDirText != settings.customSchematicsLocation)
    return modified
  }

  override fun apply() {
    val settings: PluginSettingsState = PluginSettingsState.instance
    settings.externalLibs = mySettingsComponent!!.externalLibsText
    settings.scanExplicitLibs = mySettingsComponent!!.scanExplicitLibsStatus
    settings.customSchematicsLocation = mySettingsComponent!!.customSchematicsDirText
  }

  override fun reset() {
    val settings: PluginSettingsState = PluginSettingsState.instance
    mySettingsComponent!!.externalLibsText = settings.externalLibs
    mySettingsComponent!!.scanExplicitLibsStatus = settings.scanExplicitLibs
    mySettingsComponent!!.customSchematicsDirText = settings.customSchematicsLocation
  }

  override fun disposeUIResources() {
    mySettingsComponent = null
  }
}
