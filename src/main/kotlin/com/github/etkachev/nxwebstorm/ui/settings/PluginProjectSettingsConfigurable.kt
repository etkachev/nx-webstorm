package com.github.etkachev.nxwebstorm.ui.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.Nullable
import javax.swing.JComponent

class PluginProjectSettingsConfigurable(val project: Project) : Configurable {
  private var mySettingsComponent: PluginProjectSettingsComponent? = null

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
    mySettingsComponent = PluginProjectSettingsComponent()
    return mySettingsComponent!!.panel
  }

  override fun isModified(): Boolean {
    val settings: PluginProjectSettingsState = PluginProjectSettingsState.getInstance(this.project)
    var modified: Boolean = mySettingsComponent!!.scanExplicitLibsStatus != settings.scanExplicitLibs
    modified =
      modified or (mySettingsComponent!!.customSchematicsDirText != settings.customSchematicsLocation)
    return modified
  }

  override fun apply() {
    val settings: PluginProjectSettingsState = PluginProjectSettingsState.getInstance(this.project)
    settings.scanExplicitLibs = mySettingsComponent!!.scanExplicitLibsStatus
    settings.customSchematicsLocation = mySettingsComponent!!.customSchematicsDirText
  }

  override fun reset() {
    val settings: PluginProjectSettingsState = PluginProjectSettingsState.getInstance(this.project)
    mySettingsComponent!!.scanExplicitLibsStatus = settings.scanExplicitLibs
    mySettingsComponent!!.customSchematicsDirText = settings.customSchematicsLocation
  }

  override fun disposeUIResources() {
    mySettingsComponent = null
  }
}
