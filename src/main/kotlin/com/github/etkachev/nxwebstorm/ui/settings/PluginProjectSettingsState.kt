package com.github.etkachev.nxwebstorm.ui.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Supports storing the application settings in a persistent way.
 * The State and Storage annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 */
@State(
  name = "com.github.etkachev.nxwebstorm.ui.settings.PluginProjectSettingsState",
  storages = [Storage("NxPluginProjectSettings.xml")]
)
class PluginProjectSettingsState : PersistentStateComponent<PluginProjectSettingsState?> {
  var externalLibs = PluginSettingsState.instance.externalLibs.split(",").toTypedArray()
  var scanExplicitLibs = PluginSettingsState.instance.scanExplicitLibs
  var customSchematicsLocation = PluginSettingsState.instance.customSchematicsLocation

  override fun getState(): PluginProjectSettingsState? {
    return this
  }

  override fun loadState(state: PluginProjectSettingsState) {
    XmlSerializerUtil.copyBean(state, this)
  }

  companion object {
    fun getInstance(project: Project): PluginProjectSettingsState {
      return ServiceManager.getService(project, PluginProjectSettingsState::class.java)
    }
    // val instance: PluginProjectSettingsState
    //   get() = project.getService(PluginProjectSettingsState::class.java)
  }
}
