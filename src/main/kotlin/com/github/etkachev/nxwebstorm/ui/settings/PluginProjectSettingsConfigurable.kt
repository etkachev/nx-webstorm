package com.github.etkachev.nxwebstorm.ui.settings

import com.github.etkachev.nxwebstorm.services.MyProjectService
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
    val settingCustomSchemDir = settings.customSchematicsLocation
    val componentSchematicText = mySettingsComponent!!.customSchematicsDirText
    val settingsRootDir = settings.rootDirectory
    val componentRootDirText = mySettingsComponent!!.rootNxDirectoryText
    modified = modified or
      (componentSchematicText != settingCustomSchemDir) or
      (componentRootDirText != settingsRootDir)
    return modified
  }

  override fun apply() {
    val settings: PluginProjectSettingsState = PluginProjectSettingsState.getInstance(this.project)
    settings.scanExplicitLibs = mySettingsComponent!!.scanExplicitLibsStatus
    settings.customSchematicsLocation = mySettingsComponent!!.customSchematicsDirText
    settings.rootDirectory = this.getValidRootNxDirectory(mySettingsComponent!!.rootNxDirectoryText)
  }

  override fun reset() {
    val settings: PluginProjectSettingsState = PluginProjectSettingsState.getInstance(this.project)
    val projService = MyProjectService.getInstance(this.project)
    mySettingsComponent!!.scanExplicitLibsStatus = settings.scanExplicitLibs
    mySettingsComponent!!.customSchematicsDirText = this.getCustomSchematicsLocationFromState(settings, projService)
    mySettingsComponent!!.rootNxDirectoryText = this.getRootNxDirectoryFromState(settings, projService)
  }

  private fun getValidRootNxDirectory(input: String): String {
    val projService = MyProjectService.getInstance(this.project)
    return if (input.trim().isNotBlank()) {
      return input.trim()
    } else {
      projService.defaultRootDirectory
    }
  }

  private fun getCustomSchematicsLocationFromState(
    settings: PluginProjectSettingsState,
    projService: MyProjectService
  ): String {
    return if (settings.customSchematicsLocation.isNotBlank()) {
      settings.customSchematicsLocation
    } else {
      projService.defaultCustomSchematicsLocation
    }
  }

  private fun getRootNxDirectoryFromState(
    settings: PluginProjectSettingsState,
    projService: MyProjectService
  ): String {
    return if (settings.rootDirectory.trim().isNotBlank()) {
      settings.rootDirectory
    } else {
      projService.defaultRootDirectory
    }
  }

  override fun disposeUIResources() {
    mySettingsComponent = null
  }
}
